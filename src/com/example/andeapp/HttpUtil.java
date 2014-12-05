package com.example.andeapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class HttpUtil {


	public static String Get(String urlToRead) {
		URL url;
		HttpURLConnection conn;
		BufferedReader rd;
		String line;
		String result = "";
		try {
			url = new URL(urlToRead);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));

			while ((line = rd.readLine()) != null) {
				result += line;
			}
			rd.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static String Get_(String url, String charset) throws ClientProtocolException, IOException {
		String ret = null;
		
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(url);
		
		HttpResponse response = client.execute(get);
		HttpEntity resEntity = response.getEntity();
	
		if(resEntity != null){
			if (charset == null) {
				ret = EntityUtils.toString(resEntity);
			}
			else {
				ret = convertStreamToString(resEntity.getContent(), charset);
			}
		}
		
		return ret;
	}
	
	private static String convertStreamToString(InputStream is, String charset) {

		StringBuilder sb = new StringBuilder();
		
		try{
			BufferedReader reader= new BufferedReader(new InputStreamReader(is, charset));
			String line = null;
			try {
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}catch(Exception e){
			
		}
		
		
		return sb.toString();

	}
	
}
