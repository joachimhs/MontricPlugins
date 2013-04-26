package org.eurekaj.plugins.cassandra;

import java.sql.*;
import java.util.*;
import java.util.Date;

import com.datastax.driver.core.*;
import com.datastax.driver.core.ResultSet;
import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.log4j.Logger;
import org.eurekaj.api.dao.*;
import org.eurekaj.api.datatypes.LiveStatistics;
import org.eurekaj.api.enumtypes.AlertStatus;
import org.eurekaj.api.enumtypes.UnitType;
import org.eurekaj.api.enumtypes.ValueType;
import org.eurekaj.api.service.EurekaJApplicationServices;
import org.eurekaj.plugins.cassandra.dao.CassandraAlertDao;
import org.eurekaj.plugins.cassandra.dao.CassandraLiveStatisticsDao;
import org.eurekaj.plugins.cassandra.datatypes.CassandraAlert;
import org.eurekaj.plugins.cassandra.datatypes.CassandraLiveStatistics;
import org.eurekaj.plugins.cassandra.datatypes.MetricHour;
import org.eurekaj.plugins.util.AlertStatusTypeConverter;
import org.eurekaj.spi.db.EurekaJDBPluginService;
import org.firebrandocm.dao.PersistenceFactory;
import org.firebrandocm.dao.TypeConverter;
import org.firebrandocm.dao.impl.*;
import org.firebrandocm.dao.impl.hector.HectorPersistenceFactory;
import org.joda.time.DateTime;

public class CassandraEnv extends EurekaJDBPluginService {
    private static Logger logger = Logger.getLogger(EurekaJDBPluginService.class.getName());

	private LiveStatisticsDao liveStatisticsDao;
	private EurekaJApplicationServices applicationServices;
    private AccountDao accountDao;
    private AlertDao alertDao;
    private int port;
    private Connection con;
    private PersistenceFactory persistenceFactory;
    private Session cassandraSession;

    public static void main(String[] args) {
        CassandraEnv env = new CassandraEnv();
        env.setup();

        String aarstallString = args[0];
        Integer aarstall = Integer.parseInt(aarstallString);
        String accountName = args[1];
        String metricName = args[2];

        DateTime fromDate = new DateTime(aarstall, 01, 01, 0, 0, 0);
        DateTime toDate = new DateTime(aarstall, 12, 31, 23, 59, 59);

        Long from15SecPeriod = fromDate.getMillis() / 15000;
        Long to15SecPeriod = toDate.getMillis() / 15000;

        long numMetrics = to15SecPeriod - from15SecPeriod;
        long index = 0;

        List<LiveStatistics> liveStatisticsList = new ArrayList<LiveStatistics>();
        while (index <= numMetrics) {
            liveStatisticsList.add(new CassandraLiveStatistics(metricName, accountName, from15SecPeriod + index, new Double(index), ValueType.AGGREGATE.value(), UnitType.N.value()));
            index++;
        }

        env.getLiveStatissticsDao().storeIncomingStatistics(liveStatisticsList);

        System.out.println("Stored " + index + " values in the database");
    }

	@Override
	public String getPluginName() {
		return "CassandraPlugin";
	}

    public void setPort(int port) {
        this.port = port;
    }


    public PersistenceFactory getPersistenceFactory() {
        return persistenceFactory;
    }

    public Session getCassandraSession() {
        return cassandraSession;
    }

    public void setupCql() {
        int cassandraPort = 9160;
        if (this.port > 0) { cassandraPort = this.port; }


    }

