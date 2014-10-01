package com.example.swimdroid;

import java.util.Calendar;

import com.android.datetimepicker.time.RadialPickerLayout;
import com.android.datetimepicker.time.TimePickerDialog;
import com.example.model.Esercizio;
import com.example.swimdroid.R;

import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
/**
 * Classe che gestisce il fragment per la modifica degli esercizi * 
 * 
 * @author Gabriele
 *
 */

public class ModifyExerciseFragment extends Fragment {

    public static final String TIMEPICKER_TAG = "timepicker"; //Nome del timepicker
    private TimePickerDialog timePicker = null;	//dialog del timepicker inizialmente null
    private Button timeButton;	//Bottone per settare il tempo di percorrenza
    private String selectedTime;	//Tempo selezionato in formato stringa, per il text del bottone
    private Spinner style1;		//Spinner per selezionare lo stile principale
    private String selectedStyle1;	//Stringa per salvare il nome dello stile principale
    private Spinner style2;	//Spinner per selezionare lo stile secondario
    private String selectedStyle2;	//Stringa per salvare il nome dello stile secondario
    private Spinner andatureSpinner;	//Spinner per selezionare l'andatura
    private String selectedAndatura;	//Stringa per salvare l'andatura selezionata
    private Esercizio toModify;	//Esercizio da modificare
    private int posizioneLista;	//Posizione dell'esercizio nell'array di esercizi    

    private NumberPicker mNumberPicker;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		Bundle args = getArguments();	//Prelevo gli argometni
		
		//Se ci sono argomenti e c'è la posizione nella lista, ne prelevo il valore
		if (args != null && args.containsKey("posizioneLista"))
			posizioneLista = args.getInt("posizioneLista");
		
		//prelevo l'esercizio da modificare
		toModify = ((NewExercisesListActivity) getActivity()).getExercisesList().get(posizioneLista);
		
		//Soliti flag per il fragment
		setHasOptionsMenu(true);
		setRetainInstance(true);
		
