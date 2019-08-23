package unipr.luc_af.classes;

public class ActivityGeneral {
    public Long id;
    public String name;

    ActivityGeneral(Long activityId, String activityName){
        id = activityId;
        name = activityName;
    }

    public String getName(){
        return name;
    }

    public Long getID(){
        return id;
    }
}
