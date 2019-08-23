package unipr.luc_af.models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import unipr.luc_af.classes.ActivitySession;
import unipr.luc_af.classes.Athlete;

public class AthleteModel extends ViewModel {
    private final MutableLiveData<Athlete> athleteData = new MutableLiveData<>();
    private final MutableLiveData<Athlete> athleteSelection = new MutableLiveData<>();
    private final MutableLiveData<ActivitySession[]> athleteCurrentDayActivities = new MutableLiveData<>();

    public AthleteModel() {}
    public LiveData<Athlete> getAthlete() {
        return athleteData;
    }
    public LiveData<Athlete> getSelectedAthlete() {
        return athleteSelection;
    }
    public LiveData<ActivitySession[]> getAthleteCurrentDayActivities() {
        return athleteCurrentDayActivities;
    }

    public void addAthlete(Athlete athlete){
        athleteData.setValue(athlete);
    }
    public void selectAthlete(Athlete athlete) {
        athleteSelection.setValue(athlete);
    }
    public void setCurrentDayActivities (ActivitySession[] activities) {
        athleteCurrentDayActivities.setValue(activities);
    }
}
