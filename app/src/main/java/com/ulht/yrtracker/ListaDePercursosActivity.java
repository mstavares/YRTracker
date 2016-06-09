package com.ulht.yrtracker;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
            idsDosPercusos.add(mCursor.getInt(0));
            descricoesDosPercursos.add(mCursor.getString(1));
        }

        for(int i = 0; i < idsDosPercusos.size(); i++) {
            posicaoToID.put(i, idsDosPercusos.get(i));
        }


        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                //R.layout.item,
                //R.id.item,
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

    }
}
