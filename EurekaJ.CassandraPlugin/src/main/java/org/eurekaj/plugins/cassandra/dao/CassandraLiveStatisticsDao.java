package org.eurekaj.plugins.cassandra.dao;

import java.util.*;

import com.datastax.driver.core.*;

import org.apache.log4j.Logger;
import org.eurekaj.api.dao.LiveStatisticsDao;
import org.eurekaj.api.datatypes.LiveStatistics;
import org.eurekaj.api.datatypes.LiveStatisticsUtil;
import org.eurekaj.api.enumtypes.UnitType;
import org.eurekaj.api.enumtypes.ValueType;
import org.eurekaj.plugins.cassandra.CassandraEnv;
import org.eurekaj.plugins.cassandra.datatypes.CassandraLiveStatistics;
import org.eurekaj.plugins.cassandra.datatypes.MetricHour;

public class CassandraLiveStatisticsDao implements LiveStatisticsDao {
    private static Logger logger = Logger.getLogger(CassandraLiveStatisticsDao.class.getName());

	private CassandraEnv cassandraEnv;
	
	public CassandraLiveStatisticsDao(CassandraEnv cassandraEnv) {
		this.cassandraEnv = cassandraEnv;
	}

    @Override
    public void markLiveStatisticsAsCalculated(String guiPath, String accountName, String timeperiod) {
        cassandraEnv.getCassandraSession().execute("UPDATE live_statistics set isCalculated = true where accountName = '" + accountName + "' and guiPath = '" + guiPath + "' and timeperiod = '" + timeperiod + "'");
    }

    @Override
    public void markLiveStatisticsAsCalculated(String guiPath, String accountName, Long minTimeperiod, Long maxTimeperiod) {
        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN BATCH\n");
        for (Long timeperiod = minTimeperiod; timeperiod <= maxTimeperiod; timeperiod++) {
            cassandraEnv.getCassandraSession().execute("UPDATE live_statistics set isCalculated = true where accountName = '" + accountName + "' and guiPath = '" + guiPath + "' and timeperiod = '" + timeperiod + "'");
        }
        sb.append("END BATCH");

        //logger.info(sb.toString());

        //cassandraEnv.getCassandraSession().execute(sb.toString());
    }

    @Override
    public void deleteMarkedLiveStatistics() {
        //cassandraEnv.getCassandraSession().execute("delete from live_statistics where isCalculated = true");
        ResultSet rs = cassandraEnv.getCassandraSession().execute("select * from live_statistics where isCalculated = true");
        Iterator<Row> rowIterator = rs.iterator();
        while (rowIterator.hasNext()) {
            CassandraLiveStatistics ls = populateLiveStatistics(rowIterator.next());
            cassandraEnv.getCassandraSession().execute("delete from live_statistics where accountName = '" + ls.getAccountName() + "' and guiPath = '" + ls.getGuiPath() + "' and timeperiod = '" + ls.getTimeperiod() + "'");
        }
    }

    @Override
    public void deleteLiveStatisticsBetween(String guiPath, String accountName, Long fromTimeperiod, Long toTimeperiod) {
        int startHoursSince1970 = fromTimeperiod.intValue() / 240;
        int endHoursSince1970 = toTimeperiod.intValue() / 240;

        logger.info("deleting " + (endHoursSince1970 - startHoursSince1970) + " metric hours");

        List<String> deletes = new ArrayList<String>();

        int index =0;
        for (int timeperiod = startHoursSince1970; timeperiod <= endHoursSince1970; timeperiod++) {
            deletes.add("DELETE from live_statistics where accountName = '" + accountName + "' and guiPath = '" + guiPath + "' and timeperiod = '" + timeperiod + "';\n");

            if (deletes.size() >= 50) {
                logger.info("deleted " + index + " metric hours");
                batchExecuteQueries(deletes);
                deletes.clear();
            }
            index++;
        }

        batchExecuteQueries(deletes);
        deletes.clear();
    }

