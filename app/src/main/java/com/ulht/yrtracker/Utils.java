package com.ulht.yrtracker;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

public class Utils {

    public static final String PASTA_RAIZ_FOTOS = "YRTracker";
    public static final String DEFINICOES = "Definicoes";
    public static final String DIRETORIA_RAIZ = Environment.getExternalStorageDirectory() + File.separator + PASTA_RAIZ_FOTOS;
    public static final int GL_MARKER_TEXTURE_SIZE = 333;
    private static final int COR_DO_PERCURSO = Color.RED;
    private static final int LARGURA_DA_LINHA = 7;
    private static final int TEMPO_CARREGAR = 1000;

    private Utils() {
    }

    /**
     * Conversao de metros por segundo para kilometros por hora.
     * 60 * 60 = 3600 / 1000 = 3.6
     */

    public static double metrosPorSegundoToKilometrosPorhora(double mps) {
        return mps * 3.6;
    }

    /**
     * Formata a duas casas decimais.
     */

    public static String formataDuasCasasDecimais(double valor) {
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(valor);
    }

    /**
     * Recebe a velocidade em mps, transforma para kmph e formata
     * a duas casas decimais.
     */

    public static String formataVelocidadeKmh(double velocidade) {
        return formataDuasCasasDecimais(metrosPorSegundoToKilometrosPorhora(velocidade))
                .toString() + " Km/h";
    }

    /**
     * Recebe a distancia e formata a duas casas decimais.
     */

    public static String formataDistancia(double distancia) {
        return formataDuasCasasDecimais(distancia).toString() + " m";
    }

    /**
     * Transforma a data de long para string legivel.
     */

