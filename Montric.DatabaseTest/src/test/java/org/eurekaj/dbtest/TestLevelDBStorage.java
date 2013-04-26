package org.eurekaj.dbtest;

import org.apache.log4j.Logger;
import org.eurekaj.api.datatypes.Account;
import org.eurekaj.api.datatypes.Alert;
import org.eurekaj.api.datatypes.EmailRecipientGroup;
import org.eurekaj.api.datatypes.GroupedStatistics;
import org.eurekaj.api.datatypes.LiveStatistics;
import org.eurekaj.api.datatypes.Statistics;
import org.eurekaj.api.datatypes.TriggeredAlert;
import org.eurekaj.api.datatypes.User;
import org.eurekaj.api.datatypes.basic.BasicAccount;
import org.eurekaj.api.datatypes.basic.BasicAlert;
import org.eurekaj.api.datatypes.basic.BasicEmailRecipientGroup;
import org.eurekaj.api.datatypes.basic.BasicGroupedStatistics;
import org.eurekaj.api.datatypes.basic.BasicStatistics;
import org.eurekaj.api.datatypes.basic.BasicTriggeredAlert;
import org.eurekaj.api.enumtypes.AlertStatus;
import org.eurekaj.api.enumtypes.AlertType;
import org.eurekaj.api.enumtypes.UnitType;
import org.eurekaj.api.enumtypes.ValueType;
import org.eurekaj.api.util.ListToString;
import org.eurekaj.plugins.leveldb.LevelDBEnv;
import org.eurekaj.plugins.riak.RiakEnv;
import org.eurekaj.spi.db.EurekaJDBPluginService;
import org.joda.time.DateTime;
import org.junit.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: jhs
 * Date: 4/18/12
 * Time: 11:15 AM
 * To change this template use File | Settings | File Templates.
 */
public class TestLevelDBStorage {
    private static Logger logger = Logger.getLogger(TestLevelDBStorage.class.getName());

    private EurekaJDBPluginService newEnv;

    @BeforeClass
    public static void beforeClass() throws IOException, InterruptedException, URISyntaxException {
        //EmbeddedCassandraServerHelper.startEmbeddedCassandra("cassandra-test.yaml");
    }

    @AfterClass
    public static void afterClass() throws IOException {
        //EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
        //EmbeddedCassandraServerHelper.stopEmbeddedCassandra();
    }

    @Before
    public void setup() {
        newEnv = new LevelDBEnv();
        System.setProperty("eurekaj.db.absPath", "/srv/eurekaj/eurekajDataTest");
        /*System.setProperty("eurekaj.db.type", "BerkeleyHour");
        System.setProperty("eurekaj.db.absPath", "/srv/eurekaj/eurekajDataTest");
        newEnv = new BerkeleyDbEnv();*/
        //newEnv.setPort(9171);
		newEnv.setup();
		logger.info("newEnv set up: " + newEnv.getLiveStatissticsDao());
    }

    @After
    public void tearDown() {
        newEnv.tearDown();
        newEnv = null;
    }

    @Test
    public void testCreateKeyspace() {

    }

