package org.maple.profitsystem.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.maple.profitsystem.constants.CommonConstants;

public class CSVUtil {
	
	public static String stripCSVField(String rawField) {
		if(rawField == null)
			return null;
		
		String trimStr = rawField.trim();
		if(trimStr.equals(""))
			return null;
		
		int lIndex = 0;
		int rIndex = trimStr.length() - 1;
		if(trimStr.charAt(lIndex) == CommonConstants.CSV_SURROUNDER_OF_FIELD.charAt(0)) {
			++lIndex;
		}
		if(trimStr.charAt(rIndex) != CommonConstants.CSV_SURROUNDER_OF_FIELD.charAt(0)) {
			++rIndex;
		}
		if(lIndex >= rIndex)
			return null;
		else
			return rawField.substring(lIndex, rIndex).trim();
	}
	
	public static String[] splitCSVRecord(String csvRecord) {
		String[] tmpfields = csvRecord.split(CommonConstants.CSV_SEPRATOR_BETWEEN_FIELD);
		String[] results = new String[tmpfields.length];
		for(int i = 0; i < tmpfields.length; ++i) {
			results[i] = stripCSVField(tmpfields[i]);
		}
		return results;
	}
	
	public static String readFileContent(File file) {
		BufferedReader bf = null;
		StringBuffer result = new StringBuffer();
		try {
			bf = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			char[] buffer = new char[CommonConstants.BUFFER_SIZE_OF_READER];
			int countOnce = 0;
			while((countOnce = bf.read(buffer)) != -1) {
				result.append(buffer, 0, countOnce);
			}
			return result.toString();
		} catch (Exception e) {
			return result.toString();
		} finally {
			if(bf != null) {
				try {
					bf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
