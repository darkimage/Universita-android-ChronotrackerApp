package unipr.luc_af.database;

public enum AppTables {
    TABLE_ID_COL("id"),
    ACTIVITY_TABLE("activity"),
    ACTIVITY_TABLE_COL_0("name"),
    ACTIVITY_TYPE_TABLE("activity_type"),
    ACTIVITY_TYPE_TABLE_COL_0("name"),
    ACTIVITY_TYPE_TABLE_COL_1("type"),
    ATHLETE_TABLE("athlete"),
    ATHLETE_TABLE_COL_0("name"),
    ATHLETE_TABLE_COL_1("surname"),
    ATHLETE_TABLE_COL_2("activity"),
    SESSION_TABLE("tracked_session"),
    SESSION_TABLE_COL_0("athlete"),
    SESSION_TABLE_COL_1("activity"),
    SESSION_TABLE_COL_2("activity_type"),
    SESSION_TABLE_COL_3("start_time"),
    SESSION_TABLE_COL_4("stop_time"),
    SESSION_TABLE_COL_5("distance"),
    SESSION_TABLE_COL_6("speed");

    private String name;
    AppTables(String tableName){
        name = tableName;
    }
    public String getName(){
        return name;
    }
}
