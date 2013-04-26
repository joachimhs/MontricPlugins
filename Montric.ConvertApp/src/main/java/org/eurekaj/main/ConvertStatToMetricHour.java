package org.eurekaj.main;

import java.text.DecimalFormat;
import java.util.List;

import org.eurekaj.api.datatypes.Alert;
import org.eurekaj.api.datatypes.EmailRecipientGroup;
import org.eurekaj.api.datatypes.GroupedStatistics;
import org.eurekaj.api.datatypes.LiveStatistics;
import org.eurekaj.api.datatypes.TreeMenuNode;
import org.eurekaj.api.datatypes.TriggeredAlert;
import org.eurekaj.api.enumtypes.UnitType;
import org.eurekaj.api.enumtypes.ValueType;
import org.eurekaj.berkeley.hour.db.BerkeleyDbEnv;
import org.eurekaj.plugins.cassandra.CassandraEnv;

public class ConvertStatToMetricHour {

	public static void main(String[] args) {
		if (args.length != 2) {
			System.err.println("Param1: Old Database path. \nParam2: New Database path.");			
			System.exit(-1);
		}
		
		String oldDBPath = args[0];
		String newDBPath = args[1];
		
		System.setProperty("eurekaj.db.type", "Berkeley");
		System.setProperty("eurekaj.db.absPath", oldDBPath);
		
		System.out.println("Setting up the old dev");
		org.eurekaj.berkeley.db.BerkeleyDbEnv oldEnv = new org.eurekaj.berkeley.db.BerkeleyDbEnv();
		oldEnv.setup();
		
		System.setProperty("eurekaj.db.type", "BerkeleyHour");
		System.setProperty("eurekaj.db.absPath", newDBPath);
		
		System.out.println("Setting up the new dev");
		//BerkeleyDbEnv newEnv = new BerkeleyDbEnv();
		CassandraEnv newEnv = new CassandraEnv();
		newEnv.setup();
		System.out.println("newEnv set up: " + newEnv.getLiveStatissticsDao());
		
		long hoursSince1970 = System.currentTimeMillis() / 3600000;
		long fifteenSecPeriods = System.currentTimeMillis() / 15000;
		
		DecimalFormat df = new DecimalFormat("#0.000");
		
		List<TreeMenuNode> treeMenuList = oldEnv.getTreeMenuDao().getTreeMenu();
		int numNodesConverted = 0;
		int totalNodesConverted = 0;
		Long minTimeperiod = 999999999999999l;
		Long maxTimeperiod = 0l;
		for (TreeMenuNode treeNode : treeMenuList) {
			numNodesConverted++;
			
			Long before = System.currentTimeMillis();
			System.out.println("Starting conversion (" + numNodesConverted + "/" + treeMenuList.size() + ") of: " + treeNode.getGuiPath() + " up untill: " + fifteenSecPeriods);
			List<LiveStatistics> statList = oldEnv.getLiveStatissticsDao().getLiveStatistics(treeNode.getGuiPath(), 0l, fifteenSecPeriods);
			for (LiveStatistics stat : statList) {
				String stringVal = null;
				try {
					if (stat.getValue() != null) {
						stringVal = df.format(stat.getValue());
					}
				} catch (IllegalArgumentException iae) {
					System.out.println("Unable to convert value: " + stat.getValue());
					throw new IllegalArgumentException(iae);
				}
				
				if (minTimeperiod > stat.getTimeperiod()) {
					minTimeperiod = stat.getTimeperiod();
				}
				
				if (maxTimeperiod < stat.getTimeperiod()) {
					maxTimeperiod = stat.getTimeperiod();
				}
				
				newEnv.getLiveStatissticsDao().storeIncomingStatistics(treeNode.getGuiPath(), stat.getTimeperiod(), stringVal, ValueType.VALUE, UnitType.N);
			}
			System.out.println("Conversion took: " + (System.currentTimeMillis() - before) + "ms. Converted stats: " + statList.size());
			System.out.println("---");
			
			totalNodesConverted += statList.size();
		}
		
		System.out.println("------\nTotal number of converted Statistics: " + totalNodesConverted + " minTimeperiod: " + minTimeperiod + " maxTimeperiod: " + maxTimeperiod + "\n--------\n");
		
		/*System.out.println("Converting alerts");
		List<Alert> alertList = oldEnv.getAlertDao().getAlerts(); 
		for (Alert alert : alertList) {
			newEnv.getAlertDao().persistAlert(alert); 
		}
		
		System.out.println("Converting Triggered Alerts");
		List<TriggeredAlert> triggeredAlertList = oldEnv.getAlertDao().getTriggeredAlerts(0l, fifteenSecPeriods);
		for (TriggeredAlert triggeredAlert : triggeredAlertList) {
			newEnv.getAlertDao().persistTriggeredAlert(triggeredAlert);
		}
		
		System.out.println("Converting Grouped Statistics");
		List<GroupedStatistics> groupedStatisticsList = oldEnv.getGroupedStatisticsDao().getGroupedStatistics();
		for (GroupedStatistics gs : groupedStatisticsList) {
			newEnv.getGroupedStatisticsDao().persistGroupInstrumentation(gs);
		}
		
		System.out.println("Converting Email Recipient Groups");
		List<EmailRecipientGroup> emailRecipientGroupList = oldEnv.getSmtpDao().getEmailRecipientGroups();
		for (EmailRecipientGroup eg : emailRecipientGroupList) {
			newEnv.getSmtpDao().persistEmailRecipientGroup(eg);
		}*/
		
		
		
		oldEnv.close();
		newEnv.close();
	}
}
