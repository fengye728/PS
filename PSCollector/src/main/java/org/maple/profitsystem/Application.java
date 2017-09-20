/**
 * 
 */
/**
 * @author Maple
 *
 */
package org.maple.profitsystem;

import org.apache.log4j.Logger;
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
	
	public static void main(String[] args){
		springContext = SpringApplication.run(Application.class, args);
		
		// get ps context
		PSContext context = springContext.getBean(PSContext.class);
		
		logger.info("Startup PS Collector");
		
		context.run();
		
		logger.info("Stop PS Collector");
	}

}