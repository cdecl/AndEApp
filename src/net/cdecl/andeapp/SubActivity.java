package net.cdecl.andeapp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

public class SubActivity extends Activity {

	private Handler mHandler;
	private ListAdapter adp_;
	private String SelectedCode_;
	private String SelectedName_;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		//StrictMode.enableDefaults();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sub);
		
	
		mHandler = new Handler();
		
		Intent in = getIntent();
		SelectedCode_ = in.getStringExtra("SelectedCode");
		SelectedName_ = in.getStringExtra("SelectedName");
		
		final ArrayList<ListAdapter.Model> list = new ArrayList<ListAdapter.Model>();
		adp_ = new ListAdapter(this, list);
		ListView lv = (ListView)findViewById(R.id.listView);
		lv.setAdapter(adp_);

		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (list.get(position).Icon == R.drawable.ic_action_search) {
					finish();
				}
			}
			
		});
	
		GetData();
	}
	
	private void GetData() {
		ThreadRun r = new ThreadRun();
		r.start();
	}
	
	
	class ThreadRun extends Thread {
		@Override
		public void run() {
			super.run();
			
			//http://www.kma.go.kr/wid/queryDFSRSS.jsp?zone=1168052100
			String url = "http://www.kma.go.kr/wid/queryDFSRSS.jsp";
			
			if (SelectedCode_ != null) {
				url += "?zone=";
				url += SelectedCode_;
			}
			
			Log.v("weather api url", url);
			final String res = HttpUtil.Get(url);
			
			mHandler.post(new Runnable() {
				@Override
				public void run() {

					adp_.clear();
					try {
						parseXML(res);
					} catch (XPathExpressionException e) {
						e.printStackTrace();
					} catch (ParserConfigurationException e) {
						e.printStackTrace();
					} catch (SAXException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
						
				}
			});
			
			
		}
	}

	
	private void parseXML(String xml) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		InputStream istream = new ByteArrayInputStream(xml.getBytes("utf-8"));
		   
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		org.w3c.dom.Document doc = builder.parse(istream);
		
		XPath xpath = XPathFactory.newInstance().newXPath();
       
		Node nodeRegion = (Node)xpath.evaluate("/rss/channel/item/category", doc, XPathConstants.NODE);
		String sRegion = getNodeValue(nodeRegion);
		
		// /rss/channel/item/category 데이터가 공백이면, 부모창에서 받은 지역명 사용 
		if (sRegion.isEmpty()) sRegion = SelectedName_;
		
		Node tm = (Node)xpath.evaluate("/rss/channel/item/description/header/tm", doc, XPathConstants.NODE);
		String sTM = getNodeValue(tm);
		
		SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd");
		Date d = null;
		try {
			d = f.parse(sTM.substring(0, 8));
			f.applyPattern("yyyy년 MM월 dd일 E");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
        // NodeList 가져오기 : row 아래에 있는 모든 col1 을 선택
        NodeList items = (NodeList)xpath.evaluate("/rss/channel/item/description/body/data", doc, XPathConstants.NODESET);
		
		if (items != null) {
			ListAdapter.Model m = new ListAdapter.Model(sRegion, "다른 지역 날씨", "", R.drawable.ic_action_search);
			adp_.getModelList().add(m);
			
			// 날짜
			m = new ListAdapter.Model(sRegion, f.format(d), "", -1); //R.drawable.ic_drawer);
			adp_.getModelList().add(m);
			
			for (int i = 0; i < items.getLength(); ++i) { 
				Node node = items.item(i);
				
				if (getNodeValue(node, "hour").equals("3")) {
					Calendar c = Calendar.getInstance();
					c.setTime(d);
					c.add(Calendar.DATE, 1);
					d = c.getTime();

					if (i > 0) {
						// 날짜
						m = new ListAdapter.Model(sRegion, f.format(d), "", -1); //R.drawable.ic_drawer);
						adp_.getModelList().add(m);
					}
				}
				
				String temp = getNodeValue(node, "temp");
				int nTemp = (int)(Float.parseFloat(temp));
				
				int ntmn = (int)Float.parseFloat(getNodeValue(node, "tmn"));
				int ntmx = (int)Float.parseFloat(getNodeValue(node, "tmx"));
				
				String sInfo = 
						String.format("온도 %s/%s, 강수 %s%%, 습도 %s%%, 바람 %s(%d m/s)	", 
						ntmn == -999 ? "" : String.valueOf(ntmn),
						ntmx == -999 ? "" : String.valueOf(ntmx),
						getNodeValue(node, "pop"),
						getNodeValue(node, "reh"),
						getNodeValue(node, "wdKor"),
						(int)Float.parseFloat(getNodeValue(node, "ws"))
				);
				
				Log.v("weather info", sInfo);
				
//				sky : 하늘상태코드 [맑음(1), 구름조금(2), 구름많음(3), 흐림(4)] 
//				pty : 강수상태코드 [없음(0), 비(1), 비/눈(2), 눈(3)]

				int nIcon = R.drawable.w_sunny_icon;
				
				String sky = getNodeValue(node, "sky");
				String pty = getNodeValue(node, "pty");
				//Log.v("weather sky,pyt", String.format("%s, %s", sky, pty));
				
				if (pty.equals("1")) {
					nIcon = R.drawable.w_raining_icon;
				}
				else if (pty.equals("2") || pty.equals("3")) {
					nIcon = R.drawable.w_snow_icon;
				}
				else if (sky.equals("2") || sky.equals("4")) {
					nIcon = R.drawable.w_partially_cloud_icon;
				}
				else if (sky.equals("3")) {
					nIcon = R.drawable.w_full_cloud_icon;
				}
				
				m =	new ListAdapter.Model(
						String.format("%s %02d시", f.format(d), Integer.parseInt(getNodeValue(node, "hour"))),
						String.format("%sºc %s", String.valueOf(nTemp), getNodeValue(node, "wfKor")),
						sInfo, nIcon
						);
				
				adp_.getModelList().add(m);
				
			}
		}	
	}
	
	private String getNodeValue(Node node) {
		return getNodeValue(node, null);
	}
	
	private String getNodeValue(Node node, String cName) {
		String sRet = "";
		
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			if (cName != null) {
				Element e = (Element)node;
				NodeList list = e.getElementsByTagName(cName);
				if (list != null) {
					sRet = list.item(0).getFirstChild().getNodeValue();
				}	
			}
			else { // is null
				if (node.getFirstChild() != null) {
					sRet = node.getFirstChild().getNodeValue();
				}
			}
		}
		
		return sRet;
	}
	 

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sub, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
