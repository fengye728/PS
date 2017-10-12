package org.maple.profitsystem.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.maple.profitsystem.constants.CommonConstants;

public class CSVUtil {

	public static boolean writeList2CSV(String filename, List<? extends Object> list) {
		File file = new File(filename);
		FileWriter fw = null;
		try {
			fw = new FileWriter(file);
			for(Object item : list) {
				fw.write(item.toString() + CommonConstants.CSV_NEWLINE);
			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	} 
}
