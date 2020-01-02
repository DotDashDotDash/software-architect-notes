package jvm;

import jvm.entity.InitializingOrderSon;

public class InitializingOrder {

    /**
     * Initialization order is:
     *      father static clause
     *      son static clause
     *      father normal statement
     *      son normal statement
     * @param args
     */
    public static void main(String[] args){
        new InitializingOrderSon();
    }
}
