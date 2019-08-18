package unipr.luc_af.services;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import unipr.luc_af.classes.Athlete;
import unipr.luc_af.database.DatabaseHelper;
import unipr.luc_af.database.AppTables;
import unipr.luc_af.database.interfaces.DatabaseInsert;
import unipr.luc_af.database.interfaces.DatabaseResult;

public class Database {
    private static Database instance = null;
    private DatabaseHelper dbHelper;
    private Database () { }

    public static Database getInstance() {
        if(instance == null)
            instance = new Database();
        return instance;
    }

    /**
     * Questo metodo deve essere usato prima di qualsisi
     * altra chiamata a metodi di questa classe
     * @param context la Activity di riferimento
     */
    public void setContext(Context context) {
        dbHelper = new DatabaseHelper(context);
        dbHelper.getWritableDatabase();
    }

    public long addActivity(String name) throws SQLException{
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        //Non c'e necessita di una transaction siccome stiamo eseguendo solamente una query di INSERT INTO nel caso
        //fallisca viene generato un errore del tipo SQLException
        ContentValues values = new ContentValues();
        values.put(AppTables.ACTIVITY_TABLE_COL_0.getName(), name);
        return db.insertOrThrow(AppTables.ACTIVITY_TABLE.getName(), null, values);
    }

    public long addActivityType(String name, long activity) throws SQLException{
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(AppTables.ACTIVITY_TYPE_TABLE_COL_0.getName(), name);
        values.put(AppTables.ACTIVITY_TYPE_TABLE_COL_1.getName(), activity);
        return db.insertOrThrow(AppTables.ACTIVITY_TYPE_TABLE.getName(), null, values);
    }

    public void getActivityNames(DatabaseResult result){
        //Usiamo la classe helper(wrapper) AsyncTask per eseguire la query del database su un trhead separato ma
        //aggiorniamo la UI nel thread corretto quindi il metodo OnResult della classe DatabaseResult e' eseguito nell' UI thread
        AsyncTask<Void,Void,Cursor> task = new AsyncTask<Void, Void, Cursor>() {
            @Override
            protected Cursor doInBackground(Void... voids) {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                Cursor queryCursor = db.query(AppTables.ACTIVITY_TABLE.getName(),
                        new String[]{"name"},
                        null,
                        null,
                        null,
                        null,
                        null);
                return queryCursor;
            }

            @Override
            protected void onPostExecute(Cursor cursor) { result.OnResult(cursor); }
        };
        task.execute();
    }

    public void getActivityIdFromName(String name,DatabaseResult result){
        AsyncTask<Void,Void,Cursor> task = new AsyncTask<Void, Void, Cursor>() {
            @Override
            protected Cursor doInBackground(Void... voids) {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                Cursor queryCursor = db.query(AppTables.ACTIVITY_TABLE.getName(),
                        new String[]{"id"},
                        "name = \"" + name + "\"",
                        null,
                        null,
                        null,
                        null,
                        "1");
                return queryCursor;
            }

            @Override
            protected void onPostExecute(Cursor cursor) {
                result.OnResult(cursor);
            }
        };
        task.execute();
    }

    public void addAthelete(Athlete athlete, DatabaseInsert result){
        AsyncTask<Void,Void,Long> task = new AsyncTask<Void, Void, Long>() {
            @Override
            protected Long doInBackground(Void... voids) {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(AppTables.ATHLETE_TABLE_COL_0.getName(), athlete.name);
                values.put(AppTables.ATHLETE_TABLE_COL_1.getName(), athlete.surname);
                values.put(AppTables.ATHLETE_TABLE_COL_2.getName(), athlete.activityReference);
                return db.insert(AppTables.ATHLETE_TABLE.getName(),null,values);
            }

            @Override
            protected void onPostExecute(Long id) {
                result.OnInsert(id);
            }
        };
        task.execute();
    }
}