    @Test
    public void testStoringMetricHour() {
        //DateTime fromDate = new DateTime(2012, 04, 18, 9, 0, 0);
        //DateTime toDate = new DateTime(2012, 04, 18, 12, 0, 0);
        DateTime fromDate = new DateTime(2010, 01, 01, 0, 0, 0);
        DateTime toDate = new DateTime(2010, 12, 31, 23, 59, 59);

        Long from15SecPeriod = fromDate.getMillis() / 15000;
        Long to15SecPeriod = toDate.getMillis() / 15000;

        long numMetrics = to15SecPeriod - from15SecPeriod;
        long index = 0;


        while (index <= numMetrics) {
            for (int i = 0; i < 100; i++) {
                List<LiveStatistics> liveStatisticsList = new ArrayList<LiveStatistics>();
                liveStatisticsList.add(new TestLiveStatistics("EurekaJAgent:Memory:Heap:Used" + i, "ACCOUNT NAME", from15SecPeriod + index, new Double(index), ValueType.AGGREGATE.value(), UnitType.N.value()));
                newEnv.getLiveStatissticsDao().storeIncomingStatistics(liveStatisticsList);
            }

            if (index > 0 && index % 500 == 0) {
                logger.info("stored 500 keys for: " + index + " 15 second time periods. Now at: " + index + " of " + numMetrics + " hours");
            }
            index++;
        }

        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        System.out.println("Stored " + index + " values in the database");

        int expectedNumMetrics = (4 * 60 * 3) + 1; //3 hours worth of metrics
        expectedNumMetrics = (4 * 60 * 24 * 365); //31 days worth of metrics
        List<LiveStatistics> statList = newEnv.getLiveStatissticsDao().getLiveStatistics("Test:A", "ACCOUNT NAME", from15SecPeriod, to15SecPeriod);

        Assert.assertEquals("Expecting " + expectedNumMetrics + " LiveStatistcs back from DB", expectedNumMetrics, statList.size());

        //newEnv.getLiveStatissticsDao().deleteLiveStatisticsOlderThan(new DateTime(2010, 12, 14, 23, 59, 59).toDate(), "ACCOUNT NAME");
        //newEnv.getLiveStatissticsDao().markLiveStatisticsAsCalculated("Test:A", "ACCOUNT NAME", from15SecPeriod, to15SecPeriod);

        //newEnv.getLiveStatissticsDao().deleteMarkedLiveStatistics();

        /*statList = newEnv.getLiveStatissticsDao().getLiveStatistics("Test:A", "ACCOUNT NAME", from15SecPeriod, to15SecPeriod);

        for (LiveStatistics stat : statList) {
            printStat(stat);
            Assert.assertNull("Expecting that value is NULL for timeperiod: " + stat.getTimeperiod(), stat.getValue());
        }*/
   }

    private void printStat(LiveStatistics ls) {
        long ms = ls.getTimeperiod() * 15000;
        DateTime time = new DateTime(ms);

        StringBuilder sb = new StringBuilder();
        sb.append(time.getDayOfMonth()).append(".").append(time.getMonthOfYear()).append(".").append(time.getYear());
        sb.append(" ").append(time.getHourOfDay()).append(":").append(time.getMinuteOfHour()).append(":").append(time.getSecondOfMinute());

        System.out.println(ls.getTimeperiod() + ";" + ls.getAccountName() + "; date: " + sb.toString() + " value: " + ls.getValue() + " unitType: " + ls.getUnitType() + " valueType: " + ls.getValueType());
    }

