/**
 * 
 */
/**
 * @author Maple
 *
 */
package org.maple.profitsystem.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.maple.profitsystem.constants.CommonConstants;
import org.maple.profitsystem.exceptions.PSException;

public class HttpRequestUtil {
	//private static Logger logger = Logger.getLogger(HttpRequestUtil.class);
	
	
	
	private static String commonMethod(String url, Map<String, String> propertyMap, String data, String method) throws PSException {
		String result = "";
		
        BufferedReader in = null;
        PrintWriter out = null;
		try {
			URL realUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection)realUrl.openConnection();
			
			conn.setRequestMethod(method);
			if(propertyMap != null) {
				for(String key : propertyMap.keySet()) {
					conn.setRequestProperty(key, propertyMap.get(key));
				}
			}
			
			// common property
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			
			if(data != null) {
				conn.setDoOutput(true);
				conn.setDoInput(true);
				
				out = new PrintWriter(conn.getOutputStream());
				
				out.print(data);
				out.flush();
				
			} else {
				conn.connect();
			}
  
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            char[] buffer = new char[CommonConstants.BUFFER_SIZE_OF_READER];
            int countOnce = 0;
            while ((countOnce = in.read(buffer)) != -1) {
                result += String.copyValueOf(buffer, 0, countOnce);
            }
			
		} catch (IOException e) {
			throw new PSException(e.getMessage());
		}
        finally {
            try {
                if (in != null) {
                    in.close();
                }
                if(out != null) {
                	out.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;				
	}
	
	public static String getMethod(String url, Map<String, String> propertyMap, int retryTime) throws PSException {
		boolean requestSuccess = false;
		String response = null;
		for(int i = 0; i < retryTime && !requestSuccess; i++) {
			try {
				response = HttpRequestUtil.commonMethod(url, propertyMap, null, "GET");
				requestSuccess = true;
			} catch (PSException e1) {
			}
		}
		if(!requestSuccess){
			throw new PSException("Get request failed: Reach the max number of retries " + retryTime);
		}
		
		return response;
	}
	
	public static String postMethod(String url, Map<String, String> propertyMap, String data, int retryTime) throws PSException {
		boolean requestSuccess = false;
		String response = null;
		for(int i = 0; i < retryTime && !requestSuccess; i++) {
			try {
				response = HttpRequestUtil.commonMethod(url, propertyMap, data, "POST");
				requestSuccess = true;
			} catch (PSException e1) {
			}
		}
		if(!requestSuccess){
			throw new PSException("Post request failed: Reach the max number of retries "+ retryTime);
		}
		
		return response;
	}
}