    @Override
    public void storeIncomingStatistics(List<LiveStatistics> liveStatisticsList) {
        Hashtable<String, MetricHour> metricHoursToStoreHash = new Hashtable<String, MetricHour>();

        int index = 0;
        for (LiveStatistics ls : liveStatisticsList) {
            if (index % 50 == 0) {
                logger.info("Calcluated " + index + " Live Statistics for storage");
            }

            long hoursSince1970 = ls.getTimeperiod() / 240;
            int fifteenSecondPeriodsSinceStartOfHour = LiveStatisticsUtil.getFifteensecondTimeperiodsSinceStartOfHour(ls.getTimeperiod() * 15);
            Double calculatedValue = LiveStatisticsUtil.calculateValueBasedOnUnitType(ls.getValue(), UnitType.fromValue(ls.getUnitType()));

            MetricHour mhToStore = metricHoursToStoreHash.get(ls.getAccountName() + ";" + ls.getGuiPath() + ";" + hoursSince1970);
            boolean wasInHash = mhToStore != null;

            if (mhToStore == null) {
                //Not in hash, fetch from Riak
                mhToStore = getMetricHour(ls.getAccountName(), ls.getGuiPath(), hoursSince1970);
            }

            if (mhToStore == null) {
                //Not in Riak, create
                mhToStore = new MetricHour(ls.getGuiPath(), ls.getAccountName(), hoursSince1970, ls.getValueType(), ls.getUnitType());
            }

            mhToStore.getMetrics()[fifteenSecondPeriodsSinceStartOfHour] = LiveStatisticsUtil.calculateValueBasedOnValueType(ls, calculatedValue, ValueType.fromValue(ls.getValueType()));

            if (!wasInHash) {
                metricHoursToStoreHash.put(ls.getAccountName() + ";" + ls.getGuiPath() + ";" + hoursSince1970, mhToStore);
            }

            index++;
        }

        logger.info("Storing " + metricHoursToStoreHash.size() + " MetricHours");
        index = 0;
        List<String> inserts = new ArrayList<String>();

        for (MetricHour metricHour : metricHoursToStoreHash.values()) {
            if (inserts.size() >= 50) {
                batchExecuteQueries(inserts);
                inserts.clear();
                logger.info("Stored " + index + " metric hours in Cassandra.");
            }

            inserts.add(createMetricHourInsertCql(metricHour));

            index++;
        }

        if (inserts.size() > 0) {
            logger.info("Inserting remaining " + inserts.size() + " rows");
            batchExecuteQueries(inserts);
            inserts.clear();
        }


        /*List<String> inserts = new ArrayList<String>();

        logger.info("Starting to store liveStats: " + liveStatisticsList.size());
        int index = 0;
        for (LiveStatistics ls : liveStatisticsList) {
            Double valueDouble = ls.getValue();
            Double calculatedValue = LiveStatisticsUtil.calculateValueBasedOnUnitType(valueDouble, UnitType.fromValue(ls.getUnitType()));

            int hoursSince1970 = ls.getTimeperiod().intValue() / 240;
            int fifteenSecondPeriodsSinceStartOfHour = LiveStatisticsUtil.getFifteensecondTimeperiodsSinceStartOfHour(ls.getTimeperiod()* 15);

            Double prevValue = null;
            ResultSet rs = cassandraEnv.getCassandraSession().execute("select m" + fifteenSecondPeriodsSinceStartOfHour + " from metric_hour where accountName = '" + ls.getAccountName() + "' and guiPath = '" + ls.getGuiPath() + "' and hoursSince1970 = '" + hoursSince1970 + "'");
            Iterator<Row> rowIterator = rs.iterator();
            if (rowIterator.hasNext()) {
                prevValue = rowIterator.next().getDouble("m" + fifteenSecondPeriodsSinceStartOfHour);
            } else {
                String insertCql = "insert into metric_hour (guiPath, accountName, hoursSince1970, valueType, unitType) " +
                        "values (" +
                            "'" + ls.getGuiPath() + "'," +
                            "'" + ls.getAccountName() + "'," +
                            "" + hoursSince1970 + "," +
                            "'" + ls.getValueType() + "'," +
                            "'" + ls.getUnitType() + "'" +
                        ")";
                cassandraEnv.getCassandraSession().execute(insertCql);
            }

            Double valueToPersist = calculatedValue;
            if (prevValue != null) {
                valueToPersist = LiveStatisticsUtil.calculateValueBasedOnValueType(prevValue, calculatedValue, ValueType.fromValue(ls.getValueType()));
            }

            inserts.add("insert into metric_hour(accountname, guipath, hourssince1970, m" + fifteenSecondPeriodsSinceStartOfHour + ") " +
                    "values(" +
                        "'" + ls.getAccountName() + "'," +
                        "'" + ls.getGuiPath() + "'," +
                        "" + hoursSince1970 + "," +
                        "" + valueToPersist + "" +
                    ")");

            index++;

            if (inserts.size() >= 50) {
                logger.info("Inserted " + index + " rows");
                batchExecuteQueries(inserts);
                inserts.clear();
            }
        }

        if (inserts.size() > 0) {
            logger.info("Inserting remaining " + inserts.size() + " rows");
            batchExecuteQueries(inserts);
            inserts.clear();
        }
        */
    }

