package unipr.luc_af.classes;

import unipr.luc_af.components.ChronoView;

public class StartSessionData {
    public Athlete athlete;
    public ActivitySport activitySport;
    public ActivitySportSpecialization activitySportType;
    public ChronoView.ChronoData trackingData;

    public StartSessionData(Athlete athleteRef, ActivitySport activitySportRef, ActivitySportSpecialization activitySportTypeRef) {
        athlete = athleteRef;
        activitySport = activitySportRef;
        activitySportType = activitySportTypeRef;
    }

    public StartSessionData(Athlete athleteRef,
                            ActivitySport activitySportRef,
                            ActivitySportSpecialization activitySportTypeRef,
                            ChronoView.ChronoData trackingDataRef) {
        athlete = athleteRef;
        activitySport = activitySportRef;
        activitySportType = activitySportTypeRef;
        trackingData = trackingDataRef;
    }

    public boolean isDataOk() {
        if (athlete == null ||
                activitySport == null) {
            return false;
        }
        return true;
    }
}
