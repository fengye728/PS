package org.maple.profitsystem.mappers;

import java.util.List;

import org.maple.profitsystem.models.StockQuoteModel;

public interface StockQuoteModelMapper {

    List<StockQuoteModel> selectListByCompanyId(long companyId);
}