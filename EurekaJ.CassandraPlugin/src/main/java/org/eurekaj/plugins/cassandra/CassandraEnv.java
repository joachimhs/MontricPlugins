package org.eurekaj.plugins.cassandra;

import java.util.Arrays;

import me.prettyprint.cassandra.service.ThriftKsDef;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.ComparatorType;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.factory.HFactory;

import org.eurekaj.api.dao.AlertDao;
import org.eurekaj.api.dao.GroupedStatisticsDao;
import org.eurekaj.api.dao.LiveStatisticsDao;
import org.eurekaj.api.dao.SmtpDao;
import org.eurekaj.api.dao.TreeMenuDao;
import org.eurekaj.api.service.EurekaJApplicationServices;
import org.eurekaj.plugins.cassandra.dao.CassandraLiveStatisticsDao;
import org.eurekaj.spi.db.EurekaJDBPluginService;

public class CassandraEnv extends EurekaJDBPluginService {
	private Cluster eurekaJCluster;
	private Keyspace eurekaJKeyspace;
	private LiveStatisticsDao liveStatisticsDao;
	private EurekaJApplicationServices applicationServices;

	@Override
	public String getPluginName() {
		return "CassandraPlugin";
	}

	@Override
	public void setup() {
		eurekaJCluster = HFactory.getOrCreateCluster("eurekaj_cluster", "localhost:9160");

		KeyspaceDefinition keyspaceDef = eurekaJCluster.describeKeyspace("eurekaj_keyspace");

		if (keyspaceDef == null) {
			setupKeyspace();
		}

		eurekaJKeyspace = HFactory.createKeyspace("eurekaj_keyspace", eurekaJCluster);
		
		liveStatisticsDao = new CassandraLiveStatisticsDao(this);
	}
	
	public Keyspace getEurekaJKeyspace() {
		return eurekaJKeyspace;
	}

	private void setupKeyspace() {
		ColumnFamilyDefinition cfDef = HFactory.createColumnFamilyDefinition("eurekaj_keyspace", "live_statistics", ComparatorType.ASCIITYPE);
		KeyspaceDefinition keyspaceDef = HFactory.createKeyspaceDefinition("eurekaj_keyspace", ThriftKsDef.DEF_STRATEGY_CLASS, 1, Arrays.asList(cfDef));
		eurekaJCluster.addKeyspace(keyspaceDef, true);
	}

	@Override
	public void tearDown() {
		// TODO Auto-generated method stub

	}

	@Override
	public AlertDao getAlertDao() {
		// TODO Auto-generated method stub
		return null;
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