    private String createMetricHourInsertCql(MetricHour mh) {
        StringBuilder sb = new StringBuilder();
        sb.append("insert into metric_hour(guiPath, accountName, hoursSince1970, valueType, unitType, ");
        for (int i = 0; i < 240; i++) {
            if (mh.getValueAt(i) != null) {
                if (i > 0) {
                    sb.append(",");
                }
                sb.append("m").append(i);
            }
        }
        sb.append(") values (");
        sb.append("'").append(mh.getGuiPath()).append("',");
        sb.append("'").append(mh.getAccountName()).append("',");
        sb.append(mh.getHoursSince1970()).append(",");
        sb.append("'").append(mh.getValueType()).append("',");
        sb.append("'").append(mh.getUnitType()).append("',");
        for (int i = 0; i < 240; i++) {
            if (mh.getValueAt(i) != null) {
                if (i > 0) {
                    sb.append(",");
                }
                sb.append(mh.getValueAt(i));
            }
        }

        sb.append(")");

        return sb.toString();
    }

    private void batchExecuteQueries(List<String> queries) {
        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN BATCH \n");
        for (String query : queries) {
            sb.append(query + ";\n");
        }
        sb.append("APPLY BATCH;");

        //logger.info(sb.toString());
        PreparedStatement ps = cassandraEnv.getCassandraSession().prepare(sb.toString());
        ps.setConsistencyLevel(ConsistencyLevel.QUORUM);
        BoundStatement bs = ps.bind();
        cassandraEnv.getCassandraSession().execute(bs);
    }

