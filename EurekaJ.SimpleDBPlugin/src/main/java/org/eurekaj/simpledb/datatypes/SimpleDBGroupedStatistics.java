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
package org.eurekaj.simpledb.datatypes;

import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import org.eurekaj.api.datatypes.GroupedStatistics;
import org.eurekaj.simpledb.SimpleDBUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: joahaa
 * Date: 5/7/11
 * Time: 11:41 AM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleDBGroupedStatistics implements Comparable<GroupedStatistics>, GroupedStatistics {
	private String name;
	private List<String> groupedPathList;

    public SimpleDBGroupedStatistics(GroupedStatistics groupedStatistics) {
        this.name = groupedStatistics.getName();
        this.groupedPathList = groupedStatistics.getGroupedPathList();
    }

    public SimpleDBGroupedStatistics(List<Attribute> attributeList) {
        Map<String, String> attributeMap = SimpleDBUtil.getAttributesAStringMap(attributeList);

        setName(attributeMap.get("guiPath"));
        setGroupedPathList(SimpleDBUtil.getCommaseperatedStringAsList(attributeMap.get("groupedPathList"), ","));
    }

    public List<ReplaceableAttribute> getAmazonSimpleDBAttribute() {
        List<ReplaceableAttribute> replaceableAttributeList = new ArrayList<ReplaceableAttribute>();
        replaceableAttributeList.add(new ReplaceableAttribute("name", this.getName(), true));
        replaceableAttributeList.add(new ReplaceableAttribute("groupedPathList", SimpleDBUtil.getStringListAsString(this.groupedPathList), true));

        return replaceableAttributeList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getGroupedPathList() {
        return groupedPathList;
    }

    public void setGroupedPathList(List<String> groupedPathList) {
        this.groupedPathList = groupedPathList;
    }

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
