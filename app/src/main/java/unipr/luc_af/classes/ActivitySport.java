package unipr.luc_af.classes;

public class ActivitySport {
    public Long id = new Long(-1);
    public String name;

    public ActivitySport(Long activitySportId, String activityName){
        id = activitySportId;
        name = activityName;
    }

    public ActivitySport(String activityName){
        name = activityName;
    }
}
