package com.cyzapps.SmartMath;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

public class ColorSpinnerAdapter extends ArrayAdapter<Integer> implements SpinnerAdapter {
	protected Context mContext = null;
	protected final Integer[] mObjects; // android.graphics.Color list

	public ColorSpinnerAdapter(Context context, Integer[] objects) {
		super(context, R.layout.color_spinner_item, objects);
		mContext = context;
		mObjects = objects;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		super.getDropDownView(position, convertView, parent);

		View rowView = convertView;

		if (rowView == null) {
			// Get a new instance of the row layout view
			LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
			rowView = inflater.inflate(R.layout.color_spinner_item, null);
			rowView.setBackgroundColor(mObjects[position]);
		} else {
			rowView.setBackgroundColor(mObjects[position]);
		}
		TextView tv = (TextView)rowView.findViewById(android.R.id.text1);
		tv.setText("");

		return rowView;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;

		if (rowView == null) {
			// Get a new instance of the row layout view
			LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
			rowView = inflater.inflate(R.layout.color_spinner_item, null);
			rowView.setBackgroundColor(mObjects[position]);
		} else {
			rowView.setBackgroundColor(mObjects[position]);
		}
		TextView tv = (TextView)rowView.findViewById(android.R.id.text1);
		tv.setText("");

		return rowView;
	}

}
