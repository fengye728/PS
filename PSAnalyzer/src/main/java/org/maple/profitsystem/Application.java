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
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class Application{

	private static Logger logger = Logger.getLogger(Application.class);
	
	
	public static void main(String[] args){
		ConfigurableApplicationContext springContext = SpringApplication.run(Application.class, args);
		
		AnalyzerContext analyzerContext = springContext.getBean(AnalyzerContext.class);
		
		logger.info("Startup Analyzer!");
		analyzerContext.run();
		
		logger.info("Stop Analyzer!");
	}

}