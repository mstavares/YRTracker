package com.ulht.yrtracker;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PercursoFragment extends Fragment {

    private static final String ARG_ID_PERCURSO = "ARG_ID_PERCURSO";
    private GoogleMap mMap;
    private MapView mMapView;
    private View view;

    private static final int[] IDS_TABELA = {R.id.textView_distancia, R.id.textView_velocidade_max,
            R.id.textView_velocidade_med, R.id.textView_data, R.id.textView_duracao};

    private int idPercurso;

    public static PercursoFragment newInstance(int idPercurso) {
        Bundle args = new Bundle();
        args.putInt(ARG_ID_PERCURSO, idPercurso);
        PercursoFragment fragment = new PercursoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        idPercurso = getArguments().getInt(ARG_ID_PERCURSO);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.trajeto_fragment, container, false);
        mMapView = (MapView) view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();// needed to get the map to display immediately
        mMap = mMapView.getMap();

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Utils.getAlertSimOuNao(getContext(), getString(R.string.eliminar_percurso), new Alertar() {
                    @Override
                    public void metodoPositivo(DialogInterface dialog, int id) {
                        if(MapsActivity.acessoBD().eliminaPercurso(idPercurso)) {
                            Toast.makeText(getContext(), R.string.percurso_eliminado, Toast.LENGTH_LONG).show();
                            startActivity(new Intent(getContext(), MapsActivity.class));
                            ((Activity) getContext()).finish();
                        } else {
                            Toast.makeText(getContext(), R.string.percurso_nao_eliminado, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void metodoNegativo(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                return false;
            }
        });

        final ArrayList<Location> pontosDoTrajeto = new ArrayList<>();

        lerDadosDaBD(pontosDoTrajeto);

        return view;
    }

    private void lerDadosDaBD (final ArrayList<Location> pontosDoTrajeto) {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

            ProgressDialog dialog;
            HashMap<Integer, String> posicoesComFoto = new HashMap<>();

            @Override
            protected void onPreExecute() {
                dialog = new ProgressDialog(getContext());
                dialog.setMessage(getContext().getResources().getString(R.string.a_carregar_percurso));
                dialog.setIndeterminate(true);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
            }

            @Override
            protected Void doInBackground(Void... params) {

                Cursor mCursor = MapsActivity.acessoBD().getDadosDoPercurso(idPercurso);
                for(mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
                    Location mLocation = new Location("");
                    mLocation.setTime(mCursor.getLong(1));
                    mLocation.setLatitude(mCursor.getFloat(2));
                    mLocation.setLongitude(mCursor.getFloat(3));
                    mLocation.setSpeed((mCursor.getFloat(4)));
                    pontosDoTrajeto.add(mLocation);

                    Cursor mCursor2 = MapsActivity.acessoBD().getFoto(mLocation.getTime());
                    if (mCursor2.moveToFirst()) {
                        posicoesComFoto.put(pontosDoTrajeto.indexOf(mLocation), mCursor2.getString(1));
                    }
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void params) {

                for(Map.Entry<Integer, String> entry : posicoesComFoto.entrySet()) {
                    Uri link = Uri.parse(entry.getValue());
                    Utils.criaMarkerPersonalizado(getContext(), mMap, pontosDoTrajeto.get(entry.getKey()), link);
                }

                int mUtitimoElemento = pontosDoTrajeto.size() - 1;

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(pontosDoTrajeto.get(0).getLatitude(),
                                pontosDoTrajeto.get(0).getLongitude())).zoom(17).build();

                mMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(cameraPosition));

                mMap.addMarker(Utils.criaMarker(pontosDoTrajeto.get(0), getString(R.string.partida)));
                mMap.addMarker(Utils.criaMarker(pontosDoTrajeto.get(mUtitimoElemento), getString(R.string.chegada)));
                mMap.addPolyline(Utils.desenhaOPercurso(pontosDoTrajeto));

                TextView mTextViewDistancia = (TextView) view.findViewById(IDS_TABELA[0]);
                TextView mTextViewVelocidadeMax = (TextView) view.findViewById(IDS_TABELA[1]);
                TextView mTextViewVelocidadeMed = (TextView) view.findViewById(IDS_TABELA[2]);
                TextView mTextViewDataDeInicio = (TextView) view.findViewById(IDS_TABELA[3]);
                TextView mTextViewDuracaoDoTrajeto = (TextView) view.findViewById(IDS_TABELA[4]);


                mTextViewDistancia.setText(Utils.formataDistancia(
                        Utils.calculaDistanciaPercorrida(pontosDoTrajeto)));

                mTextViewVelocidadeMax.setText(Utils.formataVelocidadeKmh(
                        Utils.calculaVelocidadeMaxima(pontosDoTrajeto)));

                mTextViewVelocidadeMed.setText(Utils.formataVelocidadeKmh(
                        Utils.calculaVelocidadeMedia(pontosDoTrajeto)));

                mTextViewDataDeInicio.setText(Utils.calculaInicioDoPercurso(pontosDoTrajeto));
                mTextViewDuracaoDoTrajeto.setText(Utils.calculaDuracaoDoPercurso(pontosDoTrajeto));


                dialog.dismiss();
            }
        };
        task.execute();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mMapView.onDestroy();
    }

}