    @Override
	public void setup() {
        int cassandraPort = 9160;
        if (this.port > 0) { cassandraPort = this.port; }

        Cluster cluster = Cluster.builder()
                .addContactPoint("192.168.1.102")
                .addContactPoint("192.168.1.104")
                .addContactPoint("192.168.1.105")
                .build();

        Session session = cluster.connect();
        this.cassandraSession = session;

        ResultSet rs = session.execute("select * from system.schema_keyspaces where keyspace_name = 'eurekaj_keyspace'");
        Iterator<Row> rows = rs.iterator();
        if (!rows.hasNext()) {
            logger.info("Creating keyspace eurekaj_keyspace");
            session.execute("CREATE KEYSPACE eurekaj_keyspace WITH replication = { 'class' : 'SimpleStrategy', 'replication_factor' : 3 }");
        }

        logger.info("USING eurekaj_keyspace");
        session.execute("USE eurekaj_keyspace");

        //SELECT keyspace_name, columnfamily_name FROM system.schema_columnfamilies  where keyspace_name = 'eurekaj_keyspace';
        List<String> columnFamilyList = new ArrayList<String>();
        List<String> indexList = new ArrayList<String>();

        rs = session.execute("SELECT keyspace_name, columnfamily_name FROM system.schema_columnfamilies where keyspace_name = 'eurekaj_keyspace'");
        rows = rs.iterator();
        while (rows.hasNext()) {
            Row row = rows.next();
            columnFamilyList.add(row.getString("columnfamily_name"));
        }

        rs = session.execute("select index_name from system.schema_columns where keyspace_name='eurekaj_keyspace'");
        rows = rs.iterator();
        while (rows.hasNext()) {
            Row row = rows.next();
            if (row.getString("index_name") != null) {
                indexList.add(row.getString("index_name"));
            }
        }

        if (!columnFamilyList.contains("metric_hour")) {
            logger.info("Creating column family metric_hour");
            //session.execute("CREATE TABLE metric_hour (guiPath varchar, accountName varchar, hoursSince1970 int, metrics map<int, double>, valueType varchar, unitType varchar, PRIMARY KEY (guiPath, accountName, hoursSince1970))");
            StringBuilder sb = new StringBuilder();
            sb.append("CREATE TABLE metric_hour (guiPath varchar, accountName varchar, hoursSince1970 int, valueType varchar, unitType varchar, ");
            for (int i = 0; i < 240; i++) {
                if (i > 0) {
                    sb.append(",");
                }
                sb.append("m").append(i).append(" double");
            }
            sb.append(", PRIMARY KEY (guiPath, accountName, hoursSince1970))");
            logger.info(sb.toString());
            session.execute(sb.toString());
        }

        if (!columnFamilyList.contains("live_statistics")) {
            logger.info("Creating column family live_statistics");
            session.execute("CREATE TABLE live_statistics (guiPath varchar, accountName varchar, timeperiod int, value double, valueType varchar, unitType varchar, isCalculated boolean, PRIMARY KEY (accountName, guiPath, timeperiod))");
        }

        if (!columnFamilyList.contains("alerts")) {
            logger.info("Creating column family alerts");
            session.execute("CREATE TABLE alerts ( id varchar, accountName varchar, alertName varchar, guiPath varchar, activated boolean, errorValue double, warningValue double, selectedAlertType varchar, alertDelay int, status varchar, emailSenderList list<varchar>, alertPluginList list<varchar>, PRIMARY KEY (id))");
        }

        if (!indexList.contains("alerts_for_account")) {
            logger.info("Creating index alerts_for_account");
            session.execute("CREATE INDEX alerts_for_account ON alerts (accountName)");
        }

        if (!indexList.contains("live_stat_is_calculated")) {
            logger.info("Creating index live_stat_is_calculated");
            session.execute("CREATE INDEX live_stat_is_calculated ON live_statistics (iscalculated)");
        }

        //select * from system.schema_columns where keyspace_name='eurekaj_keyspace';
        /*

        eurekaJCluster = HFactory.getOrCreateCluster("eurekaj_cluster", "localhost:" + cassandraPort);

		KeyspaceDefinition keyspaceDef = eurekaJCluster.describeKeyspace("eurekaj_keyspace");

		if (keyspaceDef == null) {
            keyspaceDef = setupKeyspace();
            eurekaJCluster.addKeyspace(keyspaceDef, true);
		}

        try {
            Class.forName("org.apache.cassandra.cql.jdbc.CassandraDriver");
            con = DriverManager.getConnection("jdbc:cassandra://localhost:" + port + "/eurekaj_keyspace");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        List<ColumnFamilyDefinition> columnFamilyDefinitionList = keyspaceDef.getCfDefs();
        for (ColumnFamilyDefinition cfDef : columnFamilyDefinitionList) {
            if (cfDef.getName().equalsIgnoreCase("alerts")) {
                try {
                    defineAlertColumnFamily(cfDef);
                } catch (SQLException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }

        for (ColumnFamilyDefinition cfDef : columnFamilyDefinitionList) {
            String cfName = cfDef.getName();
            for (ColumnDefinition cd : cfDef.getColumnMetadata()) {
                logger.info("\t" + cfName + " : " + cd.getName() + " :: " + cd.getIndexName());
            }
        }

        List<Class<?>> entities = new ArrayList<Class<?>>();
        entities.add(CassandraAlert.class);
        entities.add(MetricHour.class);
        try {

            persistenceFactory = new HectorPersistenceFactory.Builder()
                    .defaultConsistencyLevel(ConsistencyLevel.ANY)
                    .clusterName("eurekaj_cluster")
                    .defaultKeySpace("eurekaj_keyspace")
                    .contactNodes(new String[] {"localhost"})
                    .thriftPort(port)
                    .autoDiscoverHosts(true)
                    .entities(entities)
                    //.typeConverters(typeConverters)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        eurekaJKeyspace = HFactory.createKeyspace("eurekaj_keyspace", eurekaJCluster);
		*/
		liveStatisticsDao = new CassandraLiveStatisticsDao(this);
        alertDao = new CassandraAlertDao(this);

	}

