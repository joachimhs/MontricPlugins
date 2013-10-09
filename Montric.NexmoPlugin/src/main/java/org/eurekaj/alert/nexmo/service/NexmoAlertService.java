package org.eurekaj.alert.nexmo.service;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eurekaj.alert.nexmo.datatypes.AlertNexmoData;
import org.eurekaj.api.datatypes.Alert;
import org.eurekaj.api.enumtypes.AlertStatus;
import org.eurekaj.api.service.AlertService;

public class NexmoAlertService implements AlertService {
	private ScheduledThreadPoolExecutor threadPool;
	
	public NexmoAlertService() {
		threadPool = new ScheduledThreadPoolExecutor(10);
	}
	@Override
	public void sendAlert(List<String> recipients, Alert alert,
			AlertStatus oldStatus, double currValue, String timeString) {
		
		System.out.println("NexmoAlertService sendAlert()");
		AlertNexmoData smsData = new AlertNexmoData();
		SendTextMessageTask task = new SendTextMessageTask(alert, oldStatus, currValue, timeString, smsData);
		threadPool.schedule(task, 0, TimeUnit.MILLISECONDS);
		System.out.println("NexmoAlertService Alert Scheduled!");
	}
	@Override
	public void configure() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public String getStatus() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public int getOutstandingAlerts() {
		// TODO Auto-generated method stub
		return 0;
	}

}
