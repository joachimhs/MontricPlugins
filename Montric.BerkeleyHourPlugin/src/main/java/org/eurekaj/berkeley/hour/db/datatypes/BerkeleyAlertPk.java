package org.eurekaj.berkeley.hour.db.datatypes;

import com.sleepycat.persist.model.KeyField;
import com.sleepycat.persist.model.Persistent;

/**
 * Created with IntelliJ IDEA.
 * User: joahaa
 * Date: 2/4/13
 * Time: 12:44 PM
 * To change this template use File | Settings | File Templates.
 */
@Persistent
public class BerkeleyAlertPk {
    @KeyField(1) private String alertName;
    @KeyField(2) private String accountName;

    public BerkeleyAlertPk() {
    }

    public BerkeleyAlertPk(String alertName, String accountName) {
        this.alertName = alertName;
        this.accountName = accountName;
    }

    public String getAlertName() {
        return alertName;
    }

    public void setAlertName(String alertName) {
        this.alertName = alertName;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }
}
