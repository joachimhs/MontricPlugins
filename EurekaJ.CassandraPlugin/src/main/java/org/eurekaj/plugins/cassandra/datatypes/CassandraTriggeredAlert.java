package org.eurekaj.plugins.cassandra.datatypes;

import org.eurekaj.api.datatypes.TriggeredAlert;

/**
 * Created by IntelliJ IDEA.
 * User: jhs
 * Date: 4/18/12
 * Time: 5:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class CassandraTriggeredAlert implements Comparable<TriggeredAlert>, TriggeredAlert{
    private String alertName;
    private Long timeperiod;
    private Double errorValue;
	private Double warningValue;
    private Double alertValue;
    private Long triggeredTimeperiod;

    public CassandraTriggeredAlert(TriggeredAlert triggeredAlert) {
        this.alertName = triggeredAlert.getAlertName();
        this.timeperiod = triggeredAlert.getTimeperiod();
        this.errorValue = triggeredAlert.getErrorValue();
        this.warningValue = triggeredAlert.getWarningValue();
        this.alertValue = triggeredAlert.getAlertValue();
        this.triggeredTimeperiod = triggeredAlert.getTimeperiod();
    }

    public String getAlertName() {
        return alertName;
    }

    public void setAlertName(String alertName) {
        this.alertName = alertName;
    }

    public Long getTimeperiod() {
        return timeperiod;
    }

    public void setTimeperiod(Long timeperiod) {
        this.timeperiod = timeperiod;
    }

    public Double getErrorValue() {
        return errorValue;
    }

    public void setErrorValue(Double errorValue) {
        this.errorValue = errorValue;
    }

    public Double getWarningValue() {
        return warningValue;
    }

    public void setWarningValue(Double warningValue) {
        this.warningValue = warningValue;
    }

    public Double getAlertValue() {
        return alertValue;
    }

    public void setAlertValue(Double alertValue) {
        this.alertValue = alertValue;
    }

    public Long getTriggeredTimeperiod() {
        return triggeredTimeperiod;
    }

    public void setTriggeredTimeperiod(Long triggeredTimeperiod) {
        this.triggeredTimeperiod = triggeredTimeperiod;
    }

    @Override
    public int compareTo(TriggeredAlert other) {
        int compVal = 0;

        if (other == null || other.getAlertName() == null || other.getTimeperiod() == null) {
			return 1;
		}

		if (this.getAlertName() == null || this.getTimeperiod() == null) {
			return -1;
		}

        compVal = this.getAlertName().compareTo(other.getAlertName());

        //If alert name is equal, compare based on timeperiod as well.
        if (compVal == 0) {
            compVal = this.getTimeperiod().compareTo(other.getTimeperiod());
        }

        return compVal;
    }
}
