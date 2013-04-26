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

import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;
import org.apache.log4j.Logger;
import org.eurekaj.api.dao.AlertDao;
import org.eurekaj.api.datatypes.Alert;
import org.eurekaj.api.datatypes.TriggeredAlert;
import org.eurekaj.berkeley.hour.db.BerkeleyDbEnv;
import org.eurekaj.berkeley.hour.db.datatypes.BerkeleyAlert;
import org.eurekaj.berkeley.hour.db.datatypes.BerkeleyAlertPk;
import org.eurekaj.berkeley.hour.db.datatypes.BerkeleyTriggeredAlert;
import org.eurekaj.berkeley.hour.db.datatypes.BerkeleyTriggeredAlertPk;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: jhs
 * Date: 5/6/11
 * Time: 10:01 AM
 * To change this template use File | Settings | File Templates.
 */
public class BerkeleyAlertDao implements AlertDao {
    private static Logger log = Logger.getLogger(BerkeleyAlertDao.class.getName());

    private BerkeleyDbEnv dbEnvironment;

    private PrimaryIndex<BerkeleyAlertPk, BerkeleyAlert> alertPrimaryIdx;
    private SecondaryIndex<String, BerkeleyAlertPk, BerkeleyAlert> alertAccountNameSecondaryIndex;
    private PrimaryIndex<BerkeleyTriggeredAlertPk, BerkeleyTriggeredAlert> triggeredAlertPrimaryIdx;
    private SecondaryIndex<BerkeleyTriggeredAlertPk, BerkeleyTriggeredAlertPk, BerkeleyTriggeredAlert> triggeredAlertTimeperiodSecondaryIdx;

    public BerkeleyAlertDao(BerkeleyDbEnv dbEnvironment) {
        this.dbEnvironment = dbEnvironment;

        alertPrimaryIdx = this.dbEnvironment.getAlertStore().getPrimaryIndex(BerkeleyAlertPk.class, BerkeleyAlert.class);
        alertAccountNameSecondaryIndex = this.dbEnvironment.getAlertStore().getSecondaryIndex(alertPrimaryIdx, String.class, "accountName");
        triggeredAlertPrimaryIdx = this.dbEnvironment.getTriggeredAlertStore().getPrimaryIndex(BerkeleyTriggeredAlertPk.class, BerkeleyTriggeredAlert.class);
        triggeredAlertTimeperiodSecondaryIdx = this.dbEnvironment.getTriggeredAlertStore().getSecondaryIndex(triggeredAlertPrimaryIdx, BerkeleyTriggeredAlertPk.class, "triggeredTimeperiod");
    }

    @Override
	public void persistAlert(Alert alert) {
        BerkeleyAlert berkeleyAlert = new BerkeleyAlert(alert);
        log.info("AlertName: " + berkeleyAlert.getAlertName() + " account: " + berkeleyAlert.getAccountName() + " pk.account: " + berkeleyAlert.getPk().getAccountName());
		alertPrimaryIdx.put(berkeleyAlert);
	}

    @Override
	public BerkeleyAlert getAlert(String alertName, String accountName) {
        BerkeleyAlertPk pk = new BerkeleyAlertPk(alertName, accountName);
		return alertPrimaryIdx.get(pk);
	}

    @Override
	public List<Alert> getAlerts(String accountName) {
		List<Alert> retList = new ArrayList<Alert>();

		EntityCursor<BerkeleyAlert> pi_cursor = alertAccountNameSecondaryIndex.entities(accountName, true, accountName, true);
		try {
		    for (BerkeleyAlert node : pi_cursor) {
		        retList.add(node);
		    }
		// Always make sure the cursor is closed when we are done with it.
		} finally {
			pi_cursor.close();
		}
		return retList;
	}

    @Override
    public void persistTriggeredAlert(TriggeredAlert triggeredAlert) {
        BerkeleyTriggeredAlert berkeleyTriggeredAlert = new BerkeleyTriggeredAlert(triggeredAlert);

        triggeredAlertPrimaryIdx.put(berkeleyTriggeredAlert);
    }

