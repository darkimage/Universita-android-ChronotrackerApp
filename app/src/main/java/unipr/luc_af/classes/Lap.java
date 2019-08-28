package unipr.luc_af.classes;

public class Lap {
    public Long duration;
    public Long fromStart;

    public Lap(long elapsed, long current){
        duration = elapsed;
        fromStart = current;
    }
}
