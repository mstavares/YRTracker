package com.ulht.yrtracker;

import android.content.DialogInterface;

public interface Alertar {

    void metodoPositivo(DialogInterface dialog, int id);
    void metodoNegativo(DialogInterface dialog, int id);

}