    @Test
    public void testPersistAlert() {
        BasicAlert alert = new BasicAlert("ACCOUNT NAME", "New Alert 123");
        alert.setSelectedEmailSenderList(ListToString.convertToList("joachim@haagen.name;joachim@haagen-software.no", ";"));
        alert.setSelectedAlertPluginList(ListToString.convertToList("Plugin 1;Plugin 2", ";"));
        alert.setActivated(true);
        alert.setAlertDelay(3);
        alert.setErrorValue(22.5d);
        alert.setWarningValue(17.2d);
        alert.setGuiPath("EurekaJAgent:Memory:Heap:% Used");
        alert.setSelectedAlertType(AlertType.GREATER_THAN);
        alert.setStatus(AlertStatus.NORMAL);

        BasicAlert alertTwo = new BasicAlert("ACCOUNT TWO", "New Alert 123");
        alertTwo.setSelectedEmailSenderList(ListToString.convertToList("joachim@haagen.name;joachim@haagen-software.no", ";"));
        alertTwo.setSelectedAlertPluginList(ListToString.convertToList("Plugin 1;Plugin 2", ";"));
        alertTwo.setActivated(true);
        alertTwo.setAlertDelay(3);
        alertTwo.setErrorValue(22.5d);
        alertTwo.setWarningValue(17.2d);
        alertTwo.setGuiPath("EurekaJAgent:Memory:Heap:% Used");
        alertTwo.setSelectedAlertType(AlertType.GREATER_THAN);
        alertTwo.setStatus(AlertStatus.NORMAL);

        newEnv.getAlertDao().persistAlert(alert);
        newEnv.getAlertDao().persistAlert(alertTwo);

        Alert persistedAlert = newEnv.getAlertDao().getAlert("New Alert 123", "ACCOUNT NAME");
        Assert.assertNotNull("Expecting to be able to retrieve an alert from the DB.", persistedAlert);
        Assert.assertEquals("ACCOUNT NAME", persistedAlert.getAccountName());

        List<Alert> alertList = newEnv.getAlertDao().getAlerts("ACCOUNT NAME");
        Assert.assertNotNull("Expecting a not null alert list!", alertList);
        Assert.assertEquals("Expecting one alert", 1, alertList.size());
        Assert.assertEquals("ACCOUNT NAME", alertList.get(0).getAccountName());
        Assert.assertEquals("New Alert 123", alertList.get(0).getAlertName());
        Assert.assertEquals(2, alertList.get(0).getSelectedEmailSenderList().size());
        Assert.assertEquals("joachim@haagen.name", alertList.get(0).getSelectedEmailSenderList().get(0));
        Assert.assertEquals("joachim@haagen-software.no", alertList.get(0).getSelectedEmailSenderList().get(1));
        Assert.assertEquals(2, alertList.get(0).getSelectedAlertPluginList().size());
        Assert.assertEquals("Plugin 1", alertList.get(0).getSelectedAlertPluginList().get(0));
        Assert.assertEquals("Plugin 2", alertList.get(0).getSelectedAlertPluginList().get(1));
        Assert.assertEquals(true, alertList.get(0).isActivated());
        Assert.assertEquals(3, alertList.get(0).getAlertDelay());
        Assert.assertEquals(new Double(22.5d), alertList.get(0).getErrorValue());
        Assert.assertEquals(new Double(17.2d), alertList.get(0).getWarningValue());
        Assert.assertEquals(AlertType.GREATER_THAN, alertList.get(0).getSelectedAlertType());
        Assert.assertEquals(AlertStatus.NORMAL, alertList.get(0).getStatus());

        alertList = newEnv.getAlertDao().getAlerts("ACCOUNT TWO");
        Assert.assertNotNull("Expecting a not null alert list!", alertList);
        Assert.assertEquals("Expecting one alert", 1, alertList.size());
        Assert.assertEquals("ACCOUNT TWO", alertList.get(0).getAccountName());
        Assert.assertEquals("New Alert 123", alertList.get(0).getAlertName());
        Assert.assertEquals(2, alertList.get(0).getSelectedEmailSenderList().size());
        Assert.assertEquals("joachim@haagen.name", alertList.get(0).getSelectedEmailSenderList().get(0));
        Assert.assertEquals("joachim@haagen-software.no", alertList.get(0).getSelectedEmailSenderList().get(1));
        Assert.assertEquals(2, alertList.get(0).getSelectedAlertPluginList().size());
        Assert.assertEquals("Plugin 1", alertList.get(0).getSelectedAlertPluginList().get(0));
        Assert.assertEquals("Plugin 2", alertList.get(0).getSelectedAlertPluginList().get(1));
        Assert.assertEquals(true, alertList.get(0).isActivated());
        Assert.assertEquals(3, alertList.get(0).getAlertDelay());
        Assert.assertEquals(new Double(22.5d), alertList.get(0).getErrorValue());
        Assert.assertEquals(new Double(17.2d), alertList.get(0).getWarningValue());
        Assert.assertEquals(AlertType.GREATER_THAN, alertList.get(0).getSelectedAlertType());
        Assert.assertEquals(AlertStatus.NORMAL, alertList.get(0).getStatus());

        newEnv.getAlertDao().deleteAlert("New Alert 123", "ACCOUNT NAME");
        newEnv.getAlertDao().deleteAlert("New Alert 123", "ACCOUNT TWO");

        alertList = newEnv.getAlertDao().getAlerts("ACCOUNT NAME");
        Assert.assertNotNull("Expecting a not null alert list!", alertList);
        Assert.assertEquals(new Integer(0), new Integer(alertList.size()));
        alertList = newEnv.getAlertDao().getAlerts("ACCOUNT TWO");
        Assert.assertNotNull("Expecting a not null alert list!", alertList);
        Assert.assertEquals(new Integer(0), new Integer(alertList.size()));
    }

