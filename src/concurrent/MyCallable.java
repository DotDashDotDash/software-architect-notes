package concurrent;

import java.util.concurrent.Callable;

public class MyCallable implements Callable {
    @Override
    public Object call() {
        System.out.println(Thread.currentThread().getName() + " starts...");
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName() + " ends...");
        return "Mission finished...";
    }
}
