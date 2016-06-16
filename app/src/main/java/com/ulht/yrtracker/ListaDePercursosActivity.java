package com.ulht.yrtracker;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class ListaDePercursosActivity extends MainActivity {

    private ArrayList<Integer> idsDosPercusos = new ArrayList<>();
    private ArrayList<String> descricoesDosPercursos = new ArrayList<>();
    private HashMap<Integer, Integer> posicaoToID = new HashMap<>();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_de_percursos);

        ListView mListView = (ListView) findViewById(R.id.lista);

        Cursor mCursor = MapsActivity.acessoBD().getTodosOsPercursos();
        for(mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
            StringBuilder mStringBuilder = new StringBuilder();
            idsDosPercusos.add(mCursor.getInt(0));
            Cursor mCursor2 = MapsActivity.acessoBD().getDadosDoPercurso(mCursor.getInt(0));
            mCursor2.moveToFirst();
            mStringBuilder.append(mCursor.getString(1));
            mStringBuilder.append(Utils.LINE_FEED);
            mStringBuilder.append(Utils.formataData(mCursor2.getLong(1)));
            descricoesDosPercursos.add(mStringBuilder.toString());
        }

        for(int i = 0; i < idsDosPercusos.size(); i++) {
            posicaoToID.put(i, idsDosPercusos.get(i));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.elemento_da_lista,
                R.id.descricao_do_percurso,
                descricoesDosPercursos
        );
        mListView.setAdapter(adapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle mBundle = new Bundle();
                Intent mIntent = new Intent(ListaDePercursosActivity.this, HistoricoActivity.class);
                mBundle.putInt("id", posicaoToID.get(position));
                mIntent.putExtras(mBundle);
                startActivity(mIntent);
            }
        });


        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                Vibrator mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                mVibrator.vibrate(100);
                Utils.getAlertSimOuNao(ListaDePercursosActivity.this, getString(R.string.eliminar_percurso), new Alertar() {

                    @Override
                    public void metodoPositivo(DialogInterface dialog, int id) {
                        if(MapsActivity.acessoBD().eliminaPercurso(posicaoToID.get(position))) {
                            Toast.makeText(ListaDePercursosActivity.this, R.string.percurso_eliminado, Toast.LENGTH_LONG).show();
                            startActivity(new Intent(ListaDePercursosActivity.this, MapsActivity.class));
                            finish();
                        } else {
                            Toast.makeText(ListaDePercursosActivity.this, R.string.percurso_nao_eliminado, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void metodoNegativo(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                return true;
            }
        });

    }
}
