package org.eurekaj.plugins.cassandra.datatypes;

import org.eurekaj.api.datatypes.GroupedStatistics;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: jhs
 * Date: 4/18/12
 * Time: 5:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class CassandraGroupedStatistics implements Comparable<GroupedStatistics>, GroupedStatistics {
    private String name;
    private String accountName;
	private List<String> groupedPathList;

    public CassandraGroupedStatistics(GroupedStatistics groupedStatistics) {
        this.name = groupedStatistics.getName();
        this.groupedPathList = groupedStatistics.getGroupedPathList();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public List<String> getGroupedPathList() {
        return groupedPathList;
    }

    public void setGroupedPathList(List<String> groupedPathList) {
        this.groupedPathList = groupedPathList;
    }

    @Override
    public int compareTo(GroupedStatistics other) {
        if (other == null || other.getName() == null) {
			return 1;
		}

		if (this.getName() == null) {
			return -1;
		}

		return this.getName().compareTo(other.getName());
    }
}
