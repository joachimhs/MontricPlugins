package org.eurekaj.dbtest;

import org.eurekaj.api.datatypes.LiveStatistics;
import org.eurekaj.api.enumtypes.UnitType;
import org.eurekaj.api.enumtypes.ValueType;
import org.eurekaj.plugins.cassandra.CassandraEnv;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: jhs
 * Date: 4/18/12
 * Time: 11:15 AM
 * To change this template use File | Settings | File Templates.
 */
public class TestCassandraMetricHourStorage {
    private CassandraEnv newEnv;

    @Before
    public void setup() {
        newEnv = new CassandraEnv();
		newEnv.setup();
		System.out.println("newEnv set up: " + newEnv.getLiveStatissticsDao());

        DateTime fromDate = new DateTime(2012, 04, 18, 9, 0, 0);
        DateTime toDate = new DateTime(2012, 04, 18, 12, 0, 0);

        Long from15SecPeriod = fromDate.getMillis() / 15000;
        Long to15SecPeriod = toDate.getMillis() / 15000;

        long numMetrics = to15SecPeriod - from15SecPeriod;
        long index = 0;

        while (index <= numMetrics) {
            newEnv.getLiveStatissticsDao().storeIncomingStatistics("Test:A", from15SecPeriod + index, "" + index, ValueType.VALUE, UnitType.N);
            index++;
        }

        System.out.println("Stored " + index + " values in the databse");
    }

    @After
    public void tearDown() {
        DateTime fromDate = new DateTime(2012, 04, 18, 9, 0, 0);
        DateTime toDate = new DateTime(2012, 04, 18, 12, 0, 0);

        Long from15SecPeriod = fromDate.getMillis() / 15000;
        Long to15SecPeriod = toDate.getMillis() / 15000;

        newEnv.getLiveStatissticsDao().deleteLiveStatisticsBetween("Test:A", from15SecPeriod, to15SecPeriod);

        List<LiveStatistics> statList = newEnv.getLiveStatissticsDao().getLiveStatistics("Test:A", from15SecPeriod, to15SecPeriod);

        for (LiveStatistics stat : statList) {
            printStat(stat);
            Assert.assertNull("Expecting that value is NULL for timeperiod: " + stat.getTimeperiod(), stat.getValue());
        }

        newEnv.close();
        newEnv = null;
    }

    @Test
    public void testStoringMetricHour() {
        DateTime fromDate = new DateTime(2012, 04, 18, 9, 0, 0);
        DateTime toDate = new DateTime(2012, 04, 18, 12, 0, 0);

        Long from15SecPeriod = fromDate.getMillis() / 15000;
        Long to15SecPeriod = toDate.getMillis() / 15000;

        int numMetrics = (4 * 60 * 3) + 1; //3 hours worth of metrics
        List<LiveStatistics> statList = newEnv.getLiveStatissticsDao().getLiveStatistics("Test:A", from15SecPeriod, to15SecPeriod);

        Assert.assertEquals("Expecting " + numMetrics + " LiveStatistcs back from DB", numMetrics, statList.size());
        for (LiveStatistics ls : statList) {
            printStat(ls);
        }
   }

    private void printStat(LiveStatistics ls) {
        long ms = ls.getTimeperiod() * 15000;
        DateTime time = new DateTime(ms);


        StringBuilder sb = new StringBuilder();
        sb.append(time.getDayOfMonth()).append(".").append(time.getMonthOfYear()).append(".").append(time.getYear());
        sb.append(" ").append(time.getHourOfDay()).append(":").append(time.getMinuteOfHour()).append(":").append(time.getSecondOfMinute());

        System.out.println(ls.getTimeperiod() + ";" + sb.toString() + " : " + ls.getValue());
    }
}
