package com.ulht.yrtracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_historico:
                Cursor mCursor = MapsActivity.acessoBD().getTodosOsPercursos();
                if(mCursor.moveToFirst()) {
                    SharedPreferences mSharedPreferences = getSharedPreferences(Utils.DEFINICOES, MODE_PRIVATE);
                    if(mSharedPreferences.getBoolean(DefinicoesActivity.APRESENTACAO, false)) {
                        startActivity(new Intent(this, ListaDePercursosActivity.class));
                    } else {
                        startActivity(new Intent(this, HistoricoActivity.class).putExtras(new Bundle()));
                    }
                    finish();
                } else {
                    Toast.makeText(this, R.string.nao_ha_historico, Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.action_definicoes:
                startActivity(new Intent(this, DefinicoesActivity.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MapsActivity.class));
        finish();
    }
}
