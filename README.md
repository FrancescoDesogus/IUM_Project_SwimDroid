IUM_Project_SwimDroid
=====================

Auturi: Francesco Desogus, Gabriele Marini

Nel progetto abbiamo usato diversi package creati da altri sviluppatori, che abbiamo utilizzato così com’erano per fare determinate cose dell’interfaccia in alcune schermate.

In particolare, i package 
- com.android.datetimepicker 
- com.android.datetimepicker.time

sono relativi al TimePicker di Google per la scelta dell’ora tramite orologio;

i package
- com.antonyt.infiniteviewpager
- com.roomorama.caldroid

sono relativi al calendario dell’app;
il package
- net.londatiga.android.popupwindow

è relativo alla finestra di popup che appare quando si tiene premuto su un allenamento programmato all’interno dell’app.

I restanti package sono quelli che contengono il codice effettivamente scritto da noi.

Il package com.example.model contiene le classi del modello e la classe della gestione del db.

Il package com.example.swimdroid contiene, in modo non propriamente ordinato, le classi che rappresentano in sostanza i controller per inizializzare le view dell’app e per gestire gli input utente, oltre ad eventuali classi ausiliarie.



Visto il disordine del package, faccio un po’ un riassunto delle classi principali e di come sono connesse tra loro:

La classe SplashScreen è quella che viene chiamata all’avvio dell’app; mostra il logo dell’app e fa il setup del db principalmente (in modo sincrono però).

La classe MainActivity è un’activity per la gestione del calendario dell’app, ed si istanzia quando si conclude lo splashscreen; è in sostanza l’activity principale dell’app.

Quando si preme su un giorno del calendario viene creata un’istanza della classe DayViewActivity, che si occupa di creare a sua volta un fragment che mostri la schermata relativa ad un giorno in cui sono presenti già allenamenti o la schermata in cui non sono presenti allenamenti per quel giorno (rispettivamente, crea istanze di DayViewFragmentEmpty e DayViewFragmentFull).

Dai fragment DayViewFragmentEmpty e DayViewFragmentFull si possono creare istanze di NewTrainingActivity per la creazione di nuovi allenamenti in un’activity diversa.
Da NewTrainingActivity è possibile creare un’istanza di NewExercisesListActivity per l’inserimento degli esercizi che compongono l’allenamento, in un’ulteriore activity; questo creerà uno dei fragment tra FullExercisesList  e EmptyExercisesList a seconda che ci siano rispettivamente già esercizi nell’allenamento o no. Questi due fragment permettono la creazione di nuovi esercizi creando istanze di NewExerciseFragment.
Da FullExercisesList è inoltre possibile creare il fragment ModifyExerciseFragment per la modifica di un esercizio già esistente.

Da DayViewFragmentFull è possibile avviare l’activity per la gestione dei tempi degli atleti di un dato esercizio di un dato allenamento, gestita dalla classe AthletesTimesActivity.

Il resto delle classi sono adattatori per le liste tipiche di Android e altre piccole classi ausiliarie.


Le view che vengono create sono contenute nella cartella res/layout sottoforma di file xml come consigliato per Android. Anche qua i file sono parecchio disordinati e conviene vedere tramite le funzioni di ricerca dove i singoli file sono usati per capire a cosa corrispondono, qualora non ci fossero commenti.
