package org.eurekaj.dbtest;

import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.log4j.Logger;
import org.apache.thrift.transport.TTransportException;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.eurekaj.api.datatypes.Alert;
import org.eurekaj.api.datatypes.LiveStatistics;
import org.eurekaj.api.enumtypes.AlertStatus;
import org.eurekaj.api.enumtypes.AlertType;
import org.eurekaj.api.enumtypes.UnitType;
import org.eurekaj.api.enumtypes.ValueType;
import org.eurekaj.api.util.ListToString;
import org.eurekaj.berkeley.hour.db.BerkeleyDbEnv;
import org.eurekaj.plugins.cassandra.CassandraEnv;
import org.eurekaj.plugins.cassandra.datatypes.CassandraAlert;
import org.eurekaj.spi.db.EurekaJDBPluginService;
import org.joda.time.DateTime;
import org.junit.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: jhs
 * Date: 4/18/12
 * Time: 11:15 AM
 * To change this template use File | Settings | File Templates.
 */
public class    TestCassandraStorage {
    private static Logger logger = Logger.getLogger(TestCassandraStorage.class.getName());

    private EurekaJDBPluginService newEnv;

    @BeforeClass
    public static void beforeClass() throws IOException, ConfigurationException, TTransportException, InterruptedException, URISyntaxException {
        //EmbeddedCassandraServerHelper.startEmbeddedCassandra("cassandra-test.yaml");
    }

    @AfterClass
    public static void afterClass() throws IOException {
        //EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
        //EmbeddedCassandraServerHelper.stopEmbeddedCassandra();
    }

    @Before
    public void setup() {
        newEnv = new CassandraEnv();
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
        DateTime fromDate = new DateTime(2012, 04, 18, 9, 0, 0);
        DateTime toDate = new DateTime(2012, 04, 18, 12, 0, 0);
        //DateTime fromDate = new DateTime(2012, 01, 01, 0, 0, 0);
        //DateTime toDate = new DateTime(2012, 12, 31, 23, 59, 59);

        Long from15SecPeriod = fromDate.getMillis() / 15000;
        Long to15SecPeriod = toDate.getMillis() / 15000;

        long numMetrics = to15SecPeriod - from15SecPeriod;
        long index = 0;

        List<LiveStatistics> liveStatisticsList = new ArrayList<LiveStatistics>();
        while (index <= numMetrics) {
            liveStatisticsList.add(new TestLiveStatistics("Test:A", "ACCOUNT", from15SecPeriod + index, new Double(index), ValueType.AGGREGATE.value(), UnitType.N.value()));
            index++;
        }

        newEnv.getLiveStatissticsDao().storeIncomingStatistics(liveStatisticsList);

        System.out.println("Stored " + index + " values in the database");

        int expectedNumMetrics = (4 * 60 * 3) + 1; //3 hours worth of metrics
        List<LiveStatistics> statList = newEnv.getLiveStatissticsDao().getLiveStatistics("Test:A", "ACCOUNT", from15SecPeriod, to15SecPeriod);

        Assert.assertEquals("Expecting " + expectedNumMetrics + " LiveStatistcs back from DB", expectedNumMetrics, statList.size());

        //newEnv.getLiveStatissticsDao().markLiveStatisticsAsCalculated("Test:A", "ACCOUNT", from15SecPeriod, to15SecPeriod);

        //newEnv.getLiveStatissticsDao().deleteMarkedLiveStatistics();

        /*statList = newEnv.getLiveStatissticsDao().getLiveStatistics("Test:A", "ACCOUNT", from15SecPeriod, to15SecPeriod);

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
        CassandraAlert alert = new CassandraAlert("ACCOUNT", "New Alert 123");
        alert.setSelectedEmailSenderList(ListToString.convertToList("joachim@haagen.name;joachim@haagen-software.no", ";"));
        alert.setSelectedAlertPluginList(ListToString.convertToList("Plugin 1;Plugin 2", ";"));
        alert.setActivated(true);
        alert.setAlertDelay(3);
        alert.setErrorValue(22.5d);
        alert.setWarningValue(17.2d);
        alert.setGuiPath("EurekaJAgent:Memory:Heap:% Used");
        alert.setSelectedAlertType(AlertType.GREATER_THAN);
        alert.setStatus(AlertStatus.NORMAL);

        CassandraAlert alertTwo = new CassandraAlert("ACCOUNT TWO", "New Alert 123");
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

        Alert persistedAlert = newEnv.getAlertDao().getAlert("ACCOUNT", "New Alert 123");
        Assert.assertNotNull("Expecting to be able to retrieve an alert from the DB.", persistedAlert);
        Assert.assertEquals("ACCOUNT", persistedAlert.getAccountName());

        List<Alert> alertList = newEnv.getAlertDao().getAlerts("ACCOUNT");
        Assert.assertNotNull("Expecting a not null alert list!", alertList);
        Assert.assertEquals("Expecting one alert", 1, alertList.size());
        Assert.assertEquals("ACCOUNT", alertList.get(0).getAccountName());
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

        newEnv.getAlertDao().deleteAlert("ACCOUNT", "New Alert 123");
        newEnv.getAlertDao().deleteAlert("ACCOUNT TWO", "New Alert 123");

        alertList = newEnv.getAlertDao().getAlerts("ACCOUNT");
        Assert.assertNotNull("Expecting a not null alert list!", alertList);
        Assert.assertEquals(new Integer(0), new Integer(alertList.size()));
        alertList = newEnv.getAlertDao().getAlerts("ACCOUNT TWO");
        Assert.assertNotNull("Expecting a not null alert list!", alertList);
        Assert.assertEquals(new Integer(0), new Integer(alertList.size()));
    }
}
