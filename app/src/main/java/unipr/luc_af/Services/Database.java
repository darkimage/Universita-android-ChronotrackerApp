package unipr.luc_af.Services;
import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import unipr.luc_af.database.DatabaseHelper;
import unipr.luc_af.database.AppTables;

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
    public void setContext(Context context) { dbHelper = new DatabaseHelper(context); }

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

    public long addActivity(String name, long activityType) throws SQLException{
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        //Non c'e necessita di una transaction siccome stiamo eseguendo solamente una query di INSERT INTO nel caso
        //fallisca viene generato un errore del tipo SQLException
        ContentValues values = new ContentValues();
        values.put(AppTables.ACTIVITY_TABLE_COL_0.getName(), name);
        values.put(AppTables.ACTIVITY_TABLE_COL_1.getName(), activityType);
        return db.insertOrThrow(AppTables.ACTIVITY_TABLE.getName(), null, values);
    }

    public long addActivityType(String name) throws SQLException{
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(AppTables.ACTIVITY_TYPE_TABLE_COL_0.getName(), name);
        return db.insertOrThrow(AppTables.ACTIVITY_TYPE_TABLE.getName(), null, values);
    }
}
