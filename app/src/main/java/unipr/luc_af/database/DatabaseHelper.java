package unipr.luc_af.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import unipr.luc_af.chronotracker.helpers.FileIO;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "chronoDB.db";
    private Context appContext;

    public DatabaseHelper(Context context) {
        //Chiamiamo il construttore della classe base
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        appContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //Apriamo il file che contiene la query sql di creazione del database ed eseguiamo tutti gli statements che contiene
        try {
            String sqlCreationQuery = FileIO.getInstance().ReadFileFromAssets("db.sql", appContext);
            String[] statements = sqlCreationQuery.split(";");
            for (String statement : statements) {
                sqLiteDatabase.execSQL(statement);
            }
            String sqlInitQuery = FileIO.getInstance().ReadFileFromAssets("init.sql", appContext);
            statements = sqlInitQuery.split(";");
            for (String statement : statements) {
                sqLiteDatabase.execSQL(statement);
            }
        } catch (Exception err) {
            System.out.println(err.getLocalizedMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //TODO nelle succesive versioni scrivere il codice per l'upgrade del database
    }
}
