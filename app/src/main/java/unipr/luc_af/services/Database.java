package unipr.luc_af.services;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import unipr.luc_af.database.DatabaseHelper;
import unipr.luc_af.database.AppTables;
import unipr.luc_af.database.DatabaseResult;

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
     * altra chiamata a metodi do questa classe
     * @param context la Activity di riferimento
     */
    public void setContext(Context context) {
        dbHelper = new DatabaseHelper(context);
        dbHelper.getWritableDatabase();
    }

    /**
     * @return l'helper di gestione del database sql
     */
    public DatabaseHelper getHelper(){ return dbHelper;}

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
        final DatabaseResult res = result;
        Thread thread = new Thread(){
            public void run(){
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                Cursor queryCursor = db.query(AppTables.ACTIVITY_TYPE_TABLE.getName(), new String[]{"name"},null,null,null,null,null);
                res.OnResult(queryCursor);
                return;
            }
        };
        thread.start();
    }
}
