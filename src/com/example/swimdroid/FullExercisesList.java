package com.example.swimdroid;

import java.util.ArrayList;

import com.example.model.Esercizio;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/**
 * Classe per gestire la lista degli esercizi non vuota
 * 
 * @author Gabriele
 *
 */
public class FullExercisesList extends ListFragment{	
	
	private ArrayList<Esercizio> listaEsercizi; //lista contenente gli esercizi appena creati
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setHasOptionsMenu(true); //setto il flag delle opzioni a true
		setRetainInstance(true); //questo serve per non ricreare il fragment alla rotazione dello schermo
		
		//setto l'adapter della lista
	    setListAdapter(new ExercisesListAdapter(getActivity().getBaseContext(), R.layout.training_list_item, ((NewExercisesListActivity) getActivity()).getExercisesList()));
	    super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View v = inflater.inflate(R.layout.new_exercises_list_full, container, false);
		return v;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		
		//prelevo la lista degli esercizi per vedere poi se c'è qualcosa o meno dentro
		listaEsercizi = ((NewExercisesListActivity) getActivity()).getExercisesList();
        inflater.inflate(R.menu.new_exercises_list_full_menu, menu);
        
        //se c'è qualcosa setto il titolo a "Modifica", se non c'è nulla
        //lo setto ad "Aggiunta"
        if(listaEsercizi != null && listaEsercizi.size() > 0)
        	getActivity().getActionBar().setTitle("Modifica Esercizi");
        else getActivity().getActionBar().setTitle("Aggiunta Esercizi");
        
		getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		getActivity().getActionBar().setHomeButtonEnabled(true);
		getActivity().getActionBar().setDisplayUseLogoEnabled(false);
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		FragmentManager fm= getActivity().getSupportFragmentManager();
		
		switch (item.getItemId()) {
		
		//Se viene scelto di aggiungere un esercizio, creo il fragment per la creazione di un nuovo esercizio
		    case R.id.addExercise:
		    	NewExerciseFragment frag = new NewExerciseFragment();
				FragmentTransaction ft = fm.beginTransaction();
				ft.setCustomAnimations(R.anim.slide_right_in, R.anim.slide_left_out, R.anim.slide_left_in, R.anim.slide_right_out);
				ft.replace(R.id.exercises_fragment_container, frag, "NewExerciseFragment").addToBackStack("NewExerciseFragment").commit();
		    	return true;
		    	
			 default:
				return super.onOptionsItemSelected(item);
	    }
	}
	
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		
		//se viene cliccato un item della lista
		FragmentManager fm= getActivity().getSupportFragmentManager();

		//creo il fragment per gestire la modifica di un esercizio
		ModifyExerciseFragment frag = new ModifyExerciseFragment();
		FragmentTransaction ft = fm.beginTransaction();
		
		//Creo un bundle che conterrà la posizione ddell'esercizio nella lista, e lo passo come argomento al fragment
		//in questo modo potrò recuperarmi tutti i dati dell'eserciziodall'array
		Bundle posizione = new Bundle();
		posizione.putInt("posizioneLista", position);
		frag.setArguments(posizione);
		
		ft.setCustomAnimations(R.anim.slide_right_in, R.anim.slide_left_out, R.anim.slide_left_in, R.anim.slide_right_out);
		ft.replace(R.id.exercises_fragment_container, frag, "ModifyExerciseFragment").addToBackStack("ModifyExerciseFragment").commit();
		super.onListItemClick(l, v, position, id);
	}
}
