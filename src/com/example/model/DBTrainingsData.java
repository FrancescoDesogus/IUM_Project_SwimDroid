package com.example.model;


import hirondelle.date4j.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


import com.example.model.Training;
import com.roomorama.caldroid.CalendarHelper;

import android.content.*;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class DBTrainingsData
{  
    DbHelper mDbHelper;
    
	private static final String DB_NAME = "swimDroid.db";
    private static final int DB_VERSION = 1; 

    
    //Colonne della tabella training
  	public static final String NOME_ALLENAMENTO_COLUMN = "nome";
  	public static final String DATA_ALLENAMENTO_COLUMN = "data";
  	public static final String ORA_INIZIO__ALLENAMENTO_COLUMN = "ora_inizio";
  	public static final String ORA_FINE__ALLENAMENTO_COLUMN = "ora_fine";
  	public static final String ID_GRUPPO_ALLENAMENTO_COLUMN = "id_gruppo";
  	
    //Colonne della tabella exercise
  	public static final String RIPETIZIONI_ESERCIZIO_COLUMN = "ripetizioni";
  	public static final String DISTANZA_ESERCIZIO_COLUMN = "distanza";
  	public static final String STILE1_ESERCIZIO_COLUMN = "stile1";
  	public static final String STILE2_ESERCIZIO_COLUMN = "stile2";
  	public static final String ANDATURA_ESERCIZIO_COLUMN = "andatura";
  	public static final String TEMPO_ESERCIZIO_COLUMN = "tempo";
  	public static final String ID_ALLENAMENTO_ESERCIZIO_COLUMN = "id_allenamento";
  	
    //Colonne della tabella atleta
  	public static final String NOME_ATLETA_COLUMN = "nome";
  	public static final String COGNOME_ATLETA_COLUMN = "data";
  	public static final String ID_GRUPPO_ATLETA_COLUMN = "id_gruppo";
  	
    //Colonne della tabella tempi
  	public static final String ID_ATLETA_TEMPI_COLUMN = "id_atleta";
  	public static final String ID_ESERCIZIO_TEMPI_COLUMN = "id_esercizio";
  	public static final String RIPETIZIONE_TEMPI_COLUMN = "ripetizione";
  	public static final String TEMPO_TEMPI_COLUMN = "tempo";

  	
    public DBTrainingsData(Context ctx)
    {
        mDbHelper = new DbHelper(ctx, DB_NAME, null, DB_VERSION);   
    }

    public void close()
    { 
        mDbHelper.close();
    }
    

    /** 
	 * Classe helper per il database; si occupa sostanzialmente di crearlo e mantenerlo
	 * 
	 */
    private class DbHelper extends SQLiteOpenHelper 
    {
        private static final String TABLE_TRAINING = "training"; //Tabella indipendente degli allenamenti
        private static final String TABLE_EXERCISE = "exercise"; //Tabella degli esercizi - fa riferimento alla tabella allenamenti
        private static final String TABLE_ATHLETE = "athlete"; //Tabella indipendente degli esercizi
        private static final String TABLE_TIMES = "times"; //Tabella dei tempi - fa riferimento alla tabella esercizi e atleti
        

        //Query per la creazione della tabella degli allenamenti
		private static final String CREATE_TABLE_TRAINING = "CREATE TABLE " + TABLE_TRAINING 
				+ " ("
				+ "id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ DBTrainingsData.NOME_ALLENAMENTO_COLUMN + " TEXT NOT NULL, "
				+ DBTrainingsData.DATA_ALLENAMENTO_COLUMN + " INTEGER NOT NULL, "
				+ DBTrainingsData.ORA_INIZIO__ALLENAMENTO_COLUMN + " INTEGER NOT NULL, "
				+ DBTrainingsData.ORA_FINE__ALLENAMENTO_COLUMN + " INTEGER NOT NULL, "
				+ DBTrainingsData.ID_GRUPPO_ALLENAMENTO_COLUMN + " INTEGER NOT NULL);";
		
		
        //Query per la creazione della tabella degli esercizi
		private static final String CREATE_TABLE_EXERCISE = "CREATE TABLE " + TABLE_EXERCISE 
				+ " ("
				+ "id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ DBTrainingsData.RIPETIZIONI_ESERCIZIO_COLUMN + " INTEGER NOT NULL, "
				+ DBTrainingsData.DISTANZA_ESERCIZIO_COLUMN + " INTEGER NOT NULL, "
				+ DBTrainingsData.STILE1_ESERCIZIO_COLUMN + " TEXT NOT NULL, "
				+ DBTrainingsData.STILE2_ESERCIZIO_COLUMN + " TEXT NOT NULL, "
				+ DBTrainingsData.ANDATURA_ESERCIZIO_COLUMN + " TEXT NOT NULL, "
				+ DBTrainingsData.TEMPO_ESERCIZIO_COLUMN + " TEXT NOT NULL, "
				+ DBTrainingsData.ID_ALLENAMENTO_ESERCIZIO_COLUMN + " INTEGER REFERENCES " + TABLE_TRAINING + "(id) ON UPDATE CASCADE ON DELETE CASCADE);";
		

        //Query per la creazione della tabella degli atleti
		private static final String CREATE_TABLE_ATHLETE = "CREATE TABLE " + TABLE_ATHLETE 
				+ " ("
				+ "id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ DBTrainingsData.NOME_ATLETA_COLUMN + " TEXT NOT NULL, "
				+ DBTrainingsData.COGNOME_ATLETA_COLUMN + " TEXT NOT NULL, "
				+ DBTrainingsData.ID_GRUPPO_ATLETA_COLUMN + " INTEGER NOT NULL);";
		
		
        //Query per la creazione della tabella dei tempi
		private static final String CREATE_TABLE_TIMES = "CREATE TABLE " + TABLE_TIMES 
				+ " ("
				+ "id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ DBTrainingsData.ID_ATLETA_TEMPI_COLUMN + " INTEGER REFERENCES " + TABLE_ATHLETE + "(id) ON UPDATE CASCADE ON DELETE CASCADE, "
				+ DBTrainingsData.ID_ESERCIZIO_TEMPI_COLUMN + " INTEGER REFERENCES " + TABLE_EXERCISE + "(id) ON UPDATE CASCADE ON DELETE CASCADE, "
				+ DBTrainingsData.RIPETIZIONE_TEMPI_COLUMN + " INTEGER NOT NULL, "
				+ DBTrainingsData.TEMPO_TEMPI_COLUMN + " TEXT NOT NULL);";
			
		
        public DbHelper(Context context, String name, CursorFactory factory,int version) 
        {
                super(context, name, factory, version);
                
                
//                //Per debuggare, ogni volta cancello e ricreo le tabelle ed i dati chiamando l'onCreate
//                SQLiteDatabase _db = getWritableDatabase();
//                                      
//                _db.execSQL("DROP TABLE " + TABLE_TRAINING);
//                _db.execSQL("DROP TABLE " + TABLE_EXERCISE);
//                _db.execSQL("DROP TABLE " + TABLE_ATHLETE);
//                _db.execSQL("DROP TABLE " + TABLE_TIMES);
//
//                onCreate(_db);
        }

        /**
         * Metodo chiamato qualora il database non fosse presente; qua vengono create le tabelle e vengono popolate
         */
        @Override
        public void onCreate(SQLiteDatabase _db) 
        {
        		//Creazione delle tabelle
                _db.execSQL(CREATE_TABLE_TRAINING);
                _db.execSQL(CREATE_TABLE_EXERCISE);
                _db.execSQL(CREATE_TABLE_ATHLETE);
                _db.execSQL(CREATE_TABLE_TIMES);

                
                //Kili di dati per popolare le tabelle in 5, 4, 3, 2, 1...
                
                String data2 = "15-01-2014";
                SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
                
            	Date end_date = null;
            	
				try {
					end_date = (Date)format.parse(data2);
				} catch (ParseException e) {
					e.printStackTrace();
				} 
				
                long tempo2 = end_date.getTime();
                
                data2 = "07-01-2014";
                
                try {
					end_date = (Date)format.parse(data2);
				} catch (ParseException e) {
					e.printStackTrace();
				}
                
                long tempo3 = end_date.getTime();
                
                                
                data2 = "03-02-2014";
                
                try {
					end_date = (Date)format.parse(data2);
				} catch (ParseException e) {
					e.printStackTrace();
				}
                
                long tempo4 = end_date.getTime();
                
                
                //Recupero i tempi delle ore di inizio-fine come interi
                format = new SimpleDateFormat("k:m");
                
                data2 = "18:00";
                
                try {
					end_date = (Date) format.parse(data2);
				} catch (ParseException e) {
					e.printStackTrace();
				}
                
                long oraInizio1 = end_date.getTime();
                
                data2 = "19:00";
                
                try {
					end_date = (Date) format.parse(data2);
				} catch (ParseException e) {
					e.printStackTrace();
				}
                
                long oraFine1 = end_date.getTime();
                
                
            	data2 = "20:00";
                
                try {
					end_date = (Date) format.parse(data2);
				} catch (ParseException e) {
					e.printStackTrace();
				}
                
                long oraInizio2 = end_date.getTime();
                
                
            	data2 = "16:00";
                
                try {
					end_date = (Date) format.parse(data2);
				} catch (ParseException e) {
					e.printStackTrace();
				}
                
                long oraInizio3 = end_date.getTime();
                
                data2 = "18:00";
                
                try {
					end_date = (Date) format.parse(data2);
				} catch (ParseException e) {
					e.printStackTrace();
				}
                
                long oraFine2 = end_date.getTime();
                
                data2 = "18:00";
                
                try {
					end_date = (Date) format.parse(data2);
				} catch (ParseException e) {
					e.printStackTrace();
				}
                
                long oraFine3 = end_date.getTime();

                
                //Creazione allenamenti, 2 previsti per il 15 gennaio e uno già passato per il 7 
                addTraining(_db, "Allenamento intensivo", tempo2, oraInizio1, oraFine1, 1); //gruppo 1 - id -> 1
                addTraining(_db, "Allenamento post-palestra", tempo2, oraFine1, oraInizio2, 3); //gruppo 3 - id -> 2
                addTraining(_db, "Allenamento sprint", tempo3, oraInizio3, oraFine2, 2); //gruppo 2 - id -> 3
                addTraining(_db, "Allenamento pre-gara", tempo4, oraInizio3, oraFine3, 4); //gruppo 4 - id -> 4
                addTraining(_db, "Test resistenza braccia", tempo4, oraFine2, oraFine1, 1); //gruppo 1 - id -> 5

                
                //Creazione esercizi per gli allenamenti presenti
                addExercise(_db, 4, 100, "Stile libero", "solo braccia", "aepobica", "02-23", 1); //per allenamento intensivo - id -> 1
                addExercise(_db, 2, 200, "Rana", "solo braccia", "A2 - Resistenza Aerobica", "03-50", 1); //per allenamento intensivo - id -> 2
                addExercise(_db, 3, 150, "Delfino", "solo braccia", "A1 - Aerobico", "03-00", 1); //per allenamento intensivo - id -> 3
                addExercise(_db, 5, 50, "Stile libero", "solo gambe", "A2 - Resistenza Aerobica", "01-12", 2); //per allenamento post-palestra - id -> 4
                addExercise(_db, 1, 400, "Delfino", "solo braccia", "A1 - Aerobico", "05-23", 2); //per allenamento post-palestra - id -> 5
                addExercise(_db, 2, 400, "Delfino", "solo gambe", "B2 - VO2Max", "04-00", 3); //per allenamento sprint - id -> 6
                addExercise(_db, 1, 500, "Stile libero", "completo", "A1 - Aerobico", "05-48", 3); //per allenamento sprint - id -> 7
                addExercise(_db, 1, 500, "Rana", "completo", "A1 - Aerobico", "05-30", 4); //per allenamento pre-gara - id -> 8
                addExercise(_db, 1, 400, "Delfino", "solo braccia", "A1 - Aerobico", "05-23", 5); //per Test resistenza braccia - id -> 9

                
                
                //Creazione atleti. NOTA: l'id dei gruppi è generato qua, nel senso che per scoprire quanti gruppi ci sono,
                //per dire, bisogna guardare questa tabella e prendere tutti i id_gruppo distinti che ci sono
                addAthlete(_db, "Andrea", "Puddu", 1); //gruppo 1 - id -> 1
                addAthlete(_db, "Piero", "Marra", 1); //gruppo 1 - id -> 2
                addAthlete(_db, "Giovanni", "Murru", 1); //gruppo 1 - id -> 3 
                addAthlete(_db, "Nico", "Bortis", 2); //gruppo 2 - id -> 4 
                addAthlete(_db, "Alberto", "Mereu", 2); //gruppo 2 - id -> 5 
                addAthlete(_db, "Marta", "Floris", 2); //gruppo 2 - id -> 6 
                addAthlete(_db, "Paolo", "Meloni", 3); //gruppo 3 - id -> 7 
                addAthlete(_db, "Leonardo", "Serra", 3); //gruppo 3 - id -> 8 
                addAthlete(_db, "Davide", "Mura", 3); //gruppo 3 - id -> 9 
                addAthlete(_db, "Valeria", "Murtas", 3); //gruppo 3 - id -> 10 
                addAthlete(_db, "Giacomo", "Boi", 4); //gruppo 4 - id -> 11 
                addAthlete(_db, "Salvatore", "Sanna", 4); //gruppo 4 - id -> 12 
                
                
                /* Aggiunta tempi
                 * 
                 * Atleti ammessi per esercizio con id = 1 -> quelli del gruppo 1, con id da 1 a 3 compreso 
                 * Atleti ammessi per esercizio con id = 2 -> quelli del gruppo 1, con id da 1 a 3 compreso 
                 * Atleti ammessi per esercizio con id = 3 -> quelli del gruppo 1, con id da 1 a 3 compreso 
                 * Atleti ammessi per esercizio con id = 4 -> quelli del gruppo 3, con id da 7 a 10 compreso 
                 * Atleti ammessi per esercizio con id = 5 -> quelli del gruppo 3, con id da 7 a 10 compreso 
                 * Atleti ammessi per esercizio con id = 6 -> quelli del gruppo 2, con id da 4 a 6 compreso 
                 * Atleti ammessi per esercizio con id = 7 -> quelli del gruppo 2, con id da 4 a 6 compreso
                 * Atleti ammessi per esercizio con id = 8 -> quelli del gruppo 4, con id da 11 a 12 compreso 
                 */
                addTime(_db, 1, 1, 1, "00-00"); //Esercizio 0, Atleta 0, 4 ripetizioni
                addTime(_db, 1, 1, 2, "00-00"); //Esercizio 0, Atleta 0, 4 ripetizioni
                addTime(_db, 1, 1, 3, "00-00"); //Esercizio 0, Atleta 0, 4 ripetizioni
                addTime(_db, 1, 1, 4, "00-00"); //Esercizio 0, Atleta 0, 4 ripetizioni
                addTime(_db, 2, 1, 1, "00-00"); //Esercizio 0, Atleta 1, 4 ripetizioni
                addTime(_db, 2, 1, 2, "00-00"); //Esercizio 0, Atleta 1, 4 ripetizioni
                addTime(_db, 2, 1, 3, "00-00"); //Esercizio 0, Atleta 1, 4 ripetizioni
                addTime(_db, 2, 1, 4, "00-00"); //Esercizio 0, Atleta 1, 4 ripetizioni
                addTime(_db, 3, 1, 1, "00-00"); //Esercizio 0, Atleta 2, 4 ripetizioni
                addTime(_db, 3, 1, 2, "00-00"); //Esercizio 0, Atleta 2, 4 ripetizioni
                addTime(_db, 3, 1, 3, "00-00"); //Esercizio 0, Atleta 2, 4 ripetizioni
                addTime(_db, 3, 1, 4, "00-00"); //Esercizio 0, Atleta 2, 4 ripetizioni
                
                addTime(_db, 1, 2, 1, "00-00"); //Esercizio 1, Atleta 0, 2 ripetizioni
                addTime(_db, 1, 2, 2, "00-00"); //Esercizio 1, Atleta 0, 2 ripetizioni
                addTime(_db, 2, 2, 1, "00-00"); //Esercizio 1, Atleta 1, 2 ripetizioni
                addTime(_db, 2, 2, 2, "00-00"); //Esercizio 1, Atleta 1, 2 ripetizioni
                addTime(_db, 3, 2, 1, "00-00"); //Esercizio 1, Atleta 2, 2 ripetizioni
                addTime(_db, 3, 2, 2, "00-00"); //Esercizio 1, Atleta 2, 2 ripetizioni
                
                addTime(_db, 1, 3, 1, "00-00"); //Esercizio 2, Atleta 0, 3 ripetizioni
                addTime(_db, 1, 3, 2, "00-00"); //Esercizio 2, Atleta 0, 3 ripetizioni
                addTime(_db, 1, 3, 3, "00-00"); //Esercizio 2, Atleta 0, 3 ripetizioni
                addTime(_db, 2, 3, 1, "00-00"); //Esercizio 2, Atleta 1, 3 ripetizioni
                addTime(_db, 2, 3, 2, "00-00"); //Esercizio 2, Atleta 1, 3 ripetizioni
                addTime(_db, 2, 3, 3, "00-00"); //Esercizio 2, Atleta 1, 3 ripetizioni
                addTime(_db, 3, 3, 1, "00-00"); //Esercizio 2, Atleta 2, 3 ripetizioni
                addTime(_db, 3, 3, 2, "00-00"); //Esercizio 2, Atleta 2, 3 ripetizioni
                addTime(_db, 3, 3, 3, "00-00"); //Esercizio 2, Atleta 2, 3 ripetizioni
                
                addTime(_db, 7, 4, 1, "00-00"); //Esercizio 3, Atleta 6, 5 ripetizioni
                addTime(_db, 7, 4, 2, "00-00"); //Esercizio 3, Atleta 6, 5 ripetizioni
                addTime(_db, 7, 4, 3, "00-00"); //Esercizio 3, Atleta 6, 5 ripetizioni
                addTime(_db, 7, 4, 4, "00-00"); //Esercizio 3, Atleta 6, 5 ripetizioni
                addTime(_db, 7, 4, 5, "00-00"); //Esercizio 3, Atleta 6, 5 ripetizioni
                addTime(_db, 8, 4, 1, "00-00"); //Esercizio 3, Atleta 7, 5 ripetizioni
                addTime(_db, 8, 4, 2, "00-00"); //Esercizio 3, Atleta 7, 5 ripetizioni
                addTime(_db, 8, 4, 3, "00-00"); //Esercizio 3, Atleta 7, 5 ripetizioni
                addTime(_db, 8, 4, 4, "00-00"); //Esercizio 3, Atleta 7, 5 ripetizioni
                addTime(_db, 8, 4, 5, "00-00"); //Esercizio 3, Atleta 7, 5 ripetizioni
                addTime(_db, 9, 4, 1, "00-00"); //Esercizio 3, Atleta 8, 5 ripetizioni
                addTime(_db, 9, 4, 2, "00-00"); //Esercizio 3, Atleta 8, 5 ripetizioni
                addTime(_db, 9, 4, 3, "00-00"); //Esercizio 3, Atleta 8, 5 ripetizioni
                addTime(_db, 9, 4, 4, "00-00"); //Esercizio 3, Atleta 8, 5 ripetizioni
                addTime(_db, 9, 4, 5, "00-00"); //Esercizio 3, Atleta 8, 5 ripetizioni
                addTime(_db, 10, 4, 1, "00-00"); //Esercizio 3, Atleta 9, 5 ripetizioni
                addTime(_db, 10, 4, 2, "00-00"); //Esercizio 3, Atleta 9, 5 ripetizioni
                addTime(_db, 10, 4, 3, "00-00"); //Esercizio 3, Atleta 9, 5 ripetizioni
                addTime(_db, 10, 4, 4, "00-00"); //Esercizio 3, Atleta 9, 5 ripetizioni
                addTime(_db, 10, 4, 5, "00-00"); //Esercizio 3, Atleta 9, 5 ripetizioni
                
                addTime(_db, 7, 5, 1, "00-00"); //Esercizio 4, Atleta 6, 1 ripetizioni
                addTime(_db, 8, 5, 1, "00-00"); //Esercizio 4, Atleta 7, 1 ripetizioni
                addTime(_db, 9, 5, 1, "00-00"); //Esercizio 4, Atleta 8, 1 ripetizioni
                addTime(_db, 10, 5, 1, "00-00"); //Esercizio 4, Atleta 9, 1 ripetizioni

                addTime(_db, 4, 6, 1, "03-58"); //Esercizio 5, Atleta 3, 2 ripetizioni
                addTime(_db, 4, 6, 2, "03-55"); //Esercizio 5, Atleta 3, 2 ripetizioni
                addTime(_db, 5, 6, 1, "03-52"); //Esercizio 5, Atleta 4, 2 ripetizioni
                addTime(_db, 5, 6, 2, "03-51"); //Esercizio 5, Atleta 4, 2 ripetizioni
                addTime(_db, 6, 6, 1, "03-57"); //Esercizio 5, Atleta 5, 2 ripetizioni
                addTime(_db, 6, 6, 2, "03-54"); //Esercizio 5, Atleta 5, 2 ripetizioni
 
                addTime(_db, 4, 7, 1, "05-45"); //Esercizio 6, Atleta 3, 1 ripetizioni
                addTime(_db, 5, 7, 1, "05-48"); //Esercizio 6, Atleta 4, 1 ripetizioni
                addTime(_db, 6, 7, 1, "05-50"); //Esercizio 6, Atleta 5, 1 ripetizioni
                
                addTime(_db, 11, 8, 1, "00-00"); //Esercizio 7, Atleta 10, 1 ripetizioni
                addTime(_db, 12, 8, 1, "00-00"); //Esercizio 7, Atleta 11, 1 ripetizioni
                
                addTime(_db, 1, 9, 1, "00-00"); //Esercizio 8, Atleta 1, 2 ripetizioni
                addTime(_db, 1, 9, 2, "00-00"); //Esercizio 8, Atleta 1, 2 ripetizioni
                addTime(_db, 2, 9, 1, "00-00"); //Esercizio 8, Atleta 2, 2 ripetizioni
                addTime(_db, 2, 9, 2, "00-00"); //Esercizio 8, Atleta 2, 2 ripetizioni
                addTime(_db, 3, 9, 1, "00-00"); //Esercizio 8, Atleta 3, 2 ripetizioni
                addTime(_db, 3, 9, 2, "00-00"); //Esercizio 8, Atleta 3, 2 ripetizioni
        }

        /**
         * Metodo chiamato se la versione del db viene cambiata; da ignorare
         */
        @Override
        public void onUpgrade(SQLiteDatabase _db, int oldVersion, int newVersion) 
        {
              

        }

    }
    
    // ===========================================================
 	// Adder
 	// ===========================================================
    
    /**
     * Aggiunge un allenamento al db.
     * When to use: nella creazione dell'allenamento, dopo che l'utente preme il tasto per salvare. 
     * NOTA: la lista degli esercizi che compongono l'allenamento, aggiunta chiamando il metodo addExercise per ogni
     * esercizio, deve essere creata DOPO la creazione dell'allenamento, in quanto ogni esercizio dovrà fare riferimento
     * all'id di un allenamento, che se non è presente nel db causerà una eccezione.
     * 
     * @param pDb il db; da passare quando si chiama il metodo nell'onCreate del DbHelper, quando getWritableDatabase riourna
     *        null in quanto il db non è stato ancora istanziato del tutto; passare null se il db è già creato
     * @param pNome nome dell'allenamento
     * @param pData data dell'allenamento, in long (da formattare la data nella forma dd-MM-yyyy)
     * @param pOraInizio ora di inizio dell'allenamento, in long (da formattare l'ora nella forma k:m)
     * @param pOraFine ora di fine dell'allenamento, in long (da formattare l'ora nella forma k:m)
     * @param pGruppoId id del gruppo che deve fare l'allenamento
     * 
     * @return l'id nel db della nuova entry
     */
    public long addTraining(SQLiteDatabase pDb, String pNome, long pData, long pOraInizio, long pOraFine, int pGruppoId)
    {
    	Log.w("prova", "sono qui");
    	//Creo il nuovo ContentValues per immagazzinare la riga
        ContentValues newTraining = new ContentValues();
      
        //Aggiungo i vari valori, associati ognuno con la rispettiva colonna
        newTraining.put(NOME_ALLENAMENTO_COLUMN, pNome);
        Log.w("inserimento nome allenamento", "ok");
        newTraining.put(DATA_ALLENAMENTO_COLUMN, pData);
        newTraining.put(ORA_INIZIO__ALLENAMENTO_COLUMN, pOraInizio);
        newTraining.put(ORA_FINE__ALLENAMENTO_COLUMN, pOraFine);
        newTraining.put(ID_GRUPPO_ALLENAMENTO_COLUMN, pGruppoId);
      
        /* Adesso recupero il db; normalmente è meglio chiamare getWritableDatabase ogni volta che serve il db, ed aprirlo
         * e chiuderlo rispettivamente quando serve la prima volta e quando non serve più. Se però addTraining è chiamato
         * all'interno dell'onCreate per popolare la tabella, il db non è ancora istanziato del tutto e getWritableDatabase
         * non si può usare. Per questo motivo dall'onCreate passo il db in modo che qua dentro si utilizzi quello. 
         * Se l'add è fatta durante l'uso dell'app, allora il db e creato e bisogna passare null. In questo caso
         * si chiamerà getWritableDatabase come dovrebbe accadere normalmente */
        SQLiteDatabase db = pDb;
        
        if(pDb == null)
        	db = mDbHelper.getWritableDatabase();
        
        return db.insert(DbHelper.TABLE_TRAINING, null, newTraining); 	
    }
    
    
    /**
     * Aggiunge un esercizio al db.
     * When to use: nella creazione dell'allenamento, dopo che l'utente preme il tasto per salvare. Per aggiungere la
     * lista degli esercizi, scorrere l'array che li contiene e passare ogni esercizio a questo metodo.
     * NOTA: gli esercizi fanno riferimento all'id di un allenamento, che deve essere già presente nel db. Quindi, visto
     * che la lista degli esercizi di un allenamento e l'allenamento stesso vengono aggiunti uno dopo l'altro, bisogna
     * aggiungere PRIMA l'allenamento, POI gli esercizi che si riferiscono a quello, altrimenti verrà lanciata una eccezione.
     * 
     * @param pDb il db; da passare quando si chiama il metodo nell'onCreate del DbHelper, quando getWritableDatabase riourna
     *        null in quanto il db non è stato ancora istanziato del tutto; passare null se il db è già creato
     * @param pRipetizioni ripetizioni dell'esercizio
     * @param pDistanza distanza dell'esercizio
     * @param pStile1 stile1 dell'esercizio
     * @param pStile2 stile2 dell'esercizio
     * @param pAndatura  andatura dell'esercizio
     * @param pTempo tempo di percorrenza dell'esercizio, deve essere nella forma 00-00
     * @param pAllenamentoId id dell'allenamento di cui fa parte l'esercizio. L'id deve esistere nella tabella training
     * 
     * @return l'id nel db della nuova entry
     */
    public long addExercise(SQLiteDatabase pDb, int pRipetizioni, int pDistanza, String pStile1, String pStile2, String pAndatura, String pTempo, long pAllenamentoId)
    {
        ContentValues newExercise = new ContentValues();
      
        newExercise.put(RIPETIZIONI_ESERCIZIO_COLUMN, pRipetizioni);
        newExercise.put(DISTANZA_ESERCIZIO_COLUMN, pDistanza);
        newExercise.put(STILE1_ESERCIZIO_COLUMN, pStile1);
        newExercise.put(STILE2_ESERCIZIO_COLUMN, pStile2);
        newExercise.put(ANDATURA_ESERCIZIO_COLUMN, pAndatura);
        newExercise.put(TEMPO_ESERCIZIO_COLUMN, pTempo);
        newExercise.put(ID_ALLENAMENTO_ESERCIZIO_COLUMN, pAllenamentoId);
      
        SQLiteDatabase db = pDb;
        
        if(pDb == null)
        	db = mDbHelper.getWritableDatabase();
        
        return db.insert(DbHelper.TABLE_EXERCISE, null, newExercise); 	
    }
    
    
    /**
     * Aggiunge un'atleta al db.
     * When to use: never, solo nell'onCreate del database, quando verranno popolate tutte le tabelle con i valori di default
     * 
     * @param pDb il db
     * @param pNome nome dell'atleta
     * @param pCognome cognome dell'atleta
     * @param pGruppoId id del gruppo di cui fa parte l'atleta
     */
    public void addAthlete(SQLiteDatabase pDb, String pNome, String pCognome, int pGruppoId)
    {
        ContentValues newAthlete = new ContentValues();
      
        newAthlete.put(NOME_ATLETA_COLUMN, pNome);
        newAthlete.put(COGNOME_ATLETA_COLUMN, pCognome);
        newAthlete.put(ID_GRUPPO_ATLETA_COLUMN, pGruppoId);
      
        SQLiteDatabase db = pDb;

        db.insert(DbHelper.TABLE_ATHLETE, null, newAthlete); 	
    }
    
    
    /**
     * Aggiunge il tempo di un'atleta ad una ripetizione di un esercizio nel db.
     * When to use: nella creazione dell'allenamento, dopo che l'utente preme il tasto per salvare. Quando si scorre l'array
     * della lista degli esercizi (in cui si chiama per ogni esercizio addExercise), bisogna chiamare pure questo metodo
     * PER OGNI RIPETIZIONE DELL'ESERCIZIO per aggiungere i vari tempi (che dovranno essere tutti 00-00)
     * NOTA: dato che ogni tempo deve fare riferimento ad un esercizio già esistente nel db, bisogna prima aggiungere
     * l'esercizio col metodo addExercise e poi chiamare addTime per ogni sua ripetizione, passando come tempo 00-00 ogni volta.
     * 
     * @param pDb il db; da passare quando si chiama il metodo nell'onCreate del DbHelper, quando getWritableDatabase riourna
     *        null in quanto il db non è stato ancora istanziato del tutto; passare null se il db è già creato
     * @param pAtletaId id dell'atleta che ha fatto il tempo (riferimento alla tabella atleti)
     * @param pEsercizioId id dell'esercizio di cui si vuole aggiungere il tempo (riferimento alla tabella esercizi)
     * @param pRipetizione numero della ripetizione di cui si vuole segnare il tempo
     * @param pTempo il tempo, che deve essere nella forma 00-00
     * 
     * @return l'id nel db della nuova entry
     */
    public long addTime(SQLiteDatabase pDb, int pAtletaId, long pEsercizioId, int pRipetizione, String pTempo)
    {
        ContentValues newTime = new ContentValues();
      
        newTime.put(ID_ATLETA_TEMPI_COLUMN, pAtletaId);
        newTime.put(ID_ESERCIZIO_TEMPI_COLUMN, pEsercizioId);
        newTime.put(RIPETIZIONE_TEMPI_COLUMN, pRipetizione);
        newTime.put(TEMPO_TEMPI_COLUMN, pTempo);
      
        SQLiteDatabase db = pDb;
        
        if(pDb == null)
        	db = mDbHelper.getWritableDatabase();
        
        return db.insert(DbHelper.TABLE_TIMES, null, newTime); 	
    }
    
    // ===========================================================
 	// Getter
 	// ===========================================================
    
    /** 
     * Ritorna la lista degli allenamenti previsti per un dato giorno, in ordine di ora di inizio. 
     * When to use: per recuperare gli allenamenti per la day view
     * 
     * @param pData la data del giorno, in forma 00-00-0000
     * 
     * @return arraylist contenente i training
     */
    public ArrayList<Training> getTrainingsInDay(long pData)
    {
    	Cursor cursor;
    	int nameCol;
    	ArrayList<Training> allenamenti = new ArrayList<Training>();	
		
    	String query = "SELECT * FROM " +  DbHelper.TABLE_TRAINING + " " +
    				   "WHERE " + DATA_ALLENAMENTO_COLUMN + " = " + pData + " ORDER BY " + ORA_INIZIO__ALLENAMENTO_COLUMN + ";";
    	

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
    	
    	cursor = db.rawQuery(query, null);
    	
    	if(cursor.moveToFirst())
    	{
    		do
    		{
    			Training allenamento = new Training();
    			
    			nameCol = cursor.getColumnIndex("id");
    			allenamento.setId_allenamento(cursor.getInt(nameCol));
    			
    			nameCol = cursor.getColumnIndex(NOME_ALLENAMENTO_COLUMN);
    	    	allenamento.setNome(cursor.getString(nameCol));
    	    	
    	    	//Prelevo l'intero (long) e lo converto in una stringa rappresentante la data
    	    	nameCol = cursor.getColumnIndex(DATA_ALLENAMENTO_COLUMN);
    	    	SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy"); 
                String dateString = formatter.format(new Date(cursor.getInt(nameCol)));
    	    	allenamento.setData_allenamento(dateString);
    			
    	    	nameCol = cursor.getColumnIndex(ORA_INIZIO__ALLENAMENTO_COLUMN);
    	    	formatter = new SimpleDateFormat("k:m"); 
                String startHourString = formatter.format(new Date(cursor.getInt(nameCol)));
    	    	allenamento.setOra_inizio(startHourString);
    	    	
    	    	nameCol = cursor.getColumnIndex(ORA_FINE__ALLENAMENTO_COLUMN);
    	    	String endHourString = formatter.format(new Date(cursor.getInt(nameCol)));
    	    	allenamento.setOra_fine(endHourString);
    	    	
    	    	nameCol = cursor.getColumnIndex(ID_GRUPPO_ALLENAMENTO_COLUMN);
    	    	allenamento.setId_gruppo(cursor.getInt(nameCol));
    	    	
    	    	Log.w("DB_getTrainingsInDay", allenamento.toString());
    	    	
    	    	allenamenti.add(allenamento);
    	    	
    		} while(cursor.moveToNext());
    	}
    	
    	cursor.close();
    	
    	return allenamenti;
    }
    
    
    /** 
     * Ritorna la lista degli allenamenti previsti per un dato intervallo di giorni
     * When to use: per recuperare i giorni in cui ci sono allenamenti nella month view
     * 
     * @param startDay limite sinistro del range
     * @param endDay limite destro del range
     * 
     * @return arraylist contenente le stringhe con le date, nella forma 00-00-0000
     */
    public ArrayList<DateTime> getTrainingsDateInRange(long startDay, long endDay)
    {
        Cursor c;
        int nameCol;
        //arraylist che conterrà le date degli allenamenti
        ArrayList<DateTime> trainingsDates = new ArrayList<DateTime>();
        
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        
        //eseguo la query controllando il range di date selezionate
        c = db.rawQuery("SELECT * FROM " + DbHelper.TABLE_TRAINING +" WHERE " + DATA_ALLENAMENTO_COLUMN +" between "+ startDay + " AND " + endDay +";", null);
        
        if(c.moveToFirst())
        {
                do{
                        //seleziono la colonna
                nameCol = c.getColumnIndex(DATA_ALLENAMENTO_COLUMN);
                //Creo un nuovo date formatter
                SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy"); 
                //prelevo l'intero (long) e lo converto in una stringa rappresentante la data
                trainingsDates.add(CalendarHelper.convertDateToDateTime(new Date(c.getLong(nameCol))));

                }while(c.moveToNext());
                
                return trainingsDates;
        }
        
        c.close();
        
        return new ArrayList<DateTime>();
    }
    
    
    /** 
     * Ritorna l'allenamento che ha il dato id
     * 
     * @param pTrainingId l'id dell'allenamento da prelevare
     * 
     * @return l'allenamento trovato, o null se non c'era
     */
    public Training getTrainingFromId(long pTrainingId)
    {
    	Cursor cursor;
    	int nameCol;
    	Training allenamento = null;
		
    	String query = "SELECT * FROM " +  DbHelper.TABLE_TRAINING + " " +
    				   "WHERE id = " + pTrainingId + ";";
    	

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
    	
    	cursor = db.rawQuery(query, null);

    	
    	if(cursor.moveToFirst())
    	{
			allenamento = new Training();
			
			nameCol = cursor.getColumnIndex("id");
			allenamento.setId_allenamento(cursor.getInt(nameCol));
			
			nameCol = cursor.getColumnIndex(NOME_ALLENAMENTO_COLUMN);
	    	allenamento.setNome(cursor.getString(nameCol));
	    	
	    	//Prelevo l'intero (long) e lo converto in una stringa rappresentante la data
	    	nameCol = cursor.getColumnIndex(DATA_ALLENAMENTO_COLUMN);
	    	SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy"); 
            String dateString = formatter.format(new Date(cursor.getInt(nameCol)));
	    	allenamento.setData_allenamento(dateString);
			
	    	nameCol = cursor.getColumnIndex(ORA_INIZIO__ALLENAMENTO_COLUMN);
	    	formatter = new SimpleDateFormat("k:m"); 
            String startHourString = formatter.format(new Date(cursor.getInt(nameCol)));
	    	allenamento.setOra_inizio(startHourString);
	    	
	    	nameCol = cursor.getColumnIndex(ORA_FINE__ALLENAMENTO_COLUMN);
	    	String endHourString = formatter.format(new Date(cursor.getInt(nameCol)));
	    	allenamento.setOra_fine(endHourString);
	    	
	    	nameCol = cursor.getColumnIndex(ID_GRUPPO_ALLENAMENTO_COLUMN);
	    	allenamento.setId_gruppo(cursor.getInt(nameCol));
    	}
    	
    	cursor.close();
    	
    	return allenamento;
    }

    
    /** 
     * Ritorna la lista degli esercizi previsti per un dato allenamento. 
     * When to use: per recuperare gli esercizi quando l'user preme su un allenamento nella day view
     * 
     * @param pAllenamentoId l'id dell'allenamento in questione
     * 
     * @return arraylist contenente gli esercizi
     */
    public ArrayList<Esercizio> getExercisesInTraining(long pAllenamentoId)
    {
    	Cursor cursor;
    	int nameCol;
    	ArrayList<Esercizio> esercizi = new ArrayList<Esercizio>();
    	
    	String query = "SELECT * FROM " +  DbHelper.TABLE_EXERCISE + " " +
    				   "WHERE " + ID_ALLENAMENTO_ESERCIZIO_COLUMN + " = '" + pAllenamentoId +"';";
    	

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
    	
    	cursor = db.rawQuery(query, null);
    	
    	if(cursor.moveToFirst())
    	{
    		do
    		{
    			Esercizio esercizio = new Esercizio();
    			
    			nameCol = cursor.getColumnIndex("id");
    			esercizio.setId(cursor.getInt(nameCol));
    			
    			nameCol = cursor.getColumnIndex(RIPETIZIONI_ESERCIZIO_COLUMN);
    			esercizio.setRipetizioni(cursor.getInt(nameCol));
    	    	
    	    	nameCol = cursor.getColumnIndex(DISTANZA_ESERCIZIO_COLUMN);
    	    	esercizio.setDistanza(cursor.getInt(nameCol));
    			
    	    	nameCol = cursor.getColumnIndex(STILE1_ESERCIZIO_COLUMN);
    	    	esercizio.setStile1(cursor.getString(nameCol));
    	    	
    	    	nameCol = cursor.getColumnIndex(STILE2_ESERCIZIO_COLUMN);
    	    	esercizio.setStile2(cursor.getString(nameCol));
    	    	
    	    	nameCol = cursor.getColumnIndex(ANDATURA_ESERCIZIO_COLUMN);
    	    	esercizio.setAndatura(cursor.getString(nameCol));
    	    	
    	    	nameCol = cursor.getColumnIndex(TEMPO_ESERCIZIO_COLUMN);
    	    	esercizio.setTempo(cursor.getString(nameCol));
    	    	
    	    	nameCol = cursor.getColumnIndex(ID_ALLENAMENTO_ESERCIZIO_COLUMN);
    	    	esercizio.setId_Allenamento(cursor.getInt(nameCol));
    	    	
    	    	Log.w("DB_getExerciseInTraining", esercizio.toString());
    	    	
    	    	esercizi.add(esercizio);
    	    	
    		} while(cursor.moveToNext());
    	}
    	
    	cursor.close();
    	
    	return esercizi;
    }
    
    
    /** 
     * Ritorna la lista degli atleti appartenenti al gruppo specificato. 
     * When to use: per recuperare gli atleti da mostrare quando un utente preme nel bottone "visualizza tempi" per
     * il dato esercizio.
     * 
     * @param pGruppoId l'id del gruppo in questione
     * 
     * @return arraylist contenente gli atleti
     */
    public ArrayList<Atleta> getAthletesInGroup(int pGruppoId)
    {
    	Cursor cursor;
    	int nameCol;
    	ArrayList<Atleta> atleti = new ArrayList<Atleta>();
    	
    	String query = "SELECT * FROM " +  DbHelper.TABLE_ATHLETE + " " +
    				   "WHERE " + ID_GRUPPO_ATLETA_COLUMN + " = " + pGruppoId +";";
    	

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
    	
    	cursor = db.rawQuery(query, null);
    	
    	if(cursor.moveToFirst())
    	{
    		do
    		{
    			Atleta atleta = new Atleta();
    			
    			nameCol = cursor.getColumnIndex("id");
    	    	atleta.setId(cursor.getInt(nameCol));
    			
    	    	nameCol = cursor.getColumnIndex(NOME_ATLETA_COLUMN);
    	    	atleta.setNome(cursor.getString(nameCol));
    			
    	    	nameCol = cursor.getColumnIndex(COGNOME_ATLETA_COLUMN);
    	    	atleta.setCognome(cursor.getString(nameCol));
    	    	
    	    	nameCol = cursor.getColumnIndex(ID_GRUPPO_ATLETA_COLUMN);
    	    	atleta.setId_gruppo(cursor.getInt(nameCol));
    	    	
    	    	atleti.add(atleta);
    	    	
    	    	Log.w("DB_getAthletesInGroup", atleta.toString());
    	    	
    		} while(cursor.moveToNext());
    	}
    	
    	cursor.close();
    	
    	return atleti;
    }
    
    
    /** 
     * Ritorna la lista dei tempi fatti da un atleta per l'esercizio specificato. La classe atleta ha un campo
     * "tempi" che è un ArrayList<Tempo>, cioè deve contenere il valore ritornato da questo metodo.
     * When to use: per recuperare i tempi dell'atleta per quel dato esercizio quando l'utente preme sul dato atleta.
     * 
     * @param pAtletaId l'id dell'atleta in questione
     * @param pEsercizioId l'id dell'esercizio in questione
     * 
     * @return arraylist contenente i tempi
     */
    public ArrayList<Tempo> getAthletesTime(int pAtletaId, int pEsercizioId)
    {
    	Cursor cursor;
    	int nameCol;
    	ArrayList<Tempo> tempi = new ArrayList<Tempo>();
    	
    	String query = "SELECT * FROM " +  DbHelper.TABLE_TIMES + " " + 
    				   "WHERE " + ID_ATLETA_TEMPI_COLUMN + " = " + pAtletaId +"" + " " + 
    				   "AND " + ID_ESERCIZIO_TEMPI_COLUMN + " = " + pEsercizioId + ";";
    	

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
    	
    	cursor = db.rawQuery(query, null);
    	
    	if(cursor.moveToFirst())
    	{
    		do
    		{
    			Tempo tempo = new Tempo();
    			
    			nameCol = cursor.getColumnIndex("id");
    			tempo.setId(cursor.getInt(nameCol));
    			
    	    	nameCol = cursor.getColumnIndex(ID_ATLETA_TEMPI_COLUMN);
    	    	tempo.setIdAtleta(cursor.getInt(nameCol));
    			
    	    	nameCol = cursor.getColumnIndex(ID_ESERCIZIO_TEMPI_COLUMN);
    	    	tempo.setIdEsercizio(cursor.getInt(nameCol));
    	    	
    	    	nameCol = cursor.getColumnIndex(RIPETIZIONE_TEMPI_COLUMN);
    	    	tempo.setRipetizione(cursor.getInt(nameCol));
    	    	
    	    	nameCol = cursor.getColumnIndex(TEMPO_TEMPI_COLUMN);
    	    	tempo.setTempo(cursor.getString(nameCol));
    	    	
    	    	tempi.add(tempo);
    	    	
    		} while(cursor.moveToNext());
    	}
    	
    	cursor.close();
    	
    	return tempi;
    }
    
    
    /**
     * Prelevo la lista dei gruppi per sceglierne uno durante la creazione di un allenamento
     * @return un ArrayList di stringhe con i nomi dei gruppi
     */
    public ArrayList<String> getGroups(){
    	
    	Cursor cursor;
    	int nameCol;
    	ArrayList<String> gruppi = null;
    	
    	String query = "SELECT DISTINCT " + ID_GRUPPO_ATLETA_COLUMN + " FROM " +  DbHelper.TABLE_ATHLETE + ";";
    	
    	SQLiteDatabase db = mDbHelper.getWritableDatabase();
    	
    	cursor = db.rawQuery(query, null);
    	
    	if(cursor.moveToFirst())
    	{
    		gruppi = new ArrayList<String>();
    		
    		do
    		{
    			nameCol = cursor.getColumnIndex(ID_GRUPPO_ATLETA_COLUMN);
    			gruppi.add("Gruppo " + (cursor.getInt(nameCol)));    	    	
    		} while(cursor.moveToNext());

    	}
    	
    	cursor.close();
    	
    	return gruppi;
    	
    }
    
    
    // ===========================================================
 	// Updater
 	// ===========================================================
    
    
    /**
     * Aggiorna il tempo di un'atleta per un dato esercizio ed una data ripetizione.
     * When to use: schermata di modifica dei tempi. Dato che i tempi devono essere modificati sotto conferma dell'utente
     * (cioè quando preme il tasto per salvare), bisogna fare le modifiche solo in quel momento. Quindi mentre l'utente modifica 
     * i tempi, si mettono tutti i tempi modificati in un ArrayList<Tempo> o simile e quando l'utente salva si 
     * chiama updateAthletesTime per ogni elemento dell'array.
     * 
     * @param pTempoId l'id del tempo in questione
     * @param pTempo il nuovo tempo da mettere
     */
    public void updateAthletesTime(int pTempoId, String pTempo)
    {
        ContentValues updatedValues = new ContentValues();
        
        updatedValues.put(TEMPO_TEMPI_COLUMN, pTempo);
      
        //Specifico la clausola where
        String where = "id" + "=" + pTempoId;
        String whereArgs[] = null;
      
        //Recupero il db e faccio l'update
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.update(mDbHelper.TABLE_TIMES, updatedValues, where, whereArgs);
    }
    
    
    /**
     * Aggiorna un allenamento con i nuovi valori passati.
     * When to use: modifica di un allenamento, dopo che l'utente conferma le modifiche
     * 
     * @param pAllenamentoId l'id dell'allenamento in questione
     * @param pNome nome dell'allenamento
     * @param pData data dell'allenamento, deve essere nella forma 00-00-0000
     * @param pOraInizio ora di inizio dell'allenamento, deve essere nella forma 00:00
     * @param pOraFine ora di fine dell'allenamento, deve essere nella forma 00:00
     * @param pGruppoId id del gruppo che deve fare l'allenamento
     */
    public void updateTraining(long pAllenamentoId, String pNome, long pOraInizio, long pOraFine, int pGruppoId)
    {
    	//Creo il nuovo ContentValues per immagazzinare la riga
        ContentValues updatedValues = new ContentValues();
      
        //Aggiungo i vari valori, associati ognuno con la rispettiva colonna
        updatedValues.put(NOME_ALLENAMENTO_COLUMN, pNome);
        updatedValues.put(ORA_INIZIO__ALLENAMENTO_COLUMN, pOraInizio);
        updatedValues.put(ORA_FINE__ALLENAMENTO_COLUMN, pOraFine);
        updatedValues.put(ID_GRUPPO_ALLENAMENTO_COLUMN, pGruppoId);
      
        //Specifico la clausola where
        String where = "id" + "=" + pAllenamentoId;
        String whereArgs[] = null;
      
        //Recupero il db e faccio l'update
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.update(mDbHelper.TABLE_TRAINING, updatedValues, where, whereArgs);
    }
    
    /**
     * Aggiorna un esercizio con i nuovi valori passati.
     * When to use: 
     * 
     * @param pAllenamentoId l'id dell'allenamento in questione
     * @param pNome nome dell'allenamento
     * @param pData data dell'allenamento, deve essere nella forma 00-00-0000
     * @param pOraInizio ora di inizio dell'allenamento, deve essere nella forma 00:00
     * @param pOraFine ora di fine dell'allenamento, deve essere nella forma 00:00
     * @param pGruppoId id del gruppo che deve fare l'allenamento
     */
    public void updateExercise(int pEsercizioId, int pRipetizioni, int pDistanza, String pStile1, String pStile2, String pAndatura, String pTempo, int pAllenamentoId)
    {
        ContentValues updatedValues = new ContentValues();
      
        updatedValues.put(RIPETIZIONI_ESERCIZIO_COLUMN, pRipetizioni);
        updatedValues.put(DISTANZA_ESERCIZIO_COLUMN, pDistanza);
        updatedValues.put(STILE1_ESERCIZIO_COLUMN, pStile1);
        updatedValues.put(STILE2_ESERCIZIO_COLUMN, pStile2);
        updatedValues.put(ANDATURA_ESERCIZIO_COLUMN, pAndatura);
        updatedValues.put(TEMPO_ESERCIZIO_COLUMN, pTempo);
        updatedValues.put(ID_ALLENAMENTO_ESERCIZIO_COLUMN, pAllenamentoId);
      
        String where = "id" + "=" + pEsercizioId;
        String whereArgs[] = null;
      
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.update(mDbHelper.TABLE_EXERCISE, updatedValues, where, whereArgs);
    }
    
    
    // ===========================================================
  	// Deleter
  	// ===========================================================
    
    /**
     * Elimina l'allenamento con l'id specificato dal db
     * 
     * @param pTrainingId l'id dell'allenamento da eliminare
     */
    public void deleteTraining(long pTrainingId)
    {    	
    	String query = "DELETE FROM " +  DbHelper.TABLE_TRAINING + " " +
				   	   "WHERE id = " + pTrainingId + ";";
	

    	SQLiteDatabase db = mDbHelper.getWritableDatabase();

    	db.execSQL(query);
    }
    

    /**
     * Elimina l'esercizio con l'id specificato dal db
     * 
     * @param pEsercizioId l'id dell'esercizio da eliminare
     */
    public void deleteExercise(long pEsercizioId)
    {    	
    	String query = "DELETE FROM " +  DbHelper.TABLE_EXERCISE + " " +
				   	   "WHERE id = " + pEsercizioId + ";";
	

    	SQLiteDatabase db = mDbHelper.getWritableDatabase();

	
    	db.execSQL(query);
    }
    

    /**
     * Elimina il tempo con l'id specificato dal db
     * 
     * @param pTimeId l'id del tempo da eliminare
     */
    public void deleteTime(long pTimeId)
    {    	
    	String query = "DELETE FROM " +  DbHelper.TABLE_TIMES + " " +
				   	   "WHERE id = " + pTimeId + ";";
	

    	SQLiteDatabase db = mDbHelper.getWritableDatabase();

    	db.execSQL(query);
    }
    
    
    /**
     * Elimina il tempo dell'id dell'esercizio specificato dove le ripetizioni sono maggiori del parametro passato.
     * Usato nella modifica di un esercizio se le nuove ripetizioni inserite sono MINORI di quelle che c'erano prima. 
     * 
     * Esempio di uso: l'esercizio aveva 15 ripetizioni, l'utente l'ha modificato e ora sono 10. Bisogna togliere i tempi
     * che sono maggiori di 10. Quindi in questo caso si farebbe una chiamata del genere: deleteTimesFromRepetition(id, 10)
     * 
     * @param pExerciseId l'id dell'esercizio di cui bisogna cancellare i tempi
     * @param pLastRepetition il nuovo numero di ripetizioni
     */
    public void deleteTimesFromRepetition(long pExerciseId, int pLastRepetition)
    {    	
    	String query = "DELETE FROM " +  DbHelper.TABLE_TIMES + " " +
				   	   "WHERE " + ID_ESERCIZIO_TEMPI_COLUMN + " = " + pExerciseId + " " +
			   	   	   "AND " + RIPETIZIONE_TEMPI_COLUMN + " > " + pLastRepetition + ";";
	

    	SQLiteDatabase db = mDbHelper.getWritableDatabase();

    	db.execSQL(query);
    }
    
    
    /**
     * Elimina tutti i tempi di un dato atleta nel dato allenamento. 
     * Usato nella modifica di un allenamento. Infatti quando si modifica il gruppo di un allenamento, i precedenti
     * tempi del gruppo precedente devono essere rimossi dal db (per ogni esercizio presente nell'allenamento)
     * 
     * @param pAthleteId l'id dell'atleta
     * @param pExerciseId l'id dell'esercizio
     */
    public void deleteTimesFromAthleteInExercise(long pAthleteId, long pExerciseId)
    {    	
    	String query = "DELETE FROM " +  DbHelper.TABLE_TIMES + " " +
    				   "WHERE " + ID_ATLETA_TEMPI_COLUMN + " = " + pAthleteId + " " +
				   	   "AND " + ID_ESERCIZIO_TEMPI_COLUMN + " = " + pExerciseId + ";";
	

    	SQLiteDatabase db = mDbHelper.getWritableDatabase();

    	db.execSQL(query);
    }
}