package org.maple.profitsystem.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.maple.profitsystem.constants.CommonConstants;

public class CSVUtil {
	
	public static String stripCSVField(String rawField) {
		int lIndex = 0;
		int rIndex = rawField.length() - 1;
		while(rawField.charAt(lIndex) == CommonConstants.CSV_SURROUNDER_OF_FIELD.charAt(0))
			++lIndex;
		while(rawField.charAt(rIndex) == CommonConstants.CSV_SURROUNDER_OF_FIELD.charAt(0))
			--rIndex;
		return rawField.substring(lIndex, rIndex + 1).trim();
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
		String result = "";
		try {
			bf = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			char[] buffer = new char[CommonConstants.BUFFER_SIZE_OF_READER];
			int countOnce = 0;
			while((countOnce = bf.read(buffer)) != -1) {
				result += String.copyValueOf(buffer, 0, countOnce);
			}
			return result;
		} catch (Exception e) {
			return result;
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
