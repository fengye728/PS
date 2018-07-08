package org.maple.profitsystem.spiders;

import java.util.List;

import org.maple.profitsystem.exceptions.PSException;
import org.maple.profitsystem.models.OIModel;

public interface EarningDateSpider {

	List<OIModel> fetchEarningDate(Integer eventDay) throws PSException;
}
