package com.example.andeapp;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import android.widget.ImageView;


public class ListAdapter extends ArrayAdapter<ListAdapter.Model> {
	public static class Model{

		public String Region;
		public String Temp;
		public String Info;
		public int Icon;

		public Model(String r, String t, String i, int c) {
			Region = r;
			Temp = t;
			Info = i;
			Icon = c;
		}
	}

	private final Context context;
	private final ArrayList<ListAdapter.Model> modelsArrayList;

	public ListAdapter(Context context, ArrayList<ListAdapter.Model> modelsArrayList) {
		super(context, R.layout.viewitem, modelsArrayList);

		this.context = context;
		this.modelsArrayList = modelsArrayList;
	}
	
	public ArrayList<ListAdapter.Model> getModelList() {
		return this.modelsArrayList;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		// 1. Create inflater 
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// 2. Get rowView from inflater

		View rowView = null;
		rowView = inflater.inflate(R.layout.viewitem, parent, false);

		// 3. Get icon,title & counter views from the rowView
		((TextView) rowView.findViewById(R.id.txtRegion)).setText(modelsArrayList.get(position).Region); 
		((TextView) rowView.findViewById(R.id.txtTemp)).setText(modelsArrayList.get(position).Temp);
		((TextView) rowView.findViewById(R.id.txtInfo)).setText(modelsArrayList.get(position).Info);
		((ImageView) rowView.findViewById(R.id.imgIcon)).setImageResource(modelsArrayList.get(position).Icon); 
		
		return rowView;
	}
}