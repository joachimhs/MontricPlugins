package org.eurekaj.berkeley.hour.db.datatypes;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Entity(version=3)
public class MetricHour {
	@PrimaryKey private MetricHourPk pk;
    private String valueType;
    private String unitType;
	private Double [] valueArray;
	private Double [] oneMinuteAverageArray;
	private Double [] fiveMinuteAverageArray;
	private Double [] halfHourAverageArray;
	private Double [] hourAverageArray;
	private Double dailyAverage;
	private Double weeklyAverage;
	@SecondaryKey(relate = Relationship.MANY_TO_ONE) private Long hoursSince1970;
	
	public MetricHour(String guiPath, String accountName, Long timePeriod) {
        this.pk = new MetricHourPk(guiPath, accountName, timePeriod);
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

    public String getGuiPath() {
        return this.pk.getGuiPath();
    }

    public void setGuiPath(String guiPath) {
        this.pk.setGuiPath(getGuiPath());
    }

    public Long getHoursSince1970() {
        return this.pk.getHoursSince1970();
    }

    public void setHoursSince1970(Long hoursSince1970) {
        this.pk.setHoursSince1970(hoursSince1970);
    }

    public String getAccountName() {
        return this.pk.getAccountName();
    }

    public void setAccountName(String accountName) {
        this.pk.setAccountName(accountName);
    }

    public MetricHourPk getPk() {
        return pk;
    }

    public void setPk(MetricHourPk pk) {
        this.pk = pk;
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


    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public String getUnitType() {
        return unitType;
    }

    public void setUnitType(String unitType) {
        this.unitType = unitType;
    }
}
