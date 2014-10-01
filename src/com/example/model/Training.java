package com.example.model;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;

public class Training{
	
	private int id_allenamento;
	private String nome;
	private String data_allenamento;
	private String ora_inizio;
	private String ora_fine;
	private int id_gruppo;

	public Training(){
		setId_allenamento(-1);
		setData_allenamento("00-00-0000");
		setOra_inizio("00:00");
		setOra_fine("00:00");
		setId_gruppo(-1);
	}

	public int getId_allenamento() {
		return id_allenamento;
	}

	public void setId_allenamento(int id_allenamento) {
		this.id_allenamento = id_allenamento;
	}
	
	public String getNome() {
		return nome;
	}

	public void setNome(String pNome) {
		this.nome = pNome;
	}

	public String getData_allenamento() {
		return data_allenamento;
	}

	public void setData_allenamento(String data_allenamento) {
		this.data_allenamento = data_allenamento;
	}

	public String getOra_inizio() {
		return ora_inizio;
	}

	public void setOra_inizio(String ora_inizio) {
		this.ora_inizio = ora_inizio;
	}

	public String getOra_fine() {
		return ora_fine;
	}

	public void setOra_fine(String ora_fine) {
		this.ora_fine = ora_fine;
	}

	public int getId_gruppo() {
		return id_gruppo;
	}

	public void setId_gruppo(int id_gruppo) {
		this.id_gruppo = id_gruppo;
	}
	

	
	@Override
	public String toString()
	{
		return id_allenamento + ") " + nome + ", data: " + data_allenamento + ", " + ora_inizio + "-" + ora_fine + ", gruppo: " + id_gruppo; 
	}

	@Override
	public boolean equals(Object pTrainingObj)
	{
		Training pTraining = (Training) pTrainingObj;
		
		boolean result = this.id_allenamento == pTraining.id_allenamento;
		
		return result;
	}
}
