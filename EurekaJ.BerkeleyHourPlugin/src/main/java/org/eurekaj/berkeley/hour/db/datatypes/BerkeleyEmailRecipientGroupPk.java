package org.eurekaj.berkeley.hour.db.datatypes;

import com.sleepycat.persist.model.KeyField;
import com.sleepycat.persist.model.Persistent;

/**
 * Created with IntelliJ IDEA.
 * User: joahaa
 * Date: 2/4/13
 * Time: 12:58 PM
 * To change this template use File | Settings | File Templates.
 */
@Persistent
public class BerkeleyEmailRecipientGroupPk {
    @KeyField(1) private String emailRecipientGroupName;
    @KeyField(2) private String accountName;

    public BerkeleyEmailRecipientGroupPk() {
    }

    public BerkeleyEmailRecipientGroupPk(String emailRecipientGroupName, String accountName) {
        this.emailRecipientGroupName = emailRecipientGroupName;
        this.accountName = accountName;
    }

    public String getEmailRecipientGroupName() {
        return emailRecipientGroupName;
    }

    public void setEmailRecipientGroupName(String emailRecipientGroupName) {
        this.emailRecipientGroupName = emailRecipientGroupName;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }
}
