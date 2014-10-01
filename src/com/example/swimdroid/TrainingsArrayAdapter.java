package com.example.swimdroid;

import java.util.ArrayList;
import java.util.Scanner;

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
 * Adattatore per mostrare come view custom le entry di una ListView (in questo caso, contenente dei Training)
 * 
 * @author Francesco
 *
 */
public class TrainingsArrayAdapter extends ArrayAdapter<Training> 
{
	private ArrayList<Training> mTrainings; //Array contenente gli allenamenti da mostrare

	
	public TrainingsArrayAdapter(Context context, int layoutResourceId, ArrayList<Training> trainings) 
	{
		super(context, layoutResourceId, trainings);
				
		//Recupero l'array contenente gli allenamenti da mostrare
		mTrainings = trainings;
	}

	/**
	 * Classe statica usata nel metodo getView per implementare il ViewHolder pattern, che migliora l'efficienza
	 * della lista
	 * 
	 * @author Tiffano
	 *
	 */
	static class ViewHolder 
	{
	    TextView trainingName;
	    TextView trainingGroup;
	    TextView trainingHour;
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
			convertView = inflater.inflate(R.layout.training_list_item, null);
			
			/**
			 * Visto che la view della riga è stata appena creata, salvo i riferimenti ai suoi contenuti (che dovrò 
			 * editare dopo con i valori che servono) in modo da non dover chiamare ogni volta che capita questa riga
			 * findViewById, che appesantisce il caricamento. 
			 * Per farlo, uso il ViewHolder pattern, cioè semplicemente una classe che contiene solo gli elementi del layout
			 * della riga.
			 */
			viewHolder = new ViewHolder();
            viewHolder.trainingName = (TextView) convertView.findViewById(R.id.trainingName);
            viewHolder.trainingGroup = (TextView) convertView.findViewById(R.id.trainingGroup);
            viewHolder.trainingHour = (TextView) convertView.findViewById(R.id.trainingHour);
            
            //Creato il viewHolder, lo inserisco nella view per recuperarlo dopo
            convertView.setTag(viewHolder);
		}
		//Se la view non è null, vuol dire che la riga era già stata creata; recupero quindi il viewHolder ad essa associato
		else
			viewHolder = (ViewHolder) convertView.getTag();

		//Recupero l'item corrente della lista che deve essere visualizzato
		Training currentTraining = mTrainings.get(currentPosition);

		//Per sicurezza, controllo che non sia null
		if(currentTraining != null) 
		{
			//Adesso modifico i contenuti del viewHolder, che sono quelli della riga correntemente usata
			viewHolder.trainingName.setText(currentTraining.getNome());
			viewHolder.trainingGroup.setText("Gruppo " + currentTraining.getId_gruppo());
			
			
			/* Per l'ora, essendo salvata nel db nella forma 00:00, e dato che se l'ora o i minuti sono minori di 10
			 * viene messo solo uno 0 e non due (cioè ad esempio 07:05 sarebbe 7:5), controllo i valori con degli scanner
			 * e nel caso aggiungo gli 0 dove opportuno */
			Scanner scannerStartHour = new Scanner(currentTraining.getOra_inizio()).useDelimiter(":");
			Scanner scannerEndHour = new Scanner(currentTraining.getOra_fine()).useDelimiter(":");

			String startHour = scannerStartHour.next();
			String endHour = scannerEndHour.next();
			String startMinutes = scannerStartHour.next();
			String endMinutes = scannerEndHour.next();
			
			scannerStartHour.close();
			scannerEndHour.close();
			
			if(Integer.valueOf(startHour) < 10)
				startHour = "0" + startHour;
			
			if(Integer.valueOf(endHour) < 10)
				endHour = "0" + endHour;
			
			if(Integer.valueOf(startMinutes) < 10)
				startMinutes = "0" + startMinutes;
			
			if(Integer.valueOf(endMinutes) < 10)
				endMinutes = "0" + endMinutes;
			
			viewHolder.trainingHour.setText(startHour + ":" + startMinutes + "-" + endHour + ":" + endMinutes);
		}

		
		//Ritorno la nuova view dell'entry della lista all'activity (o meglio, al frammento)
		return convertView;
	}

}
