package unipr.luc_af.classes;

import android.os.Parcel;
import android.os.Parcelable;

public class Lap implements Parcelable {
    public Long duration;
    public Long fromStart;

    public Lap(){ }

    public Lap(Parcel parcel){
        duration = (Long)parcel.readSerializable();
        fromStart = (Long)parcel.readSerializable();
    }

    public Lap(long elapsed, long current){
        duration = elapsed;
        fromStart = current;
    }

    public static Creator<Lap> CREATOR = new Creator<Lap>() {
        @Override
        public Lap createFromParcel(Parcel parcel) {
            return new Lap(parcel);
        }

        @Override
        public Lap[] newArray(int i) {
            return new Lap[i];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeSerializable(duration);
        parcel.writeSerializable(fromStart);
    }
}
