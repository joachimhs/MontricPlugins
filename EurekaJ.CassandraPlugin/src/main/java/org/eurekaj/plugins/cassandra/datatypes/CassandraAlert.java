package org.eurekaj.plugins.cassandra.datatypes;

import org.eurekaj.api.datatypes.Alert;
import org.eurekaj.api.enumtypes.AlertStatus;
import org.eurekaj.api.enumtypes.AlertType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: jhs
 * Date: 4/18/12
 * Time: 5:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class CassandraAlert implements Comparable<Alert>, Alert {
    private String alertName;
    private String guiPath;
	private boolean activated;
	private Double errorValue;
	private Double warningValue;
	private AlertType selectedAlertType = AlertType.GREATER_THAN;
	private long alertDelay = 0;
	private AlertStatus status = AlertStatus.NORMAL;
	private List<String> selectedEmailSenderList = new ArrayList<String>();
	private List<String> selectedAlertPluginList = new ArrayList<String>();

    public CassandraAlert(Alert alert) {
        this.alertName = alert.getAlertName();
        this.guiPath = alert.getGuiPath();
        this.activated = alert.isActivated();
        this.errorValue = alert.getErrorValue();
        this.warningValue = alert.getWarningValue();
        this.selectedAlertType = alert.getSelectedAlertType();
        this.alertDelay = alert.getAlertDelay();
        this.status = alert.getStatus();
        this.selectedEmailSenderList = alert.getSelectedEmailSenderList();
        this.selectedAlertPluginList = alert.getSelectedAlertPluginList();
    }

    public String getAlertName() {
        return alertName;
    }

    public void setAlertName(String alertName) {
        this.alertName = alertName;
    }

    public String getGuiPath() {
        return guiPath;
    }

    public void setGuiPath(String guiPath) {
        this.guiPath = guiPath;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
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

    public AlertType getSelectedAlertType() {
        return selectedAlertType;
    }

    public void setSelectedAlertType(AlertType selectedAlertType) {
        this.selectedAlertType = selectedAlertType;
    }

    public long getAlertDelay() {
        return alertDelay;
    }

    public void setAlertDelay(long alertDelay) {
        this.alertDelay = alertDelay;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public void setStatus(AlertStatus status) {
        this.status = status;
    }

    public List<String> getSelectedEmailSenderList() {
        return selectedEmailSenderList;
    }

    public void setSelectedEmailSenderList(List<String> selectedEmailSenderList) {
        this.selectedEmailSenderList = selectedEmailSenderList;
    }

    public List<String> getSelectedAlertPluginList() {
        return selectedAlertPluginList;
    }

    public void setSelectedAlertPluginList(List<String> selectedAlertPluginList) {
        this.selectedAlertPluginList = selectedAlertPluginList;
    }

    public int compareTo(Alert other) {
		if (other == null || other.getGuiPath() == null) {
			return 1;
		}

		if (this.getGuiPath() == null) {
			return -1;
		}

		return this.getGuiPath().compareTo(other.getGuiPath());
	}
}
