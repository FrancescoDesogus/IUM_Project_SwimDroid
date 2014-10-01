package com.example.swimdroid;

import java.util.ArrayList;
import java.util.Scanner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.model.Atleta;
import com.example.model.Training;
import com.example.swimdroid.TrainingsArrayAdapter.ViewHolder;

/**
 * Adattatore per la lista degli atleti
 * 
 * @author Francesco
 *
 */
public class AthletesArrayAdapter extends ArrayAdapter<Atleta> 
{
	private ArrayList<Atleta> mAthletes; //Array contenente gli atleti da mostrare

	
	public AthletesArrayAdapter(Context context, int layoutResourceId, ArrayList<Atleta> athletes) 
	{
		super(context, layoutResourceId, athletes);
				
		//Recupero l'array contenente gli atleti da mostrare
		mAthletes = athletes;
	}

	/**
	 * CLasse statica usata nel metodo getView per implementare il ViewHolder pattern, che migliora l'efficienza
	 * della lista
	 * 
	 * @author Tiffano
	 *
	 */
	static class ViewHolder 
	{
	    TextView athleteName;
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
			convertView = inflater.inflate(R.layout.athletes_list_item, null);
			
			/**
			 * Visto che la view della riga è stata appena creata, salvo i riferimenti ai suoi contenuti (che dovrò 
			 * editare dopo con i valori che servono) in modo da non dover chiamare ogni volta che capita questa riga
			 * findViewById, che appesantisce il caricamento. 
			 * Per farlo, uso il ViewHolder pattern, cioè semplicemente una classe che contiene solo gli elementi del layout
			 * della riga.
			 */
			viewHolder = new ViewHolder();
            viewHolder.athleteName = (TextView) convertView.findViewById(R.id.athleteName);
            
            //Creato il viewHolder, lo inserisco nella view per recuperarlo dopo
            convertView.setTag(viewHolder);
		}
		//Se la view non è null, vuol dire che la riga era già stata creata; recupero quindi il viewHolder ad essa associato
		else
			viewHolder = (ViewHolder) convertView.getTag();

		//Recupero l'item corrente della lista che deve essere visualizzato
		Atleta currentAthlete = mAthletes.get(currentPosition);

		//Per sicurezza, controllo che non sia null
		if(currentAthlete != null) 
		{
			//Adesso modifico i contenuti del viewHolder, che sono quelli della riga correntemente usata
			viewHolder.athleteName.setText(currentAthlete.getNome() + " " + currentAthlete.getCognome());
		}

		
		//Ritorno la nuova view dell'entry della lista all'activity (o meglio, al frammento)
		return convertView;
	}

}