    @Test
    public void testTriggeredAlerts() {
        DateTime triggeredDate = new DateTime(2012, 04, 18, 15, 10, 45);
        long initialTimeperiod = triggeredDate.getMillis() / 15000;
        long timeperiod = triggeredDate.getMillis() / 15000;

        BasicTriggeredAlert triggeredAlert = new BasicTriggeredAlert();
        triggeredAlert.setAccountName("ACCOUNT NAME");
        triggeredAlert.setAlertName("EurekaJ:Memory:Heap:Used");
        triggeredAlert.setAlertValue(98.6d);
        triggeredAlert.setWarningValue(78.5d);
        triggeredAlert.setErrorValue(95.4d);
        triggeredAlert.setTimeperiod(timeperiod);
        triggeredAlert.setTriggeredTimeperiod(timeperiod);

        newEnv.getAlertDao().persistTriggeredAlert(triggeredAlert);

        timeperiod++;
        BasicTriggeredAlert triggeredAlert1 = new BasicTriggeredAlert(triggeredAlert);
        triggeredAlert1.setTimeperiod(++timeperiod);
        triggeredAlert1.setTriggeredTimeperiod(++timeperiod);
        triggeredAlert1.setAlertName("EurekaJ:Memory:Heap:Max");
        newEnv.getAlertDao().persistTriggeredAlert(triggeredAlert1);

        timeperiod++;
        BasicTriggeredAlert triggeredAlert2 = new BasicTriggeredAlert(triggeredAlert);
        triggeredAlert2.setTimeperiod(timeperiod);
        triggeredAlert2.setTriggeredTimeperiod(timeperiod);
        triggeredAlert2.setAlertName("EurekaJ:Memory:Heap:Max");
        newEnv.getAlertDao().persistTriggeredAlert(triggeredAlert2);

        timeperiod++;
        BasicTriggeredAlert triggeredAlert3 = new BasicTriggeredAlert(triggeredAlert);
        triggeredAlert3.setTimeperiod(timeperiod);
        triggeredAlert3.setTriggeredTimeperiod(timeperiod);
        newEnv.getAlertDao().persistTriggeredAlert(triggeredAlert3);

        timeperiod++;
        BasicTriggeredAlert triggeredAlert4 = new BasicTriggeredAlert(triggeredAlert);
        triggeredAlert4.setTimeperiod(timeperiod);
        triggeredAlert4.setTriggeredTimeperiod(timeperiod);
        newEnv.getAlertDao().persistTriggeredAlert(triggeredAlert4);

        timeperiod++;
        BasicTriggeredAlert triggeredAlert5 = new BasicTriggeredAlert(triggeredAlert);
        triggeredAlert5.setTimeperiod(timeperiod);
        triggeredAlert5.setTriggeredTimeperiod(timeperiod);
        newEnv.getAlertDao().persistTriggeredAlert(triggeredAlert5);


        logger.info("from ts: " + initialTimeperiod + " to ts: " + timeperiod);

        List<TriggeredAlert> triggeredAlertList = newEnv.getAlertDao().getTriggeredAlerts("ACCOUNT NAME",
                initialTimeperiod,
                timeperiod);

        Assert.assertEquals(6, triggeredAlertList.size());

        triggeredAlertList = newEnv.getAlertDao().getTriggeredAlerts(
                "EurekaJ:Memory:Heap:Max",
                "ACCOUNT NAME",
                initialTimeperiod,
                timeperiod);

        Assert.assertEquals(2, triggeredAlertList.size());
    }
    
    @Test
    public void testAccount() {
    	BasicAccount accountOne = new BasicAccount("Account Name", "Normal");
    	newEnv.getAccountDao().persistAccount(accountOne);
    	
    	Account accountOneFromDb = newEnv.getAccountDao().getAccount("Account Name");
    	
    	Assert.assertNotNull(accountOneFromDb);
    	Assert.assertEquals("Normal", accountOneFromDb.getAccountType());
    	
    	BasicAccount accountTwo = new BasicAccount("Account Name Two", "Gold");
    	newEnv.getAccountDao().persistAccount(accountTwo);

    	Account accountTwoFromDb = newEnv.getAccountDao().getAccount("Account Name Two");
    	
    	Assert.assertNotNull(accountTwoFromDb);
    	Assert.assertEquals("Gold", accountTwoFromDb.getAccountType());
    	
    	List<Account> accountlist = newEnv.getAccountDao().getAccounts();
    	
    	Assert.assertNotNull(accountlist);
    	Assert.assertEquals(2, accountlist.size());
    }
    