    @Override
    public List<TriggeredAlert> getTriggeredAlerts(String accountName, Long fromTimeperiod, Long toTimeperiod) {
        List<TriggeredAlert> retList = new ArrayList<TriggeredAlert>();

        BerkeleyTriggeredAlertPk fromKey = new BerkeleyTriggeredAlertPk(null, fromTimeperiod, accountName);
        BerkeleyTriggeredAlertPk toKey = new BerkeleyTriggeredAlertPk(null, toTimeperiod, accountName);

        EntityCursor<BerkeleyTriggeredAlert> si_cursor = triggeredAlertTimeperiodSecondaryIdx.entities(fromKey, true, toKey, true);

		try {
			for (BerkeleyTriggeredAlert triggeredAlert : si_cursor) {
				retList.add(triggeredAlert);
			}
			// Always make sure the cursor is closed when we are done with it.
		} finally {
			si_cursor.close();
		}
		return retList;
    }

    @Override
    public List<TriggeredAlert> getTriggeredAlerts(String alertname, String accountName, Long fromTimeperiod, Long toTimeperiod) {
        List<TriggeredAlert> retList = new ArrayList<TriggeredAlert>();

        BerkeleyTriggeredAlertPk fromKey = new BerkeleyTriggeredAlertPk(alertname, fromTimeperiod, accountName);
        BerkeleyTriggeredAlertPk toKey = new BerkeleyTriggeredAlertPk(alertname, toTimeperiod, accountName);

        EntityCursor<BerkeleyTriggeredAlert> pi_cursor = triggeredAlertPrimaryIdx.entities(fromKey, true, toKey, true);

		try {
			for (BerkeleyTriggeredAlert triggeredAlert : pi_cursor) {
				retList.add(triggeredAlert);
			}
			// Always make sure the cursor is closed when we are done with it.
		} finally {
			pi_cursor.close();
		}
		return retList;
    }

    @Override
    public List<TriggeredAlert> getRecentTriggeredAlerts(String accountName, int numAlerts) {
        List<TriggeredAlert> retList = new ArrayList<TriggeredAlert>();

        Calendar nowCal = Calendar.getInstance();
        Long fromTimeperiod = nowCal.getTimeInMillis() / 15000;
        nowCal.add(Calendar.HOUR, -1 * 24 * 7);
        Long toTimeperiod = nowCal.getTimeInMillis() / 15000;

        BerkeleyTriggeredAlertPk fromKey = new BerkeleyTriggeredAlertPk(null, fromTimeperiod, accountName);
        BerkeleyTriggeredAlertPk toKey = new BerkeleyTriggeredAlertPk(null, toTimeperiod, accountName);

        EntityCursor<BerkeleyTriggeredAlert> si_cursor = triggeredAlertTimeperiodSecondaryIdx.entities(fromKey, true, toKey, true);

		try {
            //Ensure that we do not attempt to fetch more alerts than there are in the DB
            if (numAlerts > si_cursor.count()) {
                numAlerts = si_cursor.count();
            }

            //If there are any triggered alerts to fetch
            if (numAlerts > 0) {
                //Add the very last triggered alert to the return list
                BerkeleyTriggeredAlert triggeredAlert = si_cursor.last();
                retList.add(triggeredAlert);
                //Add the remaining up to numAlerts - 1 triggered alerts to the return list
                for (int i = 0; i < (numAlerts-1); i++) {
                    triggeredAlert = si_cursor.prev();
                    retList.add(triggeredAlert);
                }
            }
			// Always make sure the cursor is closed when we are done with it.
		} finally {
			si_cursor.close();
		}
		return retList;
    }

	@Override
	public void deleteAlert(String alertName, String accountName) {
        BerkeleyAlertPk pk = new BerkeleyAlertPk(alertName, accountName);
		alertPrimaryIdx.delete(pk);
	}
}
