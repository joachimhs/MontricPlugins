package org.eurekaj.berkeley.hour.db.datatypes;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import org.eurekaj.api.datatypes.User;

/**
 * Created with IntelliJ IDEA.
 * User: joahaa
 * Date: 2/4/13
 * Time: 6:17 PM
 * To change this template use File | Settings | File Templates.
 */
@Entity
public class BerkeleyUser implements User {
    @PrimaryKey private BerkeleyUserPk pk;
    private String userRole;

    public BerkeleyUser() {
    }

    public BerkeleyUser(String username, String accountName, String userRole) {
        this.pk = new BerkeleyUserPk(username, accountName);
        this.userRole = userRole;
    }

    public BerkeleyUser(User user) {
        this.pk = new BerkeleyUserPk(user.getUserName(), user.getAccountName());
        this.userRole = user.getUserRole();
    }

    @Override
    public String getUserName() {
        return this.pk.getUserName();
    }

    public void setUserName(String username) {
        this.pk.setUserName(username);
    }

    @Override
    public String getAccountName() {
        return this.pk.getAccountName();
    }

    public void setAccountName(String accountName) {
        this.pk.setAccountName(accountName);
    }

    @Override
    public String getUserRole() {
        return this.userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }
}
