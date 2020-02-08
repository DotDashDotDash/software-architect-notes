import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConditionDemo {
    Lock lock = new ReentrantLock();
    Condition print1 = lock.newCondition();
    Condition print2 = lock.newCondition();
    Condition print3 = lock.newCondition();
    private volatile int printFlag = 1;
    public static int count = 0; 

    public void execute1() {
        lock.lock();
        try {
            while (printFlag!= 1) {
                try {
                    print1.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            for (int j = 0; j < 5; j++) {
                count++;
                System.out.println(Thread.currentThread().getName() + " " + count);
            }
            printFlag = 2;
            print2.signal();
        } finally {
            lock.unlock();
        }
    }

    public void execute2() {
        lock.lock();
        try {
            while (printFlag != 2) {
                try {
                    print2.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            for (int j = 0; j < 5; j++) {
                count++;
                System.out.println(Thread.currentThread().getName() + " " + count);
            }
            printFlag = 3;
            print3.signal();
        } finally {
            lock.unlock();
        }
    }

    public void execute3() {
        lock.lock();
        try {
            while (printFlag != 3) {
                try {
                    print3.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            for (int j = 0; j < 5; j++) {
                count++;
                System.out.println(Thread.currentThread().getName() + " " + count);
            }
            printFlag = 1;
            print1.signal();
        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) {
        ConditionDemo demo = new ConditionDemo();

        new Thread(new Runnable() { // 线程1
            @Override
            public void run() {
                for (int i = 1; i <= 5; i++) {
                    demo.execute1();
                }
            }
        }, "Thread-1").start();
        new Thread(new Runnable() { // 线程2
            @Override
            public void run() {
                for (int i = 1; i <= 5; i++) {
                    demo.execute2();
                }
            }
        }, "Thread-2").start();
        new Thread(new Runnable() { // 线程3
            @Override
            public void run() {
                for (int i = 1; i <= 5; i++) {
                    demo.execute3();
                }
            }
        }, "Thread-3").start();
    }
}