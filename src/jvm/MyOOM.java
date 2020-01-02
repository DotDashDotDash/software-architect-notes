package jvm;

import java.util.ArrayList;
import java.util.List;

public class MyOOM {

    /**
     * 128KB Entity
     */
    private class OOMEntity{
        char[] c;

        public OOMEntity(){
            c = new char[128 * 1024];
        }
    }

    /**
     * simulate OOM error
     */
    public void doOOM(){
        List<OOMEntity> oomEntities = new ArrayList<>();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while(true){
            //endless adding will cause OOM error
            oomEntities.add(new OOMEntity());
        }
    }

    public static void main(String[] args){
        MyOOM oom = new MyOOM();
        oom.doOOM();
    }
}
