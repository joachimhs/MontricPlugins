package org.eurekaj.berkeley.hour.db.datatypes;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import org.eurekaj.api.datatypes.Account;

import java.lang.Override;
import java.lang.String;

/**
 * Created with IntelliJ IDEA.
 * User: joahaa
 * Date: 2/4/13
 * Time: 5:50 PM
 * To change this template use File | Settings | File Templates.
 */
@Entity(version=1)
public class BerkeleyAccount implements Account {
    @PrimaryKey private String accountName;
    private String accountType;

    public BerkeleyAccount() {
    }

    public BerkeleyAccount(Account account) {
        this.accountName = account.getAccountName();
        this.accountType = account.getAccountType();
    }

    public BerkeleyAccount(String accountName, String accountType) {
        this.accountName = accountName;
        this.accountType = accountType;
    }

    @Override
    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    @Override
    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }
}
