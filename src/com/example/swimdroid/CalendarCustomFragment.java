package com.example.swimdroid;

import hirondelle.date4j.DateTime;

import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidGridAdapter;

public class CalendarCustomFragment extends CaldroidFragment {
	public static int SELECTED_TODAY_DATE = 1;
	public static int ADDED_NEW_EVENT = 2;
	
	@Override
	public CaldroidGridAdapter getNewDatesGridAdapter(int month, int year) {
		// TODO Auto-generated method stub
		return new CalendarCustomAdapter(getActivity(), month, year, getCaldroidData(), extraData);
	}

}
