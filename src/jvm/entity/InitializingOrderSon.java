package jvm.entity;

public class InitializingOrderSon extends InitializingOrderFather{
    static{
        System.out.println("Son static clause has been initialized.");
    }

    public InitializingOrderSon(){
        System.out.println("Son constructor.");
    }
}
