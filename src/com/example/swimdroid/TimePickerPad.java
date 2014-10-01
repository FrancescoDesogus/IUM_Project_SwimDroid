package com.example.swimdroid;

import java.util.Scanner;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.datetimepicker.Utils;
import com.example.model.Tempo;


/**
 * Classe per creare una dialog contenente un timepicker con un keypad per l'inserimento
 * 
 * @author Francesco
 *
 */
public class TimePickerPad
{
	private Context mContext; //Il contesto dove verrà inserita la dialog
	private Button mTimeButton; //Il bottone che causerà l'apertura della dialog
	
	private Dialog mTimePickerDialog; //La dialog contenente il timepicker
	
	private OnTimeSetListener mOnTimeSetListener; //Il listener eseguito se l'utente conferma il numero inserito
	
	private int mDigitCount; //Intero utilizzato nella dialog per inserire i tempi per contare a quale cifra l'utente è arrivato a inserire

	
	/**
	 * Costruttore che recupera le informazioni necessarie all'inserimento della dialog
	 * 
	 * @param pContext il contesto in cui apparirà la dialog
	 * @param pTimeButton il bottone che causerà l'apertura della dialog
	 */
	public TimePickerPad(Context pContext, Button pTimeButton)
	{
		mContext = pContext;
		mTimeButton = pTimeButton;
	}
	
	
	/**
	 * Crea la dialog col timepicker e si occupa di gestirla
	 */
	public void createTimePickerDialog()
	{
		mTimePickerDialog = new Dialog(mContext);
    	
		//Recupero il layout della dialog e metto un titolo
		mTimePickerDialog.setContentView(R.layout.time_picker_dialog_custom);
		mTimePickerDialog.setTitle("Inserisci il tempo");
		
		
		//Adesso devo recuperare il tempo che deve apparire inizialmente nelll'header della dialog. Per farlo
		//Recupero il testo contenuto nel bottone e lo analizzo per prelevare ogni cifra
		String currentTimeValue = (String) mTimeButton.getText();
		
		//Lo scanner ha come delimitatore ' perchè la stringa contenuta nel bottone è del tipo 00'00''
		Scanner timeScanner = new Scanner(currentTimeValue).useDelimiter("'");

		String minutes = timeScanner.next();
		String seconds = timeScanner.next();
		
		//Estrapolo le cifre una per una
		char firstCurrentDigit = minutes.charAt(0);
		char secondCurrentDigit = minutes.charAt(1);
		char thirdCurrentDigit = seconds.charAt(0);
		char fourthCurrentDigit = seconds.charAt(1);
		
		timeScanner.close();
		
		/* Il testo nell'header della dialog è composto da 4 TextView (senza contare le 2 dedicate agli apici ' e '');
		 * ogni TextView corrisponde ad una cifra, quindi quello che faccio è recuperare i riferimenti
		 * ad ognuna delle 4 view che compongono le cifre, per poi settare i valori che erano presenti nel bottone */
		final TextView firstDigitText = (TextView) mTimePickerDialog.findViewById(R.id.first_digit);
		final TextView secondDigitText = (TextView) mTimePickerDialog.findViewById(R.id.second_digit);
		final TextView thirdDigitText = (TextView) mTimePickerDialog.findViewById(R.id.third_digit);
		final TextView fourthDigitText = (TextView) mTimePickerDialog.findViewById(R.id.fourth_digit);
		
		//Inserisco ogni cifra (la stringa vuota iniziale serve perchè sennò sclera e crasha se passo solo il char)
		firstDigitText.setText("" + firstCurrentDigit);
		secondDigitText.setText("" + secondCurrentDigit);
		thirdDigitText.setText("" + thirdCurrentDigit);
		fourthDigitText.setText("" + fourthCurrentDigit);
		
		//Recupero anche il colore di default delle cifre (che sarà il colore delle cifre "non attualmente attive") ed
		//il colore blu dalle risorse (sarà il colore delle cifre "attive")
		final int defaultColor = firstDigitText.getCurrentTextColor();
		final int activeDigitColor = mContext.getResources().getColor(R.color.blue);
		
		//Setto il colore della prima cifra a blu; questo sarà il colore delle cifre "attive"
		firstDigitText.setTextColor(activeDigitColor);
	
		//Creo l'animazione per far "rimbalzare" la prima cifra per indicare che è attiva e la eseguo
		ObjectAnimator pulseAnimator = Utils.getPulseAnimator(firstDigitText, 0.85f, 1.1f);
        pulseAnimator.start();
        
		
		//Recupero tutti i bottoni che rappresentano il tastierino numerico
		Button digit0 = (Button) mTimePickerDialog.findViewById(R.id.button_digit_0);
		Button digit1 = (Button) mTimePickerDialog.findViewById(R.id.button_digit_1);
		Button digit2 = (Button) mTimePickerDialog.findViewById(R.id.button_digit_2);
		Button digit3 = (Button) mTimePickerDialog.findViewById(R.id.button_digit_3);
		Button digit4 = (Button) mTimePickerDialog.findViewById(R.id.button_digit_4);
		Button digit5 = (Button) mTimePickerDialog.findViewById(R.id.button_digit_5);
		Button digit6 = (Button) mTimePickerDialog.findViewById(R.id.button_digit_6);
		Button digit7 = (Button) mTimePickerDialog.findViewById(R.id.button_digit_7);
		Button digit8 = (Button) mTimePickerDialog.findViewById(R.id.button_digit_8);
		Button digit9 = (Button) mTimePickerDialog.findViewById(R.id.button_digit_9);
		
		//Inizializzo l'intero che tiene conto del numero di click dell'utente nel pad
		mDigitCount = 0;
		
		//Creo il listener che verrà associato ad ogni bottone (è uguale per tutti)
		View.OnClickListener digitsListener = new View.OnClickListener() {
			@Override
            public void onClick(View view) 
            {	
				//Recupero il bottone che ha causato l'evento onClick
				Button digitPressed = (Button) view;
        		
				//Recupero il numero che corrisponde al bottone (ogni bottone ha il testo uguale al numero ad esso associato)
        		int valuePressed = Integer.parseInt((String) digitPressed.getText());
	        	
        		
        		/* Adesso in base a quanti click l'utente ha fatto, devo settare il testo di una cifra piuttosto che
        		 * di un'altra, e rendere attiva la successiva, se presente. Inizializzo a null quindi gli oggetti che userò
        		 * tra poco e che verranno inizializzati per bene all'interno dello siwtch */
        		TextView digitText = null;
        		TextView nextDigitText = null;
        		
        		        		
        		//Controllo a che cifra è arrivato attualemente
        		switch(mDigitCount)
        		{
        		case 0:
        			//Se il count è a 0, l'utente ha inserito la prima cifra, quindi seleziono la TextView appropiata
        			//e mi segno anche quale sarà la prossima
        			digitText = firstDigitText;        
        			nextDigitText = secondDigitText; 
        			break;
        			
				case 1:
					digitText = secondDigitText;
					nextDigitText = thirdDigitText;
					break;
					
				case 2:
        			digitText = thirdDigitText;
        			nextDigitText = fourthDigitText;
        			break;
        			
				case 3:
					digitText = fourthDigitText;
					nextDigitText = firstDigitText;
					break;
        		}
        		

        		//Setto il testo della cifra attuale al valore premuto (la stringa vuota iniziale è per evitare crash)
        		digitText.setText("" + valuePressed);
        		
        		//Se esiste la prossima cifra, le setto il colore per le cifre attive, in quanto sarà la prossima
        		//ad essere cambiata al prossimo click
        		if(nextDigitText != null)
        		{
        			nextDigitText.setTextColor(activeDigitColor);
        			
        			//Creo anche l'animazione per far "rimbalzare" la cifra per indicare che è attiva e la eseguo
        			ObjectAnimator pulseAnimator = Utils.getPulseAnimator(nextDigitText, 0.85f, 1.1f);
        	        pulseAnimator.start();
        		}
        		
        		//Setto la cifra attuale al suo colore originale, visto che ha finito il suo lavoro
        		digitText.setTextColor(defaultColor);
        		
        		//Infine incremento il counter dei click con modulo 4 per far ritornare alla 
        		//prima cifra se ha raggiunto il limite
        		mDigitCount = (mDigitCount + 1 ) % 4;
            	
            }
		};

		//Creato il listener, lo associo ad ogni bottone
		digit0.setOnClickListener(digitsListener);
		digit1.setOnClickListener(digitsListener);
		digit2.setOnClickListener(digitsListener);
		digit3.setOnClickListener(digitsListener);
		digit4.setOnClickListener(digitsListener);
		digit5.setOnClickListener(digitsListener);
		digit6.setOnClickListener(digitsListener);
		digit7.setOnClickListener(digitsListener);
		digit8.setOnClickListener(digitsListener);
		digit9.setOnClickListener(digitsListener);
		
		
		//Prendo il riferimento al bottone done
		Button doneButton = (Button) mTimePickerDialog.findViewById(R.id.done_button);
		
		//Associo un listener al bottone done per far chiudere la dialog salvando i dati
		doneButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) 
            {	            	
            	//Recupero il valore attuale di ogni cifra del tempo che c'è nell'header
            	int firstDigit = Integer.parseInt((String) firstDigitText.getText());
            	int secondDigit = Integer.parseInt((String) secondDigitText.getText());
            	int thirdDigit = Integer.parseInt((String) thirdDigitText.getText());
            	int fourthDigit = Integer.parseInt((String) fourthDigitText.getText());
            	
            	//Devo controllare che l'input inserito corrisponda a minuti e secondi, cioè che non abbia inserito 
            	//valori come 87'64''. Nel caso ci fossero, li approssimo a 60
            	if(firstDigit > 6)
            	{
            		firstDigit = 6;
            		secondDigit = 0;
            		
            	}
            	else if(firstDigit == 6 && secondDigit > 0)
            		secondDigit = 0;
            		
            	if(thirdDigit > 6)
            	{
            		thirdDigit = 6;
            		fourthDigit = 0;
            	}
            	else if(thirdDigit == 6 && fourthDigit > 0)
            		fourthDigit = 0;
            	
            	//Adesso modifico il testo del bottone per rispecchiare il nuovo tempo
            	mTimeButton.setText(firstDigit + "" + secondDigit + "'" + thirdDigit + "" + fourthDigit + "''");
            	
            	
            	//Se è stato messo un listener, esegui il relativo metodo passando le cifre inserite dall'utente
            	if(mOnTimeSetListener != null)
            		mOnTimeSetListener.onTimeSet(firstDigit, secondDigit, thirdDigit, fourthDigit);
            	
            	//Posso ora chiudere la dialog
            	mTimePickerDialog.dismiss();
            }
        });
		
		
		/* Creo anche un listener da associare ai numeri del tempo che c'è nell'header della dialog, in modo tale che se
		 * venga premuto una cifra essa si "attivi", permettendo di cambiare quella e tutte le altre che vengono dopo.
		 * Il listener è uguale per ogni TextView che compone l'header */
		View.OnClickListener headerListener = new View.OnClickListener() {
			@Override
            public void onClick(View view) 
            {					
				TextView oldActiveDigit = null;
				
				//Devo recuperare la vecchia TextView attualmente attiva, qualora ci fosse, in modo da disattivarla.
				//Per farlo controllo a che cifra si è arrivati
        		switch(mDigitCount)
        		{
        		case 0:
        			oldActiveDigit = firstDigitText;        
        			break;
        			
				case 1:
					oldActiveDigit = secondDigitText;
					break;
					
				case 2:
					oldActiveDigit = thirdDigitText;
        			break;
        			
				case 3:
					oldActiveDigit = fourthDigitText;
					break;
        		}
        		
        		//Se la TextView è diversa da null, la riporto al colore che indica che è disattivata. Se è null vuol dire
        		//che l'utente ha inserito già le 4 cifre, quindi nessuna TextView è attualmente attiva
        		if(oldActiveDigit != null)
        			oldActiveDigit.setTextColor(defaultColor);
				
				
        		//Adesso recupero la TextView che l'utente ha clickato
				TextView digitPressed = (TextView) view;

        		//Controllo a quale delle 4 cifre corrisponde, e resetto il contatore corrispondente per far si che
				//tutte le cifre che vengono dopo questa siano editabili
				if(digitPressed.equals(firstDigitText))
					mDigitCount = 0;
				else if(digitPressed.equals(secondDigitText))
					mDigitCount = 1;
				else if(digitPressed.equals(thirdDigitText))
					mDigitCount = 2;
				else 
					mDigitCount = 3;
				
				//Creo anche l'animazione per far "rimbalzare" la cifra premuta per indicare che è attiva e la eseguo
    			ObjectAnimator pulseAnimator = Utils.getPulseAnimator(digitPressed, 0.85f, 1.1f);
    	        pulseAnimator.start();
    	        
    	        //Infine cambio il colore alla TextView per segnalare che è ora la cifra attiva
    	        digitPressed.setTextColor(activeDigitColor);
            }
		};
		
		//Creato il listener, lo associo ad ogni TextView dell'header
		firstDigitText.setOnClickListener(headerListener);
		secondDigitText.setOnClickListener(headerListener);
		thirdDigitText.setOnClickListener(headerListener);
		fourthDigitText.setOnClickListener(headerListener);
	}
	
	/**
	 * Mostra la dialog nel contesto specificato nel costruttore del timepicker
	 */
	public void show()
	{
		mTimePickerDialog.show();
	}
	
	/**
	 * Nasconde la dialog
	 */
	public void dismissTimePickerDialog()
	{
		mTimePickerDialog.dismiss();
	}
	

	/**
	 * Registra il listener inserito dal programmatore per essere eseguito quando l'utente ha confermato l'inserimento
	 */
	void setOnTimeSetListener(OnTimeSetListener pOnTimeSetListener)
	{
		mOnTimeSetListener = pOnTimeSetListener;
	}
}
