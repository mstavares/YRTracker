package com.ulht.yrtracker;


import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

public class SampleFragmentPagerAdapter extends FragmentPagerAdapter {
    private int numeroDePaginas;
    private ArrayList<String> titulosDasPaginas = new ArrayList<>();
    private ArrayList<Integer> idsDosPercusos = new ArrayList<>();

    public SampleFragmentPagerAdapter(FragmentManager fm, int teste) {
        super(fm);
        Cursor mCursor = MapsActivity.acessoBD().getPercurso(teste);
        numeroDePaginas = 1;
        for(mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
            idsDosPercusos.add(mCursor.getInt(0));
            titulosDasPaginas.add(mCursor.getString(1));
        }
    }

    public SampleFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
        Cursor mCursor = MapsActivity.acessoBD().getTodosOsPercursos();
        numeroDePaginas = mCursor.getCount();
        for(mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
            idsDosPercusos.add(mCursor.getInt(0));
            titulosDasPaginas.add(mCursor.getString(1));
        }
    }

    @Override
    public int getCount() {
        return numeroDePaginas;
    }

    @Override
    public Fragment getItem(int position) {
        return PercursoFragment.newInstance(idsDosPercusos.get(position));
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titulosDasPaginas.get(position);
    }

}