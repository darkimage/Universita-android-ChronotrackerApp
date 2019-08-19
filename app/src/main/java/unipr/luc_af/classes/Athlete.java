package unipr.luc_af.classes;

public class Athlete {
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
}
