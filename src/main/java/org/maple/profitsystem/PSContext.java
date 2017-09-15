package org.maple.profitsystem;

import java.util.ArrayList;
import java.util.List;

import org.maple.profitsystem.models.CompanyModel;
import org.maple.profitsystem.services.CompanyService;
import org.maple.profitsystem.services.StockQuoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PSContext {
	
	@Autowired
	private ConfigurationProperties properties;

	@Autowired
	private CompanyService companyService;
	
	private List<CompanyModel> companyList = new ArrayList<>();
	
	public void run() {
		String option = properties.getStartupOption();
		
		// THREE MODE: First, Normal, Recover
	}
	
	private void First() {
		// fetch all
		
		// persist to db
		companyService.addListCompaniesFullInfo(companyList);
		
		// then set schedule
	}
	
	/**
	 * Recover from backup file in disk.
	 */
	private void recover() {
		// load data from file
		loadListCompanyFullInfoFromDisk();
		
		// add the company base info and statistics into db
		companyService.addListCompaniesWithStatistics(companyList);
		// add quotes into db
		companyService.addListCompaniesFullInfo(companyList);
		
		// then set schedule
	}
	
	private void normal() {
		// load data from db
		loadListCompanyBaseInfoFromDB();
		
		// then set schedule
	}

	/**
	 * Get all companies base info from database.
	 */
	public void loadListCompanyBaseInfoFromDB() {
		companyList = companyService.getAllCompanies();
	}
	/**
	 * Recover all companies data from disk.
	 * 
	 */
	private void loadListCompanyFullInfoFromDisk() {
		companyList = companyService.loadCompanyWithFullInfoListFromDisk();
	}

	/**
	 * Store all companies data to disk.
	 * 
	 * @param records
	 * @return
	 */
	public int storeListCompanyFullInfoToDisk() {
		return companyService.persistCompanyWithFullInfoListToDisk(companyList);
	}
	
	public List<CompanyModel> getCompanyList() {
		return companyList;
	}

	public void setCompanyList(List<CompanyModel> companyList) {
		this.companyList = companyList;
	}
	
	
}
