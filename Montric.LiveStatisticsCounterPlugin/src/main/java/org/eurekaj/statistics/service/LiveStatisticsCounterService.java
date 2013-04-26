package org.eurekaj.statistics.service;

import org.eurekaj.api.datatypes.LiveStatistics;
import org.eurekaj.api.enumtypes.UnitType;
import org.eurekaj.api.enumtypes.ValueType;
import org.eurekaj.api.service.EurekaJProcessIncomingStatisticsService;
import org.eurekaj.spi.db.EurekaJDBPluginService;

import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by IntelliJ IDEA.
 * User: joahaa
 * Date: 4/3/12
 * Time: 9:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class LiveStatisticsCounterService implements EurekaJProcessIncomingStatisticsService {
    private EurekaJDBPluginService eurekaJDBPluginService = null;

    public void setEurekaJDBPluginService(EurekaJDBPluginService eurekaJDBPluginService) {
        this.eurekaJDBPluginService = eurekaJDBPluginService;
    }

    @Override
    public void processStatistics(List<LiveStatistics> liveStatisticsList) {
        Hashtable<String, AtomicInteger> statHash = new Hashtable<String, AtomicInteger>();
        Long timePeriod = 0l;

        for (LiveStatistics liveStatistics : liveStatisticsList) {
            if (timePeriod.longValue() == 0l) {
                timePeriod = liveStatistics.getTimeperiod();
            }

            String agentName = "unknownAgent";
            String guiParts[] = liveStatistics.getGuiPath().split(":");
            if (guiParts.length > 1) {
                agentName = guiParts[0];
            }

            AtomicInteger count = statHash.get(agentName);
            if (count == null) {
                count = new AtomicInteger();
                count.set(1);
                statHash.put(agentName, count);
            }
            
            count.incrementAndGet();
        }
        
        for (String key : statHash.keySet()) {
            AtomicInteger count = statHash.get(key);
            
            if (eurekaJDBPluginService != null) {
                eurekaJDBPluginService.getLiveStatissticsDao().storeIncomingStatistics(
                        "AgentStats:" + key + ":Incoming Stat Count",
                        "ACCOUNT",
                        timePeriod,
                        "" + count.get(),
                        ValueType.AGGREGATE,
                        UnitType.N);
            }
        }
    }
}