    /*private void defineAlertColumnFamily(ColumnFamilyDefinition cfDef) throws SQLException {
        ColumnDefinition alertsForAccountIndex = null;

        for (ColumnDefinition columnDefinition : cfDef.getColumnMetadata()) {
            if (columnDefinition.getName().equals("alerts_for_account")) {
                alertsForAccountIndex = columnDefinition;
                break;
            }
        }

        if (alertsForAccountIndex == null) {
            //create new secondary index alerts_for_account
            String query = "CREATE INDEX alerts_for_account ON alerts (accountname)";
            PreparedStatement statement = con.prepareStatement(query);
            statement.executeUpdate();
            logger.info("Created Secondary Index alerts_for_account on column family alerts");

        } else {
            logger.info("alerts_for_account already exists: " + alertsForAccountIndex + " :: " + alertsForAccountIndex.getIndexName() + " :: " + alertsForAccountIndex.getName());
        }
    }

    public Keyspace getEurekaJKeyspace() {
		return eurekaJKeyspace;
	}

	private KeyspaceDefinition setupKeyspace() {
		ColumnFamilyDefinition liveStatsCfDef = HFactory.createColumnFamilyDefinition("eurekaj_keyspace", "live_statistics", ComparatorType.ASCIITYPE);
        logger.info("Configured column family live_statistics");

        List<ColumnDefinition> alertsColumns = new ArrayList<ColumnDefinition>();
        BasicColumnDefinition accountNameColumn = new BasicColumnDefinition();
        accountNameColumn.setName(StringSerializer.get().toByteBuffer("accountname"));
        accountNameColumn.setValidationClass(ComparatorType.UTF8TYPE.getClassName());
        alertsColumns.add(accountNameColumn);

        ColumnFamilyDefinition alertCfDef = HFactory.createColumnFamilyDefinition("eurekaj_keyspace", "alerts", ComparatorType.UTF8TYPE, alertsColumns);
        logger.info("Configured column family alerts");

        KeyspaceDefinition keyspaceDef = HFactory.createKeyspaceDefinition("eurekaj_keyspace", ThriftKsDef.DEF_STRATEGY_CLASS, 1, Arrays.asList(liveStatsCfDef, alertCfDef));
        logger.info("Configured keyspace eurekaj_keyspace");
        return keyspaceDef;
	}*/

	@Override
	public void tearDown() {
		// TODO Auto-generated method stub

	}

	@Override
	public AlertDao getAlertDao() {
		return alertDao;
	}

	@Override
	public GroupedStatisticsDao getGroupedStatisticsDao() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LiveStatisticsDao getLiveStatissticsDao() {
		return liveStatisticsDao;
	}

    public AccountDao getAccountDao() {
        return accountDao;
    }

    @Override
	public SmtpDao getSmtpDao() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TreeMenuDao getTreeMenuDao() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void close() {
		
	}
	
	@Override
	public void setApplicationServices(EurekaJApplicationServices applicationServices) {
		this.applicationServices = applicationServices;
	}
	
	public EurekaJApplicationServices getApplicationServices() {
		return applicationServices;
	}

}
