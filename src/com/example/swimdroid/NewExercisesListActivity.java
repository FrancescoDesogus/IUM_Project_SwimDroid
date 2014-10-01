package com.example.swimdroid;

import java.util.ArrayList;

import com.example.model.Esercizio;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * Activity per la gestione dei nuovi esercizi, conterrà due fragment
 *  - EmptyExercisesList -> Fragment per la lista di esercizi vuota
 *  - FullExercisesList -> Fragment per lista di esercizi non vuota
 *  
 * @author Gabriele
 *
 */
public class NewExercisesListActivity extends FragmentActivity {
	
	//lista degli esercizi, dovrà essere riempita con gli elementi presi dal db
	private ArrayList<Esercizio> listaEsercizi;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//setto la view, che in questo caso conterrà solo un frameLayout per contenere i fragments
		setContentView(R.layout.activity_new_exercises_list);
		
		if(savedInstanceState == null){
			listaEsercizi = getIntent().getParcelableArrayListExtra("listaEsercizi");
			
			if(listaEsercizi == null)
				listaEsercizi = new ArrayList<Esercizio>();

			//setto il titolo della actionBar, la rendo attiva, e la mostro cliccabile
			if(listaEsercizi != null && listaEsercizi.size() > 0)
		 		getActionBar().setTitle("Modifica Esercizi");
		 	else getActionBar().setTitle("Modifica Esercizi");
			
			getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			getActionBar().setDisplayHomeAsUpEnabled(true);
			getActionBar().setHomeButtonEnabled(true);
			getActionBar().setDisplayUseLogoEnabled(false);
			getActionBar().setIcon(android.R.color.transparent);	
		}
		else { //se invece sto ruotando lo schermo, ripristino la lista
			listaEsercizi = savedInstanceState.getParcelableArrayList("listaEsercizi");
		}
		
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		
		//se non ci sono elementi
		if(listaEsercizi.size() < 1){		
			
			 if(savedInstanceState != null)
                return;
			 
			 //Creo il nuovo fragment e lo aggiungo (Occhio, non lo sto rimpiazzando, infatti ancora non ce ne sono)
			 //IMPORTANTE, non sto facendo addToBackStack, in quanto questo è il primo fragment
			 //se l'avessi fatto, quando premevo indietro, durante l'animazione avrei visto il fragment scomparire
			 //questo perchè quando chiudi l'activity, la prima cosa che fa è togliere tutti i fragment dallo stack
			EmptyExercisesList frag = new EmptyExercisesList();
			ft.add(R.id.exercises_fragment_container, frag).commit();

		}
		else{
			
			 if(savedInstanceState != null)
                return;
			 
			 //come sopra ma per la lista degli esercizi non vuota
			FullExercisesList frag = new FullExercisesList();
			ft.add(R.id.exercises_fragment_container, frag).commit();
		}
	}
	
	
	/**
	 * Funzione per gestire la pressione degli item dalla actionBar
	 * MOLTO IMPORTANTE: Quando si gestisce la pressione degli item tra activities e fragment
	 * è importante sapere, che android controlla prima di tutto il metodo onOptionsItemSelected dell'activity
	 * e controlla se viene gestito l'id dell'item premuto, se viene gestito in quel metodo gli altri non li chiama.
	 * Pre esempio, se premo indietro da NewExerciseFragment, dato che il tasto indietro è identificato sempre come Home
	 * chiudeva sempre l'activity, io invece volevo togliesse solo il fragment senza chiudere l'activity.
	 * Per gestire questo, ho inserito qui il controllo della dimensione dello stack dei fragment
	 * se ce n'è almeno 1, toglie quello e basta, se non ce ne sono, fa la solita azione dell'actvitiy, in questo caso la chiude	 * 
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		switch (item.getItemId()) {
	    //Se viene premuto il bottone per tornare indietro avvio l'onBackPressed
		    case android.R.id.home:
		    	onBackPressed();	
		    	return true;
		    	
			 default:
				return super.onOptionsItemSelected(item);
	    }
	}
	
	/**
	 * Gestione della pressione del tasto indietro
	 */
	@Override
	public void onBackPressed() 
	{
		//prelevo il fragment manager
    	FragmentManager fm= getSupportFragmentManager();
    	
    	//se ci sono fragment nello stack
		if(fm.getBackStackEntryCount()>0){
			//rimuovo il fragment in cima allo stack
		 	fm.popBackStack();		 	
		 	
		 	//setto la actionBar
			getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			getActionBar().setDisplayHomeAsUpEnabled(true);
			getActionBar().setHomeButtonEnabled(true);
			getActionBar().setDisplayUseLogoEnabled(false);	
		}
		else {
			//se non ci sono elementi nello stack
			Intent returnIntent = new Intent(); //creo l'intent
	    	returnIntent.putParcelableArrayListExtra("result", listaEsercizi); //metto l'array degli esercizi come extra
	    	setResult(this.RESULT_OK,returnIntent); //setto il codice del risultato
	    	finish(); //termino l'activity
	    	overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out); //avvio la transizione
			super.onBackPressed();
		}
		
	}
	
	public ArrayList<Esercizio> getExercisesList(){
		return this.listaEsercizi;
	}
	
	public void checkChange(){
		if(listaEsercizi.size() < 1){		
			
			 //Creo il nuovo fragment e lo aggiungo (Occhio, non lo sto rimpiazzando, infatti ancora non ce ne sono)
			 //IMPORTANTE, non sto facendo addToBackStack, in quanto questo è il primo fragment
			 //se l'avessi fatto, quando premevo indietro, durante l'animazione avrei visto il fragment scomparire
			 //questo perchè quando chiudi l'activity, la prima cosa che fa è togliere tutti i fragment dallo stack
			EmptyExercisesList frag = new EmptyExercisesList();
			getSupportFragmentManager().beginTransaction().replace(R.id.exercises_fragment_container, frag).commit();

		}
		else{
			 
			 //come sopra ma per la lista degli esercizi non vuota
			FullExercisesList frag = new FullExercisesList();
			getSupportFragmentManager().beginTransaction().replace(R.id.exercises_fragment_container, frag).commit();
		}		
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		//se viene rotato lo schermo, mi salvo la lista degli esercizi
		outState.putParcelableArrayList("listaEsercizi", listaEsercizi);
		super.onSaveInstanceState(outState);
	}

	
}
