package com.ulht.yrtracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.astuetz.PagerSlidingTabStrip;

public class HistoricoActivity extends MainActivity {

    private Bateria mBateria;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico);

        int id = getIntent().getExtras().getInt("id");

        mBateria = new Bateria(this);
        mBateria.registaBateria();

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        if(id != -1) {
            viewPager.setAdapter(new SampleFragmentPagerAdapter(getSupportFragmentManager(), id));
        } else {
            viewPager.setAdapter(new SampleFragmentPagerAdapter(getSupportFragmentManager()));
        }

        PagerSlidingTabStrip tabsStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabsStrip.setViewPager(viewPager);

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

}