package org.maple.profitsystem;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ps")
public class ConfigProperties {
	
	private String backupPath;		// The path of backup file
	
	private String statisticsUpdatePeriod;
	
	private String quotesUpdatePeriod;

	public String getBackupPath() {
		return backupPath;
	}

	public void setBackupPath(String backupPath) {
		this.backupPath = backupPath;
	}

	public String getStatisticsUpdatePeriod() {
		return statisticsUpdatePeriod;
	}

	public void setStatisticsUpdatePeriod(String statisticsUpdatePeriod) {
		this.statisticsUpdatePeriod = statisticsUpdatePeriod;
	}

	public String getQuotesUpdatePeriod() {
		return quotesUpdatePeriod;
	}

	public void setQuotesUpdatePeriod(String quotesUpdatePeriod) {
		this.quotesUpdatePeriod = quotesUpdatePeriod;
	}
	
	
}
