package org.eurekaj.plugins.cassandra;

import java.util.Arrays;

import me.prettyprint.cassandra.service.ThriftKsDef;
import me.prettyprint.cassandra.service.template.ColumnFamilyResult;
import me.prettyprint.cassandra.service.template.ColumnFamilyTemplate;
import me.prettyprint.cassandra.service.template.ColumnFamilyUpdater;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.ComparatorType;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.exceptions.HectorException;
import me.prettyprint.hector.api.factory.HFactory;

public class CassandraCluster {
	private Cluster eurekaJCluster;
	private Keyspace eurekaJKeyspace;
	private ColumnFamilyTemplate<String, String> liveStatisticsTemplate;
	
	public CassandraCluster() {
	}
	
	private void testCassandra() {
		System.out.println("testCassandra() called");
		
		System.out.println("testCassandra() updating livestats 123");
		
		/*Mutator<String> mutator = HFactory.createMutator(eurekaJKeyspace, StringSerializer.get());
		mutator.addInsertion("123", "live_statistics", HFactory.createStringColumn("domain", "www.datastax.com"));
		mutator.addInsertion("123", "live_statistics", HFactory.createColumn("time", System.currentTimeMillis(), StringSerializer.get(), LongSerializer.get()));
		mutator.execute();*/
			
		ColumnFamilyUpdater<String, String> updater = liveStatisticsTemplate.createUpdater("123");
		updater.setString("domain", "www.datastax.com");
		updater.setLong("time", System.currentTimeMillis());
		
		liveStatisticsTemplate.update(updater);
		
		
		System.out.println("testCassandra() getting livestats 123");
		try {
		    ColumnFamilyResult<String, String> res = liveStatisticsTemplate.queryColumns("123");
		    String value = res.getString("domain");
		    Long time = res.getLong("time");
		    System.out.println("domain: " + value + " time " + time);
		    // value should be "www.datastax.com" as per our previous insertion.
		} catch (HectorException e) {
		    // do something ...
		}
		
		System.out.println("testCassandra() finished");
	}
	
	public static void main(String[] args) {
		CassandraCluster cluster = new CassandraCluster();
		cluster.testCassandra();
	}

	private void setupKeyspace() {
		ColumnFamilyDefinition cfDef = HFactory.createColumnFamilyDefinition("eurekaj_keyspace", "live_statistics", ComparatorType.ASCIITYPE);
		KeyspaceDefinition keyspaceDef = HFactory.createKeyspaceDefinition("eurekaj_keyspace", ThriftKsDef.DEF_STRATEGY_CLASS, 1, Arrays.asList(cfDef));
		eurekaJCluster.addKeyspace(keyspaceDef, true);
	}
	
}
