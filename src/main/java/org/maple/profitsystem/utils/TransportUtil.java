package org.maple.profitsystem.utils;

public class TransportUtil {
	
	public static String stripCSVField(String rawField) {
		int lIndex = 0;
		int rIndex = rawField.length() - 1;
		while(rawField.charAt(lIndex) == '\"')
			++lIndex;
		while(rawField.charAt(rIndex) == '\"')
			--rIndex;
		return rawField.substring(lIndex, rIndex + 1).trim();
	}
}
