package org.eurekaj.berkeley.hour.db.datatypes;

import com.sleepycat.persist.model.KeyField;
import com.sleepycat.persist.model.Persistent;

/**
 * Created with IntelliJ IDEA.
 * User: joahaa
 * Date: 2/4/13
 * Time: 6:17 PM
 * To change this template use File | Settings | File Templates.
 */
@Persistent
public class BerkeleyUserPk {
    @KeyField(1) private String userName;
    @KeyField(2) private String accountName;

    public BerkeleyUserPk() {
    }

    public BerkeleyUserPk(String userName, String accountName) {
        this.userName = userName;
        this.accountName = accountName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }
}