    @Override
	public void storeIncomingStatistics(String guiPath, String accountName, Long timeperiod, String value, ValueType valueType, UnitType unitType) {
		//cassandraEnv.getApplicationServices().getLoggedInUsername();
		
		Double valueDouble = LiveStatisticsUtil.parseDouble(value);
		Double calculatedValue = LiveStatisticsUtil.calculateValueBasedOnUnitType(valueDouble, unitType);
		
		int hoursSince1970 = timeperiod.intValue() / 240;
		int fifteenSecondPeriodsSinceStartOfHour = LiveStatisticsUtil.getFifteensecondTimeperiodsSinceStartOfHour(timeperiod * 15);

        Double prevValue = null;
        ResultSet rs = cassandraEnv.getCassandraSession().execute("select m" + fifteenSecondPeriodsSinceStartOfHour + " from metric_hour where accountName = '" + accountName + "' and guiPath = '" + guiPath + "' and hoursSince1970 = '" + hoursSince1970 + "'");
        Iterator<Row> rowIterator = rs.iterator();
        if (rowIterator.hasNext()) {
            prevValue = rowIterator.next().getDouble("m" + fifteenSecondPeriodsSinceStartOfHour);
        } else {
            String insertCql = "insert into metric_hour (guiPath, accountName, hoursSince1970, valueType, unitType) " +
                    "values (" +
                        "'" + guiPath + "'," +
                        "'" + accountName + "'," +
                        "" + hoursSince1970 + "," +
                        "'" + valueType.value() + "'," +
                        "'" + unitType.value() + "'" +
                    ")";
            cassandraEnv.getCassandraSession().execute(insertCql);
        }

        Double valueToPersist = calculatedValue;
        if (prevValue != null) {
            valueToPersist = LiveStatisticsUtil.calculateValueBasedOnValueType(prevValue, calculatedValue, valueType);
        }

		/*CassandraLiveStatistics liveStatisticsToPersist = null;

        ResultSet rs = cassandraEnv.getCassandraSession().execute("select * from live_statistics where accountName = '" + accountName + "' and guiPath = '" + guiPath + "' and timeperiod = '" + timeperiod + "'");
        Iterator<Row> rowIterator = rs.iterator();
        if (rowIterator.hasNext()) {
            liveStatisticsToPersist = populateLiveStatistics(rowIterator.next());
        }

		if (liveStatisticsToPersist == null) {
            liveStatisticsToPersist = new CassandraLiveStatistics(guiPath, accountName, timeperiod, null, valueType.value(), unitType.value());
		}

		Double prevValue = liveStatisticsToPersist.getValue();
		if (prevValue == null) {
            liveStatisticsToPersist.setValue(calculatedValue);
		} else {
            liveStatisticsToPersist.setValue(LiveStatisticsUtil.calculateValueBasedOnValueType(prevValue, calculatedValue, valueType));
		}*/

        //logger.info("Persisting Live Statistics: " + liveStatisticsToPersist.getAccountName() + ";" + liveStatisticsToPersist.getGuiPath()  + " at: " + liveStatisticsToPersist.getTimeperiod() + " with Value: " + liveStatisticsToPersist.getValue());
		
		//cassandraEnv.getPersistenceFactory().persist(metricHourToPersist);

        //CREATE TABLE metric_hour ( id varchar, guiPath varchar, accountName varchar, metrics list<double>, hoursSince1970 int, valueType varchar, unitType varchar, PRIMARY KEY (id))
        String updateCql = "update metric_hour set m" + fifteenSecondPeriodsSinceStartOfHour + " = " + valueToPersist + " " +
                "where accountname = '" + accountName + "' " +
                "and guipath = '" + guiPath + "' " +
                "and hoursSince1970=" + hoursSince1970;
        /*String insertCql = "insert into live_statistics(accountName, guiPath, timeperiod, value, valueType, unitType, isCalculated)" +
                "values(" +
                    "'" + liveStatisticsToPersist.getAccountName() + "'," +
                    "'" + liveStatisticsToPersist.getGuiPath() + "'," +
                    "" + liveStatisticsToPersist.getTimeperiod() + "," +
                    "" + liveStatisticsToPersist.getValue() + "," +
                    "'" + liveStatisticsToPersist.getValueType() + "'," +
                    "'" + liveStatisticsToPersist.getUnitType() + "'," +
                    "false" +
                ")";*/
        //logger.info(updateCql);
        cassandraEnv.getCassandraSession().execute(updateCql);
		
	}

    private MetricHour getMetricHour(String accountName, String guiPath, long hoursSince1970) {
        MetricHour mh = null;

        PreparedStatement ps = cassandraEnv.getCassandraSession().prepare("select * from metric_hour where accountName = '" + accountName + "' and guiPath = '" + guiPath + "' and hoursSince1970 = '" + hoursSince1970 + "'");
        ps.setConsistencyLevel(ConsistencyLevel.QUORUM);
        BoundStatement bs = ps.bind();
        ResultSet rs =  cassandraEnv.getCassandraSession().execute(bs);

        Iterator<Row> rowIterator = rs.iterator();
        if (rowIterator.hasNext()) {
            mh = populateMetricHour(rowIterator.next());
        }

        return mh;
    }

    private MetricHour populateMetricHour(Row row) {
        MetricHour metricHour = new MetricHour();
        metricHour.setGuiPath(row.getString("guiPath"));
        metricHour.setAccountName(row.getString("accountName"));
        metricHour.setHoursSince1970(new Long(row.getInt("hoursSince1970")));
        metricHour.setUnitType(row.getString("unitType"));
        metricHour.setValueType(row.getString("valueType"));
        metricHour.setMetrics(row.getMap("metrics", Integer.class, Double.class));
        return metricHour;
    }

