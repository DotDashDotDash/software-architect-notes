package concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

public class MyExecutorService {
    private ExecutorService service = null;

    public MyExecutorService() {
        //define 5 threads
        service = Executors.newFixedThreadPool(5);
    }

    public static void main(String[] args) {
        MyExecutorService service = new MyExecutorService();
        //service.doService();
        //service.doCallable();
        service.doScheduled();
    }

    public void doScheduled() {
        ExecutorService service = Executors.newScheduledThreadPool(5);

        ((ScheduledExecutorService) service).schedule(new Runnable() {
            @Override
            public void run() {
                System.out.println("delay for 3s...");
            }
        }, 3, TimeUnit.SECONDS);

        ((ScheduledExecutorService) service).scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                System.out.println("execute at fixed rate...");
            }
        }, 1, 3, TimeUnit.SECONDS);
    }

    public void doService() {
        MyThread thread1 = new MyThread();
        MyRunnable thread2 = new MyRunnable();

        service.submit(thread1);
        service.submit(thread2);

        //shutdown the thread pool
        service.shutdown();
    }

    public void doCallable() {
        MyCallable task = new MyCallable();

        Future f = service.submit(task);    //returning Object
        try {
            System.out.println(f.get().toString());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        service.shutdown();
    }
}
