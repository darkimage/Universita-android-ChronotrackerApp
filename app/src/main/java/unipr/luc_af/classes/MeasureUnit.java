package unipr.luc_af.classes;

public class MeasureUnit {
    public Long id;
    public String name;
    public String shortName;

    public MeasureUnit(Long idRef, String nameRef, String shortNameRef){
        id = idRef;
        name = nameRef;
        shortName = shortNameRef;
    }

}
