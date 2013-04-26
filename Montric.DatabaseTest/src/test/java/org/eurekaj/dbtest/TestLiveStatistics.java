package org.eurekaj.dbtest;

import org.eurekaj.api.datatypes.LiveStatistics;

/**
 * Created with IntelliJ IDEA.
 * User: joahaa
 * Date: 3/1/13
 * Time: 11:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestLiveStatistics implements LiveStatistics {
    private String guiPath;
    private String accountName;
    private Long timeperiod;
    private Double value;
    private String valueType;
    private String unitType;

    public TestLiveStatistics(String guiPath, String accountName, Long timeperiod, Double value, String valueType, String unitType) {
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

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
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

    @Override
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
