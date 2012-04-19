package org.eurekaj.plugins.cassandra.datatypes;

import org.eurekaj.api.datatypes.TreeMenuNode;

/**
 * Created by IntelliJ IDEA.
 * User: jhs
 * Date: 4/18/12
 * Time: 5:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class CassandraTreeMenuNode implements Comparable<TreeMenuNode>, TreeMenuNode {
    private String guiPath;
	private String nodeLive;
    private Long oneMinuteAverageLastUpdated;
    private Long fiveMinuteAverageLastUpdated;
	private Long halfHourAverageLastUpdated;
	private Long hourAverageLastUpdated;
	private Long dailyAverageLastUpdated;
	private Long weeklyAverageLastUpdated;

    public CassandraTreeMenuNode() {
        super();
        halfHourAverageLastUpdated = 0l;
		hourAverageLastUpdated = 0l;
		dailyAverageLastUpdated = 0l;
		weeklyAverageLastUpdated = 0l;
        oneMinuteAverageLastUpdated = 0l;
        fiveMinuteAverageLastUpdated = 0l;
    }

    public CassandraTreeMenuNode(String guiPath, String nodeLive) {
        this();
        this.guiPath = guiPath;
        this.nodeLive = nodeLive;
    }

    public String getGuiPath() {
        return guiPath;
    }

    public void setGuiPath(String guiPath) {
        this.guiPath = guiPath;
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

    public Long getDailyAverageLastUpdated() {
        return dailyAverageLastUpdated;
    }

    public void setDailyAverageLastUpdated(Long dailyAverageLastUpdated) {
        this.dailyAverageLastUpdated = dailyAverageLastUpdated;
    }

    public Long getWeeklyAverageLastUpdated() {
        return weeklyAverageLastUpdated;
    }

    public void setWeeklyAverageLastUpdated(Long weeklyAverageLastUpdated) {
        this.weeklyAverageLastUpdated = weeklyAverageLastUpdated;
    }

    public int compareTo(TreeMenuNode other) {
		if (other == null || other.getGuiPath() == null) {
			return 1;
		}

		if (this.getGuiPath() == null) {
			return -1;
		}

		return this.getGuiPath().compareTo(other.getGuiPath());
	}
}