    @Test
    public void testUser() {
    	newEnv.getAccountDao().persistUser("joachimhs", "Account Name", "Admin");
    	newEnv.getAccountDao().persistUser("joachimhs", "Account Name Two", "Admin");
    	newEnv.getAccountDao().persistUser("brandnewuser", "Account Name", "User");
    	
    	User userOne = newEnv.getAccountDao().getUser("joachimhs", "Account Name");
    	User userTwo = newEnv.getAccountDao().getUser("joachimhs", "Account Name Two");
    	User userThree = newEnv.getAccountDao().getUser("brandnewuser", "Account Name");
    	User userNull = newEnv.getAccountDao().getUser("brandnewuser", "Account Name Two");
    	
    	Assert.assertNotNull(userOne);
    	Assert.assertEquals("joachimhs", userOne.getUserName());
    	Assert.assertEquals("Account Name", userOne.getAccountName());
    	Assert.assertEquals("Admin", userOne.getUserRole());
    	
    	Assert.assertNotNull(userTwo);
    	Assert.assertEquals("joachimhs", userTwo.getUserName());
    	Assert.assertEquals("Account Name Two", userTwo.getAccountName());
    	Assert.assertEquals("Admin", userTwo.getUserRole());
    	
    	Assert.assertNotNull(userThree);
    	Assert.assertEquals("brandnewuser", userThree.getUserName());
    	Assert.assertEquals("Account Name", userThree.getAccountName());
    	Assert.assertEquals("User", userThree.getUserRole());
    	
    	Assert.assertNull(userNull);
    }
    
    @Test
    public void testGroupedStatistics() throws InterruptedException {
    	newEnv.getGroupedStatisticsDao().persistGroupInstrumentation(new BasicGroupedStatistics("Heap %", "Account Name", Arrays.asList("Heap:Used", "Heap:Max") ));
    	newEnv.getGroupedStatisticsDao().persistGroupInstrumentation(new BasicGroupedStatistics("Non-Heap %", "Account Name", Arrays.asList("Non-Heap:Used", "Non-Heap:Max") ));
    	
    	Thread.sleep(150);
    	GroupedStatistics gsOne = newEnv.getGroupedStatisticsDao().getGroupedStatistics("Heap %", "Account Name");
    	GroupedStatistics gsTwo = newEnv.getGroupedStatisticsDao().getGroupedStatistics("Non-Heap %", "Account Name");
    	
    	assertGroupedStatistics(gsOne, gsTwo);
    	
    	List<GroupedStatistics> gsList = newEnv.getGroupedStatisticsDao().getGroupedStatistics("Account Name");
    	Assert.assertNotNull(gsList);
    	Assert.assertEquals(2, gsList.size());
    	
    	assertGroupedStatistics(gsList.get(0), gsList.get(1));
    	
    	newEnv.getGroupedStatisticsDao().deleteGroupedChart("Heap %", "Account Name");
    	newEnv.getGroupedStatisticsDao().deleteGroupedChart("Non-Heap %", "Account Name");
    	
    	Thread.sleep(150);
    	gsList = newEnv.getGroupedStatisticsDao().getGroupedStatistics("Account Namde");
    	Assert.assertNotNull(gsList);
    	Assert.assertEquals(0, gsList.size());
    }
    
