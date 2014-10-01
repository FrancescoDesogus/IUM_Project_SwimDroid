package com.example.swimdroid;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

/**
 * Fragment che gestisce la lista vuota degli esercizi
 * 
 * @author Gabriele
 *
 */
public class EmptyExercisesList extends Fragment{
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//Inflato la  view
    	View v = inflater.inflate(R.layout.new_exercises_list_empty, container, false);
    	
    	//Creo il listener per il bottone che occupa tutto lo schermo
    	v.findViewById(R.id.addExerciseButtonLarge).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				NewExerciseFragment frag = new NewExerciseFragment(); //creo il frammento
				FragmentTransaction ft = getFragmentManager().beginTransaction(); //avvio la transizione
				//setto le animazioni
				ft.setCustomAnimations(R.anim.slide_right_in, R.anim.slide_left_out, R.anim.slide_left_in, R.anim.slide_right_out);
				//e rimpiazzo il container
				ft.replace(R.id.exercises_fragment_container, frag, "NewExerciseFragment").addToBackStack("NewExerciseFragment").commit();
			}
		});
    	
		return v;
	}
	
	/**
	 * Menu delle opzioni (Action bar)
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    inflater.inflate(R.menu.new_exercises_list_empty_menu, menu); //inflato il menu
	    
	    getActivity().getActionBar().setTitle("Aggiunta Esercizi"); //setto il titolo appropriato
	    //setto i vari flag per permettere la navigazione
		getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		getActivity().getActionBar().setHomeButtonEnabled(true);
		getActivity().getActionBar().setDisplayUseLogoEnabled(false);
	    super.onCreateOptionsMenu(menu,inflater);
	}
	    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setHasOptionsMenu(true); //setto il flag dell'options menu a true
		setRetainInstance(true); //questo serve per evitare di ricreare il fragment alla rotazione dello schermo
		super.onCreate(savedInstanceState);
	}
	    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		FragmentManager fm= getActivity().getSupportFragmentManager();
		
		switch (item.getItemId()) {
		
		//se viene scelto di inserire un nuovo esercizio
		    case R.id.new_exercise_action:
		    	//Creo il fragment, setto le animazioni e rimpiazzo il container
		    	NewExerciseFragment frag = new NewExerciseFragment();
				FragmentTransaction ft = fm.beginTransaction();
				ft.setCustomAnimations(R.anim.slide_right_in, R.anim.slide_left_out, R.anim.slide_left_in, R.anim.slide_right_out);
				ft.replace(R.id.exercises_fragment_container, frag, "NewExerciseFragment").addToBackStack("NewExerciseFragment").commit();
		    	return true;
		    	
			 default:
				return super.onOptionsItemSelected(item);
	    }
	}

}
