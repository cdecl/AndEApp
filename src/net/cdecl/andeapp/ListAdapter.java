package net.cdecl.andeapp;

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
	private final ArrayList<ListAdapter.Model> models;

	public ListAdapter(Context context, ArrayList<ListAdapter.Model> models) {
		super(context, R.layout.viewitem, models);

		this.context = context;
		this.models = models;
	}
	
	public ArrayList<ListAdapter.Model> getModelList() {
		return this.models;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View rowView = null;
		rowView = inflater.inflate(R.layout.viewitem, parent, false);

		((TextView) rowView.findViewById(R.id.txtRegion)).setText(models.get(position).Region); 
		((TextView) rowView.findViewById(R.id.txtTemp)).setText(models.get(position).Temp);
		((TextView) rowView.findViewById(R.id.txtInfo)).setText(models.get(position).Info);
		((ImageView) rowView.findViewById(R.id.imgIcon)).setImageResource(models.get(position).Icon); 
		
		if (models.get(position).Icon == -1) {
			//((TextView) rowView.findViewById(R.id.txtRegion)).setVisibility(TextView.GONE);
			((TextView) rowView.findViewById(R.id.txtInfo)).setVisibility(TextView.GONE);
			
		}
		
		return rowView;
	}
}