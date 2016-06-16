package com.ulht.yrtracker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

public class DefinicoesActivity extends MainActivity {

    public static final int INTERVALO_ATUALIZACAO_DEFAULT = 2 * 1000;
    private static final int INTERVALO_ATUALIZACAO_ECO = 5 * 1000;
    public static final int DISTANCIA_ATUALIZACAO_DEFAULT = 3;
    private static final int DISTANCIA_ATUALIZACAO_ECO = 5;
    public static final String TEMPO = "tempo";
    public static final String DISTANCIA = "distancia";
    public static final String APRESENTACAO = "apresentacao";
    public static final String BATERIA = "bateria";
    private Bateria mBateria;

    private CheckBox mCheckBoxEconomia, mCheckBoxPagina;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_definicoes);

        mBateria = new Bateria(this);
        mBateria.registaBateria();

        mCheckBoxEconomia = (CheckBox) findViewById(R.id.checkbox_modo_economia);
        mCheckBoxPagina = (CheckBox) findViewById(R.id.checkbox_vista_pagina);

        lerDefinicoesEmCache();
    }

    @Override
    public void onDestroy() {
        mBateria.modoEconomia(mCheckBoxEconomia.isChecked());
        guardaApresentacaoEmCache(mCheckBoxPagina.isChecked());
        mBateria.encerraBateria();
        super.onDestroy();
    }

    public static void guardaEconomiaEmCache(Context mContext, boolean modoEconomia){
        SharedPreferences mSharedPreferences = mContext.getSharedPreferences(Utils.DEFINICOES, MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        if (modoEconomia) {
            mEditor.putInt(DISTANCIA, DISTANCIA_ATUALIZACAO_ECO);
            mEditor.putInt(TEMPO, INTERVALO_ATUALIZACAO_ECO);
        } else {
            mEditor.putInt(DISTANCIA, DISTANCIA_ATUALIZACAO_DEFAULT);
            mEditor.putInt(TEMPO, INTERVALO_ATUALIZACAO_DEFAULT);
        }
        mEditor.putBoolean(BATERIA, modoEconomia);
        mEditor.apply();
    }

    private void guardaApresentacaoEmCache(boolean modoDeApresentacaoPagina){
        SharedPreferences mSharedPreferences = getSharedPreferences(Utils.DEFINICOES, MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putBoolean(APRESENTACAO, modoDeApresentacaoPagina);
        mEditor.apply();
    }

    private void lerDefinicoesEmCache() {
        SharedPreferences mSharedPreferences = getSharedPreferences(Utils.DEFINICOES, MODE_PRIVATE);
        mCheckBoxPagina.setChecked(mSharedPreferences.getBoolean(APRESENTACAO, false));
        mCheckBoxEconomia.setChecked(mSharedPreferences.getBoolean(BATERIA, false));
    }

}
