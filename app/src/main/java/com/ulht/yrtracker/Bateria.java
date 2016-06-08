package com.ulht.yrtracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

public class Bateria {

    private static final int PERCENTAGEM_ALERTA_BATERIA = 20;
    private static boolean modoEconomia = false;
    private static boolean mFlagEconomia = false;
    private Context mContext;

    Bateria(Context mContext) {
        this.mContext = mContext;
    }

    public void registaBateria() {
        mContext.registerReceiver(mBatInfoReceiver,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(final Context mContext, Intent intent) {
            // TODO Auto-generated method stub
            if(!modoEconomia) {
                int level = intent.getIntExtra("level", 0);
                if (level < PERCENTAGEM_ALERTA_BATERIA) {
                    Utils.getAlertSimOuNao(mContext, mContext.getString(R.string.mensagem_bateria_fraca), new Alertar() {

                        @Override
                        public void metodoPositivo(final DialogInterface dialog, final int id) {
                            modoEconomia(true);
                            Toast.makeText(mContext, R.string.mensagem_confirma_modo_economia,
                                    Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void metodoNegativo(DialogInterface dialog, int id) {
                            // Nao faz nada
                        }
                    });
                }
            }
        }
    };

    public void modoEconomia (boolean modo) {
        if(modo) {
            DefinicoesActivity.guardaEconomiaEmCache(mContext, true);
            mFlagEconomia = true;
            modoEconomia = true;
        } else {
            DefinicoesActivity.guardaEconomiaEmCache(mContext, false);
            mFlagEconomia = true;
            modoEconomia = false;
        }
    }

    public static boolean getFlagEconomia() {
        return mFlagEconomia;
    }

    public static void resetFlagEconomia() {
        mFlagEconomia = false;
    }

    public static boolean isModoEconomia() {
        return modoEconomia;
    }

    public void encerraBateria() {
        mContext.unregisterReceiver(mBatInfoReceiver);
    }

}
