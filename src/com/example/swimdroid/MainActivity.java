package com.example.swimdroid;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;







import com.example.model.DBTrainingsData;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import android.annotation.SuppressLint;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.UserDictionary.Words;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Classe principale, che mostra la vista del calendario
 * @author Gabriele
 *
 */
public class MainActivity extends FragmentActivity{
	
	private CaldroidFragment caldroidFragment; //frammento relativo al calendario
    private Date selectedDate; //Mi salvo la data che è stata selezionata per ultima, mi servirà per capire dove avviare le animazioni per gli eventi
    private DBTrainingsData db; //database
    private HashMap<String, Object> extraData; //extraData da passare al fragment, che la inoltrerà all'adapter, serve per far comunicare cliente e caldroid
    private boolean newEvent; //booleano per controllare se sono stati inseriti nuovi eventi nella data selezionata, mi serve globale in quanto la uso in funzioni diverse
    private boolean removedEvent; //booleano per controllare se sono stati rimossi tutti gli eventi nella data selezionata, mi serve globale in quanto la uso in funzioni diverse

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main); //setto il layout dell'activity
		overridePendingTransition(R.anim.fade_in, R.anim.fade_out); //e metto l'animazione per passare dalla splashScreen alla MainActivity
		
		db = new DBTrainingsData(getBaseContext()); //inizializzo il db
		
		extraData = new HashMap<String, Object>(); //inizializzo l'array per i dati extra da passare al fragment
		newEvent = false; //avverto che non è stato inserito nessun nuovo evento nella data selezionata
		removedEvent = false; //avverto che non sono stati rimossi eventi dalla data selezionata

		selectedDate = null; //avverto che ancora non è stata selezionata nessuna data
		
		caldroidFragment = new CalendarCustomFragment(); //creo il fragment del calendario
		caldroidFragment.setExtraData(extraData); //setto l'extra data
		 
		 Calendar cal1 = Calendar.getInstance(); //prelevo il calendario per settare il range di date da cui caricare le date con eventi

		//Il range comincia a 30 giorni prima della data correntemente visualizzata
		cal1.add(Calendar.DATE, -30); //sottraggo 30 giorni
		Date minDate = cal1.getTime(); //setto la minDate

		//e termina a 30 giorni dopo la data correntemente visualizzata
		cal1 = Calendar.getInstance(); //riinizializzo al giorno di oggi
		cal1.add(Calendar.DATE, 30); //ed aggiungo 30 giorni
		Date maxDate = cal1.getTime(); //setto la maxDate
		 
		//setto le date selezionate
		 caldroidFragment.setSelectedDates(minDate, maxDate, this.db);
		 
		 
		 //Metto un handler con un Runnable che scatta dopo 1500 millisecondi per far lampeggiare il giorno attuale
		 new Handler().postDelayed(new Runnable() {
				
				@Override
				public void run() {
			    	extraData.put("goToday", true); //setto il flag 
			    	caldroidFragment.setExtraData(extraData); //setto l'extraData
			    	caldroidFragment.refreshView(); //infine aggiorno la vista
				}
			}, 1500);

		
		//Sell'activity è ricreata dopo la rotazione
		if (savedInstanceState != null) {
			//ripristino lo stato
			caldroidFragment.restoreStatesFromKey(savedInstanceState,"CALDROID_SAVED_STATE");
		}		
		else { //Se l'activity è creata da zero
			
			//creo un bundle in cui ci metto tutti idati necessari
			Bundle args = new Bundle();
			Calendar cal = Calendar.getInstance();
			args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
			args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
			args.putBoolean(CaldroidFragment.ENABLE_SWIPE, true);
			args.putBoolean(CaldroidFragment.SIX_WEEKS_IN_CALENDAR, true);
			args.putInt("startDayOfWeek", CaldroidFragment.MONDAY);

			// Uncomment this to customize startDayOfWeek
			// args.putInt(CaldroidFragment.START_DAY_OF_WEEK,
			// CaldroidFragment.TUESDAY); // Tuesday
			caldroidFragment.setArguments(args);
		}

		//Attacco il fragment all'activity
		FragmentTransaction t = getSupportFragmentManager().beginTransaction();
		t.replace(R.id.calendar1, caldroidFragment); //rimpiazzo il container
		t.commit();

		//Setto il listener
		final CaldroidListener listener = new CaldroidListener() {

			@Override
			public void onSelectDate(Date date, View view) {
				//se viene selezionata una data
				SimpleDateFormat month_date = new SimpleDateFormat("dd MMMM yyyy"); // inizializzo un date formatter per creare la data
				
				String giornoAnno = month_date.format(date.getTime()); //formatto la data selezionata
				String giornoSettimana = new SimpleDateFormat("EEEE").format(date); //prelevo il giorno della settimana per intero
				//ora prendo il giorno dell'anno mostrato come es. 12 Gennaio 2014 con la g maiuscola e il resto minuscolo
				giornoAnno = giornoAnno.substring(0,3) +giornoAnno.substring(3,4).toUpperCase() + giornoAnno.substring(4).toLowerCase();
				giornoSettimana = giornoSettimana.toUpperCase(); // il giorno della settimana lo setto tutto maiuscolo
				
				String selectedCalendarDate = giornoAnno; //setto la stringfa data selezionata
				String selectedDay = giornoSettimana; //e la stringa del giorno della settimana selezionata
				
				Calendar cal = Calendar.getInstance(); //prendo il calendario
				
				selectedDate = date; //mi salvo la data selezionata dal calendario

				Intent i = new Intent(MainActivity.this, DayViewActivity.class); //creo il nuovo intent per aprire la day view
				i.putExtra("dataString", selectedCalendarDate); //setto la stringa della data selezionata
				i.putExtra("dayString", selectedDay); //setto la stringa del giorno della settimana selezionato
				cal.setTime(date); //setto la data del calendario a quella selezionata così da prenderne i millisecondi
				i.putExtra("dataMillis", cal.getTimeInMillis());
				startActivityForResult(i, 1); //avvio l'activity da cui mi aspetterò un risultato, che sarà un flag booleano
				overridePendingTransition(R.anim.slide_right_in, R.anim.slide_out_fade); //setto le animazioni
			}

			@Override
			public void onChangeMonth(int month, int year) {
				
				//Se viene cambiato mese, devo aggiornare le date selezionata
				Calendar cal = Calendar.getInstance(); //prelevo il calendario
				cal.set(year, month, cal.get(Calendar.DAY_OF_MONTH)); //setto l'anno e il mese attuali, mettendo come giorno quello del mese corrente
				cal.add(Calendar.DATE, -60); //e setto come data minima del range, 60 giorni prima del giorno selezionato
				Date minDate = cal.getTime();
				
				cal = Calendar.getInstance(); //stessa cosa per la data massima del range
				cal.set(year, month, cal.get(Calendar.DAY_OF_MONTH));
				Date maxDate = cal.getTime();
				
				caldroidFragment.setSelectedDates(minDate, maxDate, db); //setto poi le dat eselezionate
				caldroidFragment.refreshView(); //aggiorno la vista
			}

			@Override
			public void onLongClickDate(Date date, View view) {
				
			}

			@Override
			public void onCaldroidViewCreated() {
				
			}

		};

		//Setto il listener
		caldroidFragment.setCaldroidListener(listener);

	}

	/**
	 * Salvo lo stato attuale del calendario
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		if (caldroidFragment != null) {
			caldroidFragment.saveStatesToKey(outState, "CALDROID_SAVED_STATE");
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//setto il menu della actionBar
		getMenuInflater().inflate(R.menu.mymenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		//se viene premuto qualcosa nella action bar
		//controllo cosa è
		switch (item.getItemId()) {
		
		//se viene premuto il bottone per tornare al giorno di oggi
		    case R.id.day_today:
		    	Calendar cal = Calendar.getInstance(); //prendo il giorno di oggi
		    	extraData.put("goToday", true); //setto il flag 
		    	caldroidFragment.setExtraData(extraData); //setto l'extraData
		    	caldroidFragment.moveToDate(cal.getTime()); //e mi sposto alla data di oggi
		    	caldroidFragment.refreshView(); //infine aggiorno la vista
		    	return true;
		    	
	    	default: return super.onOptionsItemSelected(item);
	    } 
	} 

	
	@Override
	protected void onDestroy() {
		//quando viene chiusa l'activity,  chiudo anche il db
		this.db.close();
		super.onDestroy();
	}
	
	@Override
	protected void onResume() {
		
		//se l'activity viene riesumata dopo essere stata messa in background
		if(newEvent){
			
			//se c'è un nuovo evento, aspetto 800ms per far terminare le animazioni
			//e poi aggiorno la vista, facendo quindi partire l'animazione che crea la barra degli eventi sopra il giorno
			new Handler().postDelayed(new Runnable() {
				
				@Override
				public void run() {
					extraData.put("newEventDate", selectedDate); //setto la data con nuovi eventi
			    	caldroidFragment.setExtraData(extraData); //setto l'extraData
					caldroidFragment.setCalendarDate(selectedDate); //e setto la data del calendario per ricreare la vista
					caldroidFragment.refreshView();		
				}
			}, 800);
		}
		else if(removedEvent) {
			
			//se sono stati rimossi tutti gli eventi da un giornoaspetto 800ms per far terminare le animazioni
			//e poi aggiorno la vista, facendo quindi partire l'animazione che crea la barra degli eventi sopra il giorno
				new Handler().postDelayed(new Runnable() {
				
				@Override
				public void run() {
					extraData.put("removedEventDate", selectedDate); //setto la data che ora sarà senza eventi
			    	caldroidFragment.setExtraData(extraData); //setto l'extraData
					caldroidFragment.setCalendarDate(selectedDate); //e setto la data del calendario per ricreare la vista
					caldroidFragment.refreshView();		
				}
			}, 800);
		}
		else { //se non è cambiato nulla, per sicurezza rimuovo tutto dall'extraData
			extraData.remove("newEventDate");
			extraData.remove("removedEventDate");
	    	caldroidFragment.setExtraData(extraData);
		}
		
		//infine, qualsiasi cosa sia successa non deve risuccedere se viene riesumata l'activity
		removedEvent = false;
		newEvent = false;
		super.onResume();
	}
	
	/**
	 * Quando si chiude l'activity avviata da questa, devo controllare cosa mi ha restituito
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if (requestCode == 1) {	
		     if(resultCode == RESULT_OK){
		    	 newEvent = data.getBooleanExtra("newEvent", false); //prendo il booleano, se non c'è setto false
		    	 removedEvent = data.getBooleanExtra("removedEvent", false); //prendo il booleano, se non c'è setto false
	         
		     }
		  }
	}
}
