package unipr.luc_af.database;

public enum AppTables {
    TABLE_ID_COL("id"),
    ACTIVITY_TABLE("activity"),
    ACTIVITY_TABLE_COL_0("name"),
    ACTIVITY_TYPE_TABLE("activity_type"),
    ACTIVITY_TYPE_TABLE_COL_0("name"),
    ACTIVITY_TYPE_TABLE_COL_1("activity"),
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
    SESSION_TABLE_COL_6("speed"),
    UNIT_TABLE("unit"),
    UNIT_TABLE_COL_0("name"),
    UNIT_TABLE_COL_1("short_name"),
    LAP_TABLE("laps"),
    LAP_TABLE_COL_0("lap_from_start"),
    LAP_TABLE_COL_1("lap_duration"),
    LAP_TABLE_COL_2("of_session");

    private String name;
    AppTables(String tableName){
        name = tableName;
    }
    public String getName(){
        return name;
    }
}
