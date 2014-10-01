package com.example.swimdroid;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import com.android.datetimepicker.time.RadialPickerLayout;
import com.android.datetimepicker.time.TimePickerDialog;
import com.example.model.DBTrainingsData;
import com.example.model.Training;

import android.app.ActionBar;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.ViewSwitcher;

/**
 * Activity della schermata di un giorno. E' padre dei frammenti relativi alla visione degli allenamenti
 * 
 * @author Francesco
 *
 */
public class DayViewActivity extends FragmentActivity 
{
	private String mDateSelectedString; //La data del giorno in versione String
	private long mDateSelectedLong; //La data del giorno in versione long
	private String mDateSelectedDay;
	
	private DBTrainingsData mDb; //Riferimento al db
	
	private ArrayList<Training> mPlannedTrainings; //Lista degli allenamenti previsti per questo giorno
	
	private int initialDimension;
    
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
		setContentView(R.layout.activity_day_view);
		
		//Recupero la data del giorno selezionato
		mDateSelectedString = (String) getIntent().getCharSequenceExtra("dataString");
		mDateSelectedLong = (long) getIntent().getLongExtra("dataMillis", 0);
		mDateSelectedDay = (String) getIntent().getCharSequenceExtra("dayString");
		
		//Setup dell'action bar
        ActionBar ab = this.getActionBar();
        ab.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);
        ab.setDisplayUseLogoEnabled(false);
        ab.setTitle(mDateSelectedString);
        ab.setSubtitle(mDateSelectedDay);
        ab.setIcon(android.R.color.transparent);
        
        //Creo il riferimento al db...
        mDb = new DBTrainingsData(this);
        initialDimension = mDb.getTrainingsInDay(mDateSelectedLong).size();
        
        checkTrainings(savedInstanceState);
    }	
	
    /**
     * Recupera la lista degli allenamenti del giorno; usato nei frammenti
     * 
     * @return l'array con gli allenamenti
     */
    public ArrayList<Training> getTrainings()
    {
    	return mPlannedTrainings;
    }
    
    /**
     * Recupera il db; usato nei frammenti
     * 
     * @return l'array con gli allenamenti
     */
    public DBTrainingsData getDatabase()
    {
    	return mDb;
    }
    
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		switch (item.getItemId()) {
	    // Respond to the action bar's Up/Home button
		    case android.R.id.home:
		    	onBackPressed();
		        return true;
	    }
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed() 
	{
		Intent returnIntent = new Intent();
		if(initialDimension < mPlannedTrainings.size()){
			returnIntent.putExtra("newEvent", true);
		}
		else if(initialDimension != mPlannedTrainings.size() && mPlannedTrainings.size() < 1) {
			returnIntent.putExtra("removedEvent", true);
		}
		
    	setResult(this.RESULT_OK, returnIntent); 
		finish();
		overridePendingTransition(R.anim.slide_in_fade, R.anim.slide_right_out);
		super.onBackPressed();
	}
	
	@Override
	protected void onDestroy() 
	{
		this.mDb.close();
		super.onDestroy();
	}
	
	public String getSelectedDay(){
		return mDateSelectedDay;
	}
	
	public String getSelectedDate(){
		return mDateSelectedString;
	}
	
	public long getSelectedLong(){
		return mDateSelectedLong;
	}
	
	@Override
	protected void onResume() {
		checkTrainings(null);
		super.onResume();
	}
	
	
	public void checkTrainings(Bundle savedInstanceState){
		//...e recupero gli allenamenti previsti per questo giorno
        mPlannedTrainings = mDb.getTrainingsInDay(mDateSelectedLong);
                
        
        //Controllo quanti allenamenti ci sono per questo giorno; se non ce ne sono, avvio il frammento apposito, altrimenti
        //l'altro
        if(mPlannedTrainings.size() == 0)
        {
        	//Se il frammento è stato ripristinato da uno stato precedente, ritorno per evitare di creare un altro
    		//frammento sopra il precedente
            if (savedInstanceState != null) {
                return;
            }
                            
            //Creo l'istanza del nuovo frammento
            DayViewFragmentEmpty fragment = new DayViewFragmentEmpty();

            //Aggiungo il frammento al "fragment_container del layout dell'activity e committo
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
        }
        else 
        {
            if (savedInstanceState != null) {
                return;
            }
                                        
            DayViewFragmentFull fragment = new DayViewFragmentFull();

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
        }		
	}
	
	
}
