package org.eurekaj.plugins.cassandra.datatypes;

import org.eurekaj.api.datatypes.EmailRecipientGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: jhs
 * Date: 4/18/12
 * Time: 5:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class CassandraEmailRecipientGroup implements Comparable<EmailRecipientGroup>, EmailRecipientGroup{
    private String emailRecipientGroupName;
    private String accountName;
	private String smtpServerhost;
	private String smtpUsername;
	private String smtpPassword;
	private boolean useSSL;
	private Integer port = 25;
	private List<String> emailRecipientList = new ArrayList<String>();

    public CassandraEmailRecipientGroup(EmailRecipientGroup emailRecipientGroup) {
        this.emailRecipientGroupName = emailRecipientGroup.getEmailRecipientGroupName();
        this.smtpServerhost = emailRecipientGroup.getSmtpServerhost();
        this.smtpUsername = emailRecipientGroup.getSmtpUsername();
        this.smtpPassword = emailRecipientGroup.getSmtpPassword();
        this.useSSL = emailRecipientGroup.isUseSSL();
        this.port = emailRecipientGroup.getPort();
        this.emailRecipientList = emailRecipientGroup.getEmailRecipientList();
    }

    public String getEmailRecipientGroupName() {
        return emailRecipientGroupName;
    }

    public void setEmailRecipientGroupName(String emailRecipientGroupName) {
        this.emailRecipientGroupName = emailRecipientGroupName;
    }

    public String getSmtpServerhost() {
        return smtpServerhost;
    }

    public void setSmtpServerhost(String smtpServerhost) {
        this.smtpServerhost = smtpServerhost;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getSmtpUsername() {
        return smtpUsername;
    }

    public void setSmtpUsername(String smtpUsername) {
        this.smtpUsername = smtpUsername;
    }

    public String getSmtpPassword() {
        return smtpPassword;
    }

    public void setSmtpPassword(String smtpPassword) {
        this.smtpPassword = smtpPassword;
    }

    public boolean isUseSSL() {
        return useSSL;
    }

    public void setUseSSL(boolean useSSL) {
        this.useSSL = useSSL;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public List<String> getEmailRecipientList() {
        return emailRecipientList;
    }

    public void setEmailRecipientList(List<String> emailRecipientList) {
        this.emailRecipientList = emailRecipientList;
    }

    @Override
    public int compareTo(EmailRecipientGroup other) {

        if (other == null || other.getEmailRecipientGroupName() == null) {
			return 1;
		}

		if (this.getEmailRecipientGroupName() == null) {
			return -1;
		}

		return this.getEmailRecipientGroupName().compareTo(other.getEmailRecipientGroupName());
    }
}
