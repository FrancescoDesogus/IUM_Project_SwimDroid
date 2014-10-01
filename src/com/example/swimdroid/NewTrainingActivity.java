package com.example.swimdroid;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

import com.android.datetimepicker.time.RadialPickerLayout;
import com.android.datetimepicker.time.TimePickerDialog;
import com.example.model.Atleta;
import com.example.model.DBTrainingsData;
import com.example.model.Esercizio;
import com.example.model.Training;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public class NewTrainingActivity extends FragmentActivity implements TimePickerDialog.OnTimeSetListener {

    public static final String TIMEPICKER_TAG = "timepicker"; //tag per la dialog del timepicker
	private ArrayList<Esercizio> listaEsercizi;	//Array per la lista degli esercizi relativa all'allenamento
    private boolean startHourClicked; //flag per controllare quale timepicker è stato avviato
    private int startHour; //ora dell'ora iniziale selezionata
    private int startMinute; //minuti dell'ora iniziale selezionata
    private Button startHourButton; //botton per selezionare l'ora di inizio allenamento
    private boolean endHourClicked; //flag per controllare quale timepicker è stato avviato
    private int endHour; //minuti dell'ora finale selezionata
    private int endMinute; //minuti dell'ora finale selezionata
    private Button endHourButton; //botton per selezionare l'ora di fine allenamento
    private Button exercisesButton; //bottone per la scelta di un esercizio
    private Button addGroupButton; //bottone pre la selezione di un gruppo di lavoro
    private EditText trainingName; //editText per scrivere il nome dell'allenamento
    private TextView tv; //textView per settare il titolo del form, con la data del giorno
    private TimePickerDialog startTimePicker = null; //dialog del timepicker per l'ora di inizio
    private TimePickerDialog endTimePicker = null; //dialog del timepicker per l'ora di fine
	private DBTrainingsData mDb; //Riferimento al db
	private int groupSelected; //variabile per salvare l'ide del gruppo selezionato
	private int oldGroupSelcted; //variabile per salvare l'id del del gruppo vecchio, qualora fosse cambiato (usato nella modifica dell'allenamento)
	private ArrayList<String> gruppi; //array contenente la lista dei gruppi presenti nel db
	private boolean hasModify; //booleano per salvare se sono state effettuate modifiche
	private AlertDialog mAlertDialog; //dialog per confermare o meno le modifiche
	private long trainingId; //id dell'eventuale allenamento da modificare
	private Training toModify; //allenamento da modificare
	private int initialSize; //dimensione iniziale della lista degli esercizi
	private PopupWindow pwName;
	private PopupWindow pwGroups;
	private PopupWindow pwEdit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_training); //inflato la view		

		trainingId = getIntent().getLongExtra("trainingId", -1);
		
		//setto la actionBar in base alla modifica o all'aggiunta di un allenamento
		if(trainingId < 0)
			getActionBar().setTitle("Nuovo Allenamento");
		else getActionBar().setTitle("Modifica Allenamento");
		
		getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayUseLogoEnabled(false);	
		getActionBar().setIcon(android.R.color.transparent);

        //Creo il riferimento al db...
        mDb = new DBTrainingsData(this);
		gruppi = mDb.getGroups(); //prelevo i gruppi presenti nel db
		
		//qui mi prelevo tutti i widget del layout
		exercisesButton = (Button) findViewById(R.id.addExercisesButton);
		startHourButton = (Button) findViewById(R.id.startHourButton);
		endHourButton = (Button) findViewById(R.id.endHourButton);
		addGroupButton =(Button) findViewById(R.id.addGroupButton);
		tv = (TextView) findViewById(R.id.new_training_title);
		trainingName = (EditText) findViewById(R.id.trainingName);
			
        
		//se sto ricreando l'activity da zero
        if(savedInstanceState == null){        	
        	
        	//se l'id è maggiore di 0 e quindi c'è un allemaneto da modificare inizializzo tutto
        	if(trainingId > 0){
        		toModify = mDb.getTrainingFromId(trainingId); //prelevo l'esercizio da modificare
        		listaEsercizi = mDb.getExercisesInTraining(trainingId); //prelevo la lista dei suoi esercizi
        		
        		initialSize = listaEsercizi.size(); //mi salvo la dimensione iniziale
        		
        		//prelevo l'ora di inizio e quella di fine in formato long e ne salvo l'ora e il minuto
        		Scanner startTimeScanner = new Scanner(toModify.getOra_inizio()).useDelimiter(":");
        		startHour = Integer.parseInt(startTimeScanner.next());
        		startMinute = Integer.parseInt(startTimeScanner.next());
        		
        		Scanner endTimeScanner = new Scanner(toModify.getOra_fine()).useDelimiter(":");
        		endHour = Integer.parseInt(endTimeScanner.next());
        		endMinute = Integer.parseInt(endTimeScanner.next());
        		
        		groupSelected = toModify.getId_gruppo();//prelevo l'id del gruppo
        		oldGroupSelcted = groupSelected; //salvo l'id del gruppo attuale, qualora venisse cambiato in seguito
        		trainingName.setText(toModify.getNome()); //il nome dell'allanemanto
        		exercisesButton.setVisibility(View.GONE); //e nascondo il bottone
        		
        		//trasformo 250 da dp a px
        		DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        	    int px = Math.round(250 * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));  
        	    
        	    //setto i parametri e i margini per adattarlo al solo bottone del gruppo
        		LayoutParams params = new LayoutParams(px, LayoutParams.WRAP_CONTENT);
        		params.setMargins(0, 0, 0, 0);
        		addGroupButton.setLayoutParams(params);
        		addGroupButton.setGravity(Gravity.CENTER);        		
        	}
        	else {
	        	//creo la lista degli esercizi
		        listaEsercizi = new ArrayList<Esercizio>();        	
		        initialSize = 0;
		        
		        //inizializzo l'ora di inizio e di fine
				int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
				int minutes = Calendar.getInstance().get(Calendar.MINUTE);
				
				startHour = hour;
				startMinute = minutes;
				endHour = hour+2;
				endMinute = minutes;
				groupSelected = -1;
        	}			

			//infine inizializzo i flag e le variabili
			endHourClicked = false; 
			startHourClicked = false;
			hasModify = false;
        }
        else {
        	//se invece sto ricreando l'activity dopo la rotazione, reinizializzo tutte le variabili
        	listaEsercizi = savedInstanceState.getParcelableArrayList("listaEsercizi");
        	groupSelected = savedInstanceState.getInt("groupSelected");
        	
        	//prelevo l'id dell'eventuale allenamento da modificare
        	trainingId = savedInstanceState.getLong("trainingId");
        	
        	//se l'ide è > -1 c'è un allenamento da modificare
        	if(trainingId > -1){
        		//lo prelevo
        		toModify = mDb.getTrainingFromId(trainingId);
        		
        		//e nascondo il bottone
    			exercisesButton.setVisibility(View.GONE);        		
        		DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        	    int px = Math.round(250 * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));  
        		LayoutParams params = new LayoutParams(px, LayoutParams.WRAP_CONTENT);
        		params.setMargins(0, 0, 0, 0);
        		addGroupButton.setLayoutParams(params);
        		addGroupButton.setGravity(Gravity.CENTER);       
        	}
        	
        	initialSize = savedInstanceState.getInt("initialSize");
        	
        	if(groupSelected > -1)
        		addGroupButton.setText(gruppi.get(groupSelected-1));
        	
        	startHour = savedInstanceState.getInt("startHour");
        	startMinute = savedInstanceState.getInt("startMinute");
        	endHour = savedInstanceState.getInt("endHour");
        	endMinute = savedInstanceState.getInt("endMinute");
        	startHourButton.setText(String.format("%02d:%02d", startHour, startMinute));
        	endHourButton.setText(String.format("%02d:%02d", endHour, endMinute));
        	hasModify = savedInstanceState.getBoolean("hasModify");
        }
        
		
		//prelevo il giorno della settimana selezionato
		String day = getIntent().getStringExtra("dayString").toLowerCase();
		day = day.substring(0, 1).toUpperCase() + day.substring(1);
		
		//e il giorno dell'anno selezionato
		String yearDay = getIntent().getStringExtra("dataString");
		
		//e setto il tiolo
		tv.setText( day+", " +yearDay);		

		String startTime = String.format("%02d:%02d", startHour, startMinute);
		String endTime = String.format("%02d:%02d", endHour, endMinute);
		
		//Setto come testo il tempo attuale, come tempo di inizio e fine allenamento
		startHourButton.setText(startTime);
		endHourButton.setText(endTime);
		
		//se c'è un gruppo selezionato, setto la stringa per il bottone
		if(groupSelected > -1){
			ArrayAdapter<String> adap = new ArrayAdapter<String>(NewTrainingActivity.this, R.layout.group_list_item, R.id.ListItemText, gruppi);
			
			addGroupButton.setText(adap.getItem(groupSelected-1));
		}
        
        //modifico il bottone in base a quanti esercizi sono presenti nella lista degli esercizi
		if(listaEsercizi != null && listaEsercizi.size() > 0)
			exercisesButton.setText("Modifica Esercizi");
		else exercisesButton.setText("Aggiungi Esercizi");		
        
		trainingName.clearFocus(); //tolgo il focus dall'editText
		
		final Calendar calendar = Calendar.getInstance(); //prelevo il calendario
		// e instanzio la dialog del timepicker
		startTimePicker = TimePickerDialog.newInstance(this, startHour , startMinute, true, false);

		//setto i listener per la pressione del tasto dell'ora di inizio
		startHourButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				hasModify = true; //avverto che ci sono state modifiche
				startHourClicked = true; //che è stato cliccato il bottone per l'ora di inizio...
				endHourClicked = false; //... e non quello di fine
				startTimePicker.show(getFragmentManager(), TIMEPICKER_TAG); // infine mostro la dialog
			}		
		});
		
		endTimePicker = TimePickerDialog.newInstance(this, endHour , endMinute, true, false);
	
		//stessa cosa per il bottone per settare l'ora di fine allenamento
		endHourButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				hasModify = true;
				endHourClicked = true;
				startHourClicked = false;
				endTimePicker.show(getFragmentManager(), TIMEPICKER_TAG);				
			}			
		});
		
		// setto i listener per la selezione di un gruppo
		addGroupButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				hasModify = true; //avverto che ci sono state modifiche

				final Dialog dialog = new Dialog(NewTrainingActivity.this); //creo la dialog
				dialog.setContentView(R.layout.groups_list_dialog); //setto il layout
				dialog.setTitle("Lista Gruppi"); //setto il titolo
				
				//creo l'adapter di stringe per la lista
				ArrayAdapter<String> adap = new ArrayAdapter<String>(NewTrainingActivity.this, R.layout.group_list_item, R.id.ListItemText, gruppi);
								
				ListView listaGruppi = (ListView) dialog.findViewById(R.id.listView1); //prelevo la listView del layout
				listaGruppi.setAdapter(adap); //e ne setto l'adapter
				 
				//setto i listener per la selezione del gruppo
				listaGruppi.setOnItemClickListener(new OnItemClickListener() {
			        public void onItemClick(AdapterView<?> parent, View view,int position, long id) {

			           //Recupero la stringa del gruppo selezionato
			           String selected = ((TextView) view.findViewById(R.id.ListItemText)).getText().toString();
			           groupSelected = position+1; //e mi salvo la posizione
			           ((Button) findViewById(R.id.addGroupButton)).setText(selected);
			           dialog.dismiss(); //chiudo la dialog se viene selezionato un gruppo
			           
			           if(pwGroups != null)
			        	   pwGroups.dismiss();
			        }
			      });
				
				dialog.show(); //mostro la dialog
			}
		});	
		
		//Setto i listener per il bottone di inserimento esercizi
		exercisesButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//creo l'intent
				Intent i = new Intent(NewTrainingActivity.this, NewExercisesListActivity.class);
				
				//se la lista degli esercizi esiste ed ha una dimensione maggiore di 0
				if(listaEsercizi != null && listaEsercizi.size() > 0){
					i.putParcelableArrayListExtra("listaEsercizi", listaEsercizi); //metto la lista nell'intent
					i.putExtra("mListNotEmpty", true); //e avverto che la lista non è vuota
				}
				else 
					i.putExtra("mListNotEmpty", false); //altrimenti avverto che la lista è vuota
				
				startActivityForResult(i, 1); //avvio l'activty che mi restituirà l'array modificato
				//setto le animazioni di transizione
				((Activity) NewTrainingActivity.this).overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
			}
		});
		
		
		trainingName.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {	
				if(s.toString().length() < 1){
					trainingName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.indicator_input_error, 0);
					if(pwName == null){
						LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
						pwName = new PopupWindow(inflater.inflate(R.layout.popup_error, null, false), LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,true);
					    // The code below assumes that the root container has an id called 'main'
						pwName.setFocusable(false);
						pwName.setOutsideTouchable(true);
						pwName.setTouchable(false);
						pwName.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
						pwName.showAsDropDown(trainingName, -130, -30);
					    ((TextView) pwName.getContentView().findViewById(R.id.tv_message)).setText("Inserisci il nome dell'allenamento");
					}
					else pwName.showAsDropDown(trainingName, -130, -30);
				}
				else {
					trainingName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.indicator_confirm, 0);
					if(pwName != null)
						pwName.dismiss();
				}
			}
		});
		
		//se sto ricreando l'activity alla rotaizione
		 if (savedInstanceState != null) {
			 //cerco il fragment
	            TimePickerDialog tpd = (TimePickerDialog) getFragmentManager().findFragmentByTag(TIMEPICKER_TAG);
	            if (tpd != null) {
	            	//se il fragment c'è già, setto il suo listener
	                tpd.setOnTimeSetListener(this);
	            }
	        }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//Inflato il menu
		getMenuInflater().inflate(R.menu.new_training, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		//se viene selezionato un elemento dalla action bar
		switch (item.getItemId()) {
		
			//Se si preme sulla barra per tornare indietro, avvio l'onBackPresed
		    case android.R.id.home:
		    	onBackPressed();
		        return true;
	        
	        //se si preme l'icona per salvare l'allenamento 
		    case R.id.save:
		    	
		    	if(trainingId < 0){
			    	//controllo se l'inserimento nel db va a buon fine
			    	if(insertTraining()){
			    		//e mostro il toast
			    		Toast.makeText(this, "Inserimento avvenuto con successo", Toast.LENGTH_LONG).show();
				    	finish(); //termino l'activity e setto le animazioni
				    	overridePendingTransition(R.anim.slide_in_fade, R.anim.slide_right_out);
			    	}
		    	}
		    	else if(modifyTraining(trainingId)){
		    		//e mostro il toast
		    		Toast.makeText(this, "Modifica avvenuta con successo", Toast.LENGTH_LONG).show();
			    	finish(); //termino l'activity e setto le animazioni
			    	overridePendingTransition(R.anim.slide_in_fade, R.anim.slide_right_out);
		    	}
		    	return true;
        }
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed() 
	{

		//se viene premuto il bottone indietro, controllo se ci sono modifiche
		if(hasModify || (initialSize != listaEsercizi.size()))
			showAlertDialog(); //se ce ne sono mostro la dialog
		else {
			 //sennò chiudo l'activity
			finish();
			overridePendingTransition(R.anim.slide_in_fade, R.anim.slide_right_out);
			super.onBackPressed();
		}
	}

	@Override
	public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
		//se viene selezionato il tempo nel timepicker, controllo quale timepicker sia stato avviato
		if(startHourClicked){ //se viene premuto il bottone per settare l'ora di inizio
			startHour = hourOfDay; //salvo i dati e cambio il testo del bottone
			startMinute = minute;
			startHourButton.setText(String.format("%02d:%02d", hourOfDay, minute));
			
			if(trainingId < 0){
				endHour = startHour+2; //salvo i dati e cambio il testo del bottone 
				endMinute = startMinute;
				endHourButton.setText(String.format("%02d:%02d", endHour, endMinute));
				endTimePicker = TimePickerDialog.newInstance(this, endHour, endMinute, true, false); //Reinizializzo il timepicker
			}
				
		} 
		else if(endHourClicked){ //se viene premuto il bottone per settare l'ora di fine
			endHour = hourOfDay; //salvo i dati e cambio il testo del bottone
			endMinute = minute;
			endHourButton.setText(String.format("%02d:%02d", hourOfDay, minute));
		}
	}
	
	@Override
	protected void onDestroy() 
	{
		//Quando viene chiusa l'activity, chiudo anche il db
		this.mDb.close();
		super.onDestroy();
	}
	
	
	//Se viene chiusa l'activity successiva, prelevo il risultato
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

	  if (requestCode == 1) {

	     if(resultCode == RESULT_OK){      
	    	 //se tutto è andato a buon fine, prelevo la lista degli esercizi modificata
	         listaEsercizi = data.getParcelableArrayListExtra("result");
	         
	         //se la lista è stata modificata modifico il testo del bottone
	         if(listaEsercizi != null && listaEsercizi.size() > 0){
	        	 
	        	 if(pwEdit != null)
	        		 pwEdit.dismiss();
	        	 
        		 ((TextView) findViewById(R.id.addExercisesButton)).setText("Modifica Esercizi");
	         }
	         else{
        		 ((TextView) findViewById(R.id.addExercisesButton)).setText("Aggiungi Esercizi");
	         }
	     }
	  }
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		//Se viene rotato lo schermo mi salvo tutti i dati che mi serviranno
		outState.putParcelableArrayList("listaEsercizi", listaEsercizi);
		outState.putInt("startHour", startHour);
		outState.putInt("startMinute", startMinute);
		outState.putInt("endHour", endHour);
		outState.putInt("endMinute", endMinute);
		outState.putInt("groupSelected", groupSelected);
		outState.putBoolean("hasModify", hasModify);
		outState.putLong("trainingId", trainingId);
		outState.putInt("initialSize", initialSize);
		super.onSaveInstanceState(outState);
	}
	
	/**
	 * Metodo che crea un'AlertDialog se si prova a uscire da questa schermata avendo modificato dei dati ma senza
	 * aver premuto il tasto per salvare
	 */
	public void showAlertDialog()
	{
		//Se l'alert dialog non era stata ancora creata, la creo
		if(mAlertDialog == null)
		{
			//Creo il builder della dialog. Tramite quello vengono settate tutte le sue caratteristiche prima di crearla
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
			 
			//Inserisco il titolo ed il messaggio da visualizzare
			alertDialogBuilder.setTitle("Attenzione!");
			alertDialogBuilder.setMessage("Sei sicuro di voler tornare indietro? Le modifiche effettuate non verranno salvate!");
			
			
			//Setto un listener per il bottone positivo
			alertDialogBuilder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog,int id) 
				{
					//Se si preme il tasto, termino l'activity e metto l'animazione per lo slide verso sinistra
					finish();
					overridePendingTransition(R.anim.slide_in_fade, R.anim.slide_right_out);
				}
			});
			
			//Setto un listener per il bottone negativo
			alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog,int id) 
				{
					//Se si preme il bottone chiudo la dialog
					dialog.cancel();
				}
			});
	 
			//Sistemato il builder, creo la dialog vera e propria
			mAlertDialog = alertDialogBuilder.create();
		}
		
		//Creata la dialog (se era null), la mostro
		mAlertDialog.show();		
	}
	
	
	/**
	 * Metodo per inserire un nuovo allenamento nel db
	 * 
	 * @return true se l'inserimento va a buon fine, false altrimenti
	 */
	public boolean insertTraining(){
		
		Calendar cal = Calendar.getInstance(); //prelevo il calendario
		cal.setTimeInMillis(getIntent().getLongExtra("data", 0)); //setto il tempo in millisecondi, prendendo quello che viene passato all'activity
		
		//prelevo giorno, mese e anno dalla data
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int month = cal.get(Calendar.MONTH);
		int year = cal.get(Calendar.YEAR);
		
		cal.set(year, month, day); //setto la data
		long trainingDate = cal.getTimeInMillis(); //e ne prendo il tempo in ms
		
		if(!checkErrors(true))
			return false;		
		
		//Recupero le ore di inizio e fine dai rispettivi bottoni, che sono nella forma 00:00
		String formattedStartHour = (String) startHourButton.getText();
		String formattedEndHour = (String) endHourButton.getText();
		
		//Preparo il formattatore del tipo ore:minuti
		SimpleDateFormat format = new SimpleDateFormat("k:m");

		Date startHourDate = null;
		Date endHourDate = null;
        
		//Parso le ore come oggetti Date
        try 
        {
        	startHourDate = (Date) format.parse(formattedStartHour);
        	endHourDate = (Date) format.parse(formattedEndHour);
		} 
        catch (ParseException e) 
        {
			e.printStackTrace();
		}
		
        //Recupero la versione long delle ore
        long startHourLong = startHourDate.getTime();
        long endHourLong = endHourDate.getTime();        
		
        //inserisco l'allanemnto e mi salvo il nuovo id
		long newTrainingId = mDb.addTraining(null, trainingName.getText().toString(), trainingDate, startHourLong, endHourLong, groupSelected);
		
		//scorro tutti gli esercizi della lista
		for(Esercizio es: listaEsercizi){
			
			//salvo l'id dell'esercizio appena inserito nel db
			long esId = this.mDb.addExercise(null, es.getRipetizioni(), es.getDistanza(), es.getStile1(), es.getStile2(), es.getAndatura(), es.getTempo(), newTrainingId);
			
			//prelevo l'array degli atleti componenti del gruppo dal db
			ArrayList<Atleta> atleti = this.mDb.getAthletesInGroup(groupSelected);
			
			//Per ogni atleta...
			for(Atleta a : atleti){
				
				//... E per il numero di ripetizioni dell'esercizio				
				for(int i = 0; i < es.getRipetizioni(); i++){
					//inserisco nel db il tmepo relativo alla ripetizione dell'esercizio
					this.mDb.addTime(null, a.getId(), esId, i+1, "00-00");
				}
			}
		}
		
		//confermo l'inserimento nel db
		return true;
	}
	
	public boolean modifyTraining(long id){
		
		if(!checkErrors(false))
			return false;	
		
		//Recupero le ore di inizio e fine dai rispettivi bottoni, che sono nella forma 00:00
		String formattedStartHour = (String) startHourButton.getText();
		String formattedEndHour = (String) endHourButton.getText();
		
		//Preparo il formattatore del tipo ore:minuti
		SimpleDateFormat format = new SimpleDateFormat("k:m");

		Date startHourDate = null;
		Date endHourDate = null;
        
		//Parso le ore come oggetti Date
        try 
        {
        	startHourDate = (Date) format.parse(formattedStartHour);
        	endHourDate = (Date) format.parse(formattedEndHour);
		} 
        catch (ParseException e) 
        {
			e.printStackTrace();
		}
		
        //Recupero la versione long delle ore
        long startHourLong = startHourDate.getTime();
        long endHourLong = endHourDate.getTime();      
        
		this.mDb.updateTraining(id, trainingName.getText().toString(), startHourLong, endHourLong, groupSelected);

		
        /* Controllo se il nuovo gruppo inserito dall'utente è diverso da quello vecchio; in tal caso bisogna infatti
		 * cancellare tutti i tempi di ogni atleta del precedente gruppo per ogni esercizio previsto dall'allenamento 
		 * ed inserire i nuovi tempi del nuovo gruppo con i valori di default 00-00 */
        if(oldGroupSelcted != groupSelected)
        {
        	//Recupero rispettivamente gli esercizi previsti nell'allenamento, gli atleti del gruppo
        	//precedente e gli atleti del nuovo gruppo inserito
        	ArrayList<Esercizio> exercises = mDb.getExercisesInTraining(trainingId);
        	ArrayList<Atleta> athletesInOldGroup = mDb.getAthletesInGroup(oldGroupSelcted);
        	ArrayList<Atleta> athletesInNewGroup = mDb.getAthletesInGroup(groupSelected);
        	
        	//Scorro gli esercizi, in quanto per ognuno dovrò modificare i tempi
        	for(Esercizio exercise : exercises)
        	{
        		int exerciseId = exercise.getId();
        		
        		//Elimino tutti i tempi degli atleti del gruppo precedente
        		for(Atleta athlete : athletesInOldGroup)
        			mDb.deleteTimesFromAthleteInExercise(athlete.getId(), exerciseId);
        		
        		//Scorro tutti gli atleti del nuovo gruppo...
        		for(Atleta athlete : athletesInNewGroup)
        		{
        			int repetitions = exercise.getRipetizioni();
        			
        			//...ed inserisco i tempi di ogni atleta per ogni ripetizione con il valore di default
        			for(int i = 1; i <= repetitions; i++)
        				mDb.addTime(null, athlete.getId(), exerciseId, i, "00-00");
        		}	
        	}
        	
        }
        	
		
		return true;
	}
	
	public boolean checkErrors(boolean isNew){
		boolean noErrors = true;
		
		if(trainingName.getText().toString().equals("")){
//			Toast.makeText(this, "Inserisci il nome dell'allenamento", Toast.LENGTH_SHORT).show();
			noErrors = false;
		}
		
		//se non è stato selezionato alcun gruppo avverto dell'errore e ritorno false
		if(groupSelected < 0){
			
//			Toast.makeText(this, "Devi selezionare un gruppo di lavoro!", Toast.LENGTH_SHORT).show();
			
			if(pwGroups == null){
				
				LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				pwGroups = new PopupWindow(inflater.inflate(R.layout.popup_error, null, false), LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,true);
			    // The code below assumes that the root container has an id called 'main'
				pwGroups.setFocusable(false);
				pwGroups.setOutsideTouchable(true);
				pwGroups.setTouchable(false);
				pwGroups.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
				pwGroups.showAsDropDown(addGroupButton, 0, 0);
			    ((TextView) pwGroups.getContentView().findViewById(R.id.tv_message)).setText("Seleziona un gruppo");
			}
			else pwGroups.showAsDropDown(addGroupButton, 0, 0);
			
			noErrors = false;
		}

		//se è stato selezionato un gruppo ma non un esercizio avverto sempre dell'errore e termino la funzione
		if(listaEsercizi == null || listaEsercizi.size() <= 0){
			
//			Toast.makeText(this, "Devi inserire almeno un esercizio!", Toast.LENGTH_SHORT).show();
			
			if(pwEdit == null){
				LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				pwEdit = new PopupWindow(inflater.inflate(R.layout.popup_error, null, false), LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,true);
			    // The code below assumes that the root container has an id called 'main'
				pwEdit.setFocusable(false);
				pwEdit.setOutsideTouchable(true);
				pwEdit.setTouchable(false);
				pwEdit.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
				pwEdit.showAsDropDown(exercisesButton, 0, 0);
			    ((TextView) pwEdit.getContentView().findViewById(R.id.tv_message)).setText("Inserisci almeno un esercizio");
			}
			else pwEdit.showAsDropDown(exercisesButton, 0, 0);
			
			noErrors = false;
		}
		
		if(!noErrors)
			Toast.makeText(this, "Correggi gli errori evidenziati", Toast.LENGTH_SHORT).show();
		
		return noErrors;		
	}
}
