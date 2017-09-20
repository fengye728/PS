package org.maple.profitsystem.services.impl;

import java.util.List;

import org.maple.profitsystem.mappers.CompanyModelMapper;
import org.maple.profitsystem.models.CompanyModel;
import org.maple.profitsystem.services.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(value = "transactionManager", readOnly = true, rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
public class CompanyServiceImpl implements CompanyService {
	
	@Autowired
	private CompanyModelMapper companyModelMapper;
	
	@Override
	public List<CompanyModel> getAllCompanies() {
		return companyModelMapper.selectAll();
	}
	@Override
	public List<CompanyModel> getAllCompaniesWithStatistics() {
		return companyModelMapper.selectAllWithStatistics();
	}

	@Override
	public List<CompanyModel> getAllCompaniesFull() {
		return companyModelMapper.selectAllFull();
	}
	
	@Override
	public CompanyModel getCompanyFullById(long id) {
		return companyModelMapper.selectFullById(id);
	}
}
