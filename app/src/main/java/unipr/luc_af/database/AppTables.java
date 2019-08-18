package unipr.luc_af.database;

public enum AppTables {
    ACTIVITY_TABLE("activity"),
    ACTIVITY_TABLE_COL_0("name"),
    ACTIVITY_TYPE_TABLE("activity_type"),
    ACTIVITY_TYPE_TABLE_COL_0("name"),
    ACTIVITY_TYPE_TABLE_COL_1("type");

    private String name;
    AppTables(String tableName){
        name = tableName;
    }
    public String getName(){
        return name;
    }
}
