package org.eurekaj.statistics.service;

import org.eurekaj.api.service.EurekaJProcessIncomingStatisticsService;
import org.eurekaj.spi.db.EurekaJDBPluginService;
import org.eurekaj.spi.statistics.EurekaJProcessIncomingStatisticsPluginService;

/**
 * Created by IntelliJ IDEA.
 * User: joahaa
 * Date: 4/3/12
 * Time: 9:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class StatsCounterServiceExecutor extends EurekaJProcessIncomingStatisticsPluginService {
    private LiveStatisticsCounterService liveStatisticsCounterService;

    public StatsCounterServiceExecutor() {
        this.liveStatisticsCounterService = new LiveStatisticsCounterService();
    }

    @Override
    public String getPluginName() {
        return "LiveStatisticsCounterServicePlugin";
    }

    @Override
    public EurekaJProcessIncomingStatisticsService getProcessIncomingStatisticsService() {
        return liveStatisticsCounterService;
    }

    @Override
    public void setDBPlugin(EurekaJDBPluginService eurekaJDBPluginService) {
        liveStatisticsCounterService.setEurekaJDBPluginService(eurekaJDBPluginService);
    }
}
