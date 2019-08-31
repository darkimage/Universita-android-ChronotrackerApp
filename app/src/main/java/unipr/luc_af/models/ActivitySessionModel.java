package unipr.luc_af.models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import unipr.luc_af.classes.ActivitySession;
import unipr.luc_af.classes.ActivitySport;
import unipr.luc_af.classes.ActivitySportSpecialization;
import unipr.luc_af.classes.StartSessionData;

public class ActivitySessionModel extends ViewModel {
    private final MutableLiveData<ActivitySport> dialogSelectedActivity = new MutableLiveData<>();
    private final MutableLiveData<ActivitySportSpecialization> dialogSelectedActivityType = new MutableLiveData<>();
    private final MutableLiveData<StartSessionData> sessionStartData = new MutableLiveData<>();
    private final MutableLiveData<ActivitySession> activitySession = new MutableLiveData<>();
    private final MutableLiveData<StartSessionData> startSession = new MutableLiveData<>();
    private final MutableLiveData<ActivitySession> endSession = new MutableLiveData<>();

    public ActivitySessionModel(){}

    public LiveData<StartSessionData> getSessionStartData() {
        return sessionStartData;
    }
    public LiveData<StartSessionData> getStartSession() {
        return startSession;
    }
    public LiveData<ActivitySession> getActivitySession() {
        return activitySession;
    }
    public LiveData<ActivitySport> getDialogSelectedActivity() {
        return dialogSelectedActivity;
    }
    public LiveData<ActivitySportSpecialization> getDialogSelectedActivityType() { return dialogSelectedActivityType; }
    public LiveData<ActivitySession> getEndSession() { return endSession; }

    public void setSessionStartData(StartSessionData startData){
        sessionStartData.setValue(startData);
    }
    public void setDialogSelectedActivity(ActivitySport activitySport){
        dialogSelectedActivity.setValue(activitySport);
    }
    public void setDialogSelectedActivityType(ActivitySportSpecialization activitySportType){
        dialogSelectedActivityType.setValue(activitySportType);
    }
    public void setActivitySession(ActivitySession activitySessionData){
        activitySession.setValue(activitySessionData);
    }
    public void setStartSession(StartSessionData activitySessionData){
        startSession.setValue(activitySessionData);
    }
    public void setEndSession(ActivitySession endData){
        endSession.setValue(endData);
    }
}
