/**
 * 
 */
/**
 * @author Maple
 *
 */
package org.maple.profitsystem;

import java.io.File;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.maple.profitsystem.constants.CommonConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application implements CommandLineRunner{
	
	private static Logger logger = Logger.getLogger(Application.class);
	
	@Autowired
	private PSContext context;
	
	public static void main(String[] args){
		SpringApplication.run(Application.class, args);
		
	}
	
	void init() {
		// create stock quote directory
		File stockQuotesDir = new File(CommonConstants.PATH_COMPANY_INFO_OUTPUT);
		if(!stockQuotesDir.exists()) {
			stockQuotesDir.mkdirs();
		}
		
		// set time zone of EST
		TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));
	}
	

	@Override
	public void run(String... args) throws Exception {
		init();
		logger.info("Startup Profit System");
		// load base company info
		context.run();
		logger.info("Stop Profit System!");
	}
}