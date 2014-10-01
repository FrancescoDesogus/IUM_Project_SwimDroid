package com.example.swimdroid;


import hirondelle.date4j.DateTime;

import java.util.Date;
import java.util.HashMap;

import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidGridAdapter;
import com.roomorama.caldroid.CalendarHelper;

import android.animation.ObjectAnimator;
import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.TransitionDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.example.swimdroid.R;

public class CalendarCustomAdapter extends CaldroidGridAdapter {

	private Context mContext; 
	private DateTime newEventDate; //Data relativa al giorno in cui sono stati creati nuovi allenamenti, mi serve per controllare se avviare o meno l'animazione
	private DateTime removedEventDate; //Data relativa al giorno da cui sono stati rimossi tutti gli allenamenti, mi serve per controllare se avviare o meno l'animazione
	private Boolean goToday; //Booleano per avvertire che è stato premuto il tasto per tornare al giorno di oggi, mi serve per controllare se avviare o meno l'animazione
	private View eventBar; //Barra degli eventi, mi serve globale per poterla gestire quando mi serve
	private int monthLimit; //Intero che indica il limite del range attuale di cui si sta costruendo il calendario; più info quando viene usato
	
	/**
	 * 
	 * @param context è il context dell'activity chiamante
	 * @param month è il mese attualmente visualizzato
	 * @param year è l'anno attualmente visualizzato
	 * @param caldroidData i dati passati dal fragment
	 * @param extraData i dati settati dal client (la MainActivity in questo caso)
	 */
	public CalendarCustomAdapter(Context context, int month, int year, HashMap<String, Object> caldroidData, HashMap<String, Object> extraData) {
		super(context, month, year, caldroidData, extraData);
		//Salvo il context
		this.mContext = context;
		
		monthLimit = -1; //inizializzo l'intero a -1 per dire che di default non ha valore
	}

	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		View cellView = convertView; //mi salvo la vista, relativa alla cella
		
		newEventDate = null; //inizializzo la data del giorno con nuovi eventi
		if(extraData.containsKey("newEventDate")) //se sto passando la nuova data nell'extraData la prelevo
		{
			newEventDate = CalendarHelper.convertDateToDateTime(((Date) extraData.get("newEventDate")));

			/* salvo anche il "mese limite"; dato che vengono sempre caricate le celle del mese prima e di quello dopo
			 * il mese attualmente visualizzato dall'utente (quello in cui è stato aggiunto l'elemento), mi salvo
			 * il mese subito dopo quello del giorno da aggiungere per usi futuri;
			 * controllo se l'anno è maggiore di 2013 perchè in tal caso i giorni vengono presi partendo dal mese
			 * precedente a quello dell'evento in questione, per poi passare al mese dell'evento e poi al mese successivo
			 * all'evento; se l'anno dell'evento è però <= 2013, accade il contrario: parte a prendere i giorni dal mese
			 * successivo a quello dell'evento, poi passa al mese dell'evento e poi al mese precedente all'evento;
			 * per questo motivo il mese limite nel caso dell'anno <= 2013 deve essere l'opposto del caso > 2013 */
			if(newEventDate.getYear() > 2013)
				monthLimit = newEventDate.getMonth() == 12 ? 1 : newEventDate.getMonth() + 1;
			else
				monthLimit = newEventDate.getMonth() == 0 ? 1 : newEventDate.getMonth() - 1;

			
		}
		
		removedEventDate = null; //inizializzo la data del giorno da cui sono stati rimossi tutti gli eventi
		if(extraData.containsKey("removedEventDate")) //se è presente nell'extraData la prelevo
		{
			removedEventDate = CalendarHelper.convertDateToDateTime(((Date) extraData.get("removedEventDate")));		

			//salvo il mese limite del range
			if(removedEventDate.getYear() > 2013)
					monthLimit = removedEventDate.getMonth() == 12 ? 1 : removedEventDate.getMonth() + 1; 
				else
					monthLimit = removedEventDate.getMonth() == 0 ? 1 : removedEventDate.getMonth() - 1;
		}
		
