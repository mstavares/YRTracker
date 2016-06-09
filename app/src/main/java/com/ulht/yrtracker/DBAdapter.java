package com.ulht.yrtracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

public class DBAdapter {

    private static final String DATABASE_NAME = "SE_PROJECT_2016";
    private static final int DATABASE_VERSION = 1;

    private static final String TABELA_PERCURSO =
            "CREATE TABLE percurso ("
                    + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "descricao VARCHAR(128)"
                    + ")";

    private static final String TABELA_PONTO =
            "CREATE TABLE ponto ("
                    + "_id_percurso INTEGER, "
                    + "data INTEGER PRIMARY KEY, "
                    + "latitude REAL, "
                    + "longitude REAL, "
                    + "velocidade REAL, "
                    + "FOREIGN KEY(_id_percurso) REFERENCES percurso(_id) ON DELETE CASCADE"
                    + ")";

    private static final String TABELA_FOTO =
            "CREATE TABLE foto ("
                    + "data INTEGER PRIMARY KEY, "
                    + "caminho VARCHAR(256), "
                    + "descricao VARCHAR(128),"
                    + "FOREIGN KEY(data) REFERENCES ponto(data) ON DELETE CASCADE"
                    + ")";

    private final Context context;

    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    public DBAdapter(Context ctx)
    {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            db.execSQL(TABELA_PERCURSO);
            db.execSQL(TABELA_PONTO);
            db.execSQL(TABELA_FOTO);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            db.execSQL("DROP TABLE IF EXISTS percurso");
            db.execSQL("DROP TABLE IF EXISTS ponto");
            db.execSQL("DROP TABLE IF EXISTS foto");
            onCreate(db);
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            super.onOpen(db);
            if (!db.isReadOnly()) {
                // Enable foreign key constraints
                db.execSQL("PRAGMA foreign_keys=ON;");
            }
        }

    }

    public DBAdapter open() throws SQLException
    {
        db = DBHelper.getWritableDatabase();
        return this;
    }

    public void close()
    {
        DBHelper.close();
    }

    public long inserirPercurso(String descricao)
    {
        ContentValues registo = new ContentValues();
        registo.put("descricao", descricao);
        return db.insert("percurso", null, registo);
    }

    public long inserirPonto(int percurso, Location mLocation)
    {
        ContentValues registo = new ContentValues();
        registo.put("_id_percurso", percurso);
        registo.put("data", mLocation.getTime());
        registo.put("latitude", mLocation.getLatitude());
        registo.put("longitude", mLocation.getLongitude());
        registo.put("velocidade", mLocation.getSpeed());
        return db.insert("ponto", null, registo);
    }

    public long inserirFoto(int percurso, Location mLocation, String caminho)
    {
        inserirPonto(percurso, mLocation);
        ContentValues registo = new ContentValues();
        registo.put("data", mLocation.getTime());
        registo.put("caminho", caminho);
        return db.insert("foto", null, registo);
    }

    public Cursor getPercursos(Integer idPercurso) {
        if(idPercurso == -1) {
            return getTodosOsPercursos();
        } else {
            return getPercurso(idPercurso);
        }
    }

    public Cursor getDadosDoPercurso(Integer idPercurso) throws SQLException
    {
        return db.query(
                "ponto",
                null,
                " _id_percurso=?",
                new String[] {idPercurso.toString()},
                null,
                null,
                "data",
                null);
    }

    public Cursor getTodosOsPercursos() throws SQLException {
        return db.query(true,
                "percurso",
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    public Cursor getPercurso(Integer idPercurso) throws SQLException
    {
        return db.query(
                "percurso",
                null,
                " _id=?",
                new String[] {idPercurso.toString()},
                null,
                null,
                null,
                null);
    }

    public Cursor getFoto(Long data) throws SQLException
    {
        return db.query(
                "foto",
                null,
                " data=?",
                new String[] {data.toString()},
                null,
                null,
                null,
                null);
    }

    public Cursor getPonto(Long data) throws SQLException
    {
        return db.query(
                "ponto",
                null,
                " data=?",
                new String[] {data.toString()},
                null,
                null,
                null,
                null);
    }

    public boolean eliminaPercurso(Integer idPercurso) {
        return db.delete("percurso", " _id = ?"  , new String[] {idPercurso.toString()}) > 0;
    }

}