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
    ATHLETE_TABLE_COL_2("activity");

    private String name;
    AppTables(String tableName){
        name = tableName;
    }
    public String getName(){
        return name;
    }
}
