package org.eurekaj.plugins.cassandra.dao;

import com.datastax.driver.core.ResultSet;

import com.datastax.driver.core.Row;
import org.apache.log4j.Logger;
import org.eurekaj.api.dao.AlertDao;
import org.eurekaj.api.datatypes.Alert;
import org.eurekaj.api.datatypes.TriggeredAlert;
import org.eurekaj.api.enumtypes.AlertStatus;
import org.eurekaj.api.enumtypes.AlertType;
import org.eurekaj.api.util.ListToString;
import org.eurekaj.plugins.cassandra.CassandraEnv;
import org.eurekaj.plugins.cassandra.datatypes.CassandraAlert;
import org.eurekaj.plugins.util.JSerializer;
import org.firebrandocm.dao.Query;
import static org.firebrandocm.dao.cql.QueryBuilder.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: joahaa
 * Date: 2/21/13
 * Time: 3:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class CassandraAlertDao implements AlertDao {
    private static Logger logger = Logger.getLogger(CassandraAlertDao.class.getName());

    private CassandraEnv cassandraEnv;

    public CassandraAlertDao(CassandraEnv cassandraEnv) {
        this.cassandraEnv = cassandraEnv;
        //alertTemplate = new ThriftColumnFamilyTemplate<String, String>(cassandraEnv.getEurekaJKeyspace(), "alerts", StringSerializer.get(), StringSerializer.get());
        //triggeredAlertTemplate = new ThriftColumnFamilyTemplate<String, String>(cassandraEnv.getEurekaJKeyspace(), "triggered_alerts", StringSerializer.get(), StringSerializer.get());
    }

    @Override
    public void persistAlert(Alert alert) {
        //CREATE TABLE alerts ( id varchar, guiPath varchar, accountName varchar, activated boolean, errorValue double, warningValue double, selectedAlertType varchar, alertDelay int, status varchar, selectedEmailSenderList list<varchar>, selectedAlertPluginList list<varchar>, PRIMARY KEY (id))
        cassandraEnv.getCassandraSession().execute("insert into alerts (id, accountName, alertName, guiPath, activated, errorValue, warningValue, selectedAlertType, alertDelay, status, emailSenderList, alertPluginList) " +
                "values (" +
                    "'" + alert.getAccountName() + ";" + alert.getAlertName() + "'," +
                    "'" + alert.getAccountName() + "'," +
                    "'" + alert.getAlertName() + "'," +
                    "'" + alert.getGuiPath() + "'," +
                    "" + alert.isActivated() + "," +
                    "" + alert.getErrorValue() + "," +
                    "" + alert.getWarningValue() + "," +
                    "'" + alert.getSelectedAlertType().getTypeName() + "'," +
                    "" + alert.getAlertDelay() + "," +
                    "'" + alert.getStatus().getStatusName() + "'," +
                    "" + ListToString.convertFromCassandraList(alert.getSelectedEmailSenderList(), ",") + "," +
                    "" + ListToString.convertFromCassandraList(alert.getSelectedAlertPluginList(), ",") +
                ")");
        //cassandraEnv.getPersistenceFactory().persist(new CassandraAlert(alert));

        /*ColumnFamilyUpdater<String, String> updater = alertTemplate.createUpdater(alert.getAccountName() + ";" + alert.getAlertName());
        updater.setString("accountname", alert.getAccountName());
        updater.setString("alertName", alert.getAlertName());
        updater.setString("guiPath", alert.getGuiPath());
        updater.setString("errorValue", "" + alert.getErrorValue());
        updater.setString("warningValue", "" + alert.getWarningValue());
        updater.setString("alertDelay", "" + alert.getAlertDelay());
        updater.setString("selectedAlertType", alert.getSelectedAlertType().getTypeName());
        updater.setString("status", alert.getStatus().getStatusName());
        updater.setString("isActivated", "" + alert.isActivated());
        updater.setString("alertPluginList", ListToString.convertFromList(alert.getSelectedAlertPluginList(), ";"));
        updater.setString("emailSenderList", ListToString.convertFromList(alert.getSelectedEmailSenderList(), ";"));

        alertTemplate.update(updater);*/
    }

    @Override
    public Alert getAlert(String accountName, String alertName) {
        //return cassandraEnv.getPersistenceFactory().get(CassandraAlert.class, accountName + ";" + alertName);
        /*Alert alert = null;
        ColumnFamilyResult<String, String> res = alertTemplate.queryColumns(accountName+ ";" + alertName);
        alert = extractAlertFromCfResult(res);

        return alert;*/

        CassandraAlert alert = null;

        ResultSet rs = cassandraEnv.getCassandraSession().execute("select * from alerts where id = '" + accountName + ";" + alertName + "'");
        Iterator<Row> rowIterator = rs.iterator();
        if (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            alert = populateCassandraAlert(row);
        }

        return alert;
    }

    private CassandraAlert populateCassandraAlert(Row row) {
        CassandraAlert alert;
        alert = new CassandraAlert();
        alert.setId(row.getString("id"));
        alert.setGuiPath(row.getString("guiPath"));
        alert.setErrorValue(row.getDouble("errorValue"));
        alert.setWarningValue(row.getDouble("warningValue"));
        alert.setAlertDelay(row.getInt("alertDelay"));
        alert.setSelectedAlertType(AlertType.fromValue(row.getString("selectedAlertType")));
        alert.setStatus(AlertStatus.fromValue(row.getString("status")));
        alert.setActivated(row.getBool("activated"));
        alert.setSelectedAlertPluginList(row.getList("alertPluginList", String.class));
        alert.setSelectedEmailSenderList(row.getList("emailSenderList", String.class));
        return alert;
    }

    /*private Alert extractAlertFromCfResult(ColumnFamilyResult<String, String> res) {
        CassandraAlert alert = new CassandraAlert();
        alert.setAccountName(res.getString("accountname"));
        alert.setAlertName(res.getString("alertName"));
        alert.setGuiPath(res.getString("guiPath"));
        alert.setErrorValue(res.getString("errorValue"));
        alert.setWarningValue(res.getString("warningValue"));
        alert.setAlertDelay(res.getString("alertDelay"));
        alert.setSelectedAlertType(AlertType.fromValue(res.getString("selectedAlertType")));
        alert.setStatus(AlertStatus.fromValue(res.getString("status")));
        alert.setActivated(res.getString("isActivated"));
        alert.setSelectedAlertPluginList(ListToString.convertToList(res.getString("alertPluginList"), ";"));
        alert.setSelectedEmailSenderList(ListToString.convertToList(res.getString("emailSenderList"), ";"));

        return alert;
    }

    private Alert extractAlertFromRow(Row<String, String, String> row) {
        CassandraAlert alert = new CassandraAlert();
        alert.setAccountName(row.getColumnSlice().getColumnByName("accountname").getValue());
        alert.setAlertName(row.getColumnSlice().getColumnByName("alertName").getValue());
        alert.setGuiPath(row.getColumnSlice().getColumnByName("guiPath").getValue());
        alert.setErrorValue(row.getColumnSlice().getColumnByName("errorValue").getValue());
        alert.setWarningValue(row.getColumnSlice().getColumnByName("warningValue").getValue());
        alert.setAlertDelay(row.getColumnSlice().getColumnByName("alertDelay").getValue());
        alert.setSelectedAlertType(AlertType.fromValue(row.getColumnSlice().getColumnByName("selectedAlertType").getValue()));
        alert.setStatus(AlertStatus.fromValue(row.getColumnSlice().getColumnByName("status").getValue()));
        alert.setActivated(row.getColumnSlice().getColumnByName("isActivated").getValue());
        alert.setSelectedAlertPluginList(ListToString.convertToList(row.getColumnSlice().getColumnByName("alertPluginList").getValue(), ";"));
        alert.setSelectedEmailSenderList(ListToString.convertToList(row.getColumnSlice().getColumnByName("emailSenderList").getValue(), ";"));

        return alert;
    }*/

    @Override
    public List<Alert> getAlerts(String accountName) {
        List<Alert> alertList = new ArrayList<Alert>();

        ResultSet rs = cassandraEnv.getCassandraSession().execute("select * from alerts where accountName = '" + accountName + "'");
        Iterator<Row> rowIterator = rs.iterator();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            alertList.add(populateCassandraAlert(row));
        }

        return alertList;
        /*List<String> keys = new ArrayList<String>();
        List<Alert> alerts = new ArrayList<Alert>();

        IndexedSlicesQuery<String, String, String> isq = new IndexedSlicesQuery<String, String, String>(cassandraEnv.getEurekaJKeyspace(), StringSerializer.get(), StringSerializer.get(), StringSerializer.get());
        isq.addEqualsExpression("accountname", accountName);
        isq.setColumnNames("accountname", "alertName", "guiPath", "errorValue", "warningValue", "alertDelay", "selectedAlertType", "status", "isActivated", "alertPluginList", "emailSenderList");
        isq.setColumnFamily("alerts");

        QueryResult<OrderedRows<String, String, String>> result = isq.execute();

        logger.info("found alerts for account. " + accountName + " :: " + result.get().getList().size());
        for (Row<String, String, String> row : result.get().getList()) {
            alerts.add(extractAlertFromRow(row));
        }

        for (String key : keys) {
            ColumnFamilyResult<String, String> res = alertTemplate.queryColumns(key);
            alerts.add(extractAlertFromCfResult(res));
        }

        /*ColumnFamilyResult<String, String> resultIterator = alertTemplate.queryColumns(keys);

        logger.info("queried for keys: " + resultIterator.hasNext());
        while (resultIterator.hasNext()) {
            logger.info("found key!");
            ColumnFamilyResult<String, String> alertResult = resultIterator.next();
            alerts.add(extractAlertFromCfResult(alertResult));
        }* /

        return alerts; */ //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void deleteAlert(String accountName, String alertName) {
        cassandraEnv.getCassandraSession().execute("delete from alerts where id = '" + accountName + ";" + alertName + "'");
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void persistTriggeredAlert(TriggeredAlert triggeredAlert) {
        /*ColumnFamilyUpdater<String, String> updater = triggeredAlertTemplate.createUpdater(triggeredAlert.getAccountName() + ";" + triggeredAlert.getAlertName() + ";" + triggeredAlert.getTimeperiod());
        updater.setString("accountName", triggeredAlert.getAccountName());
        updater.setString("alertName", triggeredAlert.getAlertName());
        updater.setDouble("errorValue", triggeredAlert.getErrorValue());
        updater.setDouble("warningValue", triggeredAlert.getWarningValue());
        updater.setDouble("alertValue", triggeredAlert.getAlertValue());

        triggeredAlertTemplate.update(updater);*/
    }

    @Override
    public List<TriggeredAlert> getTriggeredAlerts(String accountName, Long fromTimeperiod, Long toTimeperiod) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<TriggeredAlert> getTriggeredAlerts(String alertName, String accountName, Long fromTimeperiod, Long toTimeperiod) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<TriggeredAlert> getRecentTriggeredAlerts(String accountName, int numAlerts) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
