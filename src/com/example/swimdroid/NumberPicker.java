/*
 * Copyright (c) 2010, Jeffrey F. Cole
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 * 	Redistributions of source code must retain the above copyright notice, this
 * 	list of conditions and the following disclaimer.
 * 
 * 	Redistributions in binary form must reproduce the above copyright notice, 
 * 	this list of conditions and the following disclaimer in the documentation 
 * 	and/or other materials provided with the distribution.
 * 
 * 	Neither the name of the technologichron.net nor the names of its contributors 
 * 	may be used to endorse or promote products derived from this software 
 * 	without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.example.swimdroid;

import java.text.Collator;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

/**
 * Classe che gestisce i NumberPicker per le ripetizioni e la distanza nei form degli esercizi
 *
 * @author Gabriele
 */
public class NumberPicker 
{
	private Context mContext; //Il contesto dell'app
	private View mNumberPickerView; //La view in cui sono presenti i bottoni e gli EditText della distanza e delle ripetizioni
	
	private Button mIncRipButton; //bottone per incrementare il numero di ripetizioni
    private Button mDecRipButton; //bottone per decrementare il numero di ripetizioni
    private EditText mRipEditText; //EditText per il numero di ripetizioni
    
    private Button mIncDistButton; //bottone per incrementare la distanza
    private Button mDecDistButton; //bottone per decrementare la distanza
    private EditText mDistEditText; //EditText per la distanza
    
    private PopupWindow mPopupRip; //Popup per l'errore relativo al numero di ripetizioni
    private PopupWindow mPopupDist; //Popup per l'errore relativo alla distanza
	
