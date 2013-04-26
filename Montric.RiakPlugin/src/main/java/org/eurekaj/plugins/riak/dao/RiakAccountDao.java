package org.eurekaj.plugins.riak.dao;

import java.util.ArrayList;
import java.util.List;

import org.eurekaj.api.dao.AccountDao;
import org.eurekaj.api.datatypes.Account;
import org.eurekaj.api.datatypes.User;
import org.eurekaj.api.datatypes.basic.BasicAccount;
import org.eurekaj.api.datatypes.basic.BasicUser;

import com.basho.riak.client.IRiakClient;
import com.basho.riak.client.RiakException;
import com.basho.riak.client.RiakRetryFailedException;
import com.basho.riak.client.bucket.Bucket;
import com.basho.riak.client.query.indexes.BucketIndex;

public class RiakAccountDao implements AccountDao {
	private IRiakClient riakClient;
	
	public RiakAccountDao(IRiakClient riakClient) {
		this.riakClient = riakClient;
	}
	
	@Override
	public List<Account> getAccounts() {
		List<Account> accountList = new ArrayList<Account>();

        Bucket myBucket = null;
        try {
            myBucket = riakClient.fetchBucket("Account").execute();

            for (String key : myBucket.fetchIndex(BucketIndex.index).withValue("$key").execute()) {
            	accountList.add(myBucket.fetch(key, BasicAccount.class).execute());
            }
        } catch (RiakRetryFailedException rrfe) {
            rrfe.printStackTrace();
        } catch (RiakException e) {
            e.printStackTrace();
        }

        return accountList;
	}

	@Override
	public Account getAccount(String accountName) {
		Account account = null;
		
		Bucket myBucket = null;
        try {
            myBucket = riakClient.fetchBucket("Account").execute();
        	account = myBucket.fetch(accountName, BasicAccount.class).execute();
        } catch (RiakRetryFailedException rrfe) {
            rrfe.printStackTrace();
        } catch (RiakException e) {
            e.printStackTrace();
        }
        
		return account;
	}

	@Override
	public void persistAccount(Account account) {
		Bucket myBucket = null;
        try {
            myBucket = riakClient.fetchBucket("Account").execute();
            myBucket.store(account.getAccountName(), account).execute();
        } catch (RiakRetryFailedException rrfe) {
            rrfe.printStackTrace();
        }
	}

	@Override
	public User getUser(String username, String accountName) {
		BasicUser user = null;
		
		Bucket myBucket = null;
        try {
            myBucket = riakClient.fetchBucket("User;" + accountName).execute();
            user = myBucket.fetch(username, BasicUser.class).execute();
        } catch (RiakRetryFailedException rrfe) {
            rrfe.printStackTrace();
        } catch (RiakException e) {
            e.printStackTrace();
        }
        
		return user;
	}

	@Override
	public void persistUser(String username, String accountName, String role) {
		Bucket myBucket = null;
        try {
            myBucket = riakClient.fetchBucket("User;" + accountName).execute();
            myBucket.store(username, new BasicUser(username, accountName, role)).execute();
        } catch (RiakRetryFailedException rrfe) {
            rrfe.printStackTrace();
        }
		
	}

	
}
