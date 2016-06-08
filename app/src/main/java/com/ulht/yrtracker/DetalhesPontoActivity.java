package com.ulht.yrtracker;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

public class DetalhesPontoActivity extends MainActivity {

    private Bateria mBateria;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_ponto);

        TextView mData = (TextView) findViewById(R.id.textView_data);
        TextView mLatitude = (TextView) findViewById(R.id.textView_latitude);
        TextView mLongitude = (TextView) findViewById(R.id.textView_longitude);
        TouchImageView mFotografia = (TouchImageView) findViewById(R.id.imageView_fotografia);

        mBateria = new Bateria(this);
        mBateria.registaBateria();

        Cursor mCursor = MapsActivity.acessoBD().getPonto(getIntent().getLongExtra("mDataFoto", 0));
        if(mCursor.moveToFirst()) {
            mData.setText(Utils.formataData(mCursor.getLong(1)));
            mLatitude.setText(mCursor.getString(2));
            mLongitude.setText(mCursor.getString(3));
            mCursor = MapsActivity.acessoBD().getFoto(mCursor.getLong(1));
            if(mCursor.moveToFirst()) {
                Uri linkImagem = Uri.parse(mCursor.getString(1));
                mFotografia.setImageBitmap(Utils.redimensionaImagem(this, linkImagem));
            }
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, HistoricoActivity.class));
        finish();
    }

    @Override
    public void onDestroy() {
        mBateria.encerraBateria();
        super.onDestroy();
    }

}
