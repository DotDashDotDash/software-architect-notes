package concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MyReadWriteLock {
    final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public static void main(String[] args) {
        MyReadWriteLock myReadWriteLock = new MyReadWriteLock();
        //myReadWriteLock.doWriteLock();
        //myReadWriteLock.doUpGrades();
        //myReadWriteLock.doDownGrades();

        ExecutorService service = Executors.newCachedThreadPool();

        service.execute(new Thread(() -> {
            myReadWriteLock.doRead(Thread.currentThread());
        }));

        service.execute(new Thread(() -> {
            myReadWriteLock.doRead(Thread.currentThread());
        }));

        service.execute(new Thread(() -> {
            myReadWriteLock.doRead(Thread.currentThread());
        }));

        service.execute(new Thread(() -> {
            myReadWriteLock.doWrite(Thread.currentThread());
        }));

        service.execute(new Thread(() -> {
            myReadWriteLock.doWrite(Thread.currentThread());
        }));
    }

    public void doRead(Thread t) {
        lock.readLock().lock();
        boolean isWriteLock = lock.isWriteLocked();
        if (!isWriteLock) {
            System.out.println("current lock is read lock...");
        }

        try {
            for (int i = 0; i < 5; i++) {
                Thread.sleep(2000);
                System.out.println(t.getName() + " is reading...");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.readLock().unlock();
            System.out.println("release read lock");
        }
    }

    public void doWrite(Thread t) {
        lock.writeLock().lock();
        boolean isWriteLock = lock.isWriteLocked();
        if (isWriteLock) {
            System.out.println("current lock is write lock...");
        }

        try {
            Thread.sleep(2000);
            System.out.println(t.getName() + " is writing....");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
            System.out.println("release write lock...");
        }
    }

    public void doWriteLock() {
        Thread t = new Thread(() -> {
            lock.writeLock().lock();
            System.out.println("t thread runs...");
            lock.writeLock().unlock();
        });

        lock.writeLock().lock();
        lock.writeLock().lock();
        t.start();
        lock.writeLock().unlock();
        lock.writeLock().unlock();
        System.out.println("release one time...");
    }

    public void doUpGrades() {
        lock.readLock().lock();
        System.out.println("read lock...");
        lock.writeLock().lock();
        System.out.println("write lock...");
        lock.writeLock().unlock();
        lock.readLock().unlock();
    }

    public void doDownGrades() {
        lock.writeLock().lock();
        System.out.println("write lock...");
        lock.readLock().lock();
        System.out.println("read lock...");
        lock.readLock().unlock();
        lock.writeLock().unlock();
    }
}
