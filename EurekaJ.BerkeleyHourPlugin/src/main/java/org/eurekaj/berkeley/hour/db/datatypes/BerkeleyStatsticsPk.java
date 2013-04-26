package org.eurekaj.berkeley.hour.db.datatypes;

import com.sleepycat.persist.model.KeyField;
import com.sleepycat.persist.model.Persistent;

/**
 * Created with IntelliJ IDEA.
 * User: joahaa
 * Date: 2/4/13
 * Time: 1:10 PM
 * To change this template use File | Settings | File Templates.
 */
@Persistent
public class BerkeleyStatsticsPk {
    @KeyField(1) private String guiPath;
    @KeyField(2) private String accountName;

    public BerkeleyStatsticsPk() {
    }

    public BerkeleyStatsticsPk(String guiPath, String accountName) {
        this.guiPath = guiPath;
        this.accountName = accountName;
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
}
