package com.ulht.yrtracker;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.File;
import java.util.ArrayList;

import at.markushi.ui.CircleButton;

public class MapsActivity extends MainActivity implements OnMapReadyCallback, LocationListener {

    private static final int TIRAR_FOTOGRAFIA = 1;
    private static final int ATIVAR_GPS = 2;
    private static final File PASTA_DE_FOTOS_DA_APLICACAO = new File(Utils.DIRETORIA_RAIZ);
    private static DBAdapter bd;
    private int mIntervaloDeAtualizacao;
    private int mDistanciaDeAtualizacao;
    private ArrayList<Location> pontosDoPercurso;
    private Bateria mBateria;
    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private Marker mMarker;
    private CircleButton mButtonStart, mButtonStop, mButtonFoto;
    private int idPercurso;
    private File mPastaDasFotosDoPercurso;
    private Uri mImageUri;
    private long mDataFoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mButtonStart = (CircleButton) findViewById(R.id.startButton);
        mButtonStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startTracking();
            }
        });

        mButtonStop = (CircleButton) findViewById(R.id.stopButton);
        mButtonStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopTracking();
            }
        });

        mButtonFoto = (CircleButton) findViewById(R.id.fotoButton);
        mButtonFoto.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                tirarFotografia();
            }
        });

        mButtonStop.setVisibility(View.GONE);
        mButtonFoto.setVisibility(View.GONE);
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (!PASTA_DE_FOTOS_DA_APLICACAO.exists()) {
            PASTA_DE_FOTOS_DA_APLICACAO.mkdir();
        }

        lerDefinicoesEmCache();

        mBateria = new Bateria(this);
        mBateria.registaBateria();

        bd = new DBAdapter(this);
        bd.open();

    }

    public static DBAdapter acessoBD() {
        return bd;
    }

    private void changeLocationManagerUpdatesStatus (boolean isStarted) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if(isStarted) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, mIntervaloDeAtualizacao, mDistanciaDeAtualizacao, this);
        } else {
            mLocationManager.removeUpdates(this);
        }
    }

    private void startTracking() {
        if(verificaSeHaGPS()) {
            Utils.getAlertSimOuNao(this, getString(R.string.iniciar_percurso), new Alertar() {

                @Override
                public void metodoPositivo(final DialogInterface dialog, final int id) {
                    inserirTrajetoComDescricao();
                }

                @Override
                public void metodoNegativo(DialogInterface dialog, int id) {
                    inicializaPercurso(getString(R.string.percurso_sem_descricao));
                }
            });
        } else {
            buildAlertMessageNoGps();
        }
    }

    private void criaPastaParaFotos() {
        mPastaDasFotosDoPercurso = new File(Utils.DIRETORIA_RAIZ + File.separator + idPercurso);
        if (!mPastaDasFotosDoPercurso.exists()) {
            if(!mPastaDasFotosDoPercurso.mkdirs()) {
                Toast.makeText(MapsActivity.this, R.string.erro_criar_pasta_fotografias, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void inicializaPercurso(String descricao) {
        bd.inserirPercurso(descricao);
        pontosDoPercurso = new ArrayList<>();
        Cursor mCursor = bd.getTodosOsPercursos();
        mCursor.moveToLast();
        idPercurso = mCursor.getInt(0);
        changeLocationManagerUpdatesStatus(true);
        mButtonStart.setVisibility(View.GONE);
        mButtonStop.setVisibility(View.VISIBLE);
        criaPastaParaFotos();
        Toast.makeText(this, R.string.aguardar_sinal_gps, Toast.LENGTH_LONG).show();
    }

    private void inserirTrajetoComDescricao() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText editDescricao = new EditText(this);
        builder.setMessage(R.string.iniciar_percurso)
                .setView(editDescricao)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        inicializaPercurso(editDescricao.getText().toString());
                    }
                })
                .setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                }).show();
    }

    private void stopTracking() {
        Utils.getAlertSimOuNao(this, getString(R.string.encerrar_percurso), new Alertar() {

            @Override
            public void metodoPositivo(final DialogInterface dialog, final int id) {
                changeLocationManagerUpdatesStatus(false);
                mButtonStop.setVisibility(View.GONE);
                mButtonFoto.setVisibility(View.GONE);
                mButtonStart.setVisibility(View.VISIBLE);
                startActivity(new Intent(MapsActivity.this, MapsActivity.class));
            }

            @Override
            public void metodoNegativo(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng salaDeAula = new LatLng(38.7587452, -9.152223);
        mMarker = mMap.addMarker(new MarkerOptions()
                .position(salaDeAula)
                .title(getString(R.string.voce_esta_aqui)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(salaDeAula));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17));
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }

    @Override
    public void onLocationChanged(Location mLocation) {
        LatLng localizacao = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLng(localizacao));
        mMarker.setPosition(localizacao);
        mMarker.setTitle(String.format("Lat: %f, Lng: %f", mLocation.getLatitude(), mLocation.getLongitude()));
        pontosDoPercurso.add(mLocation);
        mMap.addPolyline(Utils.desenhaOPercurso(pontosDoPercurso));
        bd.inserirPonto(idPercurso, mLocation);
        mButtonFoto.setVisibility(View.VISIBLE);
        if(Bateria.getFlagEconomia()) {
            lerDefinicoesEmCache();
            changeLocationManagerUpdatesStatus(true);
            Bateria.resetFlagEconomia();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, R.string.gps_ativado, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, R.string.gps_desativado, Toast.LENGTH_LONG).show();
    }

    public void tirarFotografia() {
        mDataFoto = Utils.getDataAtual();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photo = new File(mPastaDasFotosDoPercurso + File.separator + Utils.formataData(mDataFoto));
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
        mImageUri = Uri.fromFile(photo);
        startActivityForResult(intent, TIRAR_FOTOGRAFIA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TIRAR_FOTOGRAFIA:
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            // ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            // public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }

                        Location mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        mLocation.setTime(mDataFoto);
                        pontosDoPercurso.add(mLocation);

                        bd.inserirFoto(idPercurso, mLocation, mImageUri.toString());

                        Utils.criaMarkerPersonalizado(this, mMap, mLocation, mImageUri);

                        Toast.makeText(this, R.string.fotografia_guardada_com_sucesso, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(this, R.string.erro_ao_guardar_a_fotografia, Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }

                }
                break;
            case ATIVAR_GPS:
                startTracking();
        }
    }

    private boolean verificaSeHaGPS() {
        final LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return (manager.isProviderEnabled(LocationManager.GPS_PROVIDER));
    }

    private void buildAlertMessageNoGps() {
        Utils.getAlertSimOuNao(this, getString(R.string.gps_desativo), new Alertar() {

            @Override
            public void metodoPositivo(DialogInterface dialog, int id) {
                Intent mIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(mIntent, ATIVAR_GPS);
            }

            @Override
            public void metodoNegativo(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
    }

    private void lerDefinicoesEmCache() {
        SharedPreferences mSharedPreferences = getSharedPreferences(Utils.DEFINICOES, MODE_PRIVATE);
        if (mSharedPreferences.getInt(DefinicoesActivity.DISTANCIA, 0) == 0) {
            DefinicoesActivity.guardaEconomiaEmCache(this, false);
        }
        mDistanciaDeAtualizacao = mSharedPreferences.getInt(DefinicoesActivity.DISTANCIA, DefinicoesActivity.DISTANCIA_ATUALIZACAO_DEFAULT);
        mIntervaloDeAtualizacao = mSharedPreferences.getInt(DefinicoesActivity.TEMPO, DefinicoesActivity.INTERVALO_ATUALIZACAO_DEFAULT);
    }

    @Override
    public void onBackPressed() {
        Utils.getAlertSimOuNao(this, getString(R.string.encerrar_aplicacao), new Alertar() {

            @Override
            public void metodoPositivo(final DialogInterface dialog, final int id) {
                MapsActivity.this.finish();
            }

            @Override
            public void metodoNegativo(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
    }

    @Override
    public void onDestroy() {
        mBateria.encerraBateria();
        super.onDestroy();
    }

}
