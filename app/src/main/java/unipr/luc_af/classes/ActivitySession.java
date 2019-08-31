package unipr.luc_af.classes;

public class ActivitySession {
    public Long id;
    public Long athlete;
    public Long activity;
    public Long activityType;
    public Long startTime;
    public Long stopTime;
    public Long distance;
    public Integer speed;
    public Lap[] laps;

    public ActivitySession(Long athleteReference,
                    Long activityReference,
                    Long activityTypeReference,
                    Long distanceReference){
        athlete = athleteReference;
        activity = activityReference;
        activity = activityTypeReference;
        distance = distanceReference;
    }

    public ActivitySession(Long activityId,
                    Long athleteRef,
                    Long activityRef,
                    Long activityTypeRef,
                    Long startTimeRef,
                    Long stopTimeRef,
                    Long distanceRef,
                    Integer speedRef,
                    Lap[] lapsRef){
        id = activityId;
        athlete = athleteRef;
        activity = activityRef;
        activityType = activityTypeRef;
        startTime = startTimeRef;
        stopTime = stopTimeRef;
        distance = distanceRef;
        speed = speedRef;
        laps = lapsRef;
    }
}
