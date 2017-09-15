package org.maple.profitsystem;

@org.springframework.boot.context.properties.ConfigurationProperties(prefix = "ps")
public class ConfigurationProperties {

	private String startupOption;

	public String getStartupOption() {
		return startupOption;
	}

	public void setStartupOption(String startupOption) {
		this.startupOption = startupOption;
	}
	
	
}
