package org.eurekaj.plugins.cassandra.datatypes;

import org.eurekaj.api.util.DoubleParser;

public class MetricHour {
	private String key; //guiPath;hoursSince1970
	private Double [] valueArray;
	private Long hoursSince1970;
	private Double [] oneMinuteAverageArray;
	private Double [] fiveMinuteAverageArray;
	private Double [] halfHourAverageArray;
	private Double [] hourAverageArray;
	private Double dailyAverage;
	private Double weeklyAverage;
    private String valueType;
    private String unitType;
	
	public MetricHour(String key, String valueType, String unitType) {
		this.setKey(key);
		valueArray = new Double[240]; //Once each 15 seconds
        oneMinuteAverageArray = new Double[60]; //Once each minute
		fiveMinuteAverageArray = new Double[60]; //Once each minute
		halfHourAverageArray = new Double[20]; //Once each 5 minutes
		hourAverageArray = new Double[6]; //Once each 10 minutes
		dailyAverage = null;
		weeklyAverage = null;
        this.valueType = valueType;
        this.unitType = unitType;
	}
	
	public MetricHour(String key, String valueArrayString, String valueType, String unitType) {
		this(key, valueType, unitType);
		valueArray = new Double[240];
		setValueArrayFromString(valueArrayString);
	}
	
	public void setValueArrayFromString(String valueString) {
		if (valueString.contains(";")) {
			int index = 0;
			for (String currValue : valueString.split(";")) {
				valueArray[index] = DoubleParser.parseDoubleFromString(currValue, null);
				index++;
			}
		} else {
			valueArray[0] = DoubleParser.parseDoubleFromString(valueString, null);
		}
		
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
	
	public String getValueArrayString() {
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < valueArray.length; i++) {
			sb.append(valueArray[i]);
			if (i < (valueArray.length - 1)) {
				sb.append(";");
			}
		}
		
		return sb.toString();
	}
	
	public String getKey() {
		return key;
	}
	
	public String getGuiPath() {
		if (this.key.contains(";")) {
			return this.key.split(";")[0];
		}
		
		return null;
	}
	
	public void setKey(String key) {
		this.key = key;
		String[] keysplit = key.split(";");
		if (keysplit.length == 2) {
			this.hoursSince1970 = Long.parseLong(keysplit[1]);
		}
	}

    public Double[] getOneMinuteAverageArray() {
        return oneMinuteAverageArray;
    }

    public void setOneMinuteAverageArray(Double[] oneMinuteAverageArray) {
        this.oneMinuteAverageArray = oneMinuteAverageArray;
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

    public Double getDailyAverage() {
        return dailyAverage;
    }

    public void setDailyAverage(Double dailyAverage) {
        this.dailyAverage = dailyAverage;
    }

    public Double getWeeklyAverage() {
        return weeklyAverage;
    }

    public void setWeeklyAverage(Double weeklyAverage) {
        this.weeklyAverage = weeklyAverage;
    }

    public Long getHoursSince1970() {
		return hoursSince1970;
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
