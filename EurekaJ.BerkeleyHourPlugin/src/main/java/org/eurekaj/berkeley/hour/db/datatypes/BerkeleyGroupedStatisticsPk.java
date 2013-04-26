package org.eurekaj.berkeley.hour.db.datatypes;

import com.sleepycat.persist.model.KeyField;
import com.sleepycat.persist.model.Persistent;

/**
 * Created with IntelliJ IDEA.
 * User: joahaa
 * Date: 2/4/13
 * Time: 1:05 PM
 * To change this template use File | Settings | File Templates.
 */
@Persistent
public class BerkeleyGroupedStatisticsPk {
    @KeyField(1) private String name;
    @KeyField(2) private String accountName;

    public BerkeleyGroupedStatisticsPk() {
    }

    public BerkeleyGroupedStatisticsPk(String accountName, String name) {
        this.accountName = accountName;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }
}
