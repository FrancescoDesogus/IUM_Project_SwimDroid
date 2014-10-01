package com.example.model;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;

public class Atleta
{
	private int id;
	private String nome;
	private String cognome;
	private int id_gruppo;

	public Atleta()
	{

	}

	public int getId() {
		return id;
	}

	public void setId(int pId) 
	{
		this.id = pId;
	}

	public String getNome() 
	{
		return nome;
	}

	public void setNome(String pNome) 
	{
		this.nome = pNome;
	}

	public String getCognome() 
	{
		return cognome;
	}

	public void setCognome(String pCognome) 
	{
		this.cognome = pCognome;
	}

	public int getId_gruppo() 
	{
		return id_gruppo;
	}

	public void setId_gruppo(int id_gruppo)
	{
		this.id_gruppo = id_gruppo;
	}

	
	@Override
	public String toString()
	{
		return id + ") " + nome + " " + cognome + ", gruppo: " + id_gruppo; 
	}
}
