package unipr.luc_af.classes;

import android.os.Parcel;
import android.os.Parcelable;

public class Athlete implements Parcelable {
    public Long id;
    public String name;
    public String surname;
    public Long activityReference;

    public Athlete(Long uniqueId, String AthleteName, String AthleteSurname, Long activity) {
        id = uniqueId;
        name = AthleteName;
        surname = AthleteSurname;
        activityReference = activity;
    }

    public Athlete(Parcel parcel) {
        id = (Long) parcel.readSerializable();
        name = (String) parcel.readSerializable();
        surname = (String) parcel.readSerializable();
        activityReference = (Long) parcel.readSerializable();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeSerializable(id);
        parcel.writeSerializable(name);
        parcel.writeSerializable(surname);
        parcel.writeSerializable(activityReference);
    }

    public static Creator<Athlete> CREATOR = new Creator<Athlete>() {
        @Override
        public Athlete createFromParcel(Parcel parcel) {
            return new Athlete(parcel);
        }

        @Override
        public Athlete[] newArray(int i) {
            return new Athlete[i];
        }
    };
}
