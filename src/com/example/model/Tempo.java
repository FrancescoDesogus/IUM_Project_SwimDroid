package com.example.model;

public class Tempo
{
	private int id;
	private int idAtleta;
	private int idEsercizio;
	private int ripetizione;
	private String tempo;

	public Tempo()
	{

	}

	
	public int getId() 
	{
		return id;
	}

	public void setId(int pId) 
	{
		this.id = pId;
	}

	
	public int getIdAtleta() 
	{
		return idAtleta;
	}

	public void setIdAtleta(int pIdAtleta) 
	{
		this.idAtleta = pIdAtleta;
	}

	public int getIdEsercizio() 
	{
		return idEsercizio;
	}

	public void setIdEsercizio(int pIdEsercizio) 
	{
		this.idEsercizio = pIdEsercizio;
	}
	
	public int getRipetizione() 
	{
		return ripetizione;
	}

	public void setRipetizione(int pRipetizione) 
	{
		this.ripetizione = pRipetizione;
	}
	
	public String getTempo() 
	{
		return tempo;
	}

	public void setTempo(String pTempo) 
	{
		this.tempo = pTempo;
	}
	

	
	@Override
	public String toString()
	{
		return id + ") id_atleta: " + idAtleta + ", id_ex: " + idEsercizio + ", rip: " + ripetizione + "tempo: " + tempo; 
	}
}

