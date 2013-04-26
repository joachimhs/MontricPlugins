package org.eurekaj.plugins.cassandra.datatypes;

import org.apache.cassandra.db.marshal.DoubleType;
import org.apache.cassandra.db.marshal.LongType;
import org.eurekaj.api.util.DoubleParser;
import org.eurekaj.plugins.util.ArrayToStringCassandraValidator;
import org.firebrandocm.dao.annotations.Column;
import org.firebrandocm.dao.annotations.ColumnFamily;
import org.firebrandocm.dao.annotations.Key;

import java.util.List;
import java.util.Map;

public class MetricHour {
    private String guiPath;
    private String accountName;
    private Long hoursSince1970;
    private Double [] metrics;
    private Double [] oneMinuteAverageArray;
	private Double [] fiveMinuteAverageArray;
	private Double [] halfHourAverageArray;
	private Double [] hourAverageArray;
	private Double dailyAverage;
	private Double weeklyAverage;
    private String valueType;
    private String unitType;
	
	public MetricHour(String accountName, String guiPath, Long hoursSince1970, String valueType, String unitType) {
		this.accountName = accountName;
        this.guiPath = guiPath;
        this.hoursSince1970 = hoursSince1970;
		metrics = new Double[240]; //Once each 15 seconds
        oneMinuteAverageArray = new Double[60]; //Once each minute
		fiveMinuteAverageArray = new Double[60]; //Once each minute
		halfHourAverageArray = new Double[20]; //Once each 5 minutes
		hourAverageArray = new Double[6]; //Once each 10 minutes
		dailyAverage = null;
		weeklyAverage = null;
        this.valueType = valueType;
        this.unitType = unitType;
	}
	
	public MetricHour(String accountName, String guiPath, Long hoursSince1970, String metrics, String valueType, String unitType) {
		this(accountName, guiPath, hoursSince1970, valueType, unitType);
		this.metrics = new Double[240];
		setMetricsArrayFromString(metrics);
	}

    public String getGuiPath() {
        return guiPath;
    }

    public void setGuiPath(String guiPath) {
        this.guiPath = guiPath;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public void setMetricsArrayFromString(String metricsString) {
		if (metricsString.contains("; ")) {
			int index = 0;
			for (String currValue : metricsString.split(";")) {
				metrics[index] = DoubleParser.parseDoubleFromString(currValue, null);
				index++;
			}
		} else {
			metrics[0] = DoubleParser.parseDoubleFromString(metricsString, null);
		}
		
	}
	
	public MetricHour() {
		super();
	}


	
	public Double getValueAt(int index) {
		return metrics[index];
	}
	
	public void setValueAt(int index, Double value) {
		metrics[index] =  value;
	}

    public Double[] getMetrics() {
        return metrics;
    }

    public void setMetrics(Double[] metrics) {
        this.metrics = metrics;
    }

    public void setMetrics(Map<Integer, Double> metricsMap) {
        if (metrics == null) {
            metrics = new Double[240];
        }
        for (Integer key : metricsMap.keySet()) {
            if (key.intValue() >= 0 && key.intValue() < 240) {
                this.metrics[key] = metricsMap.get(key);
            }
        }
    }

    public void setMetrics(List<Double> metrics) {
        if (metrics.size() >= 240) {
            for (int i = 0; i < metrics.size(); i++) {
                this.metrics[i] = metrics.get(i);
            }
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

    public void setHoursSince1970(Long hoursSince1970) {
        this.hoursSince1970 = hoursSince1970;
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
