package concurrent;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyCountDownLatch {

    public static void main(String[] args) {
        CountDownLatch latch = new CountDownLatch(5);
        ExecutorService service = Executors.newCachedThreadPool();

        service.execute(new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName() + " starts...");
                latch.countDown();
            }
        });

        service.execute(new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName() + " starts...");
                latch.countDown();
            }
        });

        service.execute(new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName() + " starts...");
                latch.countDown();
            }
        });

        service.execute(new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName() + " starts...");
                latch.countDown();
            }
        });

        service.execute(new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread().getName() + " starts...");
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.out.println("ends...");
        }
    }
}
