package unipr.luc_af.models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class TitleBarModel extends ViewModel {
    private final MutableLiveData<String> titleData = new MutableLiveData<>();

    public LiveData<String> getTitle() {
        return titleData;
    }

    TitleBarModel() {
        titleData.setValue("Testing");
    }


}
