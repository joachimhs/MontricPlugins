/**
    EurekaJ Profiler - http://eurekaj.haagen.name
    
    Copyright (C) 2010-2011 Joachim Haagen Skeie

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package org.eurekaj.berkeley.hour.db.dao;

import java.util.ArrayList;
import java.util.List;

import org.eurekaj.api.dao.SmtpDao;
import org.eurekaj.api.datatypes.EmailRecipientGroup;
import org.eurekaj.berkeley.hour.db.BerkeleyDbEnv;
import org.eurekaj.berkeley.hour.db.datatypes.BerkeleyEmailRecipientGroup;

import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.PrimaryIndex;
import org.eurekaj.berkeley.hour.db.datatypes.BerkeleyEmailRecipientGroupPk;

public class BerkeleySmtpDaoImpl implements SmtpDao {
	private BerkeleyDbEnv dbEnvironment;
	private PrimaryIndex<BerkeleyEmailRecipientGroupPk, BerkeleyEmailRecipientGroup> emailRecipientGroupPrimaryIdx;
	
	public BerkeleySmtpDaoImpl(BerkeleyDbEnv dbEnvironment) {
        this.dbEnvironment = dbEnvironment;
		emailRecipientGroupPrimaryIdx = this.dbEnvironment.getSmtpServerStore().getPrimaryIndex(BerkeleyEmailRecipientGroupPk.class, BerkeleyEmailRecipientGroup.class);
	}

	@Override
	public List<EmailRecipientGroup> getEmailRecipientGroups(String accountName) {
		List<EmailRecipientGroup> retList = new ArrayList<EmailRecipientGroup>();
		EntityCursor<BerkeleyEmailRecipientGroup> pi_cursor = emailRecipientGroupPrimaryIdx.entities();
		try {
		    for (BerkeleyEmailRecipientGroup node : pi_cursor) {
		        retList.add(node);
		    }
		// Always make sure the cursor is closed when we are done with it.
		} finally {
			pi_cursor.close();
		} 
		return retList;
	}
	
	@Override
	public EmailRecipientGroup getEmailRecipientGroup(String groupName, String accountName) {
        BerkeleyEmailRecipientGroupPk pk = new BerkeleyEmailRecipientGroupPk(groupName, accountName);
		BerkeleyEmailRecipientGroup server = emailRecipientGroupPrimaryIdx.get(pk);
		return server;
	}
	
	@Override
	public void persistEmailRecipientGroup(EmailRecipientGroup emailRecipientGroup) {
        BerkeleyEmailRecipientGroup berkeleyEmailRecipientGroup = new BerkeleyEmailRecipientGroup(emailRecipientGroup);

        if (emailRecipientGroup.getSmtpPassword() == null || emailRecipientGroup.getSmtpPassword().length() == 0) {
            //Do not overwrite password with an empty one, use the password stored in the database (if any)
            EmailRecipientGroup oldEmailGroup = getEmailRecipientGroup(emailRecipientGroup.getEmailRecipientGroupName(), emailRecipientGroup.getAccountName());
            if (oldEmailGroup != null) {
                berkeleyEmailRecipientGroup.setSmtpPassword(oldEmailGroup.getSmtpPassword());
            }
        }
		emailRecipientGroupPrimaryIdx.put(berkeleyEmailRecipientGroup);
	}
	
	@Override
	public void deleteEmailRecipientGroup(EmailRecipientGroup emailRecipientGroup) {
        BerkeleyEmailRecipientGroupPk pk = new BerkeleyEmailRecipientGroupPk(emailRecipientGroup.getEmailRecipientGroupName(), emailRecipientGroup.getAccountName());
		emailRecipientGroupPrimaryIdx.delete(pk);
	}

	@Override
	public void deleteEmailRecipientGroup(String groupName, String accountName) {
        BerkeleyEmailRecipientGroupPk pk = new BerkeleyEmailRecipientGroupPk(groupName, accountName);
		emailRecipientGroupPrimaryIdx.delete(pk);
	}
}
