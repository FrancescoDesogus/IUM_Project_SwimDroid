package com.example.swimdroid;

import java.util.Calendar;

import com.android.datetimepicker.time.RadialPickerLayout;
import com.android.datetimepicker.time.TimePickerDialog;
import com.example.model.Esercizio;
import com.example.swimdroid.R;

import android.app.ActionBar;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
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
 * Classe che gestisce il fragment per la creazione di un nuovo esercizio
 * 
 * @author Gabriele
 *
 */
public class NewExerciseFragment extends Fragment {

    public static final String TIMEPICKER_TAG = "timepicker"; //tag per il timepicker
    private Button timeButton;	//bottone per l'inserimento del tempo
    private String selectedTime;	//stringa che salva il tempo selezionato
    private Spinner style1;	//spinner per selezionare lo stile1
    private String selectedStyle1; //Stringa per salvare lo stile1 selezionato
    private Spinner style2; //spinner per selezionare lo stile2
    private String selectedStyle2; //Stringa per salvare lo stile2 selezionato
    private Spinner andatureSpinner;	//spinner per selezionare l'andatura
    private String selectedAndatura; //Stringa per salvare l'andatura selezionata
    
    private NumberPicker mNumberPicker;
    
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		//Setto il menu per il fragment ed evito che si ricrei alla rotazione
		setHasOptionsMenu(true);
		setRetainInstance(true);
		
		//modifico la actionBar
		ActionBar ab = getActivity().getActionBar();
		ab.setTitle("Nuovo Esercizio");
        ab.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);
        ab.setDisplayUseLogoEnabled(false);
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View v = inflater.inflate(R.layout.new_exercise_fragment, container, false); //inflato la view
    	
    	mNumberPicker = new NumberPicker(getActivity(), v.findViewById(R.id.newExContainer));
    	
    	//prelevo l'array di valori salvati e lo spinner
    	ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(getActivity().getBaseContext(), R.array.stiliSpinner1, android.R.layout.simple_spinner_item);
    	style1 = (Spinner) v.findViewById(R.id.spinnerStyle1);
    	
    	//setto il layout per il menu
    	adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	style1.setAdapter(adapter1); //e setto l'adapter
    	this.selectedStyle1 = (String) adapter1.getItem(0); //setto il tempo inizialmente selezionato
    	
    	//setto il listener
    	style1.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
				selectedStyle1 = parent.getItemAtPosition(pos).toString();
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
    	this.selectedStyle2 = (String) adapter2.getItem(0);
    	
    	style2.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
				selectedStyle2 = parent.getItemAtPosition(pos).toString();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
    		
		});
    	
    	//Come per gli altri spinner
    	ArrayAdapter<CharSequence> andature = ArrayAdapter.createFromResource(getActivity().getBaseContext(), R.array.Andature, android.R.layout.simple_spinner_item);
    	andatureSpinner = (Spinner) v.findViewById(R.id.andatureSpinner);
    	
    	andature.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	andatureSpinner.setAdapter(andature);
    	this.selectedAndatura = (String) andature.getItem(0);
    	
    	andatureSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
				selectedAndatura = parent.getItemAtPosition(pos).toString();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
    		
		});
		
    	//prelevo il bottone e ne setto il tempo iniziale
		timeButton = (Button) v.findViewById(R.id.PercorrenzaTimePicker);
		timeButton.setText("01'00\"");	
		this.selectedTime = "01-00"; //setto il tempo inizialmente selezionato
		
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
        inflater.inflate(R.menu.new_exercise_menu, menu);
        
		ActionBar ab = getActivity().getActionBar();
		ab.setTitle("Nuovo Esercizio");
        ab.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);
        ab.setDisplayUseLogoEnabled(false);
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		 switch (item.getItemId()) {
		 
		 	case R.id.saveExercise:
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
		 		
		 		((NewExercisesListActivity) getActivity()).getExercisesList().add(newEx);	

		    	FragmentManager fm= getActivity().getSupportFragmentManager();
		 		fm.popBackStack();
				getActivity().getActionBar().setTitle("Aggiunta Esercizi");
				getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
				getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
				getActivity().getActionBar().setHomeButtonEnabled(true);
				getActivity().getActionBar().setDisplayUseLogoEnabled(false);
				
				((NewExercisesListActivity) getActivity()).checkChange();
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
