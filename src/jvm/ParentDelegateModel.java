package jvm;

import java.io.*;

public class ParentDelegateModel {
    private static final String classPath1 = "D:/MyGithub/repository/OfferReaper/out/production/OfferReaper/jvm/";

    /**
     * User defined classloader, not destroy parent delegate model
     * only override findClass() method
     */
    private class MyClassLoaderNotBreak extends ClassLoader{
        @Override
        public Class<?> findClass(String name){
            String classPath = classPath1 + name.replace('.', '/') + ".class";
            File file = new File(classPath);
            byte[] classBytes = new byte[(int) file.length()];
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                fileInputStream.read(classBytes);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Class<?> clazz = defineClass(name, classBytes, 0, classBytes.length);
            return clazz;
        }
    }

    /**
     * User defined classloader, destroy parent delegate model
     */
    private class MyClassLoaderBreak extends ClassLoader{
        @Override
        public Class<?> loadClass(String name){
            try {
                return findClass(name);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    /**
     * In this case, we do not break the parent delegate model, although
     * using two different classloader to load one class, the result shows
     * that these two classloader has succession relationship
     */
    public void loadOneClassWithTwoClassLoader(){
        //context classloader to load MyOOM.class
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        //user defined classloader to load MyOOM.class
        ClassLoader classLoader1 = new MyClassLoaderNotBreak();
        //user defined classloader to load MyOOM.class
        ClassLoader classLoader2 = new MyClassLoaderBreak();

        try {
            System.out.println(contextLoader);
            System.out.println(classLoader1.getParent());

            Class<?> class1 = contextLoader.loadClass("jvm.MyOOM");
            Class<?> class2 = classLoader1.loadClass("jvm.MyOOM");
            Class<?> class3 = classLoader2.loadClass("jvm.MyOOM");

            System.out.println("class1 = class2 : " + (class1.hashCode() == class2.hashCode()));
            /**
             * This statement throw an exception, overriding ClassLoader.loadClass()
             * destroy PDModel, which means Object class will be loaded by classLoader2,
             * while there is no Object.class to load in our directory
             */
            System.out.println("class1 = class3 : " + (class1.hashCode() == class3.hashCode()));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Class.forName() will trigger static statement to execution,
     * This is how JDBC destroy PDModel and register driver
     */
    public void triggerStaticClause(){
        try {
            Class.forName("jvm.entity.StaticEntity");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        ParentDelegateModel model = new ParentDelegateModel();
        //model.loadOneClassWithTwoClassLoader();
        model.triggerStaticClause();
    }
}
