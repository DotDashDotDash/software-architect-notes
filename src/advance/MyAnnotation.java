package advance;

import java.lang.annotation.*;
import java.lang.reflect.Method;

public class MyAnnotation {

    public static void main(String[] args) {
        MyAnnotation myAnnotation = new MyAnnotation();
        myAnnotation.testAnnotationInheritance();
    }

    public void testAnnotationInheritance() {
        try {
            Method childMethodA = Child.class.getMethod("a");
            Method childMethodB = Child.class.getMethod("b");

            System.out.println(childMethodA.isAnnotationPresent(A.class));
            System.out.println(childMethodB.isAnnotationPresent(A.class));
            System.out.println("annotation on class inherited? " + Child.class.isAnnotationPresent(B.class));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.FIELD})
    @interface A {
        String value() default "";
    }

    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.FIELD, ElementType.TYPE})
    @interface B {
        String value() default "";
    }

    @B("father B annotation")
    class Father {

        @A("father method a")
        public void a() {
        }

        @A("father method b")
        public void b() {
        }

        @A("father method c")
        public void c() {
        }
    }

    class Child extends Father {

        @Override
        public void a() {
        }
    }
}
