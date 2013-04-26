package org.eurekaj.plugins.leveldb.dao;

import java.util.ArrayList;
import java.util.List;

import org.eurekaj.api.dao.AccountDao;
import org.eurekaj.api.datatypes.Account;
import org.eurekaj.api.datatypes.User;
import org.eurekaj.api.datatypes.basic.BasicAccount;
import org.eurekaj.api.datatypes.basic.BasicUser;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import static org.iq80.leveldb.impl.Iq80DBFactory.*;

public class LevelDBAccountDao implements AccountDao {
	private DB db;
	private static final String accountBucketKey = "account;";
	private static final String userBucketKey = "user;";
	private Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().serializeNulls().create();
	
	public LevelDBAccountDao(DB db) {
		this.db = db;
	}
	
	@Override
	public List<Account> getAccounts() {
		List<Account> accountList = new ArrayList<>();
		
		DBIterator iterator = db.iterator();
		iterator.seek(bytes(accountBucketKey));
		while (iterator.hasNext() && asString(iterator.peekNext().getKey()).startsWith(accountBucketKey)) {
			accountList.add(gson.fromJson(asString(iterator.next().getValue()), BasicAccount.class));
		}
		return accountList;
	}

	@Override
	public Account getAccount(String accountName) {
		String json = asString(db.get(bytes(accountBucketKey + accountName)));
		BasicAccount account = gson.fromJson(json, BasicAccount.class);
		return account;
	}

	@Override
	public void persistAccount(Account account) {
		db.put(bytes(accountBucketKey + account.getAccountName()), bytes(gson.toJson(new BasicAccount(account))));
	}

	@Override
	public User getUser(String username, String accountName) {
		String json = asString(db.get(bytes(userBucketKey + accountName + ";" + username)));
		BasicUser user = gson.fromJson(json, BasicUser.class);
		return user;
	}

	@Override
	public void persistUser(String username, String accountName, String role) {
		db.put(bytes(userBucketKey + accountName + ";" + username), bytes(gson.toJson(new BasicUser(username, accountName, role))));		
	}

}
