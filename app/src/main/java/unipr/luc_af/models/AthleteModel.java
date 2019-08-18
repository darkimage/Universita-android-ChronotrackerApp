package unipr.luc_af.models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import unipr.luc_af.classes.Athlete;

public class AthleteModel extends ViewModel {
    private final MutableLiveData<Athlete> athleteData = new MutableLiveData<>();
    AthleteModel() {}
    public LiveData<Athlete> getAthlete() {
        return athleteData;
    }

    public void addAthlete(Athlete athlete){
        athleteData.setValue(athlete);
    }
}
