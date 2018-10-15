package util;

public class ConfigID {
    private int i;
    private int j;

    public ConfigID(int i, int j){
        this.i = i;
        this.j = j;
    }

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public int getJ() {
        return j;
    }

    public void setJ(int j) {
        this.j = j;
    }

    public String toString() {
        return "I_i="+ i + "^j=" + j;
    }
}
