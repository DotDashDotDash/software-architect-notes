package concurrent;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyCyclicBarrier {

    public static void main(String[] args) {
        ExecutorService service = Executors.newCachedThreadPool();
        //before
        Runnable task = new Runnable() {
            @Override
            public void run() {
                System.out.println("5 threads step forth together...");
            }
        };

        CyclicBarrier barrier = new CyclicBarrier(5, task);

        service.execute(new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + " arrives...");
            try {
                barrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }));

        service.execute(new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + " arrives...");
            try {
                barrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }));

        service.execute(new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + " arrives...");
            try {
                barrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }));

        service.execute(new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + " arrives...");
            try {
                barrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }));

        service.execute(new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + " arrives...");
            try {
                barrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }));

        //check if barrier's count reset to 5
        System.out.println("barrier's count: " + barrier.getParties());
    }
}
