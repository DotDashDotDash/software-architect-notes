import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockDemo {
    public static void main(String[] args) throws Exception {
        AddDemo runnalbeDemo = new AddDemo();
        Thread thread = new Thread(runnalbeDemo::add);
        thread.start();
        Thread thread1 = new Thread(runnalbeDemo::add);
        thread1.start();
        Thread thread2 = new Thread(runnalbeDemo::add);
        thread2.start();

        Thread.sleep(4000);
        //thread1.interrupt(); //如果这里中断会抛出异常
        System.out.println(runnalbeDemo.getCount());
    }

    private static class AddDemo {
        private final AtomicInteger count = new AtomicInteger();
        private final ReentrantLock reentrantLock = new ReentrantLock(true);
        //private final Condition condition = reentrantLock.newCondition();

        private void add() {
            try {
                reentrantLock.lockInterruptibly();  //这个方法是在响应线程中断的时候抛出异常
                                                    //防止无限期的死锁
                count.getAndIncrement();
            }catch(InterruptedException e){
                e.printStackTrace();
            } finally {
                reentrantLock.unlock();
            }
        }

        int getCount() {
            return count.get();
        }
    }
}