		//E solite modifiche per la action bar
		ActionBar ab = getActivity().getActionBar();
		ab.setTitle("Modifica Esercizio");
        ab.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);
        ab.setDisplayUseLogoEnabled(false);
        
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View v = inflater.inflate(R.layout.new_exercise_fragment, container, false);
    	
    	mNumberPicker = new NumberPicker(getActivity(), v.findViewById(R.id.newExContainer));
    	mNumberPicker.setDistText(toModify.getDistanza());
    	mNumberPicker.setRipText(toModify.getRipetizioni());
    	
    	//prelevo l'array di valori, dall'array salvato in string.xml
    	ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(getActivity().getBaseContext(), R.array.stiliSpinner1, android.R.layout.simple_spinner_item);
    	style1 = (Spinner) v.findViewById(R.id.spinnerStyle1);    	
    	
    	adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);//Specifico il layout per l'adapter    	
    	style1.setAdapter(adapter1);//Applico l'adapter allo spinner
    	this.selectedStyle1 = toModify.getStile1();  //setto il valore dello stile1 selezionato a quello precedentemente salvato
    	style1.setSelection(adapter1.getPosition(toModify.getStile1())); //Setto la selezione all'elemento già presente
    	
    	//setto il listener per lo spinner
    	style1.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
				selectedStyle1 = parent.getItemAtPosition(pos).toString(); //prelevo la stringa e la salvo come stile1
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
    		
		});
    	
    	//Come per il precedente spinner
    	ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(getActivity().getBaseContext(), R.array.stiliSpinner2, android.R.layout.simple_spinner_item);
    	style2 = (Spinner) v.findViewById(R.id.spinnerStyle2);
    	
    	adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	style2.setAdapter(adapter2);
    	this.selectedStyle2 = toModify.getStile2();
    	style2.setSelection(adapter2.getPosition(toModify.getStile2()));
    	
    	style2.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
				selectedStyle2 = parent.getItemAtPosition(pos).toString();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
    		
		});
    	
    	//Uguale agli altri spinner
    	ArrayAdapter<CharSequence> andature = ArrayAdapter.createFromResource(getActivity().getBaseContext(), R.array.Andature, android.R.layout.simple_spinner_item);
    	andatureSpinner = (Spinner) v.findViewById(R.id.andatureSpinner);
    	
    	andature.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	andatureSpinner.setAdapter(andature);
    	this.selectedAndatura = toModify.getAndatura();
    	andatureSpinner.setSelection(andature.getPosition(toModify.getAndatura()));
    	
    	andatureSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
				selectedAndatura = parent.getItemAtPosition(pos).toString();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
    		
		});    	
		
    	//prelevo il bottone per settare il tempo
		timeButton = (Button) v.findViewById(R.id.PercorrenzaTimePicker);
		timeButton.setText(toModify.getTempo().substring(0,2) + "'" +toModify.getTempo().substring(3,5) +"\""); //setto il tempo come testo iniziale
		this.selectedTime = toModify.getTempo(); //e setto il tempo selezionato a quello precedentemente salvato
		
		timeButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//Creo il timepicker col number pad passando come parametro il bottone che causerà l'apertura della
            	//dialog, in modo tale che una volta settato il tempo il testo del bottone venga direttamente cambiato
            	TimePickerPad timePicker = new TimePickerPad(getActivity(), timeButton);
        		
            	//Creo la dialog vera e propria
        		timePicker.createTimePickerDialog();
        		
        		//Setto un listener per recuperare il nuovo tempo una volta che l'utente ha confermato
        		timePicker.setOnTimeSetListener(new OnTimeSetListener()
        		{
        			@Override
        			public void onTimeSet(int firstDigit, int secondDigit, int thirdDigit, int fourthDigit) 
        			{
        				selectedTime = firstDigit + "" + secondDigit + "-" + thirdDigit + "" + fourthDigit;
        			}
        			
        		});
        		
        		//Infine mostro la dialog del timepicker
        		timePicker.show();
			}			
		});
    	
		return v;
	}
	
	/**
	 * IMPORTANTE: per evitare che la actionBar si resetti quando si ruota lo schermo nel caso di un fragment
	 * è molto importante mettere qui dentro tutto ciò che riguarda la actionBar, e mettere un if(savedInstanceState == Null) 
	 * nell'activity, dentro cui ci sarà la roba relativa alla ctionBar dell'activity
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.modify_exercise_menu, menu);
        

		ActionBar ab = getActivity().getActionBar();
		ab.setTitle("Modifica Esercizio");
        ab.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);
        ab.setDisplayUseLogoEnabled(false);
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		//se viene selezionato qualcosa dalla action bar
    	FragmentManager fm= getActivity().getSupportFragmentManager();
		 switch (item.getItemId()) {
		 
		 	//se viene premuta l'icona per salvare le modifiche
		 	case R.id.saveModify:
		 		
		 		//Creo un nuovo oggetto Esercizio e lo inizializzo con i valori inseriti
		 		Esercizio newEx = new Esercizio();		 	
		 		
		 		if(mNumberPicker.getRip() < 1){
		 			Toast.makeText(getActivity(), "Il numero di ripetizioni dev'essere almeno 1", Toast.LENGTH_SHORT).show();
		 			return true;
		 		}
		 		
		 		if(mNumberPicker.getDist() < 1){
		 			Toast.makeText(getActivity(), "La distanza dev'essere di almeno 25m", Toast.LENGTH_SHORT).show();
		 			return true;
		 		}
		 			
		 		newEx.setRipetizioni(mNumberPicker.getRip());
		 		newEx.setDistanza(mNumberPicker.getDist());
		 		newEx.setStile1(this.selectedStyle1);
		 		newEx.setStile2(this.selectedStyle2);
		 		newEx.setAndatura(this.selectedAndatura);
		 		newEx.setTempo(this.selectedTime);
		 		
		 		//prendo la lista degli esercizi dall'activity e modifico il valore dell'esercizio, indicandone la posizion
		 		((NewExercisesListActivity) getActivity()).getExercisesList().set(posizioneLista, newEx);	
		 		
		 		//rimuovo il fragment dallo stack
		 		fm.popBackStack();
		 		
		 		//e cambio la actionBar
				getActivity().getActionBar().setTitle("Modifica Esercizi");
				getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
				getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
				getActivity().getActionBar().setHomeButtonEnabled(true);
				getActivity().getActionBar().setDisplayUseLogoEnabled(false);
				
				((NewExercisesListActivity) getActivity()).checkChange(); //controllo nell'activity se ci sono stati cambiamenti
				Toast.makeText(getActivity().getBaseContext(), "Modifiche Salvate", Toast.LENGTH_SHORT).show(); //e avverto che le modifiche sono state salvate
		 		return true;
		 		
	 		//se viene premuta l'icona per cancellare un esercizio
		 	case R.id.deleteExercise:
		 		
		 		//prelevo l'array dall'activity e ne rimuovo l'elemento
		 		((NewExercisesListActivity) getActivity()).getExercisesList().remove(posizioneLista);	
		 		
		 		//rimuovo il fragment dallo stack
		 		fm.popBackStack();
		 		
		 		//e cambio la actionBar
				getActivity().getActionBar().setTitle("Modifica Esercizi");
				getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
				getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
				getActivity().getActionBar().setHomeButtonEnabled(true);
				getActivity().getActionBar().setDisplayUseLogoEnabled(false);
				
				((NewExercisesListActivity) getActivity()).checkChange(); //controllo se ci sono cambiamenti nella lista
				Toast.makeText(getActivity().getBaseContext(), "Esercizio rimosso", Toast.LENGTH_SHORT).show(); //E avverto l'utente
				return true;
		 		
	        default: return true;
		 }
	}
	
	@Override
	public void onDestroyView() {
		mNumberPicker.dismissPopups();
		super.onDestroyView();
	}

}