package unipr.luc_af.services;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import java.util.Calendar;
import java.util.Date;

import unipr.luc_af.classes.ActivitySport;
import unipr.luc_af.classes.Athlete;
import unipr.luc_af.classes.NoLeakAsyncTask;
import unipr.luc_af.database.DatabaseHelper;
import unipr.luc_af.database.AppTables;
import unipr.luc_af.database.interfaces.DatabaseInsert;
import unipr.luc_af.database.interfaces.DatabaseResult;

public class Database {
    private static Database instance = null;
    private Activity mContext;
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
    public void setContext(Activity context) {
        mContext = context;
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

    public void getActivitiesTypesOfActivity(ActivitySport activity, DatabaseResult result){
        NoLeakAsyncTask<Void,Void,Cursor> task = new NoLeakAsyncTask<>(
            mContext,
            (Void... voids)-> {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                Utils utils = Utils.getInstance();
                Cursor queryCursor = db.query(AppTables.ACTIVITY_TYPE_TABLE.getName(),
                        new String[]{
                                AppTables.TABLE_ID_COL.getName(),
                                AppTables.ACTIVITY_TYPE_TABLE_COL_0.getName(),
                                AppTables.ACTIVITY_TYPE_TABLE_COL_1.getName()},
                        utils.concatString(" ",
                                AppTables.ACTIVITY_TYPE_TABLE_COL_1.getName(),
                                "=",
                                activity.id.toString()),
                        null,
                        null,
                        null,
                        null);
                return queryCursor;
            },
            (cursor)->{ result.OnResult(cursor);}
        );
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void getActivities(DatabaseResult result){
        NoLeakAsyncTask<Void,Void,Cursor> task = new NoLeakAsyncTask<>(
            mContext,
            (Void... voids)-> {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                Cursor queryCursor = db.query(AppTables.ACTIVITY_TABLE.getName(),
                        new String[]{
                                AppTables.TABLE_ID_COL.getName(),
                                AppTables.ACTIVITY_TABLE_COL_0.getName()},
                        null,
                        null,
                        null,
                        null,
                        null);
                return queryCursor;
            },
            (Cursor cursor) -> { result.OnResult(cursor); }
        );
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void getActivityNames(DatabaseResult result){
        //Usiamo la classe helper(wrapper) NoLeakAsyncTask che estende la classe AsyncTask per eseguire la query del database su un trhead separato ma
        //aggiorniamo la UI nel thread corretto quindi il metodo OnResult della classe DatabaseResult e' eseguito nell' UI thread
        //La Classe NoLeakAsyncTask assicura anche che non ci siano memory leak nel caso la AsyncTask viva piu della activity che
        //l'ha iniziata utilizzando un WeakReference all'activity.
        NoLeakAsyncTask<Void,Void,Cursor> task = new NoLeakAsyncTask<>(
                mContext,
                (Void... in) ->{
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    Cursor queryCursor = db.query(AppTables.ACTIVITY_TABLE.getName(),
                            new String[]{AppTables.ACTIVITY_TABLE_COL_0.getName()},
                            null,
                            null,
                            null,
                            null,
                            null);
                    return queryCursor;
                },
                (cursor) -> { result.OnResult(cursor); }
                );
        task.executeOnExecutor(NoLeakAsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void getActivityIdFromName(String name, DatabaseResult result){
        NoLeakAsyncTask<Void,Void,Cursor> task = new NoLeakAsyncTask<>(
            mContext,
            (Void... in) -> {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                Utils utils = Utils.getInstance();
                Cursor queryCursor = db.query(AppTables.ACTIVITY_TABLE.getName(),
                        new String[]{AppTables.TABLE_ID_COL.getName()},
                        utils.concatString(" ",
                                AppTables.ACTIVITY_TABLE_COL_0.getName(),
                                "=",
                                "\"" + name + "\""),
                        null,
                        null,
                        null,
                        null,
                        "1");
                return queryCursor;
            },
            (cursor) -> { result.OnResult(cursor); }
        );
        task.executeOnExecutor(NoLeakAsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void addAthlete(Athlete athlete, DatabaseInsert result){
        NoLeakAsyncTask<Void,Void,Long> task = new NoLeakAsyncTask<>(
            mContext,
            (Void... in)-> {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                if(athlete.id == -1) {
                    values.put(AppTables.ATHLETE_TABLE_COL_0.getName(), athlete.name);
                    values.put(AppTables.ATHLETE_TABLE_COL_1.getName(), athlete.surname);
                    values.put(AppTables.ATHLETE_TABLE_COL_2.getName(), athlete.activityReference);
                }else{

                    values.put(AppTables.TABLE_ID_COL.getName(), athlete.id);
                    values.put(AppTables.ATHLETE_TABLE_COL_0.getName(), athlete.name);
                    values.put(AppTables.ATHLETE_TABLE_COL_1.getName(), athlete.surname);
                    values.put(AppTables.ATHLETE_TABLE_COL_2.getName(), athlete.activityReference);
                }
                return db.insert(AppTables.ATHLETE_TABLE.getName(), null, values);
            },
            (id) ->{ result.OnInsert(id); }
        );
        task.execute();
    }

    public void getAthletes(DatabaseResult result){
        NoLeakAsyncTask<Void,Void,Cursor> task = new NoLeakAsyncTask<>(
            mContext,
            (Void... in) ->{
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                Cursor queryCursor = db.query(AppTables.ATHLETE_TABLE.getName(),
                        new String[]{
                                AppTables.TABLE_ID_COL.getName(),
                                AppTables.ATHLETE_TABLE_COL_0.getName(),
                                AppTables.ATHLETE_TABLE_COL_1.getName(),
                                AppTables.ATHLETE_TABLE_COL_2.getName()},
                        null,
                        null,
                        null,
                        null,
                        null);
                return queryCursor;
                },
            (cursor) -> {result.OnResult(cursor);});
        task.executeOnExecutor(NoLeakAsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void getActivitiesOfDay(Calendar date, Athlete athlete, DatabaseResult result){
        NoLeakAsyncTask<Void,Void,Cursor> task = new NoLeakAsyncTask<>(mContext,
            (Void... in) ->{
                Calendar endDate = (Calendar) date.clone();
                endDate.add(Calendar.DAY_OF_MONTH,1);
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                Utils utils = Utils.getInstance();
                Cursor queryCursor = db.query(AppTables.SESSION_TABLE.getName(),
                        new String[0],
                        utils.concatString( " ",
                                AppTables.SESSION_TABLE_COL_3.getName(),
                                "BETWEEN",
                                String.valueOf(date.getTime().getTime()),
                                "AND",
                                String.valueOf(endDate.getTime().getTime()),
                                "AND",
                                AppTables.SESSION_TABLE_COL_0.getName(),
                                "=",
                                athlete.id.toString()
                                ),
                        null,
                        null,
                        null,
                        null,
                        "1");
                return queryCursor;
            },
            (cursor) -> {result.OnResult(cursor);});
        task.executeOnExecutor(NoLeakAsyncTask.THREAD_POOL_EXECUTOR);
    }
}
