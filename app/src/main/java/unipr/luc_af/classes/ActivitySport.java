package unipr.luc_af.classes;

public class ActivitySport extends ActivityGeneral {

    public ActivitySport(Long activitySportId, String activityName) {
        super(activitySportId, activityName);
    }

    public ActivitySport(String activityName) {
        super(new Long(-1), activityName);
    }
}
