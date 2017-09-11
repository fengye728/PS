package org.maple.profitsystem.utils;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.maple.profitsystem.constants.CommonConstants;

public class TradingDateUtil {

	@SuppressWarnings("deprecation")
	public static int betweenTradingDays(Date start, Date end) {
		if(start == null || end == null)
			return 0;
		
		if(start.after(end)) {
			return -betweenTradingDays(end, start);
		}
		
		// eliminate the hour, minute and second's effect
		start.setHours(0);
		start.setMinutes(0);
		start.setSeconds(0);
		end.setHours(0);
		end.setMinutes(0);
		end.setSeconds(0);
		
		final int millisecondsOfDay = 1000 * 60 * 60 * 24;
		
		int days = (int)((end.getTime() - start.getTime()) / millisecondsOfDay);
		
		int weeks = (int)(days / 7);
		
		if(days <= 0) {
			return 0;
		} else {
			// minus the weekend days
			int startDay = start.getDay();
			int endDay = end.getDay();
			
			int redundantDays = 0;
			if(startDay < endDay) {
				if(endDay == 6) {
					redundantDays -= 1;
				}
			} else if(startDay > endDay) {
				weeks++;
				if(startDay == 6) {
					redundantDays += 1;
				}
			}
			
			return days - (weeks * 2 - redundantDays);
		}
	}
	
	/**
	 * 
	 * @param date yyyyMMdd
	 * @return
	 */
	public static Date convertNumDate2Date(Integer date) {
		DateFormat df = new SimpleDateFormat (CommonConstants.DATE_FORMAT_OUT);
		try {
			return df.parse(String.valueOf(date));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 
	 * @param date
	 * @return Integer date formatting yyyyMMdd
	 */
	public static Integer convertDate2NumDate(Date date) {
		DateFormat df = new SimpleDateFormat (CommonConstants.DATE_FORMAT_OUT);
		return Integer.valueOf(df.format(date));
	}
	
	/**
	 * Check whether market was already opend in the date.
	 * 
	 * @param date
	 * @return
	 */
	public static boolean hasMarketOpened(Calendar date) {
		if(date.get(Calendar.HOUR_OF_DAY) < 16) {
			return false;
		} else {
			return true;
		}
	}
}
