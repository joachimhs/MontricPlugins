package org.eurekaj.berkeley.hour.db.datatypes;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Entity(version=1)
public class MetricHour {
	@PrimaryKey private String key; //hoursSince1970;guiPath
	private Double [] valueArray;
	@SecondaryKey(relate = Relationship.MANY_TO_ONE) private Long hoursSince1970;
	
	public MetricHour(String key) {
		this.setKey(key);
		valueArray = new Double[240];
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
