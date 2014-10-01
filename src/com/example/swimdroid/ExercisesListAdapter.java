package com.example.swimdroid;

import java.util.ArrayList;
import java.util.Scanner;

import com.example.model.Esercizio;
import com.example.model.Training;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Adattatore per mostrare gli esercizi nella ListView
 * 
 * @author Gabriele
 *
 */
public class ExercisesListAdapter extends ArrayAdapter<Esercizio> 
{
	private ArrayList<Esercizio> mExercises; //Array contenente gli esercizi da mostrare

	
	public ExercisesListAdapter(Context context, int layoutResourceId, ArrayList<Esercizio> exercises) 
	{
		super(context, layoutResourceId, exercises);
				
		//Recupero l'array contenente gli esercizi
		mExercises = exercises;
	}

	/**
	 * CLasse statica usata nel metodo getView per implementare il ViewHolder pattern, che migliora l'efficienza
	 * della lista
	 * 
	 * @author Gabriele
	 *
	 */
	static class ViewHolder 
	{
	    TextView ripxDistView;
	    TextView ripxDistValue;
	    TextView stileView;
	    TextView stileValue;
	    TextView andaturaView;
	    TextView andaturaValue;
	    TextView tempoView;
	    TextView tempoValue;
	}
	
	/**
	 * Questo metodo stabilisce il layout di ogni entry della lista. Qua bisogna specificare cosa deve essere mostrato,
	 * editando appositamente gli elementi contenuti nel layout di ogni riga della lista.
	 * 
	 * @currentPosition la posizione corrente all'interno dell'array degli oggetti da visualizzare nella lista
	 * @convertView  la view della riga (essenzialmente un'altra riga non visibile che è stata riciclata), che verrà modificata opportunamente
	 * @parent la ViewGroup dove sta la rowView
	 */
	@Override
	public View getView(int currentPosition, View convertView, ViewGroup parent)
	{		
		ViewHolder viewHolder = null;
				
		//Controllo se la view è null; se lo è, bisogna inflatarla, altrimenti si può già usare direttamente
		if(convertView == null)
		{
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			//Inflato la view con il layout di una singola riga della lista
			convertView = inflater.inflate(R.layout.exercises_list_item, null);
			
			/**
			 * Visto che la view della riga è stata appena creata, salvo i riferimenti ai suoi contenuti (che dovrò 
			 * editare dopo con i valori che servono) in modo da non dover chiamare ogni volta che capita questa riga
			 * findViewById, che appesantisce il caricamento. 
			 * Per farlo, uso il ViewHolder pattern, cioè semplicemente una classe che contiene solo gli elementi del layout
			 * della riga.
			 */
			viewHolder = new ViewHolder();
            viewHolder.ripxDistView = (TextView) convertView.findViewById(R.id.RipxDistView);
            viewHolder.ripxDistValue = (TextView) convertView.findViewById(R.id.RipxDistValue);
            viewHolder.stileView = (TextView) convertView.findViewById(R.id.StileView);
            viewHolder.stileValue = (TextView) convertView.findViewById(R.id.StileValue);
            viewHolder.andaturaView = (TextView) convertView.findViewById(R.id.AndaturaView);
            viewHolder.andaturaValue = (TextView) convertView.findViewById(R.id.AndaturaValue);
            viewHolder.tempoView = (TextView) convertView.findViewById(R.id.TempoView);
            viewHolder.tempoValue = (TextView) convertView.findViewById(R.id.TempoValue);
            
            //Creato il viewHolder, lo inserisco nella view per recuperarlo dopo
            convertView.setTag(viewHolder);
		}
		//Se la view non è null, vuol dire che la riga era già stata creata; recupero quindi il viewHolder ad essa associato
		else
			viewHolder = (ViewHolder) convertView.getTag();

		//Recupero l'item corrente della lista che deve essere visualizzato
		Esercizio currentExercise = mExercises.get(currentPosition);

		//Per sicurezza, controllo che non sia null
		if(currentExercise != null) 
		{
			//Adesso modifico i contenuti del viewHolder, che sono quelli della riga correntemente usata
			  viewHolder.ripxDistView.setText("Ripetizioni x Distanza");
	          viewHolder.ripxDistValue.setText(currentExercise.getRipetizioni() +" x " +currentExercise.getDistanza() +"m");
	          viewHolder.stileView.setText("Stile");
	          viewHolder.stileValue.setText(currentExercise.getStile1() +" - " +currentExercise.getStile2());
	          viewHolder.andaturaView.setText("Andatura");
	          viewHolder.andaturaValue.setText(currentExercise.getAndatura());
	          viewHolder.tempoView.setText("Tempo di Percorrenza");
	          
		      //Devo trasformare il tempo salvato nell'esercizio nella forma 00-00 in 00'00'', in modo da mostrarlo;
		      //uso quindi uno scanner per separare i minuti dai secondi
		  	  Scanner timeScanner = new Scanner(currentExercise.getTempo()).useDelimiter("-");
		      	
		      String minutes = timeScanner.next();
		  	  String seconds = timeScanner.next();
		  		
		  	  timeScanner.close();
	          
	          viewHolder.tempoValue.setText(minutes + "'" + seconds + "\"");
		}

		
		//Ritorno la nuova view dell'entry della lista all'activity (o meglio, al frammento)
		return convertView;
	}

}