    /**
     * Costruisce l'oggetto NumberPicker che gestisce i bottoni delle ripetizioni e della distanza. Il costruttore
     * si occupa di inizializzare tutti i valori, creando i vari listener per gli EditText ed i bottoni
     * 
     * @param pContext il contesto dell'app
     * @param pView la view in cui sono presenti i bottoni e gli EditText
     */
	public NumberPicker(Context pContext, View pView)
	{
		mContext = pContext;
		mNumberPickerView = pView;
		
		//prelevo i bottoni e l'edittext per le ripetizioni
		mIncRipButton = (Button) mNumberPickerView.findViewById(R.id.incRip);
		mDecRipButton = (Button) mNumberPickerView.findViewById(R.id.decRip);
		mRipEditText = (EditText) mNumberPickerView.findViewById(R.id.editRip);
				
				mIncRipButton.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						Integer value = 0;
						//provo a convertire a intero il valore, se ci riesco salvo il valore
						//altrimento lo metto di default a 1
						try {
							value = Integer.parseInt(mRipEditText.getText().toString());
						} catch( NumberFormatException nfe ){
							value = 1;
						}
						
						increment(value, 1, mRipEditText);
					}
				});
				
				mRipEditText.addTextChangedListener(new TextWatcher() {
					
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {				
					}
					
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					}
					
					@Override
					public void afterTextChanged(Editable s) {	
						//controllo la lunghezza della stringa
						if(s.toString().length() < 1){
							//se è minore di 1 e non c'è nessun popup mostrato
							if(mPopupRip == null){
								LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
								mPopupRip = new PopupWindow(inflater.inflate(R.layout.popup_error, null, false), LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,true);
							    //setto il layout del popup
								mPopupRip.setFocusable(false); //evito che si prenda il focus impedendo di continuare a scrivere
								mPopupRip.setOutsideTouchable(true); //setto che si può cliccare anche fuori
								mPopupRip.setTouchable(false); //e che non si può cliccare su di esso
								mPopupRip.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED); //questo evita che il popup risulti sopra la tastiera
								mPopupRip.showAsDropDown(mRipEditText, -210, -25); //setto la posizione giusta
							    ((TextView) mPopupRip.getContentView().findViewById(R.id.tv_message)).setText("Inserisci almeno 1"); //ed infine inserisco il testo
							}
							else mPopupRip.showAsDropDown(mRipEditText, -210, -25); //se è stato creato in precedenza lo mostro semplicemente
						}
						else {
							//se la lunghezza della stringa è > 1 rimuovo il popup
							if(mPopupRip != null)
								mPopupRip.dismiss();
						}
					}
				});
				
				mDecRipButton.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						//provo a convertire a intero il valore, se ci riesco salvo il valore
						//altrimento lo metto di default a 1
						Integer value = 0;
						try {
							value = Integer.parseInt(mRipEditText.getText().toString());
						} catch( NumberFormatException nfe ){
							value = 1;
						}
						
						decrement(value, 1, mRipEditText); //decremento il valore
					}
				});

				//prelevo i bottoni e l'edittext per la distanza
				mIncDistButton = (Button) mNumberPickerView.findViewById(R.id.incDist);
				mDecDistButton = (Button) mNumberPickerView.findViewById(R.id.decDist);
				mDistEditText = (EditText) mNumberPickerView.findViewById(R.id.editDist);
				
				mIncDistButton.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						//provo a convertire a intero il valore, se ci riesco salvo il valore
						//altrimento lo metto di default a 1
						Integer value = 0;
						try {
							value = Integer.parseInt(mDistEditText.getText().toString());
						} catch( NumberFormatException nfe ){
							value = 1;
						}
						
						increment(value, 25, mDistEditText); //incremento il valore
					}
				});
				
				mDistEditText.addTextChangedListener(new TextWatcher() {
					
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
						
					}
					
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					}
					
					@Override
					public void afterTextChanged(Editable s) {	
						//controllo la lunghezza della stringa
						if(s.toString().length() < 1){
							//se è minore di 1 e non c'è nessun popup mostrato
							if(mPopupDist == null){
								LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
								mPopupDist = new PopupWindow(inflater.inflate(R.layout.popup_error, null, false), LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,true);
							    // The code below assumes that the root container has an id called 'main'
								mPopupDist.setFocusable(false); //evito che si prenda il focus impedendo di continuare a scrivere
								mPopupDist.setOutsideTouchable(true); //setto che si può cliccare anche fuori
								mPopupDist.setTouchable(false); //e che si può cliccare su di esso
								mPopupDist.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED); //questo evita che il popup risulti sopra la tastiera
								mPopupDist.showAsDropDown(mDistEditText, -210, -25); //lo mostro nella posizione giusta
							    ((TextView) mPopupDist.getContentView().findViewById(R.id.tv_message)).setText("Inserisci almeno 25"); //e setto il testo di errore
							}
							else 
								mPopupDist.showAsDropDown(mDistEditText, -210, -25); //se è stato creato in precedenza lo mostro semplicemente
						}
						else {
							//se la lunghezza della stringa è > 1 rimuovo il popup
							if(mPopupDist != null)
							{
								Log.w("pop", "POPUP = NULL? " +  (mPopupDist == null));
								mPopupDist.dismiss();
							}
						}
					}
				});
				
				mDecDistButton.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						Integer value = 0;
						//provo a convertire a intero il valore, se ci riesco salvo il valore
						//altrimento lo metto di default a 1
						try {
							value = Integer.parseInt(mDistEditText.getText().toString());
						} catch( NumberFormatException nfe ){
							value = 1;
						}
						
						decrement(value, 25, mDistEditText); //decremento il valore
					}
				});
	}
	
	
	/**
	 * Ritorna il valore delle ripetizioni contenuto nell'apposito EditText
	 * 
	 * @return le ripetizioni, già messe come intero
	 */
	public int getRip()
	{
		int value = 0;
		
		try 
		{
			value = Integer.parseInt(mRipEditText.getText().toString());
		} 
		catch( NumberFormatException nfe )
		{
			value = -1;
		}
		
		return value;
	}
	
	/**
	 * Ritorna il valore della distanza contenuto nell'apposito EditText
	 * 
	 * @return la distanza, già messa come intero
	 */
	public int getDist()
	{
		int value = 0;
		
		try 
		{
			value = Integer.parseInt(mDistEditText.getText().toString());
			
			value = (value + (25-1)) / 25 * 25; //lo arrotondo al più vicino valore possibile, multiplo dello step
		} 
		catch( NumberFormatException nfe )
		{
			value = -1;
		}

		
		return value;
	}
	
	
	/**
	 * Inserisce il valore passato come parametro nell'EditText delle ripetizioni, in modo che sia visibile
	 * 
	 * @param pRip le ripetizioni da inserire
	 */
	public void setRipText(int pRip)
	{
		mRipEditText.setText(String.valueOf(pRip));
	}
	
	/**
	 * Inserisce il valore passato come parametro nell'EditText della distanza, in modo che sia visibile
	 * 
	 * @param pDist la distanza da inserire
	 */
	public void setDistText(int pDist)
	{
		mDistEditText.setText(String.valueOf(pDist));
	}
	
	

	
	/**
	 * Funzione per incrementare il valore dell'editText
	 * 
	 * @param value il valore attuale dell'edittext
	 * @param step lo step per aumentarne il valore
	 * @param valueText l'edittext della quale verra modificato il valore
	 */
	public void increment(Integer value, Integer step, EditText valueText){		
		if( value < step*999 ){ //se il valore è minore di un certo massimo
			
			//Incremento il valore con quello dello step solo se lo step è 1 o se è 25 ma il numero è multiplo di 25
			if(step == 1 || (step == 25 && (value % 25 == 0)))
				value = value + step; //incremento il valore
			
			value = (value + (step-1)) / step * step; //lo arrotondo al più vicino valore possibile, multiplo dello step
			valueText.setText( value.toString() ); //e setto il testo nell'editText
		}
	}

	/**
	 * Funzione per decrementare il valore dell'editText
	 * 
	 * @param value il valore attuale dell'edittext
	 * @param step lo step per decrementarne il valore
	 * @param valueText l'edittext della quale verra modificato il valore
	 */
	public void decrement(Integer value, int step, EditText valueText){
		if( value > step ){ //se il valore è maggiore dello step (valore minimo)
			
			if(step == 1 || (step == 25 && (value % 25 == 0)))
				value = value - step; //incremento il valore

			value = Math.round(value / step) * step; //lo arrotondo al più vicino valore possibile, multiplo dello step
			valueText.setText( value.toString() ); //e setto il testo nell'editText
		}
	}	
	
	
	/**
	 * Controlla se ci sono popup di errore da togliere e nel caso li toglie. Da chiamare sempre prima di distruggere
	 * la view dei fragment, in quanto altrimenti i popup rimarrebbero
	 */
	public void dismissPopups() 
	{
		if(mPopupDist != null)
			mPopupDist.dismiss();
	
		if(mPopupRip != null)
			mPopupRip.dismiss();
	}

}
