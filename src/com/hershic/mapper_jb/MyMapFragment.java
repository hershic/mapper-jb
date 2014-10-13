package com.hershic.mapper_jb;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.MapFragment;

public class MyMapFragment extends MapFragment {

	public MyMapFragment() {
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.activity_main, container,
				false);
		return rootView;
	}
	
	public void onAttach(Activity a) {
		MainActivity.setFragmentManager(getFragmentManager());
	}
}
