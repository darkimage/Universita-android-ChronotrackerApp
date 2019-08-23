package unipr.luc_af.classes;

public class ActivitySportSpecialization extends ActivityGeneral {
    public Long activitySport;

    public ActivitySportSpecialization(Long activitySportId, String activityName, Long activitySportReference){
        super(activitySportId,activityName);
        activitySport = activitySportReference;
    }

    public ActivitySportSpecialization(String activityName, Long activitySportReference){
        super(new Long(-1), activityName);
        activitySport = activitySportReference;
    }

}
