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
package org.eurekaj.plugins.cassandra.datatypes;

import org.apache.cassandra.db.marshal.DoubleType;
import org.apache.cassandra.db.marshal.LongType;
import org.eurekaj.api.datatypes.LiveStatistics;
import org.firebrandocm.dao.annotations.Column;
import org.firebrandocm.dao.annotations.ColumnFamily;
import org.firebrandocm.dao.annotations.Key;

public class CassandraLiveStatistics implements LiveStatistics, Comparable<LiveStatistics> {
    private String guiPath;
    private String accountName;
	private Long timeperiod;
    private Double value;
    private String valueType;
    private String unitType;
    private Boolean calculated;

    public CassandraLiveStatistics() {
    }

    public CassandraLiveStatistics(String guiPath, String accountName, Long timeperiod, Double value) {
        this.guiPath = guiPath;
        this.timeperiod = timeperiod;
        this.accountName = accountName;
        this.value = value;
    }

    public CassandraLiveStatistics(String guiPath, String accountName, Long timeperiod, Double value, String valueType, String unitType) {
        this.guiPath = guiPath;
        this.accountName = accountName;
        this.timeperiod = timeperiod;
        this.value = value;
        this.valueType = valueType;
        this.unitType = unitType;
    }

    public String getGuiPath() {
        return guiPath;
    }

    public void setGuiPath(String guiPath) {
        this.guiPath = guiPath;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public Long getTimeperiod() {
        return timeperiod;
    }

    public void setTimeperiod(Long timeperiod) {
        this.timeperiod = timeperiod;
    }

    public void setTimeperiod(String timeperiod) {
        if (timeperiod == null) {
            this.timeperiod = null;
        } else {
            try {
                this.timeperiod = Long.parseLong(timeperiod);
            } catch (NumberFormatException nfe) {
                this.timeperiod = null;
            }
        }
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public void setValue(String value) {
        if (value == null) {
            this.value = null;
        } else {
            try {
                this.value = Double.parseDouble(value);
            } catch (NumberFormatException nfe) {
                this.value = null;
            }
        }
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public String getUnitType() {
        return unitType;
    }

    public void setUnitType(String unitType) {
        this.unitType = unitType;
    }

    public Boolean getCalculated() {
        return calculated;
    }

    public void setCalculated(Boolean calculated) {
        this.calculated = calculated;
    }

    public int compareTo(LiveStatistics other) {
		if (other == null || other.getTimeperiod() == null) {
			return 1;
		}

		if (this.getTimeperiod() == null) {
			return -1;
		}

		return this.getTimeperiod().compareTo(other.getTimeperiod());
	}
}
