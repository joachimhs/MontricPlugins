package org.eurekaj.berkeley.hour.db.datatypes;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Entity(version=2)
public class MetricHour {
	@PrimaryKey private String key; //hoursSince1970;guiPath
	private Double [] valueArray;
	private Double [] oneMinuteAverageArray;
	private Double [] fiveMinuteAverageArray;
	private Double [] halfHourAverageArray;
	private Double [] hourAverageArray;
	private Double dailyAverage;
	private Double weeklyAverage;
	@SecondaryKey(relate = Relationship.MANY_TO_ONE) private Long hoursSince1970;
	
	public MetricHour(String key) {
		this.setKey(key);
		valueArray = new Double[240]; //Once each 15 seconds
		oneMinuteAverageArray = new Double[60]; //Once each minute
		fiveMinuteAverageArray = new Double[60]; //Once each minute
		halfHourAverageArray = new Double[20]; //Once each 5 minutes
		hourAverageArray = new Double[6]; //Once each 10 minutes
		dailyAverage = null;
		weeklyAverage = null;
	}
	
	public MetricHour() {
		super();
	}
	
	public Double getValueAt(int index) {
		return valueArray[index];
	}
	
	public void setValueAt(int index, Double value) {
		valueArray[index] =  value;
	}
	
	public Double getDailyAverage() {
		return dailyAverage;
	}
	
	public void setDailyAverage(Double dailyAverage) {
		this.dailyAverage = dailyAverage;
	}
	
	public Double[] getFiveMinuteAverageArray() {
		return fiveMinuteAverageArray;
	}
	
	public void setFiveMinuteAverageArray(Double[] fiveMinuteAverageArray) {
		this.fiveMinuteAverageArray = fiveMinuteAverageArray;
	}
	
	public Double[] getHalfHourAverageArray() {
		return halfHourAverageArray;
	}
	
	public void setHalfHourAverageArray(Double[] halfHourAverageArray) {
		this.halfHourAverageArray = halfHourAverageArray;
	}
	
	public Double[] getHourAverageArray() {
		return hourAverageArray;
	}
	
	public void setHourAverageArray(Double[] hourAverageArray) {
		this.hourAverageArray = hourAverageArray;
	}
	
	public Double[] getOneMinuteAverageArray() {
		return oneMinuteAverageArray;
	}
	
	public void setOneMinuteAverageArray(Double[] oneMinuteAverageArray) {
		this.oneMinuteAverageArray = oneMinuteAverageArray;
	}
	
	public Double getWeeklyAverage() {
		return weeklyAverage;
	}
	
	public void setWeeklyAverage(Double weeklyAverage) {
		this.weeklyAverage = weeklyAverage;
	}
	
	public String getKey() {
		return key;
	}
	
	public String getGuiPath() {
		if (this.key.contains(";")) {
			return this.key.split(";")[1];
		}
		
		return null;
	}
	
	public void setKey(String key) {
		this.key = key;
		String[] keysplit = key.split(";");
		if (keysplit.length == 2) {
			this.hoursSince1970 = Long.parseLong(keysplit[0]);
		}
	}
	
	public Long getHoursSince1970() {
		return hoursSince1970;
	}
}
