package unipr.luc_af.classes;

public class ActivitySession {
    public Long id = new Long(-1);
    public Long athlete;
    public Long activity;
    public Long activityType;
    public Long startTime = new Long(-1);
    public Long stopTime = new Long(-1);
    public Long distance;
    public Integer speed = new Integer(-1);

    ActivitySession(Long athleteReference,
                    Long activityReference,
                    Long activityTypeReference,
                    Long distanceReference){
        athlete = athleteReference;
        activity = activityReference;
        activity = activityTypeReference;
        distance = distanceReference;
    }

    ActivitySession(Long activityId,
                    Long athleteReference,
                    Long activityReference,
                    Long activityTypeReference,
                    Long start_time,
                    Long stop_time,
                    Long distanceReference,
                    Integer activitySpeed){
        id = activityId;
        athlete = athleteReference;
        activity = activityReference;
        activityType = activityTypeReference;
        startTime = start_time;
        stopTime = stop_time;
        distance = distanceReference;
        speed = activitySpeed;
    }
}
