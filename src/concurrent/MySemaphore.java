package concurrent;

import java.util.concurrent.Semaphore;

public class MySemaphore {
    Semaphore semaphore = new Semaphore(5, true);

    public static void main(String[] args) {
        MySemaphore mySemaphore = new MySemaphore();
        mySemaphore.doSemaphore();
    }

    public void doSemaphore() {
        Task[] tasks = new Task[20];

        for (int i = 0; i < 20; i++) {
            tasks[i] = new Task();
        }

        for (int i = 0; i < 20; i++) {
            tasks[i].start();
        }
    }

    private class Task extends Thread {
        @Override
        public void run() {
            try {
                semaphore.acquire();
                System.out.println(Thread.currentThread().getName() + " acquired a lock.");
                //Thread.sleep(3000);
                /**
                 * logic code
                 */
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                semaphore.release();
                System.out.println(Thread.currentThread().getName() + " released a lock.");
            }
        }
    }
}
