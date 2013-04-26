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

import java.util.ArrayList;
import java.util.List;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import org.eurekaj.api.datatypes.GroupedStatistics;

@Entity(version=3)
public class BerkeleyGroupedStatistics implements Comparable<BerkeleyGroupedStatistics>, GroupedStatistics {
	@PrimaryKey private BerkeleyGroupedStatisticsPk pk;
	private List<String> groupedPathList;

    public BerkeleyGroupedStatistics(GroupedStatistics groupedStatistics) {
        this.pk = new BerkeleyGroupedStatisticsPk(groupedStatistics.getAccountName(), groupedStatistics.getName());
        this.groupedPathList = groupedStatistics.getGroupedPathList();
    }

	public BerkeleyGroupedStatistics() {
		groupedPathList = new ArrayList<String>();
	}


	public String getName() {
		return this.pk.getName();
	}
	
	public void setName(String name) {
		this.pk.setName(name);
	}

    public String getAccountName() {
        return this.pk.getAccountName();
    }

    public void setAccountName(String accountName) {
        this.pk.setAccountName(accountName);
    }

    public List<String> getGroupedPathList() {
		return groupedPathList;
	}

	public void setGroupedPathList(List<String> groupedPathList) {
		this.groupedPathList = groupedPathList;
	}

	@Override
	public String toString() {
		return "BerkeleyGroupedStatistics [groupedPathList=" + groupedPathList + "]";
	}
	
	public int compareTo(BerkeleyGroupedStatistics other) {
		if (other == null || other.getName() == null) {
			return 1;
		}
		
		if (this.getName() == null) {
			return -1;
		}
		
		return this.getName().compareTo(other.getName());
	}
}
