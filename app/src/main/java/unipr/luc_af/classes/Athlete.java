package unipr.luc_af.classes;

import unipr.luc_af.database.interfaces.DatabaseResult;
import unipr.luc_af.services.Database;

public class Athlete {
    public String name;
    public String surname;
    public Long activityReference;

    public Athlete(String AthleteName, String AthleteSurname, Long activity) {
        name = AthleteName;
        surname = AthleteSurname;
        activityReference = activity;
    }
}