		goToday = false; //inizializzo il booleano per avvertire che ci dev'essere l'aniamzione sul giorno di oggi, se è presente nell'extraData lo prelevo
		if(extraData.containsKey("goToday"))
			goToday = (Boolean) extraData.get("goToday");

		//Se la cella è nuova la inflato (termini osceni)
		if (convertView == null) {
			cellView = inflater.inflate(R.layout.custom_cell, null);
		}
		
		//salvo il padding per ripristinarlo alla fine
		int topPadding = cellView.getPaddingTop();
		int leftPadding = cellView.getPaddingLeft();
		int bottomPadding = cellView.getPaddingBottom();
		int rightPadding = cellView.getPaddingRight();
		
		//prelevo la textView relativa al numero del giorno
		TextView tv1 = (TextView) cellView.findViewById(R.id.tv1);
		
		//prelevo anche la barra relativa ai giorni con eventi
		eventBar = cellView.findViewById(R.id.eventPresent);
		
		/*** Scommentare questa riga per cambiare il font delle celle, CAUSA LAG ALLO SWYPE ***/
//		Typeface font = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Regular.ttf");
//		tv1.setTypeface(font);  

		//setto il colore per il numero del giorno
		tv1.setTextColor(Color.parseColor("#2d2d2d"));
		
		//Prelevo il tempo che andrà in questa cella
		DateTime dateTime = this.datetimeList.get(position);
		
		//booleano per avvertire se devo resettare la view della cella
		boolean shouldResetSelectedView = false;

		//Controllo se ci sono date selezionate, se non è null controllo che il giorno che sto controllando sia una data selezionata
		if (selectedDates != null && selectedDates.indexOf(dateTime) != -1) {
			
			if (CaldroidFragment.selectedBackgroundDrawable != -1) { //se la cella non ha un evento
				
				//setto la cella al background custom, e metto invisibile la barra sopra
				cellView.setBackgroundResource(CaldroidFragment.selectedBackgroundDrawable);
				eventBar.setVisibility(View.INVISIBLE);
				
			} else if(!dateTime.equals(getToday())){ //se invece è una data selezionata ma non è il giorno di oggi
				
				// ma c'è un evento modifico il bg e setto la barra visibile					
				cellView.setBackgroundResource(R.drawable.cell_bg);				
				eventBar.setVisibility(View.VISIBLE);
								
			} else {
				
				//se invece il giorno è oggi, setto che bisogna resettare questa vista e quindi cambiarla
				shouldResetSelectedView = true;			
				
				//e metto visibile la barra
				eventBar.setVisibility(View.VISIBLE);				
			}

			tv1.setTextColor(CaldroidFragment.selectedTextColor);

		} else { //se la data non si trova tra quelle selezionate, avverto che bisogna resettare la vista
			shouldResetSelectedView = true;
		}
		
		//Setto il colore dei mesi successivi o precedenti 
		if (dateTime.getMonth() != month) {
			tv1.setTextColor(Color.parseColor("#999999"));
			cellView.setBackgroundResource(R.drawable.disable_cell);
		}

		//se devo resttare la vista
		if (shouldResetSelectedView) {
			
			//se il giorno attualmente controllato è quello di oggi
			if (dateTime.equals(getToday())) {
				
				//controllo se è stato premuto il tasto per tornare al giorno di oggi
				if(goToday){
					
					//setto il background alla transizione che voglio
					cellView.setBackgroundResource(R.drawable.today_transition);
					
					//creo la transizione per i drawable, e prendo il background che ho appena settato
					final TransitionDrawable transition = (TransitionDrawable) cellView.getBackground();
					
					//Faccio partire la transizione
					final int millis = 200;
					transition.startTransition(millis);
					
					//e dopo 300ms la inverto così da tornare allo stato normale, dopo aver evidenziato la cella
					cellView.postDelayed(new Runnable() {
						
			            @Override
			            public void run() {			            	
			            	transition.reverseTransition(millis);
			            }
			            
			        }, millis+200);
					
					//dopo che ho fatto l'animazione rimuovo il flag dall'extraData, così al prossimo giro o aggiornamento della vista
					//non verrà fatta partire di nuovo l'animazione, in quando l'extraData non conterrà la key
					//e goToday rimarrà false
					extraData.remove("goToday");
				}
				else {
					
					//se non è stato premuto il bottone per tornare al giorno di oggi
					//setto il bacgkround normale, senza animazione
					cellView.setBackgroundResource(R.drawable.today_without_border_selector);
				}
				
				//setto colore e grassetto per il giorno di oggi
				tv1.setTextColor(Color.parseColor("#2a2a2a"));
				tv1.setTypeface(null, Typeface.BOLD);
				
			} else if(dateTime.getMonth() == month) {
				//se invece il giorno in oggetto non è oggi ma fa parte del mese correntemente visualizzato
				//setto il background per i giorni "normali", che non sono oggi ma neanche di altri mesi
				cellView.setBackgroundResource(R.drawable.cell_bg);
			}
		}		

		
		
