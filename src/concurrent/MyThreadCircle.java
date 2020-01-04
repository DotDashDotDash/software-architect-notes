package concurrent;

public class MyThreadCircle {

    public static void main(String[] args) {
        MyThreadCircle circle = new MyThreadCircle();
        circle.waitAndNotify();
    }

    /**
     * This is a simple test case for wait() && notify()
     * notice that before using wait(), you must get the monitor
     * of waitThread, otherwise an exception will be threw.
     */
    public void waitAndNotify() {
        Thread waitThread = new Thread();
        Thread notifyThread = new Thread();

        //get waitThread's monitor
        synchronized (waitThread) {
            try {
                waitThread.start();
                waitThread.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //wakes waitThread up with notifyThread
        synchronized (notifyThread) {
            notifyThread.notify();
        }
    }
}
