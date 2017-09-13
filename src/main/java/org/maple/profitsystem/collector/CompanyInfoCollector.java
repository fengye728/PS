/**
 * 
 */
/**
 * @author Maple
 *
 */
package org.maple.profitsystem.collector;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.maple.profitsystem.constants.CommonConstants;
import org.maple.profitsystem.exceptions.HttpException;
import org.maple.profitsystem.exceptions.PSException;
import org.maple.profitsystem.models.CompanyInfoModel;
import org.maple.profitsystem.spiders.FINVIZSpider;
import org.maple.profitsystem.spiders.NASDAQSpider;
import org.maple.profitsystem.utils.CSVUtil;

public class CompanyInfoCollector {
	
	private static Logger logger = Logger.getLogger(CompanyInfoCollector.class);
	
	public static List<CompanyInfoModel> getAndUpdateCompanyFullInfoList(int loadOption, int persistOption) {
 		
		List<CompanyInfoModel> companyList = loadFullCompanyInfoList(loadOption);
		//List<CompanyInfoModel> companyList = new ArrayList<>();
		
		updateCompanyBaseInfoByCompanyList(companyList);
		
		updateCompanyDetailInfoByCompanyList(companyList, persistOption);
		
		return companyList;
	}
	
	
	private static List<CompanyInfoModel> loadFullCompanyInfoList(int loadOption) {
		List<CompanyInfoModel> result = null;
		switch(loadOption) {
		case CommonConstants.LOAD_OPTION_DISK:
			result = loadFullCompanyInfoListFromDisk();
			break;
		case CommonConstants.LOAD_OPTION_DATABASE:
			result = loadFullCompanyInfoListFromDatabase();
			break;
		}
		return result;
	}
	
	/**
	 * Load a list of full company info from database.
	 * @return
	 */
	private static List<CompanyInfoModel> loadFullCompanyInfoListFromDatabase() {
		// TODO 
		return null;
	}
	
	/**
	 * Load a list of full company info from disk.
	 * 
	 * @param companyListPath The parent directory path of the list of company file.
	 * @return List of full company info
	 */
	private static List<CompanyInfoModel> loadFullCompanyInfoListFromDisk() {
		// For debug
		long lastTime = System.currentTimeMillis();
		
		List<CompanyInfoModel> result = new ArrayList<>();
		File path = new File(CommonConstants.PATH_COMPANY_INFO_OUTPUT);
		if(path.exists()) {
			File[] diskFiles = path.listFiles();
			
			String[] contents = new String[diskFiles.length];
			for(int i = 0; i < diskFiles.length; ++i) {
				contents[i] = CSVUtil.readFileContent(diskFiles[i]);

			}
			
			for(int i = 0; i < contents.length; ++i) {
				try {
					CompanyInfoModel fullCompanyInfo = CompanyInfoModel.parseFullFromFileCSV(contents[i]);
					result.add(fullCompanyInfo);
				} catch (PSException e) {
					logger.error("Load " + diskFiles[i].getName() + " failed!");
				}
				
			}

		}
		
		logger.info("Count of company loading from disk:" + result.size() + "|" + "Cost time: " + ((System.currentTimeMillis() - lastTime) / 1000));
		return result;
	}
	
	/**
	 * Update the list of the target company info(just with base info).
	 * @param targetList
	 * @return
	 * @throws HttpException
	 */
	private static void updateCompanyBaseInfoByCompanyList(List<CompanyInfoModel> targetList){
		List<CompanyInfoModel> newestList = null;
		try {
			newestList = NASDAQSpider.fetchCompanyListWithBaseInfo();
			targetList.addAll(newestList);
			
			// remove duplicate objects
			HashSet<CompanyInfoModel> set = new HashSet<>(targetList);
			List<CompanyInfoModel> result = new ArrayList<>(set);
			targetList.clear();
			targetList.addAll(result);
			
			logger.info("Update company base info success");
		} catch (HttpException e) {
			logger.error("Update company base info fail: " + e.getMessage());
		}
	}
	
	/**
	 * Update the quotes list and statistics info of companies in companyList.
	 * 
	 * @param companyList
	 */
	private static void updateCompanyDetailInfoByCompanyList(List<CompanyInfoModel> companyList, int persistOption) {
		List<CompanyInfoModel> failedList = new ArrayList<>();
		for(CompanyInfoModel company : companyList) {
			try {
				// update detail info
				updateCompanyDetailInfoByCompany(company);
				
				logger.info("Update success - " + company.getSymbol() + " | Count:" + company.getQuoteList().size());
			} catch (PSException e) {
				failedList.add(company);
				logger.error("Updated fail - " + company.getSymbol() + ":" + e.getMessage());
			}
			// persist
			// TODO need to improve
			company.persist(persistOption);
		}
		logger.error("Amount of failed Companies:" + failedList.size());
	}
	
	/**
	 * Update the statistics info and quote list of the specified company.
	 * 
	 * @param company
	 * @throws PSException
	 */
	private static void updateCompanyDetailInfoByCompany(CompanyInfoModel company) throws PSException {
		boolean isGetQuoteList = true;
		boolean isGetStatistic = true;
		
		// add newest quotes into quote list
		try {
			company.addNewestQuoteList(NASDAQSpider.fetchNewestStockQuotesByCompany(company));
		} catch (PSException e) {
			logger.error(e.getMessage());
			isGetQuoteList = false;
		} catch (HttpException e) {
			// TODO Use alternative web site
			logger.error(e.getMessage());
			
			// can not get info to do this
			isGetQuoteList = false;
		}
		
		// get company statistics info
		try {
			company.setStatistics(FINVIZSpider.fetchCompanyStatistics(company.getSymbol()));
		} catch (PSException e) {
			logger.error(e.getMessage());
			isGetStatistic = false;
		} catch (HttpException e) {
			// TODO Use alternative web site
			logger.error(e.getMessage());
			
			// can not get info to do this
			isGetStatistic = false;
		}
		
		
		if(isGetQuoteList && isGetStatistic) {
			return ;
		} else {
			final String FAIL_GET_QUOTE_LIST = "Failed to fetch quote list";
			final String FAIL_GET_STATISTICS = "Failed to fetch statistic";
			final String SEPRATOR = " | ";
			String error = "";
			if(!isGetQuoteList){
				error += FAIL_GET_QUOTE_LIST + SEPRATOR;
			}
			if(!isGetStatistic) {
				error += FAIL_GET_STATISTICS;
			}
			throw new PSException(error);
		}
	}
	
}