    private CassandraLiveStatistics populateLiveStatistics(Row row) {
        CassandraLiveStatistics ls = new CassandraLiveStatistics();
        ls.setAccountName(row.getString("accountName"));
        ls.setGuiPath(row.getString("guiPath"));
        ls.setTimeperiod(new Long(row.getInt("timeperiod")));
        ls.setValue(row.getDouble("value"));
        ls.setValueType(row.getString("valueType"));
        ls.setUnitType(row.getString("unitType"));
        ls.setCalculated(row.getBool("isCalculated"));
        return ls;
    }

	@Override
	public List<LiveStatistics> getLiveStatistics(String guiPath, String accountName, Long minTimeperiod, Long maxTimeperiod) {
		Long fromHoursSince1970 = minTimeperiod / 240;
    	Long toHoursSince1970 = maxTimeperiod / 240;
    	
    	List<LiveStatistics> retList = new ArrayList<LiveStatistics>();

        /*ResultSet rs = cassandraEnv.getCassandraSession().execute("select * from live_statistics where accountname = 'ACCOUNT' and guiPath  = 'Test:A' and timeperiod >= " + minTimeperiod + " and timeperiod <= " + maxTimeperiod);
        Iterator<Row> rowIterator = rs.iterator();
        while (rowIterator.hasNext()) {
            retList.add(populateLiveStatistics(rowIterator.next()));
        }*/
    	for (Long index = fromHoursSince1970; index <= toHoursSince1970; index++) {
    		//Iterate over all hours to gather stats from
    		//MetricHour metricHour = cassandraEnv.getPersistenceFactory().get(MetricHour.class, accountName + ";" + guiPath + ";" + index);
            MetricHour metricHour = null;
    		ResultSet rs = cassandraEnv.getCassandraSession().execute("select * from metric_hour where accountName = '" + accountName + "' and guiPath = '" + guiPath + "' and hoursSince1970=" + index + "");
            Iterator<Row> rowIterator = rs.iterator();
            if (rowIterator.hasNext()) {
                metricHour = populateMetricHour(rowIterator.next());
            } else {
                metricHour = new MetricHour(accountName,  guiPath, index, ValueType.VALUE.value(), UnitType.N.value());
            }

    		//If this is the first hour, start from the correct 15-second timeslot within the hour
    		Integer minTimeperiodWithinTheHour = 0;
    		if (index.longValue() == fromHoursSince1970.longValue()) {
    			minTimeperiodWithinTheHour = LiveStatisticsUtil.getFifteensecondTimeperiodsSinceStartOfHour(minTimeperiod * 15);
    		}
    		
    		//If this is the last hour, end with the correct 15-second timeslot within the hour
    		Integer maxTimeperiodWithinTheHour = null;
    		if (index.longValue() == toHoursSince1970.longValue()) {
    			maxTimeperiodWithinTheHour = LiveStatisticsUtil.getFifteensecondTimeperiodsSinceStartOfHour(maxTimeperiod * 15);
    		}
    		
    		retList.addAll(createLivestatisticsFromMetricHour(metricHour, minTimeperiodWithinTheHour, maxTimeperiodWithinTheHour, index));
    	}
		    	
		return retList;
	}
	
	private List<LiveStatistics> createLivestatisticsFromMetricHour(MetricHour metricHour, Integer minTimeperiodWithinTheHour, Integer maxTimeperiodWithinTheHour, Long hoursSince1970) {
    	List<LiveStatistics> retList = new ArrayList<LiveStatistics>();
    	
    	if (maxTimeperiodWithinTheHour == null) {
    		maxTimeperiodWithinTheHour = 239;
    	}
    	
    	for (int index = minTimeperiodWithinTheHour; index <= maxTimeperiodWithinTheHour; index++) {
    		Long timeperiod = (hoursSince1970 * 240) + index;

            //logger.info("Creating LiveStats for: " + metricHour.getId() + " with value: " + metricHour.getValueAt(index));
    		retList.add(new CassandraLiveStatistics(metricHour.getGuiPath(), metricHour.getAccountName(), timeperiod, metricHour.getValueAt(index), metricHour.getValueType(), metricHour.getUnitType()));
    	}
    	
    	return retList;
    }

	@Override
	public void deleteLiveStatisticsOlderThan(Date date, String accountName) {
		// TODO Auto-generated method stub
		
	}

}
