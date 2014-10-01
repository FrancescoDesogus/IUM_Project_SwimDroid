package com.example.swimdroid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;





import net.londatiga.android.popupwindow.ActionItem;
import net.londatiga.android.popupwindow.QuickAction;





import com.android.datetimepicker.Utils;
import com.android.datetimepicker.time.TimePickerDialog;
import com.example.model.Atleta;
import com.example.model.DBTrainingsData;
import com.example.model.Esercizio;
import com.example.model.Tempo;
import com.example.model.Training;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.webkit.WebView.FindListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Fragment usato quando nella day view sono presenti degli allenamenti da mostrare sotto forma di lista
 * 
 * @author Francesco
 *
 */
public class DayViewFragmentFull extends ListFragment
{
	private ArrayList<Training> mTrainings; //Array contenente la lista degli allanamenti da mostrare
	private TrainingsArrayAdapter mArrayAdapter; //Adattatore per la ListView
	
	private DayViewActivity mActivity; //L'activity che ospita il frammento
	private Context mContext; //Il contesto dell'activity
	private DBTrainingsData mDb; //Il database, preso dall'activity
	
	private View mPreviousClickedListItem; //Riferimento all'elemento della lista clickato in precedenza
	
	private Training mCurrentlyOpenedTraining; //Riferimento all'allenamento della lista attualmente clickato (se c'è)
	private View mCurrentlyOpenedTrainingView; //Riferimento alla view dell'allenamento correntemente aperto (se c'è)
	private LinearLayout mPreviousRoot; //Il nodo root precedente che contiene il layout della lista degli esercizi precedentemente aperta (se c'è)
	
	private LayoutInflater mInflater; //Oggetto per inflatare view; per non crearlo ogni volta che lo si fa, l'ho messo come campo della classe
		
	private HashMap<Integer, ArrayList<Esercizio>> mExercisesListMap; //Map che relaziona l'id di un allenamento con la lista dei suoi esercizi
	private HashMap<Esercizio, View> mExercisesListEntryViewMap; //Map che relaziona un esercizio con la sua view nella lista degli esercizi
	
	private Animation mSlideDown; //Animazione per lo sliding down delle liste degli esercizi degli allenamenti
	private Animation mSlideUp; //Animazione per lo sliding up delle liste degli esercizi degli allenamenti

	private QuickAction mQuickAction; //Popup che appare con un long click per la modifica/cancellazione
	public static final int POPUP_ACTION_MODIFY = 0; //Costante usata come id per l'azione di modifica nella finestra di popup
	public static final int POPUP_ACTION_DELETE = 1; //Costante usata come id per l'azione di eliminazione nella finestra di popup
	public static final int POPUP_ACTION_ADD_EXERCISE = 2; //Costante usata come id per l'azione di eliminazione nella finestra di popup

