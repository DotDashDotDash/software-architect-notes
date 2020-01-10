package design;

public class SingletonPrinciple {
    public static void main(String[] args) {
        SingletonPrinciple instance1 = SingletonPrinciple.getInstance();
        SingletonPrinciple instance2 = SingletonPrinciple.getInstance();
        System.out.println(instance1 == instance2);
    }

    public static SingletonPrinciple getInstance() {
        return Handler.instance;
    }

    static class Handler {
        public static SingletonPrinciple instance = new SingletonPrinciple();
    }
}
