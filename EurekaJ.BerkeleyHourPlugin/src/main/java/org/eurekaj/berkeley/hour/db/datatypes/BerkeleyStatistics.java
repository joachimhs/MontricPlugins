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
package org.eurekaj.berkeley.hour.db.datatypes;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import org.eurekaj.api.datatypes.Statistics;

@Entity(version=2)
public class BerkeleyStatistics implements Statistics, Comparable<Statistics> {
	@PrimaryKey private BerkeleyStatsticsPk pk;
	private String nodeLive;
    private Long oneMinuteAverageLastUpdated;
    private Long fiveMinuteAverageLastUpdated;
	private Long halfHourAverageLastUpdated;
	private Long hourAverageLastUpdated;
	private Long dailyAverageLastUpdated;
	private Long weeklyAverageLastUpdated;
	
	public BerkeleyStatistics() {
		halfHourAverageLastUpdated = 0l;
		hourAverageLastUpdated = 0l;
		dailyAverageLastUpdated = 0l;
		weeklyAverageLastUpdated = 0l;
        oneMinuteAverageLastUpdated = 0l;
        fiveMinuteAverageLastUpdated = 0l;
	}
	
	public BerkeleyStatistics(Statistics statistics) {
		this.pk = new BerkeleyStatsticsPk(statistics.getGuiPath(), statistics.getAccountName());
		this.nodeLive = statistics.getNodeLive();
		this.oneMinuteAverageLastUpdated = statistics.getOneMinuteAverageLastUpdated();
		this.fiveMinuteAverageLastUpdated = statistics.getFiveMinuteAverageLastUpdated();
		this.halfHourAverageLastUpdated = statistics.getHalfHourAverageLastUpdated();
		this.hourAverageLastUpdated = statistics.getHourAverageLastUpdated();
		this.dailyAverageLastUpdated = statistics.getDailyAverageLastUpdated();
		this.weeklyAverageLastUpdated = statistics.getWeeklyAverageLastUpdated();
	}
	
	public BerkeleyStatistics(String guiPath, String accountName, String nodeLive) {
		super();
        this.pk = new BerkeleyStatsticsPk(guiPath, accountName);
		this.nodeLive = nodeLive;
		halfHourAverageLastUpdated = 0l;
		hourAverageLastUpdated = 0l;
		dailyAverageLastUpdated = 0l;
		weeklyAverageLastUpdated = 0l;
        oneMinuteAverageLastUpdated = 0l;
        fiveMinuteAverageLastUpdated = 0l;
	}
	
	public String getGuiPath() {
		return this.pk.getGuiPath();
	}
	public void setGuiPath(String guiPath) {
		this.pk.setGuiPath(getGuiPath());
	}

    public String getAccountName() {
        return this.pk.getAccountName();
    }

    public void setAccountName(String accountName) {
        this.pk.setAccountName(accountName);
    }

    public BerkeleyStatsticsPk getPk() {
        return pk;
    }

    public void setPk(BerkeleyStatsticsPk pk) {
        this.pk = pk;
    }

    public String getNodeLive() {
		return nodeLive;
	}
	public void setNodeLive(String nodeLive) {
		this.nodeLive = nodeLive;
	}

    public Long getOneMinuteAverageLastUpdated() {
        return oneMinuteAverageLastUpdated;
    }

    public void setOneMinuteAverageLastUpdated(Long oneMinuteAverageLastUpdated) {
        this.oneMinuteAverageLastUpdated = oneMinuteAverageLastUpdated;
    }

    public Long getFiveMinuteAverageLastUpdated() {
        return fiveMinuteAverageLastUpdated;
    }

    public void setFiveMinuteAverageLastUpdated(Long fiveMinuteAverageLastUpdated) {
        this.fiveMinuteAverageLastUpdated = fiveMinuteAverageLastUpdated;
    }

    public Long getDailyAverageLastUpdated() {
		return dailyAverageLastUpdated;
	}
	
	public void setDailyAverageLastUpdated(Long dailyAverageLastUpdated) {
		this.dailyAverageLastUpdated = dailyAverageLastUpdated;
	}
	
	public Long getHalfHourAverageLastUpdated() {
		return halfHourAverageLastUpdated;
	}
	
	public void setHalfHourAverageLastUpdated(Long halfHourAverageLastUpdated) {
		this.halfHourAverageLastUpdated = halfHourAverageLastUpdated;
	}
	
	public Long getHourAverageLastUpdated() {
		return hourAverageLastUpdated;
	}
	
	public void setHourAverageLastUpdated(Long hourAverageLastUpdated) {
		this.hourAverageLastUpdated = hourAverageLastUpdated;
	}
	
	public Long getWeeklyAverageLastUpdated() {
		return weeklyAverageLastUpdated;
	}
	
	public void setWeeklyAverageLastUpdated(Long weeklyAverageLastUpdated) {
		this.weeklyAverageLastUpdated = weeklyAverageLastUpdated;
	}
	
	public int compareTo(Statistics other) {
		if (other == null || other.getGuiPath() == null) {
			return 1;
		}
		
		if (this.getGuiPath() == null) {
			return -1;
		}
		
		return this.getGuiPath().compareTo(other.getGuiPath());
	}
	
	
}
