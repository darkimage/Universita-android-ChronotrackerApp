package unipr.luc_af.classes;

public class ActivitySportSpecialization {
    public Long id = new Long(-1);
    public String name;
    public Long activitySport;

    public ActivitySportSpecialization(Long activitySportId, String activityName, Long activitySportReference){
        id = activitySportId;
        name = activityName;
        activitySport = activitySportReference;
    }

    public ActivitySportSpecialization(String activityName, Long activitySportReference){
        name = activityName;
        activitySport = activitySportReference;
    }
}
