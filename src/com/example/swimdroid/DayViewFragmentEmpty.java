package com.example.swimdroid;

import java.util.Calendar;

import com.android.datetimepicker.time.RadialPickerLayout;
import com.android.datetimepicker.time.TimePickerDialog;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Toast;

/**
 * Fragment che viene usato quando nella day view non ci sono allenamenti previsti
 * 
 * @author Francesco
 *
 */
public class DayViewFragmentEmpty extends Fragment
{
	 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
    	View v = inflater.inflate(R.layout.day_view_empty_list, container, false); //inflato la vista del fragment
       
    	//setto i listener per il bottone di aggiunta allenamenti
        v.findViewById(R.id.addTrainingButtonLarge).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
				Intent i = new Intent(getActivity(), NewTrainingActivity.class); //creo l'intent per avviare l'activity per la creazione di un nuovo allenamento
				i.putExtra("dayString", ((DayViewActivity) getActivity()).getSelectedDay()); //metto come extra il giorno della settimana selezionata
				i.putExtra("dataString", ((DayViewActivity) getActivity()).getSelectedDate()); //setto inoltre il giorno dell'anno selezionato
				i.putExtra("data", ((DayViewActivity) getActivity()).getSelectedLong()); //infine setto la data in millisecondi
				startActivity(i); //avvio l'activity
				getActivity().overridePendingTransition(R.anim.slide_right_in, R.anim.slide_out_fade); //e setto l'animazione
            }
        });
        
        return v;
    }    
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.empty_training_list_actions, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	setHasOptionsMenu(true);
    	super.onCreate(savedInstanceState);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Se viene premuto il bottone per aggiugnere un allenamento
        switch (item.getItemId()) {
            case R.id.addTrainingAction:
            	Intent i = new Intent(getActivity(), NewTrainingActivity.class); //creo l'intent per avviare l'activity per la creazione di un nuovo allenamento
				i.putExtra("dayString", ((DayViewActivity) getActivity()).getSelectedDay()); //metto come extra il giorno della settimana selezionata
				i.putExtra("dataString", ((DayViewActivity) getActivity()).getSelectedDate()); //setto inoltre il giorno dell'anno selezionato
				i.putExtra("data", ((DayViewActivity) getActivity()).getSelectedLong()); //infine setto la data in millisecondi
				startActivity(i); //avvio l'activity
				getActivity().overridePendingTransition(R.anim.slide_right_in, R.anim.slide_out_fade); //e setto l'animazione
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}