package basic;

import jvm.MyOOM;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MyReflection {

    public static void main(String[] args) {
        MyReflection myReflection = new MyReflection();
        //myReflection.getMyClass();
        //myReflection.getMyField();
        //myReflection.getMyConstructor();
        //myReflection.getMyInstance();
        //myReflection.reflectionVisibility();
        myReflection.doCheckSingleTon();
    }

    private void doCheckSingleTon() {
        try {
            Class clazz = Class.forName("basic.ReflectionEntity");

            //Object obj1 = clazz.getConstructor().newInstance();
            //Object obj2 = clazz.getConstructor().newInstance();
            Object obj1 = clazz.newInstance();
            Object obj2 = clazz.newInstance();
            System.out.println("obj1 = obj2 : " + (obj1 == obj2));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getMyClass() {
        try {
            //method1
            Class class1 = MyOOM.class;
            //method2
            Class class2 = Class.forName("jvm.MyOOM");
            //method3
            Class class3 = new MyOOM().getClass();

            System.out.println("class1 = class2 : " + (class1 == class2));
            System.out.println("class2 = class3 : " + (class2 == class3));
            System.out.println("class1 = class3 : " + (class1 == class3));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void getMyField() {
        try {
            Class clazz = Class.forName("jvm.MyOOM");
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                System.out.println(field);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void getMyConstructor() {
        try {
            Class clazz = Class.forName("jvm.MyOOM");
            Constructor constructor = clazz.getConstructor();
            System.out.println(constructor);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private void getMyInstance() {
        try {
            Class clazz = Class.forName("jvm.MyOOM");
            Constructor constructor = clazz.getConstructor();

            MyOOM myOOM1 = (MyOOM) clazz.newInstance();
            MyOOM myOOM2 = (MyOOM) constructor.newInstance();

            System.out.println("myOOM1 = myOOM2 : " + (myOOM1 == myOOM2));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reflectionVisibility() {
        try {
            //test ReflectionEntity
            Class clazz = Class.forName("basic.ReflectionEntity");

            /**
             * fields1 has all declared fields, fields2 only has public fields
             */
            Field[] fields1 = clazz.getDeclaredFields();
            Field[] fields2 = clazz.getFields();
            //display visible fields
            for (Field field : fields1) {
                System.out.println(field);
            }
            System.out.println();
            for (Field field : fields2) {
                System.out.println(field);
            }
            System.out.println();

            /**
             * methods1 has all methods including inherited methods like notify(), toString()
             * and other native method, while methods2 only has declared methods
             */
            Method[] methods1 = clazz.getMethods();
            Method[] methods2 = clazz.getDeclaredMethods();
            for (Method method : methods1) {
                System.out.println(method);
            }
            System.out.println();
            for (Method method : methods2) {
                System.out.println(method);
            }
            System.out.println();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
