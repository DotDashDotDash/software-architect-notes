package jvm.entity;

public class InitializingOrderFather {
    static{
        System.out.println("Father static clause has been initialized.");
    }

    public InitializingOrderFather(){
        System.out.println("Father constructor.");
    }
}
