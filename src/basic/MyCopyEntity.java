package basic;

import java.io.Serializable;

public class MyCopyEntity implements Serializable {
    private MyReflection myReflection = new MyReflection();
    private int a = 1;

    public MyCopyEntity() {
    }

    public MyReflection getMyReflection() {
        return this.myReflection;
    }
}
