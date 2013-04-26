/**
    EurekaJ Profiler - http://eurekaj.haagen.name
    
    Copyright (C) 2010-2011 Joachim Haagen Skeie

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package org.eurekaj.berkeley.hour.db.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.sleepycat.persist.SecondaryIndex;
import org.eurekaj.api.dao.LiveStatisticsDao;
import org.eurekaj.api.dao.TreeMenuDao;
import org.eurekaj.api.datatypes.*;
import org.eurekaj.api.enumtypes.UnitType;
import org.eurekaj.api.enumtypes.ValueType;
import org.eurekaj.berkeley.hour.db.BerkeleyDbEnv;
import org.eurekaj.berkeley.hour.db.datatypes.*;

import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.PrimaryIndex;


public class BerkeleyTreeMenuDao implements TreeMenuDao, LiveStatisticsDao {
	private BerkeleyDbEnv dbEnvironment;
	private PrimaryIndex<BerkeleyStatsticsPk, BerkeleyStatistics> treeMenuPrimaryIdx;
	//statIndex = db.<BerkeleyLiveStatistics>createMultidimensionalIndex(BerkeleyLiveStatistics.class, new String[] {"guiPath", "timeperiod"}, true);
	private PrimaryIndex<BerkeleyLiveStatisticsPk, BerkeleyLiveStatistics> liveStatPrimaryIdx;
    private SecondaryIndex<Long, BerkeleyLiveStatisticsPk, BerkeleyLiveStatistics> liveStatTimeperiodIdx;
    
    private PrimaryIndex<MetricHourPk, MetricHour> metricHourPrimaryIdx;
    private SecondaryIndex<MetricHourPk, MetricHourPk, MetricHour> metricHourTimeperiodIdx;

    public BerkeleyTreeMenuDao(BerkeleyDbEnv dbEnvironment) {
        this.dbEnvironment = dbEnvironment;
        treeMenuPrimaryIdx = this.dbEnvironment.getTreeMenuStore().getPrimaryIndex(BerkeleyStatsticsPk.class, BerkeleyStatistics.class);
		liveStatPrimaryIdx = this.dbEnvironment.getLiveStatisticsStore().getPrimaryIndex(BerkeleyLiveStatisticsPk.class, BerkeleyLiveStatistics.class);
        liveStatTimeperiodIdx = this.dbEnvironment.getLiveStatisticsStore().getSecondaryIndex(liveStatPrimaryIdx, Long.class, "secondaryTimeperiod");
        
        metricHourPrimaryIdx = this.dbEnvironment.getMetricHourStore().getPrimaryIndex(MetricHourPk.class, MetricHour.class);
        metricHourTimeperiodIdx = this.dbEnvironment.getMetricHourStore().getSecondaryIndex(metricHourPrimaryIdx, MetricHourPk.class, "hoursSince1970");
    }
    
    public static void main(String[] args) {
    	System.out.println(LiveStatisticsUtil.getFifteensecondTimeperiodsSinceStartOfHour(1323263816l)); //67
    	System.out.println(LiveStatisticsUtil.getFifteensecondTimeperiodsSinceStartOfHour(1323266394l)); //239
    	System.out.println(LiveStatisticsUtil.getFifteensecondTimeperiodsSinceStartOfHour(1323265200l)); //160
    	System.out.println(LiveStatisticsUtil.getFifteensecondTimeperiodsSinceStartOfHour(1323262800l)); //0 
    	System.out.println(LiveStatisticsUtil.getFifteensecondTimeperiodsSinceStartOfHour(1323264315l)); //101
    }

    @Override
    public void deleteLiveStatisticsBetween(String guiPath, String accountName, Long fromTimeperiod, Long toTimeperiod) {
        List<LiveStatistics> delStats = getLiveStatistics(guiPath, accountName, fromTimeperiod, toTimeperiod);

        //Strore NULL instead of value
        for (LiveStatistics delStat : delStats) {
            storeIncomingStatistics(delStat.getGuiPath(), delStat.getAccountName(), delStat.getTimeperiod(), null, ValueType.fromValue(delStat.getValueType()), UnitType.fromValue(delStat.getUnitType()));
        }
    }

    @Override
    public void markLiveStatisticsAsCalculated(String guiPath, String accountName, String timeperiod) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void markLiveStatisticsAsCalculated(String guiPath, String accountName, Long minTimeperiod, Long maxTimeperiod) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void deleteMarkedLiveStatistics() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    private List<LiveStatistics> createLivestatisticsFromMetricHour(MetricHour metricHour, Integer minTimeperiodWithinTheHour, Integer maxTimeperiodWithinTheHour, Long hoursSince1970) {
    	List<LiveStatistics> retList = new ArrayList<LiveStatistics>();
    	
    	if (maxTimeperiodWithinTheHour == null) {
    		maxTimeperiodWithinTheHour = 239;
    	}
    	
    	for (int index = minTimeperiodWithinTheHour; index <= maxTimeperiodWithinTheHour; index++) {
    		Long timeperiod = (hoursSince1970 * 240) + index;
    		
    		retList.add(new BerkeleyLiveStatistics(metricHour.getGuiPath(), timeperiod, metricHour.getValueAt(index), metricHour.getValueType(), metricHour.getUnitType()));
    	}
    	
    	return retList;
    }

    @Override
	public List<LiveStatistics> getLiveStatistics(String guiPath, String accountName,
			Long minTimeperiod, Long maxTimeperiod) {
    	
    	Long fromHoursSince1970 = minTimeperiod / 240;
    	Long toHoursSince1970 = maxTimeperiod / 240;
    	
    	List<LiveStatistics> retList = new ArrayList<LiveStatistics>();

    	for (Long index = fromHoursSince1970; index <= toHoursSince1970; index++) {
    		//Iterate over all hours to gather stats from
            MetricHourPk pk = new MetricHourPk(guiPath, accountName, index);
    		MetricHour metricHour = metricHourPrimaryIdx.get(pk);
    		if (metricHour == null) {
    			metricHour = new MetricHour(guiPath, accountName, index);
    		}
    		
    		//If this is the first hour, start from the correct 15-second timeslot within the hour
    		Integer minTimeperiodWithinTheHour = 0;
    		if (index.longValue() == fromHoursSince1970.longValue()) {
    			minTimeperiodWithinTheHour = LiveStatisticsUtil.getFifteensecondTimeperiodsSinceStartOfHour(minTimeperiod * 15);
    		}
    		
    		//If this is the last hour, end with the correct 15-second timeslot within the hour
    		Integer maxTimeperiodWithinTheHour = null;
    		if (index.longValue() == toHoursSince1970.longValue()) {
    			maxTimeperiodWithinTheHour = LiveStatisticsUtil.getFifteensecondTimeperiodsSinceStartOfHour(maxTimeperiod * 15);
    		}
    		
    		retList.addAll(createLivestatisticsFromMetricHour(metricHour, minTimeperiodWithinTheHour, maxTimeperiodWithinTheHour, index));
    	}		
		
		return retList;
	}

    @Override
	public List<Statistics> getTreeMenu(String accountName) {
		List<Statistics> retList = new ArrayList<Statistics>();
		EntityCursor<BerkeleyStatistics> pi_cursor = treeMenuPrimaryIdx.entities();
		try {
		    for (BerkeleyStatistics node : pi_cursor) {
		        retList.add(node);
		    }
		// Always make sure the cursor is closed when we are done with it.
		} finally {
			pi_cursor.close();
		} 
		return retList;
	}

    @Override
	public BerkeleyStatistics getTreeMenu(String guiPath, String accountName) {
        BerkeleyStatsticsPk pk = new BerkeleyStatsticsPk(guiPath, accountName);
		return treeMenuPrimaryIdx.get(pk);
	}

    @Override
    public void persistTreeMenu(Statistics statistics) {
		treeMenuPrimaryIdx.put(new BerkeleyStatistics(statistics));    	
    }
	
	private BerkeleyStatistics updateTreeMenu(String guiPath, String accountName, boolean hasValueInformation) {
		BerkeleyStatsticsPk pk = new BerkeleyStatsticsPk(guiPath, accountName);
		BerkeleyStatistics treeMenu = treeMenuPrimaryIdx.get(pk);
		if (treeMenu == null) {
			//Create new TreeMenu at guiPath
			treeMenu = new BerkeleyStatistics(guiPath, accountName, "Y");
		} else {
			//Update treeMenu at guiPath
			treeMenu.setNodeLive("Y");
		}
		
		if (treeMenu.getGuiPath() == null) {
			treeMenu.setGuiPath(guiPath);
		}
		
		persistTreeMenu(treeMenu);
		
		return treeMenu;
	}


    @Override
    public void storeIncomingStatistics(String guiPath, String accountName, Long timeperiod, String value, ValueType valueType, UnitType unitType) {
		BerkeleyStatistics treeMenu = updateTreeMenu(guiPath, accountName, value != null);
		Double valueDouble = LiveStatisticsUtil.parseDouble(value);
		Double calculatedValue = LiveStatisticsUtil.calculateValueBasedOnUnitType(valueDouble, unitType);
		
		long hoursSince1970 = timeperiod.intValue() / 240;
		int fifteenSecondPeriodsSinceStartOfHour = LiveStatisticsUtil.getFifteensecondTimeperiodsSinceStartOfHour(timeperiod * 15);

        MetricHourPk pk = new MetricHourPk(guiPath, accountName, hoursSince1970);
		MetricHour mh = metricHourPrimaryIdx.get(pk);
		if (mh == null) {
			mh = new MetricHour(guiPath, accountName, hoursSince1970);
		}
        
        if (mh.getValueType() == null) {
            mh.setValueType(valueType.value());
        }
        
        if (mh.getUnitType() == null) {
            mh.setUnitType(unitType.value());
        }

		Double prevValue = mh.getValueAt(fifteenSecondPeriodsSinceStartOfHour);
		if (prevValue == null) {
			mh.setValueAt(fifteenSecondPeriodsSinceStartOfHour, calculatedValue);
		} else {
			mh.setValueAt(fifteenSecondPeriodsSinceStartOfHour, LiveStatisticsUtil.calculateValueBasedOnValueType(prevValue, calculatedValue, valueType));
		}
		
		//System.out.println("\t\tstored value: " + mh.getValueAt(fifteenSecondPeriodsSinceStartOfHour));
		
		metricHourPrimaryIdx.put(mh);
	}

    @Override
    public void storeIncomingStatistics(List<LiveStatistics> liveStatisticsList) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void deleteLiveStatisticsOlderThan(Date date, String accountName) {
        Long hoursSince1970 = date.getTime() / 3600000;

        MetricHourPk fromKey = new MetricHourPk();
		fromKey.setHoursSince1970(0l);
        fromKey.setAccountName(accountName);

        MetricHourPk toKey = new MetricHourPk();
        fromKey.setHoursSince1970(hoursSince1970);
        fromKey.setAccountName(accountName);

        EntityCursor<MetricHour> pi_cursor = metricHourTimeperiodIdx.entities(fromKey, true, toKey, false);
		try {
			for (MetricHour node : pi_cursor) {
				metricHourPrimaryIdx.delete(node.getPk());
			}
			// Always make sure the cursor is closed when we are done with it.
		} finally {
			pi_cursor.close();
		}
    }

    @Override
    public void deleteTreeMenu(String guiPath, String accountName) {
		EntityCursor<BerkeleyStatistics> pi_cursor = treeMenuPrimaryIdx.entities();
		try {
		    for (BerkeleyStatistics node : pi_cursor) {
		        if (node.getGuiPath().startsWith(guiPath) || node.getGuiPath().equals(guiPath)) {
		        	treeMenuPrimaryIdx.delete(node.getPk());
		        }
		    }
		// Always make sure the cursor is closed when we are done with it.
		} finally {
			pi_cursor.close();
		} 
    }

}
