package org.eurekaj.berkeley.hour.db.datatypes;

import com.sleepycat.persist.model.KeyField;
import com.sleepycat.persist.model.Persistent;

/**
 * Created with IntelliJ IDEA.
 * User: joahaa
 * Date: 2/4/13
 * Time: 1:16 PM
 * To change this template use File | Settings | File Templates.
 */
@Persistent
public class MetricHourPk {
    @KeyField(1) private String guiPath;
    @KeyField(2) private Long hoursSince1970;
    @KeyField(3) private String accountName;

    public MetricHourPk() {
    }

    public MetricHourPk(String guiPath, String accountName, Long hoursSince1970) {
        this.guiPath = guiPath;
        this.hoursSince1970 = hoursSince1970;
        this.accountName = accountName;
    }

    public String getGuiPath() {
        return guiPath;
    }

    public void setGuiPath(String guiPath) {
        this.guiPath = guiPath;
    }

    public Long getHoursSince1970() {
        return hoursSince1970;
    }

    public void setHoursSince1970(Long hoursSince1970) {
        this.hoursSince1970 = hoursSince1970;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }
}
