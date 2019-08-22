package unipr.luc_af.classes;

public class StartSessionData {
    public Athlete athlete;
    public ActivitySport activitySport;
    public ActivitySportSpecialization activitySportType;

    public StartSessionData(Athlete athleteRef,ActivitySport activitySportRef, ActivitySportSpecialization activitySportTypeRef){
        athlete = athleteRef;
        activitySport = activitySportRef;
        activitySportType = activitySportTypeRef;
    }

    public boolean isDataOk(){
        if( athlete == null ||
            activitySport == null){
            return false;
        }
        return true;
    }
}