    public static String formataData(long data) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(data);
    }

    /**
     * Transforma a data de long para uma string que so tem
     * horas, minutos e segundos.
     */

    public static String formataDuracao(long data) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(data);
    }

    /**
     * Calcula a velocidade media.
     */

    public static double calculaVelocidadeMedia(ArrayList<Location> pontosDoTrajeto) {
        double velocidadeTotal = 0.0;
        for (int i = 0; i < pontosDoTrajeto.size(); i++) {
            velocidadeTotal += pontosDoTrajeto.get(i).getSpeed();
        }
        return velocidadeTotal / pontosDoTrajeto.size();
    }

    /**
     * Calcula a velocidade maxima.
     */

    public static double calculaVelocidadeMaxima(ArrayList<Location> pontosDoTrajeto) {
        ArrayList<Float> velocidades = new ArrayList<>();
        for (int i = 0; i < pontosDoTrajeto.size(); i++) {
            velocidades.add(pontosDoTrajeto.get(i).getSpeed());
        }
        Collections.sort(velocidades);
        return velocidades.get(velocidades.size() - 1);
    }

    /**
     * Calcula a distancia percorrida.
     */

    public static double calculaDistanciaPercorrida(ArrayList<Location> pontosDoTrajeto) {
        double distanciaPercorrida = 0.0;
        for (int i = 0; i < pontosDoTrajeto.size() - 1; i++) {
            distanciaPercorrida += pontosDoTrajeto.get(i).distanceTo(pontosDoTrajeto.get(i + 1));
        }
        return distanciaPercorrida;
    }

    /**
     * Calcula a duração do percurso.
     */

    public static String calculaDuracaoDoPercurso(ArrayList<Location> pontosDoTrajeto) {
        long tempo = pontosDoTrajeto.get(pontosDoTrajeto.size() - 1).getTime()
                - pontosDoTrajeto.get(0).getTime();
        return formataDuracao(tempo);
    }

    /**
     * Devolve a data atual em long
     */

    public static long getDataAtual() {
        return Calendar.getInstance().getTime().getTime();
    }

    /**
     * Calcula a data de inicio do percurso.
     * O ArrayList já vem ordenado da base de dados (o query ordena por data)
     */

    public static String calculaInicioDoPercurso(ArrayList<Location> pontosDoTrajeto) {
        return formataData(pontosDoTrajeto.get(0).getTime());
    }

    /**
     * Desenha a linha do percurso sobre o mapa.
     */

    public static PolylineOptions desenhaOPercurso (ArrayList<Location> pontosDoTrajeto) {
        PolylineOptions options = new PolylineOptions().width(LARGURA_DA_LINHA).color(COR_DO_PERCURSO).geodesic(true);
        for (Location mLocation : pontosDoTrajeto) {
            options.add(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()));
        }
        return options;
    }

    /**
     * Cria um AlertDialog generico de resposta sim ou nao
     */

    public static void getAlertSimOuNao (Context mContext, String mensagem, final Alertar target) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(mensagem)
                .setPositiveButton(R.string.sim, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        target.metodoPositivo(dialog, id);
                    }
                })
                .setNegativeButton(R.string.nao, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        target.metodoNegativo(dialog, id);
                    }
                }).show();
    }

    /**
     * Redimenciona uma imagem de acordo com o aspeto do ecrã.
     */

    public static Bitmap redimensionaImagem (Context mContext, Uri link) {
        WindowManager wm = (WindowManager) mContext.getSystemService(mContext.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return redimensionaImagem(mContext, size.x, size.y, link);
    }

    /**
     * Redimenciona uma imagem de acordo com os parâmetros, largura e altura.
     */

    public static Bitmap redimensionaImagem (Context mContext, int largura, int altura, Uri link) {
        Bitmap imagemRedimencionada = null;
        try {
            mContext.getContentResolver().notifyChange(link, null);
            ContentResolver cr = mContext.getContentResolver();
            Bitmap bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, link);
            imagemRedimencionada = Bitmap.createScaledBitmap(bitmap, largura, altura, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imagemRedimencionada;
    }

    /**
     * Devolve a imagem do Bitmap do marker.
     */

    private static Bitmap aplicaImagemMarker (Context context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    /**
     * Cria um marker personalizado através de um Bitmap.
     */

    public static void criaMarkerPersonalizado (final Context mContext, final GoogleMap mMap, final Location mLocation, final Uri link) {
        AsyncTask<Void, Void, Bitmap> task = new AsyncTask<Void, Void, Bitmap>() {

            ProgressDialog dialog;

            @Override
            protected void onPreExecute() {
                dialog = new ProgressDialog(mContext);
                dialog.setMessage(mContext.getResources().getString(R.string.a_carregar_percurso));
                dialog.setIndeterminate(true);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
            }

            @Override
            protected Bitmap doInBackground(Void... params) {
                return redimensionaImagem(mContext, Utils.GL_MARKER_TEXTURE_SIZE, Utils.GL_MARKER_TEXTURE_SIZE, link);
            }

            @Override
            protected void onPostExecute(Bitmap redimensionada) {
                View marker = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.marker, null);
                ImageView imagem = (ImageView) marker.findViewById(R.id.foto);
                imagem.setImageBitmap(redimensionada);

                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()))
                        .title(Utils.formataData(mLocation.getTime()))
                        .icon(BitmapDescriptorFactory.fromBitmap(Utils.aplicaImagemMarker(mContext, marker))));

                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        Intent intent = new Intent(mContext, DetalhesPontoActivity.class);
                        intent.putExtra("mDataFoto", mLocation.getTime());
                        mContext.startActivity(intent);
                    }
                });
                dialog.dismiss();
            }
        };
        task.execute();
    }

    /**
     * Cria um marker genérico.
     */

    public static MarkerOptions criaMarker (Location mLocation, String mensagem) {
        MarkerOptions mMarkerOptions = new MarkerOptions()
                .position(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()))
                .title(mensagem);

        mMarkerOptions.icon(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_ROSE));

        return mMarkerOptions;
    }

    /**
     * Loading "a carregar dados"
     */

    public static void aCarregar (final Context mContext) {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

            ProgressDialog dialog;

            @Override
            protected void onPreExecute() {
                dialog = new ProgressDialog(mContext);
                dialog.setMessage(mContext.getResources().getString(R.string.a_carregar_percurso));
                dialog.setIndeterminate(true);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Thread.sleep(TEMPO_CARREGAR);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void params) {
                dialog.dismiss();
            }
        };
        task.execute();
    }
}
