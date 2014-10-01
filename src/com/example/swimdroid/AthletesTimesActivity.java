package com.example.swimdroid;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Scanner;

import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.android.datetimepicker.Utils;
import com.android.datetimepicker.time.RadialPickerLayout;
import com.android.datetimepicker.time.TimePickerDialog;
import com.example.model.Atleta;
import com.example.model.DBTrainingsData;
import com.example.model.Esercizio;
import com.example.model.Tempo;
import com.example.model.Training;

/**
 * Classe che mostra la lista degli atleti che hanno fatto/dovranno fare un dato esercizio. Si occupa anche di mostrare
 * i tempi relativi ad un'atleta quando esso viene premuto.
 * 
 * @author Francesco
 *
 */
public class AthletesTimesActivity extends ListActivity
{
	private Esercizio mExercise; //Esercizio di cui si vogliono vedere i tempi, recuperato dall'activity precedente
	private int mGroupId; //L'id del gruppo che deve eseguire/eseguirà l'esercizio, recuperato dall'activity precedente
	
	private DBTrainingsData mDb; //Riferimento al db
	
	private ArrayList<Atleta> mGroup; //Lista degli atleti che devono eseguire/eseguiranno l'esercizio
	private AthletesArrayAdapter mArrayAdapter; //Adattatore per la ListView 
	
	private View mPreviousClickedListItem; //Riferimento all'elemento della lista clickato in precedenza
	
	private LinearLayout mPreviousRoot; //Il nodo root precedente che contiene il layout della lista dei tempi precedentemente aperta
	
	private LayoutInflater mInflater; //Oggetto per inflatare view; per non crearlo ogni volta che lo si fa, l'ho messo come campo della classe
	
	private HashMap<Integer, ArrayList<Tempo>> mTimesListMap; //Map che relaziona l'id di un'atleta con la lista dei suoi tempi
	private HashMap<Button, Tempo> mTimeButtonMap; //Map che relaziona il bottone associato ad un tempo al tempo stesso
	
	private ArrayList<Tempo> mModifiedTimes; //Array che contiene tutti i tempi modificati dall'utente (qualora ne modificasse)
	
	private Animation mSlideDown; //Animazione per lo sliding down delle liste dei tempi degli atleti
	private Animation mSlideUp; //Animazione per lo sliding up delle liste dei tempi degli atleti
	
	private boolean hasModifiedTimes; //Boolean che indica se l'utente ha modificato almeno un tempo; è false di default
	
	private AlertDialog mAlertDialog; //AlertDialog per chiedere di confermare se vuole tornare indietro perdendo le modifiche
	
	

	
	@Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
		setContentView(R.layout.activity_athletes_times);
		
		//Recupero l'esercizio di cui si vogliono vedere i tempi e l'id del gruppo 
		mExercise = getIntent().getParcelableExtra("esercizio");
		mGroupId = getIntent().getIntExtra("groupId", 0);

