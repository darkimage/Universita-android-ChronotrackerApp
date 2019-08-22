package unipr.luc_af.models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import unipr.luc_af.classes.ActivitySport;
import unipr.luc_af.classes.ActivitySportSpecialization;
import unipr.luc_af.classes.StartSessionData;

public class ActivitySessionModel extends ViewModel {
    private final MutableLiveData<ActivitySport> dialogSelectedActivity = new MutableLiveData<>();
    private final MutableLiveData<ActivitySportSpecialization> dialogSelectedActivityType = new MutableLiveData<>();
    private final MutableLiveData<StartSessionData> sessionStartData = new MutableLiveData<>();

    ActivitySessionModel(){}

    public LiveData<StartSessionData> getSessionStartData() {
        return sessionStartData;
    }
    public LiveData<ActivitySport> getDialogSelectedActivity() {
        return dialogSelectedActivity;
    }
    public LiveData<ActivitySportSpecialization> getDialogSelectedActivityType() { return dialogSelectedActivityType; }

    public void setSessionStartData(StartSessionData startData){
        sessionStartData.setValue(startData);
    }
    public void setDialogSelectedActivity(ActivitySport activitySport){
        dialogSelectedActivity.setValue(activitySport);
    }
    public void setDialogSelectedActivityType(ActivitySportSpecialization activitySportType){
        dialogSelectedActivityType.setValue(activitySportType);
    }
}
