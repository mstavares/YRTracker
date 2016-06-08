package com.ulht.yrtracker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class DefinicoesActivity extends MainActivity {

    public static final int INTERVALO_ATUALIZACAO_DEFAULT = 2 * 1000;
    private static final int INTERVALO_ATUALIZACAO_ECO = 5 * 1000;
    public static final int DISTANCIA_ATUALIZACAO_DEFAULT = 3;
    private static final int DISTANCIA_ATUALIZACAO_ECO = 5;
    private static final String[] OPCOES = {"Modo Economia"};
    public static final String TEMPO = "tempo";
    public static final String DISTANCIA = "distancia";
    private Bateria mBateria;
    private boolean modoEconomia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_definicoes);



        mBateria = new Bateria(this);
        mBateria.registaBateria();

        modoEconomia = Bateria.isModoEconomia();

    }

    @Override
    public void onBackPressed() {

        startActivity(new Intent(this, MapsActivity.class));
        finish();
    }

    @Override
    public void onDestroy() {
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
        mEditor.commit();
    }

}
