package org.maple.profitsystem.services.impl;

import java.util.List;

import org.maple.profitsystem.mappers.StockQuoteModelMapper;
import org.maple.profitsystem.models.StockQuoteModel;
import org.maple.profitsystem.services.StockQuoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(value = "transactionManager", readOnly = true, rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
public class StockQuoteServiceImpl implements StockQuoteService {

	@Autowired
	private StockQuoteModelMapper stockQuoteModelMapper;
	
	@Override
	public List<StockQuoteModel> getAllStockQuotesByCompanyId(long companyId) {
		return stockQuoteModelMapper.selectListByCompanyId(companyId);
	}
}
