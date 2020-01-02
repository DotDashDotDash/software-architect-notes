package jvm;

public class MyClassLoader {

    public void getClassLoader(){
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        //AppClassLoader
        System.out.println(classLoader);
        //ExtClassLoader
        System.out.println(classLoader.getParent());
        //null, cause this classloader is written by c code
        System.out.println(classLoader.getParent().getParent());
    }

    public static void main(String[] args){
        MyClassLoader myClassLoader = new MyClassLoader();
        myClassLoader.getClassLoader();
    }
}