		/* controllo se bisogna aggiungere o rimuovere le barre degli eventi; lo capisco dal fatto che monthLimit ha un valore
		 * diverso da -1; in tal caso, voglio che vengano aggiunte/rimosse tutte le barre possibili, quindi quello che faccio
		 * è chiamare il metodo checkModifyAnimations per animare tutte le barre, e solo quando il mese attuale
		 * è pari a circa metà del mese limite (ho messo 12 perchè è abbastanza grande da far si che in ogni mese
		 * non appaia tra i giorni disabilitati del mese precedente/successivo) blocco la creazione delle animazioni */
		if(monthLimit != -1 && (dateTime.getMonth() == monthLimit  && dateTime.getDay() == 12))
		{			
			//rimuovo dall'hashmap i rispettivi elementi (in realtà non possono essere mai presenti entrambi contemporaneamente,
			//ma tanto se un elemento nella map non esiste ritorna solo null, meglio di mettere delle if)...
			extraData.remove("newEventDate");
			extraData.remove("removedEventDate");
			
			//...e resetto il mese limite, per indicare che non c'è più niente da rimuovere/aggiungere
			monthLimit = -1;
		}
		
		/** Ora il tocco magico, chiamo questa funzione a cui passo la data correntemente controllata
		 *  per vedere se devo avviare qualche animazione relativa alla barra degli eventi
		 */
		checkModifyAnimations(dateTime);
		
		//setto il testo della textview, al numero del giorno di oggi
		tv1.setText("" + dateTime.getDay());

		//Per qualche oscuro motivo dopo aver settato setBackgroundResource, il padding scompare
		//Allora lo resetto, mettendo i valori che mi ero salvato
		cellView.setPadding(leftPadding, topPadding, rightPadding,bottomPadding);

		return cellView;
	}
	
	/**
	 * Funzione che controlla se ci devono essere animazioni relative alla barra degli eventi nella cella
	 * @param dateTime La data correntemente controllata
	 */
	public void checkModifyAnimations(DateTime dateTime){
		
		//se la data dei nuovi eventi non è null ed è proprio quella correntemente controllata
		if((newEventDate != null ) && (newEventDate.equals(dateTime))){			
			eventBar.setVisibility(View.INVISIBLE); //setto la barra invisibile
			Animation animFadeIn = AnimationUtils.loadAnimation(mContext, R.anim.fade_in); //carico l'animazione di fade in
			eventBar.startAnimation(animFadeIn); //avvio l'animazione
			eventBar.setVisibility(View.VISIBLE); //e risetto la barra visibile per il post animazione
		}
		//se la data degli eventi rimossi non è null ed è proprio quella correntemente controllata
		else if((removedEventDate != null ) && (removedEventDate.equals(dateTime))){
			eventBar.setVisibility(View.VISIBLE); //setto la barra visibile
			Animation animFadeOut = AnimationUtils.loadAnimation(mContext, R.anim.fade_out); //carico l'animazione di fade out
			eventBar.startAnimation(animFadeOut); //avvio l'animazione
			eventBar.setVisibility(View.INVISIBLE); //e risetto la barra invisibile per il post animazione
		}	
	}
}