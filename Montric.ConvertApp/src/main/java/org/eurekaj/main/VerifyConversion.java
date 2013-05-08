package org.eurekaj.main;

import java.util.List;

import org.eurekaj.api.datatypes.LiveStatistics;
import org.eurekaj.berkeley.hour.db.BerkeleyDbEnv;
import org.eurekaj.plugins.cassandra.CassandraEnv;

public class VerifyConversion {

	public static void main(String[] args) {
		Long minTimeperiod = 88113896l;//(1323297825l / 15) - 1000;
		Long maxTimeperiod = 88253917l;//minTimeperiod + 2000;
				
		String guiPath = "JSFlotJAgent:Memory:Heap:Used";
		
		if (args.length != 2) {
			System.err.println("Param1: Old Database path. \nParam2: New Database path.");			
			System.exit(-1);
		}
		
		String oldDBPath = args[0];
		String newDBPath = args[1];
		
		System.setProperty("montric.db.type", "CassandraPlugin");
		System.setProperty("montric.db.absPath", newDBPath);
		
		System.out.println("Setting up the new dev");
		//BerkeleyDbEnv newEnv = new BerkeleyDbEnv();
		CassandraEnv newEnv = new CassandraEnv();
		newEnv.setup();
		
		List<LiveStatistics> liveStatList = newEnv.getLiveStatissticsDao().getLiveStatistics(guiPath, minTimeperiod, maxTimeperiod);
		System.out.println("statsize: " + liveStatList.size());
	
		for (LiveStatistics ls : liveStatList) {
			if (ls.getValue() != null) {
				System.out.println(ls.getTimeperiod() + ";" + ls.getValue());
			}
		}
		
		System.out.println("----------------");
		
		System.setProperty("montric.db.type", "Berkeley");
		System.setProperty("montric.db.absPath", oldDBPath);
		
		System.out.println("Setting up the old dev");
		org.eurekaj.berkeley.db.BerkeleyDbEnv oldEnv = new org.eurekaj.berkeley.db.BerkeleyDbEnv();
		oldEnv.setup();
		
		liveStatList = oldEnv.getLiveStatissticsDao().getLiveStatistics(guiPath, minTimeperiod, maxTimeperiod);
		System.out.println("statsize: " + liveStatList.size());
	
		for (LiveStatistics ls : liveStatList) {
			System.out.println(ls.getTimeperiod() + ";" + ls.getValue());
		}
		
	}
}
