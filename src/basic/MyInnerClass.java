package basic;

public class MyInnerClass {
    static int outStaticField = 888;
    int outField = 444;

    public static void main(String[] args) {
        InnerStaticClass innerStaticClass = new InnerStaticClass();
        innerStaticClass.getOutField();
        InnerNotStaticClass innerNotStaticClass = new MyInnerClass().new InnerNotStaticClass();
        innerNotStaticClass.getOutField();
    }

    /**
     * inner static class
     */
    public static class InnerStaticClass {
        static int innerStaticField;
        int innerNotStaticField;

        public static int innerStaticMethod() {
            return 999;
        }

        public int innerNotStaticMethod() {
            return 1000;
        }

        public void getOutField() {
            System.out.println(outStaticField);
            //System.out.println(outField); //cannot get out non-static field
        }
    }

    /**
     * inner not static class
     */
    public class InnerNotStaticClass {
        int innerNotStaticField;
        //static int innerStaticField;  //this statement is not allowed in not-static inner class

        public int innerNotStaticMethod() {
            return 9999;
        }

        public void getOutField() {
            System.out.println(outStaticField);
            System.out.println(outField);
        }
    }
}
