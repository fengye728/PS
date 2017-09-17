/**
 * 
 */
/**
 * @author Maple
 *
 */
package org.maple.profitsystem;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ConfigProperties.class)
public class Application implements CommandLineRunner{
	
	private static Logger logger = Logger.getLogger(Application.class);
	
	@Autowired
	private PSContext context;
	
	public static void main(String[] args){
		SpringApplication.run(Application.class, args);
		
	}

	@Override
	public void run(String... args) throws Exception {
		logger.info("Startup Profit System");
		
		context.run();
		
		logger.info("Stop Profit System!");
	}
}