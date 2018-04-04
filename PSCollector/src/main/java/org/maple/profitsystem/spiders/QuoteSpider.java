package org.maple.profitsystem.spiders;

import java.util.List;

import org.maple.profitsystem.exceptions.HttpException;
import org.maple.profitsystem.models.StockQuoteModel;

public interface QuoteSpider {
	List<StockQuoteModel> fetchQuotes(String symbol, Integer startDt) throws HttpException;
}