	private View mLongClickedTrainingView; //Riferimento alla view dell'allenamento long-clickato (l'esercizio a cui fa riferimento il popup)
	private Training mLongClickedTraining; //Riferimento all'allenamento long-clickato (in sostanza, l'esercizio a cui si riferisce il popup)
	private int mLongClickedTrainingPositionInList; //Posizione in lista dell'allenamento long-clickato
	
	
	/**
	 * Primo metodo chiamato una volta creato il frammento; in questo punto non c'è ancora la view e l'activity
	 * non è stata del tutto creata. E' un metodo fatto per salvarsi il riferimento all'activity padre
	 */
	@Override
	public void onAttach(Activity pParent)
	{
		super.onAttach(pParent);
		
		//Salvo i riferimenti all'activity e al contesxt
		mActivity = (DayViewActivity) pParent;
		mContext = pParent.getBaseContext();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		setHasOptionsMenu(true);
	    super.onCreate(savedInstanceState);
	
	    //Recupero gli allenamenti da visualizzare ed il db dall'activity
	    mTrainings = mActivity.getTrainings();
		mDb = mActivity.getDatabase();

		//Istanzio le map, usate quando devono essere mostrati gli esercizi per migliorare l'efficienza
		mExercisesListMap = new HashMap<Integer, ArrayList<Esercizio>>();
		mExercisesListEntryViewMap = new HashMap<Esercizio, View>();

	    //Istanzio l'oggetto per inflatare views, usato quando bisogna mostrare gli esercizi di un allenamento
	    mInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    
	    //Istanzio le animazioni per le liste degli esercizi
    	mSlideDown = AnimationUtils.loadAnimation(mContext, R.anim.slide_down);
    	mSlideUp = AnimationUtils.loadAnimation(mContext, R.anim.slide_up);
    	
	    //Istanzio l'ArrayAdapter della lista degli allenamenti passando il contesto, il layout di ogni entry
	    //della lista e la lista stessa
	    mArrayAdapter = new TrainingsArrayAdapter(mContext, R.layout.training_list_item, mTrainings);
	    setListAdapter(mArrayAdapter);

	    
	    //Adesso devo creare la finestra di popup; per farlo, creo prima gli item che conterrà, dando a ciascuno
	    //un id, una label da far apparire e un'icona
	    ActionItem modifyItem = new ActionItem(POPUP_ACTION_MODIFY, "Modifica", getResources().getDrawable(R.drawable.ic_edit_white));
		ActionItem deleteItem = new ActionItem(POPUP_ACTION_DELETE, "Elimina", getResources().getDrawable(R.drawable.ic_delete));
		ActionItem addExerciseItem = new ActionItem(POPUP_ACTION_ADD_EXERCISE, "Agg. Esercizio", getResources().getDrawable(R.drawable.ic_add));

		
    	//Istanzio il popup
		mQuickAction = new QuickAction(mContext);
		
		//Aggiungo gli item sopra creati
		mQuickAction.addActionItem(deleteItem);
		mQuickAction.addActionItem(modifyItem);
		mQuickAction.addActionItem(addExerciseItem);

		
		//Infine associo un click listener per far qualcosa quando si preme un item
		mQuickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
			@Override
			public void onItemClick(QuickAction quickAction, int pos, int actionId) 
			{
				//Controllo quale item è stato premuto
				switch(actionId)
				{
				//Caso in cui si deve modificare l'allenamento
				case POPUP_ACTION_MODIFY:
					
					//Creo l'intent per avviare l'activity per la creazione di un nuovo allenamento
					Intent i = new Intent(getActivity(), NewTrainingActivity.class); 
					i.putExtra("dayString", ((DayViewActivity) getActivity()).getSelectedDay()); //Metto come extra il giorno della settimana selezionata
					i.putExtra("dataString", ((DayViewActivity) getActivity()).getSelectedDate()); //Setto inoltre il giorno dell'anno selezionato
					i.putExtra("data", ((DayViewActivity) getActivity()).getSelectedLong()); //Setto la data in millisecondi
					i.putExtra("trainingId", (long) mLongClickedTraining.getId_allenamento()); //Infine metto l'id dell'allenamento da modificare
					startActivity(i); //Avvio l'activity
					getActivity().overridePendingTransition(R.anim.slide_right_in, R.anim.slide_out_fade); //e setto l'animazione
					
					break;
					
				//Caso in cui si deve eliminare l'allenamento
				case POPUP_ACTION_DELETE: 
					
					showDeleteTrainingDialog(mLongClickedTraining);
					
					break;
					
				//Caso in cui si deve aggiungere un esercizio all'allenamento
				case POPUP_ACTION_ADD_EXERCISE:
					
					showAddExerciseDialog(mLongClickedTraining, mLongClickedTrainingPositionInList);
					
					break;
				}
			}
		});
	}
	
	/**
	 * Metodo chiamato dopo l'onCreate, si occupa di creare la view e basta
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
	    //Ritorno la view che ha come layout la ListView				
	    return inflater.inflate(R.layout.day_view_training_list, container, false);
	}
	
	/**
	 * Metodo chiamato non appena l'activity che ospita il fragment è stata totalmente inizializzata. Qua dentro
	 * setto il listener per i long click sulle entry della lista degli allenamenti. Mi serve farlo qua perchè mi serve
	 * un riferimento alla ListView, che è parte dell'activity di fatto e quindi devo assicurarmi che l'activity
	 * sia del tutto inizializzata prima di recuperare la lista
	 */
	@Override
	public void onActivityCreated (Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		
		//Recupero la ListView e le associo il listener per i long click
		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {

	        @Override
	        public boolean onItemLongClick(AdapterView<?> listView, View clickedView, int positionInList, long rowId) 
	    	{	        	
	        	/* Il popup deve apparire con la freccia verso l'alto sotto all'entry premuta se c'è spazio per metterla,
	        	 * altrimenti deve apparire sopra l'entry con la freccia rivolta verso il basso. Questo normalmente funziona
	        	 * di default, ma se la lista degli esercizi è aperta allora ci son problemi perchè l'entry dell'allenamento
	        	 * è visto come una view molto più grande vista la presenza degli esercizi, ed il popup viene messo tutto
	        	 * male. Per fixxare questo problema, controllo se la view creata corrisponde all'allenamento correntemente
	        	 * "aperto" (qualora ci fosse); in tal caso, passo al popup la view contenente solo l'entry dell'allenamento,
	        	 * in modo tale da non considerare gli esercizi (la view che viene passata è l'anchor del popup, cioè
	        	 * sostanzialmente serve a dire in base a cosa deve posizionarsi). Altrimenti, se è stata premuta un'entry
	        	 * "chiusa", passo direttamente la view clickata. Non posso passare anche in questo caso la view contenente
	        	 * solo l'entry dell'allenamento, perchè altrimenti il popup non si metterebbe correttamente se si preme
	        	 * su un allenamento molto in basso nello schermo (anche se non mi è chiaro il perchè) */
	        	if(clickedView == mCurrentlyOpenedTrainingView)
	        	{
	        		View anchorView = clickedView.findViewById(R.id.trainingEntryContainer);
	        		mQuickAction.show(anchorView);
	        	}
	        	else 
	        		mQuickAction.show(clickedView);
	        	
	        	//Aggiungo riferimenti rispettivamente all'allenamento long-clickato, la sua view e la sua posizione
	        	//all'interno della ListView
	        	mLongClickedTraining = mTrainings.get(positionInList);
	        	mLongClickedTrainingView = clickedView;
	        	mLongClickedTrainingPositionInList = positionInList;
	        		
	    		return true;
	    	}
	    });
	}
	
	/**
	 * Metodo chiamato quando viene premuto un elemento della lista. Nel nostro caso, bisogna far uscire la lista
	 * degli esercizi sotto l'allenamento premuto. Per farlo non conviene mettere un'altra ListView, principalmente per il
	 * fatto che sarebbe scrollabile e non sarebbe corretto (nel senso che l'unica ListView che deve scorrere è quella
	 * "esterna" degli allenamenti). 
	 * Una soluzione è creare la lista come un LinearLayout con dentro a sua volta il layout di ogni esercizio, che è
	 * quello che fa questo metodo. Per maggiore efficienza, quando si preme su un allenamento e viene creata la lista 
	 * dei suoi esercizi dinamicamente, la si mantiene in memoria in modo tale che se si deve mostrare di nuovo quella 
	 * lista in un secondo momento non si debba allocare e creare niente.
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
	    
	    //Salvo il riferimento alla view attualmente clickata
	    mCurrentlyOpenedTrainingView = clickedView;
	    
	    //Salvo il riferimento all'allenamento attualmente aperto
	    mCurrentlyOpenedTraining = mTrainings.get(positionInList);
	    
	    //Creo un'animazione di un piccolo rimbalzo per la view clickata, per dare un senso di feedback
	    ObjectAnimator pulseAnimator = Utils.getPulseAnimator(clickedView, 0.98f, 1.01f);
        pulseAnimator.start();
        
        //Recupero la freccia della view clickata, che bisognerà animare in qualche modo a seconda che sia già stata
        //clickata la view oppure no
        View arrow = clickedView.findViewById(R.id.arrow);
    	Animation anim = null;
        
	    //Recuperaro il layout che sarà il contenitore della lista degli esercizi
	    final LinearLayout myRoot = (LinearLayout) clickedView.findViewById(R.id.day_view_exercise_list_container);

	    	    
	    //Se il nodo root è uguale al precedente, vuol dire che è stato premuto lo stesso allenamento, quindi bisogna
	    //togliere la lista e basta
	    if(myRoot == mPreviousRoot)
	    {	    		    	
	    	//Setto a null il nodo precedente, in modo tale che se al prossimo click si preme sullo stesso elemento
	    	// di prima non si cada di nuovo in questa if
	    	mPreviousRoot = null;
	    	
	    	//Setto a null l'allenamento correntemente aperto, visto che ora non ce ne sono più
	    	mCurrentlyOpenedTraining = null;
	    	
	    	//Se la view clickata era la stessa di prima, vuol dire che la freccia deve animarsi al contrario per tornare
	    	//a puntare verso il basso; quindi creo l'animazione e la metto nella freccia
	    	anim = new AnimationUtils().loadAnimation(getActivity(), R.anim.arrow_rotation_reverse);
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
					//Una volta finita l'animazione, nascondo la view che contiene la lista degli esercizi
					myRoot.setVisibility(View.GONE);		
				}
			});
	    	
	    	//Faccio partire l'animazione per lo sliding up
	    	myRoot.startAnimation(mSlideUp);
	    }
	    //Se l'entry clickata non è la stessa di prima, procedo col popolarla (o con renderla visibile, se era già stata creata)
	    else
	    {	    		    	
	    	//In modo da far si che se c'è già una lista esercizi aperta quando se ne preme un'altra la vecchia si chiuda,
		    //controllo se esiste un nodo precedente e nel caso lo rendo invisibile prima di procedere
	    	if(mPreviousRoot != null)
	    	{
		    	mPreviousRoot.setVisibility(View.GONE);
		    	
		    	//Recupero anche la freccia dell'elemento della lista clickato la volta prima, che dovrà animarsi
		    	//per tornare a puntare verso il basso
		    	View arrowPrevious = mPreviousClickedListItem.findViewById(R.id.arrow);
		    	
		    	//Creo l'animazione e la setto alla freccia
		    	Animation animPrevious = new AnimationUtils().loadAnimation(mContext, R.anim.arrow_rotation_reverse);
		    	arrowPrevious.setBackgroundResource(R.drawable.ic_expand_holo);
		    	arrowPrevious.setAnimation(animPrevious);
		    	arrowPrevious.startAnimation(animPrevious);	
	    	}
	    	
	    	//Creo l'animazione per l'oggetto della lista appena clickato in modo che la freccia vada a puntare verso l'alto
	    	anim = new AnimationUtils().loadAnimation(getActivity(), R.anim.arrow_rotation);
			arrow.setBackgroundResource(R.drawable.ic_collapse_holo);
	    	arrow.setAnimation(anim);
	    	arrow.startAnimation(anim);	    	
	    	
	    	//Recupero l'id dell'allenamento clickato
	    	int trainingId = mTrainings.get(positionInList).getId_allenamento();
	    	
	    	//Usando l'id appena preso, recupero dalla map la lista degli esercizi corrispondenti a quell'id, qualora
	    	//ci fosse già (se non c'è viene restituito null)
	    	ArrayList<Esercizio> exercises = mExercisesListMap.get(trainingId);
	    	
	    	/* Se la lista degli esercizi è null, vuol dire che è la prima volta che è stata premuta quell'entry
	    	 * della lista, quindi vuol dire che bisogna recuperare gli esercizi dal db, creare le view per ogni entry
	    	 * e salvarle in memoria in modo tale che se si dovesse mostrare in un secondo momento la lista degli esercizi
	    	 * di questo dato allenamento non si debba rifare tutto da zero */
	    	if(exercises == null)
	    	{
	    		exercises = mDb.getExercisesInTraining(mTrainings.get(positionInList).getId_allenamento());
	    		
	    		//Ora che ho recuperato gli esercizi, li salvo nella map
	    		mExercisesListMap.put(trainingId, exercises);

	    		//Adesso scorro tutti gli esercizi previsti per l'allenamento; bisognerà creare la view di ogni esercizio
		    	for(final Esercizio exercise : exercises)
		    	{
		    		//Inflato nella view il layout di una singola entry della lista
		    		final View view = mInflater.inflate(R.layout.day_view_exercise_list_item, null, true);
		    		
		    		//Recupero il container superiore dell'entry dell'esercizio...
		            final RelativeLayout entryContainer = (RelativeLayout) view.findViewById(R.id.day_view_exercise_list_item_supercontainer);

		            //...e setto un listener che non fa niente; questo è per evitare che premendo su un esercizio si attivi
		            //l'onItemClick della lista, chiudendola (o se è un long click, aprendo un popup)
		            entryContainer.setOnLongClickListener(new View.OnLongClickListener()
			        {
						@Override
						public boolean onLongClick(View v) 
						{							
							return true;
						}
			       });
		            
		    			
		    		//Salvo la view corrispondente all'esercizio nella map, in modo da mantenere sempre il riferimento
	    			mExercisesListEntryViewMap.put(exercise, view);
		    			
	    			//Adesso bisogna popolare le TextView del layout (di default vuote) con i valori del particolare esercizio
	    			TextView textViewValue = (TextView) view.findViewById(R.id.ripetizioni_per_distanza_value);
	    			textViewValue.setText(exercise.getRipetizioni() + " x " + exercise.getDistanza() + "m");
	    			
	    			textViewValue = (TextView) view.findViewById(R.id.stile_value);
	    			textViewValue.setText(exercise.getStile1() + " - " + exercise.getStile2());
	    			
	    			textViewValue = (TextView) view.findViewById(R.id.tipologia_value);
	    			textViewValue.setText(exercise.getAndatura());
	    			
	    			//Per stampare il tempo di percorrenza uso uno scanner per dividere i minuti dai secondi, essendo
	    			//il tempo salvato nel db con la forma 00-00
	    			Scanner timeScanner = new Scanner(exercise.getTempo()).useDelimiter("-");
	    			

	    			String minutes = timeScanner.next();
	    			String seconds = timeScanner.next();
	    			
	    			timeScanner.close();
	    			
	    			textViewValue = (TextView) view.findViewById(R.id.tempo_di_percorrenza_value);
	    			textViewValue.setText(minutes + "'" + seconds + "''");
	    			
	    			
	    			ImageView deleteImage = (ImageView) view.findViewById(R.id.delete_exercise_image);
	    			
	    			deleteImage.setOnClickListener(new View.OnClickListener()
			        {
			            @Override
			            public void onClick(View v) 
			            {	            	
			            	showDeleteExerciseDialog(exercise);
			            }
			        });
	    			
	    			
	    			ImageView modifyImage = (ImageView) view.findViewById(R.id.modify_exercise_image);
	    			
	    			modifyImage.setOnClickListener(new View.OnClickListener()
			        {
			            @Override
			            public void onClick(View v) 
			            {	            	
			            	showModifyExerciseDialog(view, exercise, positionInList);
			            }
			        });
	    			
	    			
	    			//Modificati tutte le TextView, procedo con inserire la view appena creata nel contenitore
			        myRoot.addView(view);
			        
			        //Recupero il bottone per settare l'onClickListener che porterà all'activity per la 
			        //visualizzazione dei tempi degli atleti
			        Button showTimeButton = (Button) view.findViewById(R.id.visualizza_tempi);
			        			        
			        //Setto il click listener per creare la nuova activity quando si preme il bottone, passando le 
			        //informazioni necessarie alla visualizzazione dei tempi degli atleti
			        showTimeButton.setOnClickListener(new View.OnClickListener()
			        {
			            @Override
			            public void onClick(View v) 
			            {	            	
			            	//Creo l'intent per avviare la nuova activity usando il contesto e specificando la classe
			            	//della nuova activity da usare
			            	Intent intent = new Intent(mContext, AthletesTimesActivity.class);
			            	
			            	/* Inserisco l'esercizio come extra dell'intent, in modo da recuperarlo all'interno della nuova
			            	 * Activity. Nota: per poter far questo, la classe Esercizio deve implementare l'interfaccia
			            	 * Parcelable. More info dentro il file della classe Esercizio.
			            	 * Passo anche l'id del gruppo che dovrà fare l'esercizio, in modo da recuperare in seguito
			            	 * gli atleti che lo compongono */
			            	intent.putExtra("esercizio", exercise);
			            	intent.putExtra("groupId", mTrainings.get(positionInList).getId_gruppo());
			            	
			            	//Avvio l'activity con l'intent creato...
							startActivity(intent);
							
							//...e faccio partire l'animazione dello slide (nota: parte dalla classe padre di questo 
							//fragment, non dal fragment stesso)
							mActivity.overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
			            }
			       });
		    	}

		    	//Avvio l'animazione per lo sliding down...
		    	myRoot.startAnimation(mSlideDown);
		    	
		    	//...e rendo visibile la view, che di default è invisibile (altrimenti si vedrebbe una riga sottile bianca
		    	//sotto ogni allenamento
		    	myRoot.setVisibility(View.VISIBLE);
	    	}
	    	//Se la lista degli esercizi non era null, vuol dire che era già stato aperto quel dato allenamento e le view
	    	//di ogni esercizio sono già state create; quindi basta riportare visibile il contenitore
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
	 * Metodo per mostrare la dialog che consente la modifica di un dato esercizio
	 * 
	 * @param currentExerciseView la view dell'esercizio com'era prima della modifica
	 * @param exercise l'esercizio in questione
	 * @param positionInList la posizione nella lista dell'allenamento "padre" della lista degli esercizi
	 */
	public void showModifyExerciseDialog (final View currentExerciseView, final Esercizio exercise, final int positionInList)
	{
		//Creo la dialog passando il contesto
		final Dialog dialog = new Dialog(mActivity);
    	
		//Recupero il layout della dialog e metto un titolo
		dialog.setContentView(R.layout.exercise_dialog_form);
		dialog.setTitle("Modifica esercizio");
		
		
		//Creo il NumberPicker per gestire i bottoni e gli EditText delle ripetizioni e della distanza; gli passo la view
		//più esterna che contiene i bottoni e gli EditText
		final NumberPicker numberPicker = new NumberPicker(getActivity(), dialog.findViewById(R.id.dialog_container));
    	
		//Inserisco nei rispettivi EditText i valori attuali delle ripetizioni e della distanza
		numberPicker.setRipText(exercise.getRipetizioni());
		numberPicker.setDistText(exercise.getDistanza());
		
    	
    	//Prelevo l'array di valori dello spinner per lo stile1 dall'array salvato in string.xml
    	ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(getActivity().getBaseContext(), R.array.stiliSpinner1, android.R.layout.simple_spinner_item);
    	
    	//Recupero lo spinner per lo stile1
    	Spinner style1 = (Spinner) dialog.findViewById(R.id.spinnerStyle1);
    	
    	//Specifico il layout da usare quando si preme lo spinner
    	adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	
    	//Setto l'adattatore nello spinner
    	style1.setAdapter(adapter1);
    	
    	/* Salvo inoltre il valore corrente dello spinner come quello selezionato dall'utente, in modo tale che se per 
    	 * esempio l'utente modifica tutti i dati eccetto questo, verrà salvato questo valore. NOTA: la stringa 
    	 * è messa come array di 1 elemento per fregare java, nel senso che mi serve poter modificare la stringa
    	 * anche dentro un metodo chiamato in un listener, ma non posso farlo se la stringa non è final (ed in tal caso
    	 * non potrei assegnarli più nulla) */
    	final String[] selectedStyle1 = {exercise.getStile1()};  
    	
    	//Metto il valore attuale dello stile1 come valore da mostare inizialmente nello spinner
    	style1.setSelection(adapter1.getPosition(exercise.getStile1()));  	
    	
    	//Metto il listener per la selezione di un elemento nello spinner
    	style1.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) 
			{
				//Al click, cambio il valore selezionato con quello premuto
		    	selectedStyle1[0] = parent.getItemAtPosition(pos).toString();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) 
			{
				
			}
    		
		});
    	
    	//Recupero lo spinner dello stile2 e faccio uguale a come fatto sopra
    	ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(getActivity().getBaseContext(), R.array.stiliSpinner2, android.R.layout.simple_spinner_item);
    	
    	Spinner style2 = (Spinner) dialog.findViewById(R.id.spinnerStyle2);

    	adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

    	style2.setAdapter(adapter2);
    	
    	final String[] selectedStyle2 = {exercise.getStile2()};
    	
    	style2.setSelection(adapter2.getPosition(exercise.getStile2()));
    	
    	style2.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) 
			{
				selectedStyle2[0]  = parent.getItemAtPosition(pos).toString();

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) 
			{
				
			}
    		
		});
    	
    	//Recupero lo spinner per l'andatura e faccio uguale a quanto fatto sopra
    	ArrayAdapter<CharSequence> andature = ArrayAdapter.createFromResource(getActivity().getBaseContext(), R.array.Andature, android.R.layout.simple_spinner_item);
    	
    	Spinner andatureSpinner = (Spinner) dialog.findViewById(R.id.andatureSpinner);

    	andature.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

    	andatureSpinner.setAdapter(andature);
    	
    	final String[] selectedAndatura = {exercise.getAndatura()};
    	
    	andatureSpinner.setSelection(andature.getPosition(exercise.getAndatura()));
    	
    	andatureSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) 
			{
				selectedAndatura[0] = parent.getItemAtPosition(pos).toString();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0)
			{

			}
    		
		});
    	
    	
    	//Setto il tempo di percorrenza selezionato dall'utente come quello attuale dell'esercizio
    	final String[] selectedTime = {exercise.getTempo()};
    	
    	//Adesso devo trasformare il tempo salvato nell'esercizio nella forma 00-00 in 00'00'', in modo da mostrarlo;
    	//uso quindi uno scanner per separare i minuti dai secondi
		Scanner timeScanner = new Scanner(exercise.getTempo()).useDelimiter("-");
    	
    	String minutes = timeScanner.next();
		String seconds = timeScanner.next();
		
		timeScanner.close();
			    			
		//Recupero il bottone che mostra il tempo e setto il testo
		final Button timeButton = (Button) dialog.findViewById(R.id.PercorrenzaTimePicker);
		timeButton.setText(minutes + "'" + seconds + "''");
				
		//Associo al bottone il listener per far aprire la dialog per modificare il tempo se premuto
		timeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) 
            {	
            	//Creo il timepicker col number pad passando come parametro il bottone che causerà l'apertura della
            	//dialog, in modo tale che una volta settato il tempo il testo del bottone venga direttamente cambiato
            	TimePickerPad timePicker = new TimePickerPad(mActivity, timeButton);
        		
            	//Creo la dialog vera e propria
        		timePicker.createTimePickerDialog();
        		
        		//Setto un listener per recuperare il nuovo tempo una volta che l'utente ha confermato
        		timePicker.setOnTimeSetListener(new OnTimeSetListener()
        		{
        			@Override
        			public void onTimeSet(int firstDigit, int secondDigit, int thirdDigit, int fourthDigit) 
        			{
        				selectedTime[0] = firstDigit + "" + secondDigit + "-" + thirdDigit + "" + fourthDigit;
        			}
        			
        		});
        		
        		//Infine mostro la dialog del timepicker
        		timePicker.show();
            }
        });
    	
		
		//Recupero il bottone per la conferma della modifica dell'esercizio...
		Button doneButton = (Button) dialog.findViewById(R.id.done_button);
				
		//...e ne associo un listener per far chiudere la dialog salvando i dati
		doneButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) 
            {	 
            	//Controllo se le ripetizioni sono minori di uno e mostro un errore nel caso, terminando la funzione
            	if(numberPicker.getRip() < 1)
            	{
		 			Toast.makeText(mActivity, "Il numero di ripetizioni dev'essere almeno 1!", Toast.LENGTH_LONG).show();
		 			return;
		 		}
		 		
            	//Controllo anche se la distanza è minore di uno e mostro un errore nel caso, terminando la funzione
		 		if(numberPicker.getDist() < 1)
		 		{
		 			Toast.makeText(mActivity, "La distanza dev'essere di almeno 25m!", Toast.LENGTH_LONG).show();
		 			return;
		 		}
		 		
            	//Se è tutto ok, procedo col rimuovere l'associazione attuale exercise -> view dall'hashmap, 
		 		//dato che ora ne verrà messa una nuova
            	mExercisesListEntryViewMap.remove(exercise);
            	
            	//Recupero le vecchie e le nuove ripetizioni. Infatti se il loro valore non è uguale, devo aggiungere
            	//o togliere i rispettivi tempi degli atleti per quelle ripetizioni dal db
            	int newRepetitions = numberPicker.getRip();
            	int oldRepetitions = exercise.getRipetizioni();
            	
            	//Se il numero inserito è maggiore di quello vecchio, devo aggiungere deii tempi nel db
            	if(newRepetitions > oldRepetitions)
            	{
            		//Recupero il numero di tempi totale che devo aggiungere
            		int difference = newRepetitions - oldRepetitions;
      
            		//Recupero l'id del gruppo dell'allenamento; infatti dovrò aggiungere i tempi delle ripetizioni
            		//per ogni atleta presente nel gruppo
            		int groupId = mTrainings.get(positionInList).getId_gruppo();
            		
            		//Recupero gli atleti
            		ArrayList<Atleta> athletes = mDb.getAthletesInGroup(groupId);
            			
            		//Scorro tante volte quante sono le nuove ripetizioni da inserire
            		for(int i = 1; i <= difference; i++)
            		{	
            			//Per ogni atleta inserisco il nuovo tempo (con valore di default 00-00)
            			for(Atleta athlete : athletes)
            				mDb.addTime(null, athlete.getId(), exercise.getId(), oldRepetitions + i, "00-00");
            		}
            	}
            	//Altrimenti, se le nuove ripetizioni sono meno delle vecchie, devo cancellare quelle che ora sono in più
            	else if (newRepetitions < oldRepetitions)
            	{
            		//Passo il valore delle nuove ripetizioni al metodo. Tutti i tempi con valore > di newRepetitions
            		//verranno cancellati
            		mDb.deleteTimesFromRepetition(exercise.getId(), newRepetitions);
            	}
            	            	
            	//Aggiorno il contenuto dell'esercizio con i campi prelevati dal form
            	exercise.setRipetizioni(numberPicker.getRip());
            	exercise.setDistanza(numberPicker.getDist());
            	exercise.setStile1(selectedStyle1[0]);
            	exercise.setStile2(selectedStyle2[0]);
            	exercise.setAndatura(selectedAndatura[0]);
            	exercise.setTempo(selectedTime[0]);
            	
            	//Creo la nuova view dell'esercizio; il procedimento è analogo a quello presente in onListItemClick
            	final View newView = mInflater.inflate(R.layout.day_view_exercise_list_item, null, true);
            	
                final RelativeLayout entryContainer = (RelativeLayout) newView.findViewById(R.id.day_view_exercise_list_item_supercontainer);
                
                entryContainer.setOnLongClickListener(new View.OnLongClickListener()
		        {
					@Override
					public boolean onLongClick(View v) 
					{							
						return true;
					}
		        });
                

                mExercisesListEntryViewMap.put(exercise, newView);
    			
                TextView textViewValue = (TextView) newView.findViewById(R.id.ripetizioni_per_distanza_value);
    			textViewValue.setText(exercise.getRipetizioni() + " x " + exercise.getDistanza() + "m");
    			
    			textViewValue = (TextView) newView.findViewById(R.id.stile_value);
    			textViewValue.setText(exercise.getStile1() + " - " + exercise.getStile2());
    			
    			textViewValue = (TextView) newView.findViewById(R.id.tipologia_value);
    			textViewValue.setText(exercise.getAndatura());
        		

    			
        		Scanner timeScanner = new Scanner(exercise.getTempo()).useDelimiter("-");

        		String minutes = timeScanner.next();
        		String seconds = timeScanner.next();
        		
        		timeScanner.close();
        		
        		textViewValue = (TextView) newView.findViewById(R.id.tempo_di_percorrenza_value);
        		textViewValue.setText(minutes + "'" + seconds + "''");
        		
        		
        		ImageView deleteImage = (ImageView) newView.findViewById(R.id.delete_exercise_image);
        		
        		deleteImage.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v) 
                    {	            	
                    	showDeleteExerciseDialog(exercise);
                    }
                });
        		
        		
        		ImageView modifyImage = (ImageView) newView.findViewById(R.id.modify_exercise_image);
        		
        		modifyImage.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v) 
                    {	            	
                    	showModifyExerciseDialog(newView, exercise, positionInList);
                    }
                });
        		
                
                Button showTimeButton = (Button) newView.findViewById(R.id.visualizza_tempi);
                			        
     
                showTimeButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v) 
                    {	            	
                    	Intent intent = new Intent(mContext, AthletesTimesActivity.class);
                    	
                    	intent.putExtra("esercizio", exercise);
                    	intent.putExtra("groupId", mTrainings.get(positionInList).getId_gruppo());
                    	
        				startActivity(intent);

        				mActivity.overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
                    }
                });
        	    
        	    
                /* Adesso che ho creato la view e settato i listener che bisognava settare, devo sostituire la view
                 * appena creata con la view precedente dell'esercizio. Per farlo, recupero il padre della view,
                 * che sarebbe sostanzialmente l'entry dell'allenamento nella ListView */
        	    ViewGroup parent = (ViewGroup) currentExerciseView.getParent();

        	    //Recupero la posizione della vista corrente nel genitore, in modo da sostituirla
                int index = parent.indexOfChild(currentExerciseView);
                
                //Rimuovo le view (compresa la nuova, anche se di fatto non è stata messa)...
                parent.removeView(currentExerciseView);
                parent.removeView(newView);
                
                //...ed inserisco la nuova view nel padre all'indice della precedente
                parent.addView(newView, index);
                
                
                //Aggiorno il db con i nuovi dati dell'esercizio
                mDb.updateExercise(exercise.getId(), exercise.getRipetizioni(), exercise.getDistanza(), exercise.getStile1(), exercise.getStile2(), exercise.getAndatura(), exercise.getTempo(), exercise.getId_Allenamento());
                
                //Stampo un toast per dare conferma all'utente della modifica
				Toast.makeText(mContext, "Esercizio modificato!", Toast.LENGTH_LONG).show();

                //Infine rimuovo la dialog
	            dialog.dismiss();
            }
        });
		
		
		//Recupero il bottone di cancel dalla dialog
		Button cancelButton = (Button) dialog.findViewById(R.id.cancel_button);
		
		//Associo un listener al bottone per far chiudere la dialog se clickato
		cancelButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) 
            {	 
            	dialog.dismiss();
            }
        });

		//Finita l'inizializzazione della dialog, la mostro
		dialog.show();
	}
	
	
	/**
	 * Metodo per mostrare la dialog che consente l'aggiunta di un esercizio all'allenamento selezionato
	 * 
	 * @param pTraining l'allenamento selezionato
	 * @param positionInList la posizione nella ListView dell'allenamento
	 */
	public void showAddExerciseDialog (final Training pTraining, final int positionInList)
	{
		final Dialog dialog = new Dialog(mActivity);
    	
		//Recupero il layout della dialog (riciclato da quello di modifica di un esercizio) e metto un titolo
		dialog.setContentView(R.layout.exercise_dialog_form);
		dialog.setTitle("Aggiungi esercizio");


		final NumberPicker numberPicker = new NumberPicker(getActivity(), dialog.findViewById(R.id.dialog_container));
    	
		
    	ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(getActivity().getBaseContext(), R.array.stiliSpinner1, android.R.layout.simple_spinner_item);
    	
    	Spinner style1 = (Spinner) dialog.findViewById(R.id.spinnerStyle1);
    	
    	adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	style1.setAdapter(adapter1);

    	final String[] selectedStyle1 = {(String) adapter1.getItem(0)};  
    	
    	style1.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) 
			{
				//Al click, cambio il valore selezionato con quello premuto
		    	selectedStyle1[0] = parent.getItemAtPosition(pos).toString();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) 
			{
				
			}
    		
		});
    	
    	ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(getActivity().getBaseContext(), R.array.stiliSpinner2, android.R.layout.simple_spinner_item);
    	
    	Spinner style2 = (Spinner) dialog.findViewById(R.id.spinnerStyle2);

    	adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	style2.setAdapter(adapter2);
    	
    	final String[] selectedStyle2 = {(String) adapter2.getItem(0)};
    	
    	style2.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) 
			{
				selectedStyle2[0]  = parent.getItemAtPosition(pos).toString();

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) 
			{
				
			}
    		
		});
    	

    	ArrayAdapter<CharSequence> andature = ArrayAdapter.createFromResource(getActivity().getBaseContext(), R.array.Andature, android.R.layout.simple_spinner_item);
    	
    	Spinner andatureSpinner = (Spinner) dialog.findViewById(R.id.andatureSpinner);

    	andature.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	andatureSpinner.setAdapter(andature);
    	
    	final String[] selectedAndatura = {(String) (String) andature.getItem(0)};
    	
    	andatureSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) 
			{
				selectedAndatura[0] = parent.getItemAtPosition(pos).toString();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0)
			{

			}
    		
		});
    	
    	
    	final String[] selectedTime = {"01-00"};
    				    			
		final Button timeButton = (Button) dialog.findViewById(R.id.PercorrenzaTimePicker);
		timeButton.setText("01'00\"");
				
		timeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) 
            {	
            	TimePickerPad timePicker = new TimePickerPad(mActivity, timeButton);
        		
        		timePicker.createTimePickerDialog();
        		
        		timePicker.setOnTimeSetListener(new OnTimeSetListener()
        		{
        			@Override
        			public void onTimeSet(int firstDigit, int secondDigit, int thirdDigit, int fourthDigit) 
        			{
        				selectedTime[0] = firstDigit + "" + secondDigit + "-" + thirdDigit + "" + fourthDigit;
        			}
        			
        		});
        		
        		timePicker.show();
            }
        });
    	
		
		
		//Recupero il bottone per la conferma della modifica dell'esercizio...
		Button doneButton = (Button) dialog.findViewById(R.id.done_button);
				
		//...e ne associo un listener per far chiudere la dialog salvando i dati
		doneButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) 
            {	 
            	//Controllo se le ripetizioni sono minori di uno e mostro un errore nel caso, terminando la funzione
            	if(numberPicker.getRip() < 1)
            	{
		 			Toast.makeText(mActivity, "Il numero di ripetizioni dev'essere almeno 1!", Toast.LENGTH_LONG).show();
		 			return;
		 		}
		 		
            	//Controllo anche se la distanza è minore di uno e mostro un errore nel caso, terminando la funzione
		 		if(numberPicker.getDist() < 1)
		 		{
		 			Toast.makeText(mActivity, "La distanza dev'essere di almeno 25m!", Toast.LENGTH_LONG).show();
		 			return;
		 		}
            	
            	//Creo il nuovo esercizio...
            	final Esercizio exercise = new Esercizio();
            	            	
            	//...e lo riempio con i campi prelevati dal form
            	exercise.setRipetizioni(numberPicker.getRip());
            	exercise.setDistanza(numberPicker.getDist());
            	exercise.setStile1(selectedStyle1[0]);
            	exercise.setStile2(selectedStyle2[0]);
            	exercise.setAndatura(selectedAndatura[0]);
            	exercise.setTempo(selectedTime[0]);
            	exercise.setId_Allenamento(pTraining.getId_allenamento());
            	
            	//Aggiungo l'esercizio al db e recupero il suo id
            	long exerciseId = mDb.addExercise(null, exercise.getRipetizioni(), exercise.getDistanza(), exercise.getStile1(), exercise.getStile2(), exercise.getAndatura(), exercise.getTempo(), pTraining.getId_allenamento());
        		
            	//Setto l'id appena preso nell'esercizio
        		exercise.setId((int) exerciseId);
        		
        		
        		//Recupero gli atleti del gruppo dell'allenamento dal db; dovrò infatti aggiungere i tempi per ognuno
        		//per ogni ripetizione dell'esercizio
        		ArrayList<Atleta> athletes = mDb.getAthletesInGroup(pTraining.getId_gruppo());
        		
        		//Scorro gli atleti e per ognuno inserisco i tempi di default
        		for(Atleta athlete : athletes)
        		{
        			int repetitions = exercise.getRipetizioni();
        			
        			for(int i = 1; i <= repetitions; i++)
        				mDb.addTime(null, athlete.getId(), exercise.getId(), i, "00-00");
        		}
            	
        		
        		/* Adesso le aggiunte al db sono state fatte. Bisogna aggiornare la vista però. Ci sono 3 casi possibili:
        		 * 1) l'allenamento in cui abbiamo inserito l'esercizio non è mai stato aperto ancora, cioè l'utente
        		 *    non ha ancora clickato su di esso per far spuntare i suoi esercizi. In questo caso, non si deve
        		 *    fare niente riguardo la view, perchè quando l'utente premerà sull'allenamento verranno recuperati
        		 *    dal db gli esercizi relativi ad esso (compreso quello appena inserito), e per ognuno verrà creata
        		 *    la view (come accade normalmente). E' il caso più semplice.
        		 * 2) l'allenamento è stato aperto in precedenza, ma non è quello aperto attualmente (se ce n'è uno aperto).
        		 *    In questo caso la sua lista degli esercizi è già stata recuperata ed è stata creata la view di ognuno.
        		 *    Quello che bisogna fare in questo caso è creare la view del nuovo esercizio ed inserirla nella view
        		 *    dell'allenamento. 
        		 * 3) l'allenamento è quello correntemente aperto. In questo caso si agisce in modo identico a quello del
        		 * 	  punto 2, con l'unica differenza che nell'aggiunta della view verrà inserita un'animazione di fade in
        		 *    (infatti mentre nel punto 2 la lista non è visibile dall'utente, qua lo è, ed è meglio mettere
        		 *    un'animazione per non farla comparire all'improvviso) */
        		
        		//Recupero gli esercizi associati all'allenamento nell'apposita map. Gli esercizi potrebbero esserci o non
        		//esserci, verrà controllato fra poco
        		ArrayList<Esercizio> currentExercises = mExercisesListMap.get(pTraining.getId_allenamento());
        		
        		//Dichiaro che l'allenamento considerato non è quello attualmente aperto (se ce ne fosse uno)
        		boolean isTrainingCurrentlyOpen = false;
        		
        		//Controllo se c'è un allenamento attualmente aperto dall'utente; nel caso, controllo se tale allenamento
        		//è quello che è stato long-clickato dall'utente
        		if(mCurrentlyOpenedTraining != null)
        			isTrainingCurrentlyOpen = pTraining.equals(mCurrentlyOpenedTraining);
        		
        		/* Se l'allenamento considerato è quello aperto, o non è quello aperto ma la lista degli esercizi non è null
        		 * (e quindi è stato aperto in precedenza), si ricade nei punti 2 o 3 descritti sopra. Il modo di agire 
        		 * per entrambi è praticamente uguale; quello che si fa è aggiungere la view dell'esercizio manualmente */
            	if(isTrainingCurrentlyOpen || (!isTrainingCurrentlyOpen && (currentExercises != null)))
            	{
    				//Aggiungo l'esercizio all'array che li contiene per questo dato allenamento (so per certo
            		//che questo array esiste ed ha almeno un elemento, altrimenti non sarei in quest'if)
        			currentExercises.add(exercise);
        			
        			
        			//Recupero la view contenente la lista degli esercizi dal riferimento all'allenamento long-clickato
        		    final LinearLayout myRoot = (LinearLayout) mLongClickedTrainingView.findViewById(R.id.day_view_exercise_list_container);

        		    //Adesso creo la nuova view dell'esercizio da inserire; il procedimento è uguale a quello
        		    //in onListItemClick, quindi no comment
		    		final View newView = mInflater.inflate(R.layout.day_view_exercise_list_item, null, true);
		    		
		            final RelativeLayout entryContainer = (RelativeLayout) newView.findViewById(R.id.day_view_exercise_list_item_supercontainer);

	
		            entryContainer.setOnLongClickListener(new View.OnLongClickListener()
			        {
						@Override
						public boolean onLongClick(View v) 
						{							
							return true;
						}
			       });
		            
	    			mExercisesListEntryViewMap.put(exercise, newView);
		    			
	    			TextView textViewValue = (TextView) newView.findViewById(R.id.ripetizioni_per_distanza_value);
	    			textViewValue.setText(exercise.getRipetizioni() + " x " + exercise.getDistanza() + "m");
	    			
	    			textViewValue = (TextView) newView.findViewById(R.id.stile_value);
	    			textViewValue.setText(exercise.getStile1() + " - " + exercise.getStile2());
	    			
	    			textViewValue = (TextView) newView.findViewById(R.id.tipologia_value);
	    			textViewValue.setText(exercise.getAndatura());

	    			
	    			Scanner timeScanner = new Scanner(exercise.getTempo()).useDelimiter("-");
	    			
	    			String minutes = timeScanner.next();
	    			String seconds = timeScanner.next();
	    			
	    			timeScanner.close();
	    			
	    			textViewValue = (TextView) newView.findViewById(R.id.tempo_di_percorrenza_value);
	    			textViewValue.setText(minutes + "'" + seconds + "''");
	    			
	    			
	    			ImageView deleteImage = (ImageView) newView.findViewById(R.id.delete_exercise_image);
	    			
	    			deleteImage.setOnClickListener(new View.OnClickListener()
			        {
			            @Override
			            public void onClick(View v) 
			            {	            	
			            	showDeleteExerciseDialog(exercise);
			            }
			        });
	    			
	    			
	    			ImageView modifyImage = (ImageView) newView.findViewById(R.id.modify_exercise_image);
	    			
	    			modifyImage.setOnClickListener(new View.OnClickListener()
			        {
			            @Override
			            public void onClick(View v) 
			            {	            	
			            	showModifyExerciseDialog(newView, exercise, positionInList);
			            }
			        });
	    			
	    			
			        myRoot.addView(newView);
			        

			        Button showTimeButton = (Button) newView.findViewById(R.id.visualizza_tempi);
			        			        
			        showTimeButton.setOnClickListener(new View.OnClickListener()
			        {
			            @Override
			            public void onClick(View v) 
			            {	            	
			            	Intent intent = new Intent(mContext, AthletesTimesActivity.class);

			            	intent.putExtra("esercizio", exercise);
			            	intent.putExtra("groupId", mTrainings.get(positionInList).getId_gruppo());
			            	
							startActivity(intent);

							mActivity.overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
			            }
			        });
			        
			        //Se l'allenamento long-clickato è quello aperto attualmente, aggiungo un'animazione di fade in
			        //per far comparire il nuovo allenamento
			        if(isTrainingCurrentlyOpen)
	            	{
						Animation fadeIn = AnimationUtils.loadAnimation(mContext, R.anim.fade_in);
						
						newView.startAnimation(fadeIn);		
	            	}
            	}
            	
            	
                //Stampo un toast per dare conferma all'utente della modifica
				Toast.makeText(mContext, "Esercizio aggiunto!", Toast.LENGTH_LONG).show();

                //Infine rimuovo la dialog
	            dialog.dismiss();
            }
        });
		
		
		//Recupero il bottone di cancel dalla dialog
		Button cancelButton = (Button) dialog.findViewById(R.id.cancel_button);
		
		//Associo un listener al bottone per far chiudere la dialog se clickato
		cancelButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) 
            {	 
            	dialog.dismiss();
            }
        });

		//Finita l'inizializzazione della dialog, la mostro
		dialog.show();
	}
	
	
	
	/**
	 * Crea l'AlertDialog per confermare la cancellazione di un esercizio, e procede col farla nel caso
	 * 
	 * @param pExercise l'esercizio da eliminare (se l'utente conferma)
	 */
	public void showDeleteExerciseDialog(final Esercizio pExercise)
	{		
		//Creo il builder della dialog. Tramite quello vengono settate tutte le sue caratteristiche prima di crearla
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity);
		 
		//Inserisco il titolo ed il messaggio da visualizzare
		alertDialogBuilder.setTitle("Attenzione!");
		alertDialogBuilder.setMessage("Sei sicuro di voler cancellare questo esercizio?");
		
		
		//Setto un listener per il bottone positivo
		alertDialogBuilder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog,int id) 
			{
				//Se l'utente ha premuto il bottone di conferma, elimino l'esercizio dal db
				mDb.deleteExercise(pExercise.getId());
				
				//Mostro un messaggio di conferma
				Toast.makeText(mContext, "Esercizio eliminato!", Toast.LENGTH_LONG).show();
				
				
				//Devo anche rimuovere l'esercizio dall'array che era associato all'id dell'allenamento di cui fa parte, in
				//modo tale da poter controllare in seguito se l'array è vuoto per fare qualcosa a riguardo
				final ArrayList<Esercizio> exercises = mExercisesListMap.get(pExercise.getId_Allenamento());
				
				//Recuperato l'array, rimuovo l'esercizio
				exercises.remove(pExercise);
						
				
				//Controllo poi se gli esercizi rimasti in questo allenamento sono pari a zero; in tal caso,
				//elimino pure l'allenamento relativo
				if(exercises != null && exercises.size() == 0)
				{							
					//Recupero l'allenamento dal db...
					Training trainingToDelete = mDb.getTrainingFromId(pExercise.getId_Allenamento());

					//...e rimuovo l'allenamento dal db
					mDb.deleteTraining(trainingToDelete.getId_allenamento());
					

					//Rimuovo l'allenamento dall'array che contiene quelli presenti nel giorno
					mTrainings.remove(trainingToDelete);
					
					/* Ora devo sistemare la view, per fare in modo che non appaia più l'allenamento.
					 * Per farlo creo una copia dell'array degli allenamenti, perchè se uso l'array stesso per le prossime 
					 * azioniverrebbe svuotato (non so neanch'io perchè) */
					ArrayList<Training> trainingsCopy = (ArrayList<Training>) mTrainings.clone();
					
					/* Il modo più semplice per aggiornare la lista degli allenamenti è svuotare l'ArrayAdapter associato alla
				     * ListView e riempirlo di nuovo resetandolo, in modo che la view venga ricreata da zero con le sole
				     * informazioni presenti nel db (e quindi senza l'allenamento eliminato). Non c'è stato modo di far si che
				     * venisse cancellata SOLO la view di quel dato allenamento perchè succedevano un sacco di casini con la
				     * lista degli esercizi associata all'allenamento (con una ListView "normale" si sarebbe potuto fare 
				     * facilmente rimuovendo l'elemento dall'ArrayAdapter e facendo notifyDataSetChanged) */	
					mArrayAdapter.clear();
					
					//Risetto l'ArrayAdapter alla ListView, in modo da resetarlo
					setListAdapter(mArrayAdapter);
					
					//Prendo ogni allenamento presente nell'array degli allenamenti copiato e lo inserisco nell'ArrayAdapter
					for(Training t : trainingsCopy)
						mArrayAdapter.add(t);
					
					//Avverto l'ArrayAdapter che i dati sono cambiati in modo da farlo aggiornare visivamente
					mArrayAdapter.notifyDataSetChanged();
					
					//Per sicurezza, dato che sto praticamente partendo da zero, svuoto anche le map
					mExercisesListMap.clear();
					mExercisesListEntryViewMap.clear();
			    	
					//Dato che ora tutti gli allenamenti "saranno chiusi", segnalo che non è stato aperto nessuno 
					//precedentemente in modo da riniziare da zero
					mPreviousRoot = null;
					
					
					//Se l'array ora è vuoto, devo cambiare il frammento e mostrare la day view vuota
					if(mTrainings.size() == 0)
					{
						//Creo l'istanza del nuovo frammento
			            DayViewFragmentEmpty fragment = new DayViewFragmentEmpty();

			            //Creo la transizione mettendo l'animazione di fade out del frammento attuale e di
			            //fade in per il frammento che verrà messo
			            FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
						ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);

			            //Rimpiazzo il frammento attualmente presente nel fragment_container del layout 
						//dell'activity con quello appena creato e committo
						ft.replace(R.id.fragment_container, fragment, "DayViewFragmentEmpty").commit();
					}
				}
				//Altrimenti se l'allenamento non è da cancellare, devo animare la scomparsa dell'esercizio
				else
				{
					//Recupero la view corrispondente all'esercizio, che dovrò animare e poi nascondere
					final View exerciseView = mExercisesListEntryViewMap.get(pExercise);
					
					//Creo l'animazione per il fadeout della view
					Animation fadeOut = AnimationUtils.loadAnimation(mContext, R.anim.fade_out);
					
					//Setto un listener per nascondere la view quando finisce l'animazione
					fadeOut.setAnimationListener(new AnimationListener() {
						
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
							//Una volta finita l'animazione, nascondo la view
							exerciseView.setVisibility(View.GONE);
						}				
					});
					
					//Finita di creare l'animazione, l'associo alla view dell'esercizio
					exerciseView.startAnimation(fadeOut);		
				}
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
		AlertDialog deleteDialog = alertDialogBuilder.create();
					
		//Creata la dialog (se era null), la mostro
		deleteDialog.show();
	}
	
	
	/**
	 * Crea l'AlertDialog per confermare la cancellazione di un allenamento, e procede col farla nel caso.
	 * Per commenti su cosa accade di preciso nel codice, vedere showDeleteExerciseDialog
	 * 
	 * @param pTraining l'allenamento da eliminare (se l'utente conferma)
	 */
	public void showDeleteTrainingDialog(final Training pTraining)
	{		
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity);
 		
		alertDialogBuilder.setTitle("Attenzione!");
		alertDialogBuilder.setMessage("Sei sicuro di voler cancellare questo allenamento?");
		
		
		alertDialogBuilder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog,int id) 
			{
				mDb.deleteTraining(pTraining.getId_allenamento());
				
				Toast.makeText(mContext, "Allenamento eliminato!", Toast.LENGTH_LONG).show();
				
				//Recupero gli esercizi previsti nell'allenamento; dovrò infatti cancellarli dal db
				ArrayList<Esercizio> exerciseInTraining = mDb.getExercisesInTraining(pTraining.getId_allenamento());
				
				//Recupero anche gli atleti del gruppo dell'allenamento; dovrò cancellare i loro tempi dal
				//db per ogni esercizio
				ArrayList<Atleta> athletes = mDb.getAthletesInGroup(pTraining.getId_gruppo());

				//Scorro ogni esercizio presente nell'array
				for(Esercizio exercise : exerciseInTraining)
				{
					//Per ogni esercizio devo iliminare i tempi dal db per ogni atleta, quindi scorro anche loro
					for(Atleta athlete : athletes)
					{
						//Recupero i tempi di ogni atleta...
						ArrayList<Tempo> times = mDb.getAthletesTime(athlete.getId(), exercise.getId());
						
						//...e li scorro, eliminandoli uno per uno
						for(Tempo time : times)
							mDb.deleteTime(time.getId());
					}
					
					//Eliminati i tempi, procedo con la rimozione dell'esercizio
					mDb.deleteExercise(exercise.getId());
				}
								
				mTrainings.remove(pTraining);
				
				
				/* Ora devo sistemare la view, per fare in modo che non appaia più l'allenamento ed i relativi esercizi.
				 * Per farlo creo una copia dell'array degli allenamenti, perchè se uso l'array stesso per le prossime azioni
				 * verrebbe svuotato (non so neanch'io perchè) */
				ArrayList<Training> trainingsCopy = (ArrayList<Training>) mTrainings.clone();
				
				/* Il modo più semplice per aggiornare la lista degli allenamenti è svuotare l'ArrayAdapter associato alla
			     * ListView e riempirlo di nuovo resetandolo, in modo che la view venga ricreata da zero con le sole
			     * informazioni presenti nel db (e quindi senza l'allenamento eliminato). Non c'è stato modo di far si che
			     * venisse cancellata SOLO la view di quel dato allenamento perchè succedevano un sacco di casini con la
			     * lista degli esercizi associata all'allenamento (con una ListView "normale" si sarebbe potuto fare 
			     * facilmente rimuovendo l'elemento dall'ArrayAdapter e facendo notifyDataSetChanged) */	
				mArrayAdapter.clear();
				
				//Risetto l'ArrayAdapter alla ListView, in modo da resetarlo
				setListAdapter(mArrayAdapter);
				
				//Prendo ogni allenamento presente nell'array degli allenamenti copiato e lo inserisco nell'ArrayAdapter
				for(Training t : trainingsCopy)
					mArrayAdapter.add(t);
				
				//Avverto l'ArrayAdapter che i dati sono cambiati in modo da farlo aggiornare visivamente
				mArrayAdapter.notifyDataSetChanged();
				
				//Per sicurezza, dato che sto praticamente partendo da zero, svuoto anche le map
				mExercisesListMap.clear();
				mExercisesListEntryViewMap.clear();
		    	
				//Dato che ora tutti gli allenamenti "saranno chiusi", segnalo che non è stato aperto nessuno 
				//precedentemente in modo da riniziare da zero
				mPreviousRoot = null;

				
				//Se l'array è ora vuoto, devo cambiare il frammento e mostrare la day view vuota
				if(mTrainings.size() == 0)
				{
		            DayViewFragmentEmpty fragment = new DayViewFragmentEmpty();

		            FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
					ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);

					ft.replace(R.id.fragment_container, fragment, "DayViewFragmentEmpty").commit();
				}				
			}
		});
		

		alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog,int id) 
			{
				dialog.cancel();
			}
		});
 
		AlertDialog deleteDialog = alertDialogBuilder.create();
					
		deleteDialog.show();
	}

	
	
	@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) 
	{
        inflater.inflate(R.menu.empty_training_list_actions, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }
	
	 @Override
	    public boolean onOptionsItemSelected(MenuItem item) 
	 {
        // Handle presses on the action bar items
        switch (item.getItemId())
        {
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
