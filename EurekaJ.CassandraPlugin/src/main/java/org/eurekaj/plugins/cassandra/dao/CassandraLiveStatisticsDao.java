package org.eurekaj.plugins.cassandra.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.template.ColumnFamilyResult;
import me.prettyprint.cassandra.service.template.ColumnFamilyTemplate;
import me.prettyprint.cassandra.service.template.ColumnFamilyUpdater;
import me.prettyprint.cassandra.service.template.ThriftColumnFamilyTemplate;

import org.eurekaj.api.dao.LiveStatisticsDao;
import org.eurekaj.api.datatypes.LiveStatistics;
import org.eurekaj.api.datatypes.LiveStatisticsUtil;
import org.eurekaj.api.enumtypes.UnitType;
import org.eurekaj.api.enumtypes.ValueType;
import org.eurekaj.plugins.cassandra.CassandraEnv;
import org.eurekaj.plugins.cassandra.datatypes.CassandraLiveStatistics;
import org.eurekaj.plugins.cassandra.datatypes.MetricHour;

public class CassandraLiveStatisticsDao implements LiveStatisticsDao {
	private CassandraEnv cassandraEnv;
	private ColumnFamilyTemplate<String, String> liveStatisticsTemplate;
	
	public CassandraLiveStatisticsDao(CassandraEnv cassandraEnv) {
		this.cassandraEnv = cassandraEnv;
		liveStatisticsTemplate = new ThriftColumnFamilyTemplate<String, String>(cassandraEnv.getEurekaJKeyspace(), "live_statistics", StringSerializer.get(), StringSerializer.get());
	}

    @Override
    public void deleteLiveStatisticsBetween(String guiPath, Long fromTimeperiod, Long toTimeperiod) {
        List<LiveStatistics> delStats = getLiveStatistics(guiPath, fromTimeperiod, toTimeperiod);

        //Strore NULL instead of value
        for (LiveStatistics delStat : delStats) {
            storeIncomingStatistics(delStat.getGuiPath(), delStat.getTimeperiod(), null, ValueType.fromValue(delStat.getValueType()), UnitType.fromValue(delStat.getUnitType()));
        }
    }

    @Override
	public void storeIncomingStatistics(String guiPath, Long timeperiod, String value, ValueType valueType, UnitType unitType) {
		//cassandraEnv.getApplicationServices().getLoggedInUsername();
		
		Double valueDouble = LiveStatisticsUtil.parseDouble(value);
		Double calculatedValue = LiveStatisticsUtil.calculateValueBasedOnUnitType(valueDouble, unitType);
		
		int hoursSince1970 = timeperiod.intValue() / 240;
		int fifteenSecondPeriodsSinceStartOfHour = LiveStatisticsUtil.getFifteensecondTimeperiodsSinceStartOfHour(timeperiod * 15);
		
		MetricHour mh = null;
		ColumnFamilyResult<String, String> res = liveStatisticsTemplate.queryColumns(guiPath + ";" + hoursSince1970);
		String metrichourString = res.getString("metricHour");
		if (metrichourString == null) {
			mh = new MetricHour(guiPath + ";" + hoursSince1970, valueType.value(), unitType.value());
		} else {
			mh = new MetricHour(guiPath + ";" + hoursSince1970, metrichourString, valueType.value(), unitType.value());
		}
		
		Double prevValue = mh.getValueAt(fifteenSecondPeriodsSinceStartOfHour);
		if (prevValue == null) {
			mh.setValueAt(fifteenSecondPeriodsSinceStartOfHour, calculatedValue);
		} else {
			mh.setValueAt(fifteenSecondPeriodsSinceStartOfHour, LiveStatisticsUtil.calculateValueBasedOnValueType(prevValue, calculatedValue, valueType));
		}
		
		ColumnFamilyUpdater<String, String> updater = liveStatisticsTemplate.createUpdater(guiPath + ";" + hoursSince1970);
		updater.setString("metricHour", mh.getValueArrayString());
		updater.setLong("time", System.currentTimeMillis());
		
		liveStatisticsTemplate.update(updater);
		
	}

	@Override
	public List<LiveStatistics> getLiveStatistics(String guiPath, Long minTimeperiod, Long maxTimeperiod) {
		Long fromHoursSince1970 = minTimeperiod / 240;
    	Long toHoursSince1970 = maxTimeperiod / 240;
    	
    	List<LiveStatistics> retList = new ArrayList<LiveStatistics>();

    	for (Long index = fromHoursSince1970; index <= toHoursSince1970; index++) {
    		//Iterate over all hours to gather stats from
    		MetricHour metricHour = null;
    		
    		ColumnFamilyResult<String, String> res = liveStatisticsTemplate.queryColumns(guiPath + ";" + index);
    		String metrichourString = res.getString("metricHour");
    		
    		if (metrichourString == null) {
    			metricHour = new MetricHour(guiPath + ";" + index, ValueType.VALUE.value(), UnitType.N.value());
    		} else {
    			metricHour = new MetricHour(guiPath + ";" + index, metrichourString, ValueType.VALUE.value(), UnitType.N.value());
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
    		
    		retList.add(new CassandraLiveStatistics(metricHour.getGuiPath(), timeperiod, metricHour.getValueAt(index)));
    	}
    	
    	return retList;
    }

	@Override
	public void deleteLiveStatisticsOlderThan(Date date) {
		// TODO Auto-generated method stub
		
	}

}