		//Setup dell'action bar
        ActionBar ab = this.getActionBar();
        ab.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);
        ab.setDisplayUseLogoEnabled(false);
        ab.setTitle("Gruppo " + mGroupId);
		getActionBar().setIcon(android.R.color.transparent);
        
        //Per stampare il tempo di percorrenza nell'action bar uso uno scanner per dividere i minuti dai 
		//secondi, essendo il tempo salvato nel db con la forma 00-00
		Scanner timeScanner = new Scanner(mExercise.getTempo()).useDelimiter("-");

		String minutes = timeScanner.next();
		String seconds = timeScanner.next();
		
		timeScanner.close();
		
		String time = minutes + "'" + seconds + "''";
        
		//Setto il sottotitolo dell'action bar
        ab.setSubtitle(mExercise.getRipetizioni() + "x" + mExercise.getDistanza() + " " + mExercise.getStileAbbreviato() + " " + mExercise.getStile2() + " - " + time);
        
        
        //Istanzio le map, usate quando devono essere mostrati i tempi di un'atleta per migliorare l'efficienza
        mTimesListMap = new HashMap<Integer, ArrayList<Tempo>>();
        mTimeButtonMap = new HashMap<Button, Tempo>();
        
        //Inizializzo l'array che conterrà la lista dei tempi modificati dall'utente (qualora ne modificasse)
        mModifiedTimes = new ArrayList<Tempo>();
        
	    //Istanzio l'oggetto per inflatare views, usato quando bisogna mostrare i tempi di un'atleta
	    mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    
	    //Istanzio le animazioni per le liste dei tempi
	  	mSlideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
	  	mSlideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
	  	        
        //Creo il riferimento al db...
        mDb = new DBTrainingsData(this);
        
        //...e recupero gli atleti appartenenti al gruppo specificato
        mGroup = mDb.getAthletesInGroup(mGroupId);
        
        //Istanzio l'ArrayAdapter per mostrare gli atleti nella lista
        mArrayAdapter = new AthletesArrayAdapter(this, R.layout.athletes_list_item, mGroup);
	    setListAdapter(mArrayAdapter);
	    
	    //Inizializzo a false il boolenao che indica se l'utente ha modificato almeno un tempo
	    hasModifiedTimes = false;
    }	

	
	/**
	 * Metodo chiamato quando viene premuto un elemento della lista. Nel nostro caso, bisogna far uscire la lista
	 * dei tempi sotto l'atleta premuto. Al tap su un'atleta viene creata la lista come un LinearLayout con dentro a 
	 * sua volta il layout di ogni esercizio. Per maggiore efficienza, quando si preme su un allenamento e viene creata 
	 * la lista  dei suoi esercizi dinamicamente, la si mantiene in memoria in modo tale che se si deve mostrare di nuovo 
	 * quella lista in un secondo momento non si debba allocare e creare niente.
	 * 
	 * @listView la ListView in cui c'è stato il click
	 * @clickedView la particolare entry della lista in cui c'è stato il click
	 * @positionInList il numero della riga (partendo da 0)
	 * @rowId l'id della risorsa specifica di quella riga
	 */
	@Override
	public void onListItemClick(ListView listView, View clickedView, final int positionInList, long rowId)
	{
	    super.onListItemClick(listView, clickedView, positionInList, rowId);
	    

	    //Creo un'animazione di un piccolo rimbalzo per la view clickata, per dare un senso di feedback
	    ObjectAnimator pulseAnimator = Utils.getPulseAnimator(clickedView, 0.98f, 1.01f);
        pulseAnimator.start();
	    
        //Recupero la freccia della view clickata, che bisognerà animare in qualche modo a seconda che sia già stata
        //clickata la view oppure no
	    View arrow = clickedView.findViewById(R.id.arrow);
    	Animation anim = null;
	    	   
	    //Recuperaro il layout che sarà il contenitore della lista dei tempi
	    final LinearLayout myRoot = (LinearLayout) clickedView.findViewById(R.id.athletes_times_list_container);
	    	    	    
	    //Se il nodo root è uguale al precedente, vuol dire che è stato premuto lo stesso atleta, quindi bisogna
	    //togliere la lista e basta
	    if(myRoot == mPreviousRoot)
	    {	    	
	    	//Setto a null il nodo precedente, in modo tale che se al prossimo click si preme sullo stesso elemento
	    	// di prima non si cada di nuovo in questa if
	    	mPreviousRoot = null;
	    	
	    	//Se la view clickata era la stessa di prima, vuol dire che la freccia deve animarsi al contrario per tornare
	    	//a puntare verso il basso; quindi creo l'animazione e la metto nella freccia
	    	anim = new AnimationUtils().loadAnimation(this, R.anim.arrow_rotation_reverse);
	    	arrow.setBackgroundResource(R.drawable.ic_expand_holo);
	    	arrow.setAnimation(anim);
	    	arrow.startAnimation(anim);	
	    	
	    	/* Creo un listener per l'animazione dello sliding up; infatti adesso la lista dovrà animarsi, e bisogna che
	    	 * una volta finita l'animazione la view diventi invisibile. Il listener lo creo qua per avere riferimento
	    	 * alla view corretta all'interno del listener */
	    	mSlideUp.setAnimationListener(new AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) 
				{
					
				}
				
				@Override
				public void onAnimationRepeat(Animation animation) 
				{
					
				}
				
				@Override
				public void onAnimationEnd(Animation animation) 
				{
					//Una volta finita l'animazione, nascondo la view che contiene la lista dei tempi per questo dato atleta
					myRoot.setVisibility(View.GONE);		
				}				
			});
	    	
	    	//Faccio partire l'animazione per lo sliding up
	    	myRoot.startAnimation(mSlideUp);
	    }
	    //Se l'entry clickata non è la stessa di prima, procedo col popolarla (o con renderla visibile, se era già stata creata)
	    else
	    {	    	
	    	//In modo da far si che se c'è già una lista dei tempi aperta quando se ne preme un'altra la vecchia si chiuda,
		    //controllo se esiste un nodo precedente e nel caso lo rendo invisibile prima di procedere
	    	if(mPreviousRoot != null)
	    	{
		    	mPreviousRoot.setVisibility(View.GONE);
		    			    	
		    	//Recupero anche la freccia dell'elemento della lista clickato la volta prima, che dovrà animarsi
		    	//per tornare a puntare verso il basso
		    	View arrowPrevious = mPreviousClickedListItem.findViewById(R.id.arrow);
		    	
		    	//Creo l'animazione e la setto alla freccia
		    	Animation animPrevious = new AnimationUtils().loadAnimation(this, R.anim.arrow_rotation_reverse);
		    	arrowPrevious.setBackgroundResource(R.drawable.ic_expand_holo);
		    	arrowPrevious.setAnimation(animPrevious);
		    	arrowPrevious.startAnimation(animPrevious);	
	    	}
	    	
	    	//Creo l'animazione per l'oggetto della lista appena clickato in modo che la freccia vada a puntare verso l'alto
	    	anim = new AnimationUtils().loadAnimation(this, R.anim.arrow_rotation);
			arrow.setBackgroundResource(R.drawable.ic_collapse_holo);
	    	arrow.setAnimation(anim);
	    	arrow.startAnimation(anim);	
	    	
	    	//Recupero l'id dell'atleta clickato
	    	int athleteId = mGroup.get(positionInList).getId();
	    	
	    	//Usando l'id appena preso, recupero dalla map la lista dei tempi corrispondenti a quell'id, qualora
	    	//ci fosse già (se non c'è viene restituito null)
	    	ArrayList<Tempo> times = mTimesListMap.get(athleteId);
	    	
	    	/* Se la lista dei tempi è null, vuol dire che è la prima volta che è stata premuta quell'entry
	    	 * della lista, quindi vuol dire che bisogna recuperare i tempi dal db, creare le view per ogni entry
	    	 * e salvarle in memoria in modo tale che se si dovesse mostrare in un secondo momento la lista degli esercizi
	    	 * di questo dato allenamento non si debba rifare tutto da zero */
	    	if(times == null)
	    	{
	    		times = mDb.getAthletesTime(athleteId, mExercise.getId());
	    		
	    		//Ora che ho recuperato i tempi, li salvo nella map
	    		mTimesListMap.put(athleteId, times);

	    		//Adesso scorro tutti gli esercizi previsti per l'allenamento; bisognerà creare la view di ogni esercizio
		    	for(final Tempo time : times)
		    	{
		    		//Inflato nella view il layout di una singola entry della lista
		    		View view = mInflater.inflate(R.layout.athletes_times_list_item, null, true);
    		
		    			
	    			//Adesso bisogna popolare le TextView del layout (di default vuote) con i valori del particolare esercizio
	    			TextView textViewValue = (TextView) view.findViewById(R.id.num_ripetizione);
	    			textViewValue.setText(time.getRipetizione() + ".");
	    			
	    			
	    			//Per stampare il tempo dell'atleta nel bottone, prima devo spezzare la stringa salvata
	    			Scanner timeScanner = new Scanner(time.getTempo()).useDelimiter("-");

	    			String minutes = timeScanner.next();
	    			String seconds = timeScanner.next();
	    			
	    			timeScanner.close();
	    				    			
	    			//Recupero il bottone che mostra il tempo e setto il testo
	    			final Button timeButton = (Button) view.findViewById(R.id.athlete_time_button);
	    			timeButton.setText(minutes + "'" + seconds + "''");
	    			
	    			//Associo al bottone il listener per far aprire la dialog per modificare il tempo se premuto
	    			timeButton.setOnClickListener(new View.OnClickListener()
			        {
			            @Override
			            public void onClick(View v) 
			            {	
			            	//Creo il timepicker col number pad passando come parametro il bottone che causerà l'apertura della
			            	//dialog, in modo tale che una volta settato il tempo il testo del bottone venga direttamente cambiato
			        		TimePickerPad timePicker = new TimePickerPad(AthletesTimesActivity.this, timeButton);
			        		
			        		//Creo la dialog vera e propria
			        		timePicker.createTimePickerDialog();
			        		
			        		//Setto un listener per recuperare il nuovo tempo una volta che l'utente ha confermato
			        		timePicker.setOnTimeSetListener(new OnTimeSetListener()
			        		{
			        			@Override
			        			public void onTimeSet(int firstDigit, int secondDigit, int thirdDigit, int fourthDigit) 
			        			{
			                    	//Recupero il tempo associato al bottone che era stato premuto, in modo da salvarmi che quel
			                    	//tempo è stato modificato (utile quando l'utente salverà per sapere subito che tempi modificare nel db)
			                    	Tempo modifiedTime = mTimeButtonMap.get(timeButton);
			                    	
			                    	//Setto il nuovo tempo...
			                    	modifiedTime.setTempo(firstDigit + "" + secondDigit + "-" + thirdDigit + "" + fourthDigit);
			                    	
			                    	//...e lo aggiungo all'array che contiene tutti i tempi modificati dall'utente
			                    	mModifiedTimes.add(modifiedTime);
			                    	
			                    	
			                    	//Segnalo che l'utente ha modificato almeno un tempo; serve per mostrare l'alert dialog 
			                    	hasModifiedTimes = true;
			        			}
			        			
			        		});

			        		//Infine mostro la dialog del timepicker
			        		timePicker.show();
			            }
			        });

	    			
	    			//Salvo il tempo corrispondente al bottone sopra creato, in modo tale da avere un riferimento al tempo
	    			//quando il bottone viene premuto ed il rispettivo tempo viene modificato
	    			mTimeButtonMap.put(timeButton, time);
			        
			        //Modificati tutti i campi, procedo con inserire la view appena creata nel contenitore
			        myRoot.addView(view);
		    	}
		    	
		    	//Avvio l'animazione per lo sliding down...
		    	myRoot.startAnimation(mSlideDown);
		    	
		    	//...e rendo visibile la view, che di default è invisibile (altrimenti si vedrebbe una riga sottile bianca
		    	//sotto ogni allenamento
		    	myRoot.setVisibility(View.VISIBLE);
	    	}
	    	//Se la lista dei tempi non era null, vuol dire che era già stato premuto quel dato atleta e le view
	    	//di ogni tempo sono già state create; quindi basta riportare visibile il contenitore
	    	else
	    	{	    		
	    		//Rendo visibile la view...
	    		myRoot.setVisibility(View.VISIBLE);
	    		
	    		//...e faccio partire l'animazione
	    		myRoot.startAnimation(mSlideDown);
	    	}
		    
		    //Infine salvo il nodo root attuale come "nuovo precedente", da usare "al prossimo giro"
		    mPreviousRoot = myRoot;
	    }
	    
	    //In ogni caso, terminata l'esecuzione salvo il riferimento all'oggetto della lista clickato
	    mPreviousClickedListItem = clickedView;
	}

	
	
	/**
	 * Metodo che mette tutti i tempi modificati dall'utente nel db
	 */
	public void saveAthletesTimes()
	{
		//Scorro l'array che contiene tutti i tempi modificati..
		for(Tempo time : mModifiedTimes)
		{			
			//...e per ogni tempo chiamo l'apposito metodo del db
			mDb.updateAthletesTime(time.getId(), time.getTempo());
		}
		
		Toast.makeText(this, "Dati modificati correttamente!", Toast.LENGTH_LONG).show();
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
			alertDialogBuilder.setMessage("Sei sicuro di voler tornare indietro? Le modifiche effettuate verranno perse!");
			
			
			//Setto un listener per il bottone positivo
			alertDialogBuilder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog,int id) 
				{
					//Se si preme il tasto, termino l'activity e metto l'animazione per lo slide verso sinistra
					finish();
					overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
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
	 * Creazione dell'icona nell'action bar
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		getMenuInflater().inflate(R.menu.new_training, menu);
		return true;
	}
	
    
	/**
	 * Metodo chiamato quando si preme un bottone messo nell'action bar
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		switch (item.getItemId()) 
		{
		    case android.R.id.home:
    			onBackPressed();	
		        return true;
		        
	        //Se si preme il tasto per salvare, metto nel db i tempi modificati e torno indietro all'activity precedente
		    case R.id.save:
		    	saveAthletesTimes();

		    	//Salvati i dati, chiudo l'activity e avvio l'animazione per lo slide verso sinistra
		    	finish();
				overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
				
		    	return true;
	    }
		
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Chiamato quando si preme il back fisico del cell
	 */
	@Override
	public void onBackPressed() 
	{
		//Se era stata creata l'alert dialog ed era presente, la rimuovo
		if(mAlertDialog != null && mAlertDialog.isShowing())			
			mAlertDialog.dismiss();
		
		//Altrimenti controllo se l'utente ha modificato dei tempi; in tal caso faccio apparire la dialog per chiedere se vuole
		//tornare indietro senza salvare
		else if(hasModifiedTimes)
			showAlertDialog();
		else
		{
			//Altrimenti termino l'activity e metto l'animazione per lo slide verso sinistra
			finish();
			overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
			
			super.onBackPressed();
		}	
	}
	
	/**
	 * Chiamato quando l'activity muore. Qua dentro bisogna chiudere il db
	 */
	@Override
	protected void onDestroy() 
	{
		this.mDb.close();
		super.onDestroy();
	}

	
	
}
