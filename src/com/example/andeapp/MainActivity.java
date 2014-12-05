package com.example.andeapp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

/*
 * http://www.kma.go.kr/DFSROOT/POINT/DATA/top.json.txt
 * http://www.kma.go.kr/DFSROOT/POINT/DATA/mdl.11.json.txt
 * http://www.kma.go.kr/DFSROOT/POINT/DATA/leaf.11680.json.txt
 * http://www.kma.go.kr/wid/queryDFSRSS.jsp?zone=1168052100
 */


public class MainActivity extends Activity implements OnItemClickListener{

	private Handler mHandler;
	private ArrayAdapter<Region> adp_;
	private String SelectedCode_;
	private String SelectedName_;
	
	private OpenSqlite sqlite_;
	private SQLiteDatabase db_;
	
	private static final String DEFAULT_REGION = "DEFAULT_REGION";
	

	class Region {
		public String Code;
		public String Value;
		
		public Region() {}
		
		public Region(String c, String v) {
			Code = c;
			Value = v;
		}
		
		@Override
		public String toString() {
			return Value;
		}
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mHandler = new Handler();
		
		sqlite_ = new OpenSqlite(MainActivity.this);
		sqlite_.createDataBase();
		
		adp_ = new ArrayAdapter<Region>(getApplicationContext(), android.R.layout.simple_list_item_1);
		ListView lv = (ListView)findViewById(R.id.listRegion);
		lv.setAdapter(adp_);
		lv.setOnItemClickListener(this);

		SelectedCode_ = "";
		Initialize();
		DefaultWeatherInfo();
	}
	
	public void Initialize() {
		SelectedCode_ = "";
		GetRegionData();
	}
	
	public void DefaultWeatherInfo() {
		SelectedCode_ = "";
		SelectedName_ = "";
		StartSubIntent();
	}
	
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if(event.getKeyCode() == KeyEvent.KEYCODE_BACK ) {
			if(event.getAction() == KeyEvent.ACTION_DOWN) {
				
				if (SelectedCode_.length() == 10) {
					SelectedCode_ = SelectedCode_.substring(0, 5);
					GetRegionData();
					return true;
				}
				else if (SelectedCode_.length() == 5) {
					SelectedCode_ = SelectedCode_.substring(0, 2);
					GetRegionData();
					return true;
				}
				else if (SelectedCode_.length() == 2) {
					SelectedCode_ = "";
					GetRegionData();
					return true;
				}
			} 
		}

		return super.dispatchKeyEvent(event);
	}
	
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		
		Region r = adp_.getItem(position);
		String dis  = "";
		
		SelectedCode_ = r.Code;
		SelectedName_ = r.Value;
		
		if (SelectedCode_.equals(DEFAULT_REGION)) {
			dis = String.format("현재 지역 날씨 데이터를 가져 오는 중...", r.Value);
			Toast.makeText(getApplicationContext(), dis, Toast.LENGTH_SHORT).show();

			StartSubIntent();
		}
		else if (SelectedCode_.length() == 10) {
			dis = String.format("%s 날씨 데이터를 가져 오는 중...", r.Value);
			Toast.makeText(getApplicationContext(), dis, Toast.LENGTH_SHORT).show();
			
			StartSubIntent();
		}
		else {
			//dis = String.format("%s 선택", r.Value);
			//Toast.makeText(getApplicationContext(), dis, Toast.LENGTH_SHORT).show();
			
			GetRegionData();
		}
		
	}
	
	public void StartSubIntent() {
		Intent intent = new Intent(getApplicationContext(), SubActivity.class);
		
		if (SelectedCode_.length() == 10) {
			intent.putExtra("SelectedCode", SelectedCode_);
			intent.putExtra("SelectedName", SelectedName_);
		}
		startActivity(intent);
	}
	
	public void GetRegionData()
	{
		(new RegionDBThread()).start();
	}
	


	class RegionDBThread extends Thread {
		@Override
		public void run() {
			super.run();
			
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					
					String sql = "";
					
					adp_.clear();
					
					if (SelectedCode_.equals("")) {
						adp_.add(new Region("DEFAULT_REGION", ":: 현재 지역 날씨"));
						sql = "SELECT code, codename FROM tcode WHERE length(code) = 2 ;";
					}
					else if (SelectedCode_.length() == 2) {
						adp_.add(new Region("", ":: 초기 화면 (시/도 선택)"));
						sql = String.format(
								"SELECT code, (SELECT codename FROM tcode WHERE code = '%s' LIMIT 1) || ' ' || codename AS codename "
								+ "FROM tcode WHERE code like '%s%%' AND length(code) = 5 ;"
								, SelectedCode_, SelectedCode_);
						
						Log.v("sql", sql);
					}
					else if (SelectedCode_.length() == 5) {
						adp_.add(new Region("", ":: 초기 화면 (시/도 선택)"));
						sql = String.format(
								"SELECT code, (SELECT codename FROM tcode WHERE code = '%s' LIMIT 1) || ' ' || "
								+ "(SELECT codename FROM tcode WHERE code = '%s' LIMIT 1) || ' ' || codename AS codename "
								+ "FROM tcode WHERE code like '%s%%' AND length(code) = 10 ;"
								, SelectedCode_.substring(0, 2), SelectedCode_, SelectedCode_);
					}
					
					db_ = sqlite_.getReadableDatabase();
					Cursor cur = db_.rawQuery(sql, null);
					
					while (cur.moveToNext()) {
						String code = cur.getString(cur.getColumnIndex("code"));
						String codename = cur.getString(cur.getColumnIndex("codename"));
						
						adp_.add(new Region(code, codename));
						
					}

				}
			});
			
		}
	}

	
	class RegionHttpThread extends Thread {
		@Override
		public void run() {
			super.run();
			
			try {
				String url = "";
				
				if (SelectedCode_.equals("")) {
					url = "http://www.kma.go.kr/DFSROOT/POINT/DATA/top.json.txt";
				}
				else if (SelectedCode_.length() == 2) {
					url = String.format("http://www.kma.go.kr/DFSROOT/POINT/DATA/mdl.%s.json.txt", SelectedCode_);
				}
				else if (SelectedCode_.length() == 5) {
					url = String.format("http://www.kma.go.kr/DFSROOT/POINT/DATA/leaf.%s.json.txt", SelectedCode_);
				}
				
				String sJson = HttpUtil.Get(url);
				Log.v("httpget", sJson);
				//Toast.makeText(getApplicationContext(), sJson, Toast.LENGTH_SHORT).show();
				
				final JSONArray jarr = new JSONArray(sJson);
				
				mHandler.post(new Runnable() {
					@Override
					public void run() {

						adp_.clear();
						for (int i = 0; i < jarr.length(); ++i) {
							JSONObject o;
							try {
								o = jarr.getJSONObject(i);
								Region g = new Region(o.getString("code"), o.getString("value"));
								adp_.add(g);
								
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
						
					}
				});
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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
