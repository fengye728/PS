package org.maple.profitsystem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.maple.profitsystem.collector.CompanyInfoCollector;
import org.maple.profitsystem.models.CompanyModel;
import org.maple.profitsystem.services.CompanyService;
import org.maple.profitsystem.systems.EVBBSystem;
import org.maple.profitsystem.systems.EVBBSystemResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PSContext {
	
	private static Logger logger = Logger.getLogger(PSContext.class);
	
	@Autowired
	private ConfigProperties properties;

	@Autowired
	private CompanyService companyService;
	
	@Autowired
	private CompanyInfoCollector companyInfoCollector;
	
	@Autowired
	private EVBBSystem evbb;
	
	// ------------ Properties ------------
	
	private List<CompanyModel> companyList = new ArrayList<>();
	

	
	public List<CompanyModel> getCompanyList() {
		return companyList;
	}
	
	public void setCompanyList(List<CompanyModel> companyList) {
		this.companyList = companyList;
	}
	
	public void init() {
		// create stock quote directory
		File stockQuotesDir = new File(properties.getBackupPath());
		if(!stockQuotesDir.exists()) {
			stockQuotesDir.mkdirs();
		}
		
		// set time zone of EST
		TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));
	}
	
	public void run() {
		init();
		
		logger.info("Loading data from disk...");
		companyList = companyService.loadCompanyWithFullInfoListFromDisk();
		logger.info("Loaded data from disk completed! Count of records: " + companyList.size());
		for(CompanyModel company : companyList) {
			List<EVBBSystemResult> result = evbb.analyzeAll(company);
			if(result.size() > 0) {
				for(EVBBSystemResult op : result) {
					System.out.println(op.getCompany().getSymbol() + "|" + op.getCompany().getStatistics() + "|" + op.getCompany().getQuoteList().get(op.getDayIndex()));
				}
			}
		}
//		// load data from db first
//		if(loadListCompanyBaseInfoFromDB() == 0) {
//			// no data in db
//			// then load data from disk
//			loadListCompanyFullInfoFromDisk();
//		}
//		
//		update();
//
//		
//		// then set schedule
//		storeListCompanyFullInfoToDisk();
	}
	
	private void update() {
		// fetch companies base info
		//companyInfoCollector.addListNewCompaniesBaseInfo();
		// fetch companies statistics info
		
		//companyInfoCollector.updateListCompanyStatistics();
		
		// fetch companies quotes
		//companyInfoCollector.updateListCompanyQuotes();
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
		companyList = companyService.getAllCompaniesFull();
		int count = companyService.persistCompanyWithFullInfoListToDisk(companyList);
		logger.info("Persisted data into disk completely! Count of records: " + count);
		return count;
	}
}

class UpdateCompanyTask extends Thread {
	
	@Override
	public void run() {
		
	}
}
