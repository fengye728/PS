package org.maple.profitsystem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.maple.profitsystem.collector.CompanyInfoCollector;
import org.maple.profitsystem.models.CompanyModel;
import org.maple.profitsystem.services.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CollectorContext {
	
	static final String ARG_UPDATE_NOW = "-n";
	
	private static Logger logger = Logger.getLogger(CollectorContext.class);
	
	@Autowired
	private ConfigProperties properties;

	@Autowired
	private CompanyService companyService;
	
	@Autowired
	private CompanyInfoCollector companyInfoCollector;
	
	// ------------ Properties ------------
	private List<CompanyModel> companyList = new ArrayList<>();
	
	public List<CompanyModel> getCompanyList() {
		return companyList;
	}
	
	public void setCompanyList(List<CompanyModel> companyList) {
		this.companyList = companyList;
	}
	
	// ----------- Public functions ------------------------
	public void init() {
		// create stock quote directory
		File stockQuotesDir = new File(properties.getBackupPath());
		if(!stockQuotesDir.exists()) {
			stockQuotesDir.mkdirs();
		}
		
		// set time zone of EST
		TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));
	}
	
	public void run(String[] args) {
		init();
		
		// load data from db first
		if(loadListCompanyBaseInfoFromDB() == 0) {
			// no data in db
			// then load data from disk
			loadListCompanyFullInfoFromDisk();
		}
		
		// check args
		if(args.length == 1 && args[0].equals(ARG_UPDATE_NOW)) {
			
			// update and store all in first time
			if(companyList.size() == 0) {
				companyInfoCollector.addListNewCompaniesBaseInfo();
				companyInfoCollector.updateListCompanyQuotes();
				companyInfoCollector.updateListCompanyStatistics();
			} else {
				// update quotes first
				companyInfoCollector.updateListCompanyQuotes();
				
				companyInfoCollector.addListNewCompaniesBaseInfo();
				companyInfoCollector.updateListCompanyStatistics();
			}
		}
	}
	
	@Scheduled(cron = "${schedule.cron.companies}", zone = "${schedule.timezone}")
	public void scheduleUpdateCompanies() {
		companyInfoCollector.addListNewCompaniesBaseInfo();
	}
	
	@Scheduled(cron = "${schedule.cron.statistics}", zone = "${schedule.timezone}")
	public void scheduleUpdateCompaniesStatistics() {
		companyInfoCollector.updateListCompanyStatistics();
	}
	
	@Scheduled(cron = "${schedule.cron.quotes}", zone = "${schedule.timezone}")
	public void scheduleUpdateCompaniesQuotes() {
		companyInfoCollector.updateListCompanyQuotes();
	}
	
	@Scheduled(cron = "${schedule.cron.persist}", zone = "${schedule.timezone}")
	public void scheduleBackupToDisk() {
		storeListCompanyFullInfoToDisk();
	}
	
	/**
	 * Get all companies base info from database.
	 */
	private int loadListCompanyBaseInfoFromDB() {
		logger.info("Loading data from database...");
		companyList = companyService.getAllCompaniesWithStatistics();
		logger.info("Loaded data from database completed! Count of records: " + companyList.size());
		return companyList.size();
	}
	
	/**
	 * Recover all companies data from disk.
	 * 
	 */
	private void loadListCompanyFullInfoFromDisk() {
		// load
		logger.info("Loading data from disk...");
		companyList = companyService.loadCompanyWithFullInfoListFromDisk();
		logger.info("Loaded data from disk completed! Count of records: " + companyList.size());
		
		// persist
		logger.info("Persist data from disk into database...");
		int persist2dbCount = companyService.addListCompaniesFullInfo(companyList);
		logger.info("Persisted data from disk into database completely! Count of records: " + persist2dbCount);
	}

	/**
	 * Store all companies data to disk.
	 * 
	 * @param records
	 * @return
	 */
	private int storeListCompanyFullInfoToDisk() {
		logger.info("Persist data into disk...");
		int count = companyService.persistCompanyWithFullInfoListToDisk(companyList);
		logger.info("Persisted data into disk completely! Count of records: " + count);
		return count;
	}
}