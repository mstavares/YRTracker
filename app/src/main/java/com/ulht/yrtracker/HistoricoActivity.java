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

        mBateria = new Bateria(this);
        mBateria.registaBateria();

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new SampleFragmentPagerAdapter(getSupportFragmentManager(), getIntent().getExtras().getInt("id", -1)));

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