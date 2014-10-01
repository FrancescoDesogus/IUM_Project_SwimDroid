package com.example.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Classe che rappresenta un esercizio. Dato che ad un certo punto dell'app bisogna passare un esercizio da un'activity
 * ad un'altra, per farlo occorre che questa classe implementi l'interfaccia Parcelable. L'implementazione è standard,
 * si trovano esempi un po' ovunque su internet. Avendo implementato l'interfaccia Parcelable, è possibile inserire
 * un esercizio come "extras" in un intent.
 * 
 * @author Tiffano
 *
 */
public class Esercizio implements Parcelable
{
	private int id;
	private int ripetizioni;
	private int distanza;
	private String stile1;
	private String stile2;
	private String stileAbbreviato;
	private String andatura;
	private String tempo;
	private int id_allenamento;

	public Esercizio()
	{

	}
	
    /** 
     * Questo campo CREATOR è richiesto dall'interfaccia Parcelable, e serve per creare una nuova istanza dell'oggetto
	 * Esercizio partendo da un parcel ricevuto. L'implementazione è standard, si trova su developer.android.com
	 */
	public static final Parcelable.Creator<Esercizio> CREATOR = new Parcelable.Creator<Esercizio>() 
	{
		public Esercizio createFromParcel(Parcel in) 
		{
			return new Esercizio(in);
		}

		public Esercizio[] newArray(int size) {
			return new Esercizio[size];
		}
	};

	/**
	 * Questo costruttore è usato solo all'interno del CREATOR qua sopra (notare che il costruttore è private), e serve
	 * per istanziare un nuovo oggetto Esercizio partendo da un parcel
	 * 
	 * @param incomingParcelable il parcelable da cui prendere i dati
	 */
	private Esercizio(Parcel incomingParcelable) 
	{
		//Recupero tutti i valori. NOTA: si recuperano nell'ordine con cui sono messi nel metodo writeToParcel
		id = incomingParcelable.readInt();
		ripetizioni = incomingParcelable.readInt();
		distanza = incomingParcelable.readInt();
		stile1 = incomingParcelable.readString();
		stile2 = incomingParcelable.readString();
		stileAbbreviato = incomingParcelable.readString();
		andatura = incomingParcelable.readString();
		tempo = incomingParcelable.readString();
		id_allenamento = incomingParcelable.readInt();
	}
	
	public int getId() 
	{
		return id;
	}

	public void setId(int pId) 
	{
		this.id = pId;
	}

	public int getRipetizioni() 
	{
		return ripetizioni;
	}

	public void setRipetizioni(int pRipetizioni) 
	{
		this.ripetizioni = pRipetizioni;
	}
	
	public int getDistanza() 
	{
		return distanza;
	}

	public void setDistanza(int pDistanza) 
	{
		this.distanza = pDistanza;
	}

	public String getStile1() 
	{
		return stile1;
	}

	public void setStile1(String pStile1) 
	{
		this.stile1 = pStile1;
		
		//Avvio anche il metodo che produce l'abbreviazione dello stile
		setStileAbbreviato();
	}
	
	public String getStile2() 
	{
		return stile2;
	}

	public void setStile2(String pStile2) 
	{
		this.stile2 = pStile2;		
	}
	
	public String getStileAbbreviato() 
	{
		return stileAbbreviato;
	}

	
	public void setStileAbbreviato() 
	{
		String abbreviation = "";
		
		if(stile1.equalsIgnoreCase("stile libero"))
			abbreviation = "SL";
		else if(stile1.equalsIgnoreCase("rana"))
			abbreviation = "RA";
		else if(stile1.equalsIgnoreCase("delfino"))
			abbreviation = "DE";
		else if(stile1.equalsIgnoreCase("dorso"))
			abbreviation = "DO";
		else if(stile1.equalsIgnoreCase("misti"))
			abbreviation = "MX";
		else if(stile1.equalsIgnoreCase("stile gara"))
			abbreviation = "SG";

			
		this.stileAbbreviato = abbreviation;
	}
	
	public String getAndatura() 
	{
		return andatura;
	}

	public void setAndatura(String pAndatura) 
	{
		this.andatura = pAndatura;
	}

	
	public String getTempo() 
	{
		return tempo;
	}

	public void setTempo(String pTempo) 
	{
		this.tempo = pTempo;
	}


	public int getId_Allenamento() 
	{
		return id_allenamento;
	}

	public void setId_Allenamento(int pId_Allenamento)
	{
		this.id_allenamento = pId_Allenamento;
	}

	
	@Override
	public String toString()
	{
		return id + ") rip: " + ripetizioni + ", dis: " + distanza + ", " + stile1 + "-" + stile2 + "-" + stileAbbreviato + "- " + andatura + ", tempo: " + tempo + ", id_allenamento: " + id_allenamento; 
	}
	
	@Override
	public boolean equals(Object pExerciseObj)
	{
		Esercizio pExercise = (Esercizio) pExerciseObj;
		
		boolean result = this.id == pExercise.id;
		
		return result;
	}
	
	@Override
	public int hashCode()
	{
		return this.id;
	}


	/**
	 * Metodo dell'interfaccia Parcelable; è sempre così
	 */
	@Override
	public int describeContents() 
	{
		return 0;
	}


	/**
	 * Metodo dell'interfaccia Parcelable. Inserisce i campi della classe all'interno del parcelable passato come parametro
	 * 
	 * @dest il Parcel in cui inserire i dati
	 * @flags intero usato quando si inserisce in un Parcel una classe custom (nel caso di Esercizio non serve)
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) 
	{
		dest.writeInt(id);
		dest.writeInt(ripetizioni);
		dest.writeInt(distanza);
		dest.writeString(stile1);
		dest.writeString(stile2);
		dest.writeString(stileAbbreviato);
		dest.writeString(andatura);
		dest.writeString(tempo);
		dest.writeInt(id_allenamento);
	}
}