    private void assertGroupedStatistics(GroupedStatistics gsOne, GroupedStatistics gsTwo) {
    	int numVerifiedGroupedStats = 0;
    	Assert.assertNotNull(gsOne);
    	
    	//If gsOne and gsTwo is swapped, swap them back
    	if (gsTwo.getName().equals("Heap %") && gsOne.getName().equals("Non-Heap %")) {
    		GroupedStatistics gsTemp = gsOne;
    		gsOne = gsTwo;
    		gsTwo = gsTemp;
    	} 
    	
    	if (gsOne.getName().equals("Heap %") || gsTwo.getName().equals("Non-Heap %")) {
    	
	    	Assert.assertEquals("Heap %", gsOne.getName());
	    	Assert.assertEquals("Account Name", gsOne.getAccountName());
	    	Assert.assertNotNull(gsOne.getGroupedPathList());
	    	Assert.assertEquals(2, gsOne.getGroupedPathList().size());
	    	Assert.assertEquals("Heap:Used", gsOne.getGroupedPathList().get(0));
	    	Assert.assertEquals("Heap:Max", gsOne.getGroupedPathList().get(1));
	    	numVerifiedGroupedStats++;
    	}
    	
    	Assert.assertNotNull(gsTwo);
    	if (gsTwo.getName().equals("Non-Heap %")) {
    		Assert.assertEquals("Non-Heap %", gsTwo.getName());
        	Assert.assertEquals("Account Name", gsTwo.getAccountName());
        	Assert.assertNotNull(gsTwo.getGroupedPathList());
        	Assert.assertEquals(2, gsTwo.getGroupedPathList().size());
        	Assert.assertEquals("Non-Heap:Used", gsTwo.getGroupedPathList().get(0));
        	Assert.assertEquals("Non-Heap:Max", gsTwo.getGroupedPathList().get(1));
        	numVerifiedGroupedStats++;
    	}
    	
    	Assert.assertEquals(2, numVerifiedGroupedStats);
    }
    
    @Test
    public void testEmailGroup() throws InterruptedException {
    	newEnv.getSmtpDao().persistEmailRecipientGroup(
    			new BasicEmailRecipientGroup(
    					"Email Group Name", 
    					"Account Name", 
    					"localhost", 
    					"username", 
    					"password", 
    					true, 
    					579, 
    					Arrays.asList("username@emailaddr.com", "username2@emailaddr.org"))
    			);
    	
    	EmailRecipientGroup emailGroupOne = newEnv.getSmtpDao().getEmailRecipientGroup("Email Group Name", "Account Name");
    	
    	Assert.assertNotNull(emailGroupOne);
    	Assert.assertEquals("Email Group Name", emailGroupOne.getEmailRecipientGroupName());
    	Assert.assertEquals("Account Name", emailGroupOne.getAccountName());
    	Assert.assertEquals("localhost", emailGroupOne.getSmtpServerhost());
    	Assert.assertEquals("username", emailGroupOne.getSmtpUsername());
    	Assert.assertEquals("password", emailGroupOne.getSmtpPassword());
    	Assert.assertTrue(emailGroupOne.isUseSSL());
    	Assert.assertEquals(new Integer(579), emailGroupOne.getPort());
    	Assert.assertNotNull(emailGroupOne.getEmailRecipientList());
    	Assert.assertEquals(2, emailGroupOne.getEmailRecipientList().size());
    	Assert.assertEquals("username@emailaddr.com", emailGroupOne.getEmailRecipientList().get(0));
    	Assert.assertEquals("username2@emailaddr.org", emailGroupOne.getEmailRecipientList().get(1));
    	
    	//newEnv.getSmtpDao().deleteEmailRecipientGroup(emailGroupOne);
    	newEnv.getSmtpDao().deleteEmailRecipientGroup("Email Group Name", "Account Name");
    	
    	Thread.sleep(550);
    	EmailRecipientGroup deletedGroup = newEnv.getSmtpDao().getEmailRecipientGroup("Email Group Name", "Account Name");
    	
    	Assert.assertNull(deletedGroup);
    }
    
    @Test
    public void testTreeMenu() throws InterruptedException {
    	newEnv.getTreeMenuDao().persistTreeMenu(new BasicStatistics("EurekaJAgent:Memory:Heap:Used %", "Account Name", "Y"));
    	
    	Statistics statOne = newEnv.getTreeMenuDao().getTreeMenu("EurekaJAgent:Memory:Heap:Used %", "Account Name");
    	
    	Assert.assertNotNull(statOne);
    	Assert.assertEquals("EurekaJAgent:Memory:Heap:Used %", statOne.getGuiPath());
    	Assert.assertEquals("Account Name", statOne.getAccountName());
    	Assert.assertEquals("Y", statOne.getNodeLive());
    	
    	newEnv.getTreeMenuDao().deleteTreeMenu("EurekaJAgent:Memory:Heap:Used %", "Account Name");
    	
    	Thread.sleep(550);
    	Statistics deletedStatOne = newEnv.getTreeMenuDao().getTreeMenu("EurekaJAgent:Memory:Heap:Used %", "Account Name");
    	
    	Assert.assertNull(deletedStatOne);
    }
}
