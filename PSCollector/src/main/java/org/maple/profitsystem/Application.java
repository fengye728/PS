/**
 * 
 */
/**
 * @author Maple
 *
 */
package org.maple.profitsystem;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.maple.profitsystem.exceptions.PSException;
import org.maple.profitsystem.spiders.impl.QuoteSpiderFidelity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(ConfigProperties.class)
@EnableScheduling
public class Application{

	public static ConfigurableApplicationContext springContext;
	
	private static Logger logger = Logger.getLogger(Application.class);
	
	public static void main(String[] args) throws IOException{
		springContext = SpringApplication.run(Application.class, args);
		
		// get ps context
		CollectorContext context = springContext.getBean(CollectorContext.class);
		
		logger.info("Startup PS Collector");
		
		// Debug statements
		QuoteSpiderFidelity s = new QuoteSpiderFidelity();
		try {
			s.fetchQuotes("PPPPP", 20170522);
			s.fetchQuotes("QQQ", 20170522);
		} catch (PSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// ---------------
		
		context.run(args);
		
		logger.info("Startup schduled tasks...");
	}

}