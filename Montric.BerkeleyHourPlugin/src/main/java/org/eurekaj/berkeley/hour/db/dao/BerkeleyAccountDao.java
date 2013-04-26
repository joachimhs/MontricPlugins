package org.eurekaj.berkeley.hour.db.dao;

import com.sleepycat.persist.EntityCursor;
import org.eurekaj.api.dao.AccountDao;
import org.eurekaj.api.datatypes.Account;
import org.eurekaj.api.datatypes.User;
import org.eurekaj.berkeley.hour.db.BerkeleyDbEnv;
import org.eurekaj.berkeley.hour.db.datatypes.BerkeleyAccount;

import java.lang.Override;
import java.lang.String;
import java.util.ArrayList;
import java.util.List;

import com.sleepycat.persist.PrimaryIndex;
import org.eurekaj.berkeley.hour.db.datatypes.BerkeleyUser;
import org.eurekaj.berkeley.hour.db.datatypes.BerkeleyUserPk;


/**
 * Created with IntelliJ IDEA.
 * User: joahaa
 * Date: 2/4/13
 * Time: 5:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class BerkeleyAccountDao implements AccountDao {
    private BerkeleyDbEnv dbEnvironment;

    private PrimaryIndex<String, BerkeleyAccount> accountPrimaryIndex;
    private PrimaryIndex<BerkeleyUserPk, BerkeleyUser> userPrimaryIndex;

    public BerkeleyAccountDao(BerkeleyDbEnv dbEnvironment) {
        this.dbEnvironment = dbEnvironment;

        accountPrimaryIndex = this.dbEnvironment.getAccountStore().getPrimaryIndex(String.class, BerkeleyAccount.class);
        userPrimaryIndex = this.dbEnvironment.getUserStore().getPrimaryIndex(BerkeleyUserPk.class, BerkeleyUser.class);
    }

    @Override
    public Account getAccount(String accountName) {
        return accountPrimaryIndex.get(accountName);
    }

    @Override
    public void persistAccount(Account account) {
        BerkeleyAccount accountObject = new BerkeleyAccount(account);
        accountPrimaryIndex.put(accountObject);
    }

    @Override
    public List<Account> getAccounts() {
        List<Account> retList = new ArrayList<Account>();
        EntityCursor<BerkeleyAccount> pi_cursor = accountPrimaryIndex.entities();
        try {
            for (BerkeleyAccount node : pi_cursor) {
                retList.add(node);
            }
            // Always make sure the cursor is closed when we are done with it.
        } finally {
            pi_cursor.close();
        }
        return retList;
    }

    @Override
    public User getUser(String username, String accountName) {
        BerkeleyUserPk pk = new BerkeleyUserPk(username, accountName);
        return userPrimaryIndex.get(pk);
    }

    @Override
    public void persistUser(String username, String accountName, String role) {
        BerkeleyUser user = new BerkeleyUser(username, accountName, role);
        userPrimaryIndex.put(user);
    }
}
