package org.maple.profitsystem.services.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.maple.profitsystem.ConfigProperties;
import org.maple.profitsystem.exceptions.PSException;
import org.maple.profitsystem.mappers.CompanyModelMapper;
import org.maple.profitsystem.models.CompanyModel;
import org.maple.profitsystem.models.StockQuoteModel;
import org.maple.profitsystem.services.CompanyService;
import org.maple.profitsystem.services.CompanyStatisticsService;
import org.maple.profitsystem.services.StockQuoteService;
import org.maple.profitsystem.utils.CSVUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(value = "transactionManager", rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
public class CompanyServiceImpl implements CompanyService {
	
	private static Logger logger = Logger.getLogger(CompanyServiceImpl.class);
	
	@Autowired
	private ConfigProperties properties;
	
	@Autowired
	private CompanyModelMapper companyModelMapper;
	
	@Autowired
	private CompanyStatisticsService companyStatisticsService;
	
	@Autowired
	private StockQuoteService stockQuoteService;

	/**
	 * Load a list of full company info from disk.
	 * 
	 * @return List of full company info
	 */
	@Override
	public List<CompanyModel> loadCompanyWithFullInfoListFromDisk() {
		
		// For counting time
		long lastTime = System.currentTimeMillis();
		
		logger.info("Loading full company info list from disk...");
		List<CompanyModel> result = new ArrayList<>();
		File path = new File(properties.getBackupPath());
		if(path.exists()) {
			File[] diskFiles = path.listFiles();
			
			String[] contents = new String[diskFiles.length];
			for(int i = 0; i < diskFiles.length; ++i) {
				contents[i] = CSVUtil.readFileContent(diskFiles[i]);

			}
			
			for(int i = 0; i < contents.length; ++i) {
				try {
					CompanyModel fullCompanyInfo = CompanyModel.parseFullFromFileCSV(contents[i]);
					result.add(fullCompanyInfo);
				} catch (PSException e) {
					logger.error("Load " + diskFiles[i].getName() + " failed!");
				}
				
			}

		}
		
		logger.info("Loaded full company info list from disk completed! Count of companies:" + result.size() + "|" + "Cost time: " + ((System.currentTimeMillis() - lastTime) / 1000));
		return result;
	}
	
	@Override
	public int persistCompanyWithFullInfoToDisk(CompanyModel record) {
		if(null == record) {
			return 0;
		}
		String filename = record.getSymbol();
		
		FileWriter fw = null;
		try {
			
			File file = new File(properties.getBackupPath() + File.separator + filename);
			if(!file.exists())
				file.createNewFile();
			fw = new FileWriter(file);
			
			// write company info
			fw.write(record.formatFullCSV());

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return 0;
		} catch (IOException e) {
			logger.error("Store failed - " + record.getSymbol() + ": " + e.getMessage());
			return 0;
		} finally {
			if(fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return 1;
	}
	
	@Override
	public int persistCompanyWithFullInfoListToDisk(List<CompanyModel> records) {
		int count = 0;
		for(CompanyModel record : records) {
			count += persistCompanyWithFullInfoToDisk(record);
		}
		return count;
	}

	@Override
	public List<CompanyModel> getAllCompanies() {
		return companyModelMapper.selectAll();
	}

	@Override
	public int addCompanyWithStatistics(CompanyModel record) {
		if(null == record)  {
			return 0;
		}
		// set date
		record.setCreateDt(new Date());
		record.setLastUpdateDt(new Date());
		if(companyModelMapper.insert(record) == 0) {
			return 0;
		} else {
			return companyStatisticsService.addCompanyStatistics(record.getStatistics());
		}
	}

	@Override
	public int updateCompany(CompanyModel record) {
		if(null == record) {
			return 0;
		}
		record.setLastUpdateDt(new Date());
		return companyModelMapper.updateByPrimaryKeySelective(record);
	}

	@Override
	public int addListCompaniesWithStatistics(List<CompanyModel> records) {
		if(records == null || records.isEmpty()) {
			return 0;
		} else {
			int count = 0;
			for(CompanyModel record : records) {
				count += addCompanyWithStatistics(record);
			}
			return count;
		}
	}

	@Override
	public int addCompanyFullInfo(CompanyModel record) {
		// add base info and statistics
		if(0 == addCompanyWithStatistics(record))
			return 0;
		
		stockQuoteService.addStockQuoteList(record.getQuoteList());
		return 1;
	}

	@Override
	public int addListCompaniesFullInfo(List<CompanyModel> records) {
		if(null == records)
			return 0;
		int count = 0;
		for(CompanyModel record : records) {
			count += addCompanyFullInfo(record);
		}
		return count;
	}

	@Override
	public int updateCompanyWithQuotes(CompanyModel record) {
		if(null == record) {
			return 0;
		}
		List<StockQuoteModel> quotes = record.getQuoteList();
		int count = stockQuoteService.addStockQuoteList(quotes);
		if(count == 0) {
			return 0;
		} else {
			record.setLastUpdateDt(new Date());
			// set last quote date
			record.setLastQuoteDt(quotes.get(quotes.size() - 1).getQuoteDate());
			return this.updateCompany(record);
		}
	}

	@Override
	public List<CompanyModel> getAllCompaniesWithStatistics() {
		return companyModelMapper.selectAllWithStatistics();
	}

	@Override
	public List<CompanyModel> getAllCompaniesFull() {
		return companyModelMapper.selectAllFull();
	}
}
