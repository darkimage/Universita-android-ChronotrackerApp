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
    private final MutableLiveData<ActivitySession[]> currentDayActivities = new MutableLiveData<>();
    private final MutableLiveData<ActivitySportSpecialization> dialogSelectedActivityType = new MutableLiveData<>();
    private final MutableLiveData<ActivitySession> activitySession = new MutableLiveData<>();
    private final MutableLiveData<ActivitySession> toolbarSession = new MutableLiveData<>();
    private final MutableLiveData<StartSessionData> startSession = new MutableLiveData<>();
    private final MutableLiveData<ActivitySession> restoreSession = new MutableLiveData<>();
    private final MutableLiveData<ActivitySession> endSession = new MutableLiveData<>();
    private final MutableLiveData<ActivitySession> selectedActivitySession = new MutableLiveData<>();

    public ActivitySessionModel() {
    }

    public LiveData<StartSessionData> getStartSession() {
        return startSession;
    }

    public LiveData<ActivitySession> getRestoreSession() {
        return restoreSession;
    }

    public LiveData<ActivitySession> getSelectedActivitySession() {
        return selectedActivitySession;
    }

    public LiveData<ActivitySession> getToolBarSession() {
        return toolbarSession;
    }

    public LiveData<ActivitySession> getActivitySession() {
        return activitySession;
    }

    public LiveData<ActivitySport> getDialogSelectedActivity() {
        return dialogSelectedActivity;
    }

    public LiveData<ActivitySportSpecialization> getDialogSelectedActivityType() {
        return dialogSelectedActivityType;
    }

    public LiveData<ActivitySession> getEndSession() { return endSession; }

    public LiveData<ActivitySession[]> getCurrentDayActivities() {
        return currentDayActivities;
    }

    public void setDialogSelectedActivity(ActivitySport activitySport) {
        dialogSelectedActivity.setValue(activitySport);
    }

    public void setDialogSelectedActivityType(ActivitySportSpecialization activitySportType) {
        dialogSelectedActivityType.setValue(activitySportType);
    }

    public void setActivitySession(ActivitySession activitySessionData) {
        activitySession.setValue(activitySessionData);
    }

    public void setStartSession(StartSessionData activitySessionData) {
        startSession.setValue(activitySessionData);
    }

    public void setEndSession(ActivitySession endData) {
        endSession.setValue(endData);
    }

    public void setSelectedActivitySession(ActivitySession activitySessionData) {
        selectedActivitySession.setValue(activitySessionData);
    }

    public void setToolbarSession(ActivitySession session) {
        toolbarSession.setValue(session);
    }

    public void setRestoreSession(ActivitySession session) {
        restoreSession.setValue(session);
    }

    public void setCurrentDayActivities(ActivitySession[] activitySessionData) {
        currentDayActivities.setValue(activitySessionData);
    }
}
