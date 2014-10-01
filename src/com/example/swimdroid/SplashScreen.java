package com.example.swimdroid;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashScreen extends Activity {
	
	private ImageView swimmer; //l'immagine dello splashcreen
	private TextView splashText; //le due scritte, questa non in grassetto
	private TextView splashText2; //questa in grassetto
	private Animation animMoveDown; //l'animazione per far scendere il logo
  	private Animation fadeIn; //l'animazione per far comparire le scritte
  	private boolean mTouched; //flag che mi dice se è già partita l'activity dopo lo splash screen oppure no
  	private boolean mFinished;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splashscreen);				
		
		//inizializzo tutte le variabili
		mTouched = false;
		mFinished = false;
		splashText = (TextView) findViewById(R.id.textView1);
		splashText2 = (TextView) findViewById(R.id.TextView01);
		swimmer = (ImageView) findViewById(R.id.imageView1);
		swimmer.setDrawingCacheEnabled(true); //tip per migliorare le prestazioni (disegna/renderizza l'immagine prima di mostrarla)
		animMoveDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.move_down);
		animMoveDown.setStartOffset(400);
		//faccio partire l'animazione 400ms dopo che viene caricata l'activity, così evito che parta l'animazione prima che l'activity sia ben visibile
		fadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
		fadeIn.setStartOffset(900);
		
		//come prima, metto un ritardo di 900ms, che sarà in realtà di 500 infatti questa e l'animazione animMoveDown partono insieme
		//ma la prima parte dopo 400ms, questa dopo 900, quindi quando l'altra è già partita da 500ms
		
		//lo metto invisibile, in quanto lo mostrerò dopo l'altra animazione
		splashText.setVisibility(View.INVISIBLE);
		splashText2.setVisibility(View.INVISIBLE);
	}
	
	
	@Override
	public void onWindowFocusChanged (boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus){ //Se l'app è in primo piano, faccio partire l'animazione principale
        	
        	//sposto l'immagine più in alto, in quanto l'animazione la porterà di nuovo giù di 500px (-500+500 = 0),
			swimmer.setTranslationY(-500);
			
			//faccio partire tutte le animaz insieme
			swimmer.startAnimation(animMoveDown);
			splashText.startAnimation(fadeIn);
          	splashText2.startAnimation(fadeIn);
          	fadeIn.setAnimationListener(new AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					// TODO Auto-generated method stubm
					//avverto che le animazioni sono terminate e dopo 200ms parte l'activity con le animazioni
					mFinished = true;
					new Handler().postDelayed(new Runnable() {
						
						@Override
						 public void run() {
							if(!mTouched){
							   //creo il nuovo intent, e setto le animazioni per le transazioni, faccio poi partire l'activity
							   Intent i = new Intent(SplashScreen.this, MainActivity.class);
							   overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
							   startActivity(i);
					            
							   //importante, per evitare che si veda l'activity chiudersi prima che compaia la nuova, aspetto 550ms
							   //ovvero la durata dell'animazione fade_in o fade_out, così che questa activity si chiuda solo dopo
							   //l'animazione
							   new Handler().postDelayed(new Runnable(){
				
								   @Override
								   public void run() {
									   overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
									   finish();
								   }
							   }, 2500);
							}
						}
					}, 200);				
				}
			});
        }
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}
	
	//se viene toccato lo schermo
	public boolean onTouchEvent (MotionEvent event) {
		//controllo il tipo di evento, controllo poi che non siano già terminate le animazioni
		//perchè nel caso sarà già partito l'altro handler, che dopo 200ms farà partire la mainActivity
		//controllo inoltre che non sia già avvenuto un tocco, perchè può capitare che premendo velocemente
		//si apra due volte
		  if (event.getAction() == MotionEvent.ACTION_DOWN && !mFinished && !mTouched) {
			  //se le animazioni sono già partite (non importa quale, infatti una segue l'altra
			  //fermo tutte le animazioni
		       swimmer.clearAnimation();
		       splashText2.clearAnimation();
		       splashText.clearAnimation();
		       //rimetto il logo al suo posto in quanto l'animazione non lo porterà più giù perchè stoppata
		       swimmer.setTranslationY(0);
		       //e mostro le textview
		       splashText.setVisibility(View.VISIBLE);
		       splashText2.setVisibility(View.VISIBLE);
		       
		       //avverto che è stato toccato lo schermo
			   mTouched = true;
			   
			   //dopo che è stato toccato, aspetto 100ms per far vedere bene lo splash screen, dopodichè faccio partire la nuova activity
			   new Handler().postDelayed(new Runnable(){
				   
				   @Override
				   public void run() {
					   //creo il nuovo intent, e setto le animazioni per le transazioni, faccio poi partire l'activity
					   Intent i = new Intent(SplashScreen.this, MainActivity.class);
					   overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
					   startActivity(i);
			            
					   //importante, per evitare che si veda l'activity chiudersi prima che compaia la nuova, aspetto 550ms
					   //ovvero la durata dell'animazione fade_in o fade_out, così che questa activity si chiuda solo dopo
					   //l'animazione
					   new Handler().postDelayed(new Runnable(){
		
						   @Override
						   public void run() {
							   overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
							   finish();
						   }
					   }, 2500);
				   }
			   }, 100); 
		    
			   return true;
		  }
		  
		  return super.onTouchEvent(event);
	}
}
