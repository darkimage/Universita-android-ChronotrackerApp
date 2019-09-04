package unipr.luc_af.classes;

import android.os.Parcel;
import android.os.Parcelable;

public class ActivitySession implements Parcelable {
    public Long id;
    public Long athlete;
    public Long activity;
    public Long activityType;
    public Long startTime;
    public Long stopTime;
    public Long distance;
    public Integer speed;
    public Lap[] laps;

    public ActivitySession() {};

    public ActivitySession(Parcel parcel){
        athlete = (Long)parcel.readSerializable();
        activity = (Long)parcel.readSerializable();
        activityType = (Long)parcel.readSerializable();
        startTime = (Long)parcel.readSerializable();
        stopTime = (Long)parcel.readSerializable();
        distance = (Long)parcel.readSerializable();
        speed = (Integer) parcel.readSerializable();
        laps = (Lap[]) parcel.readArray(Lap.class.getClassLoader());
    }

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeSerializable(id);
        parcel.writeSerializable(athlete);
        parcel.writeSerializable(activity);
        parcel.writeSerializable(activityType);
        parcel.writeSerializable(startTime);
        parcel.writeSerializable(stopTime);
        parcel.writeSerializable(distance);
        parcel.writeSerializable(speed);
        parcel.writeTypedArray(laps,i);
    }

    public static Creator<ActivitySession> CREATOR = new Creator<ActivitySession>() {
        @Override
        public ActivitySession createFromParcel(Parcel parcel) {
            return new ActivitySession(parcel);
        }

        @Override
        public ActivitySession[] newArray(int i) {
            return new ActivitySession[i];
        }
    };
}
