package unipr.luc_af.models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Arrays;

public class PopupItemsModel extends ViewModel {
    private final MutableLiveData<int[]> popupItemsData = new MutableLiveData<>();

    public PopupItemsModel() {
    }

    public LiveData<int[]> getActiveItems() {
        return popupItemsData;
    }

    public void setActiveItems(int[] items) {
        popupItemsData.setValue(items);
    }

    public void addActiveItem(int item) {
        int newLength = popupItemsData.getValue().length + 1;
        int[] newItems = Arrays.copyOf(popupItemsData.getValue(), newLength);
        newItems[newLength - 1] = item;
        popupItemsData.setValue(newItems);
    }

    public void removeAllActiveItems() {
        popupItemsData.setValue(new int[0]);
    }
}
