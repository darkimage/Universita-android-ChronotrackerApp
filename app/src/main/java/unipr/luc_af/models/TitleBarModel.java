package unipr.luc_af.models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class TitleBarModel extends ViewModel {
    private final MutableLiveData<String> titleData = new MutableLiveData<>();

    TitleBarModel() { }

    public LiveData<String> getTitle() {
        return titleData;
    }

    public void setTitle(String title){ titleData.setValue(title);}


}
