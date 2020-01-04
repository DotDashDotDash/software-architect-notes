package concurrent;

public class MyThreadCircle {

    public static void main(String[] args) {
        MyThreadCircle circle = new MyThreadCircle();
        //circle.waitAndNotify();
        //circle.terminateThread();
        //circle.doYield();
        circle.doJoin();
    }

    public void doJoin() {
        Thread t1 = new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < 10; i++) {
                System.out.println("A" + i);
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                Thread.sleep(2000);
                t1.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < 10; i++) {
                System.out.println("B" + i);
            }
        });

        t1.start();
        t2.start();
    }

    public void waitAndNotify() {
        Object object = new Object();
        Thread t1 = new Thread(() -> {
            synchronized (object) {
                try {
                    Thread.sleep(2000);
                    System.out.println("waiting...");
                    object.wait();
                    System.out.println("finish waiting...");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        });

        Thread t2 = new Thread(() -> {
            synchronized (object) {
                object.notify();
            }
        });

        t1.start();
        t2.start();
    }

    public void terminateThread() {
        Thread t = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;       //quit loop
                }
            }
        });

        t.start();
        t.interrupt();
    }

    public void doYield() {
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                System.out.println("A" + i);
                if (i % 5 == 0) {
                    Thread.yield();
                }
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                System.out.println("B" + i);
                if (i % 5 == 0) {
                    Thread.yield();
                }
            }
        });

        t1.setPriority(Thread.MIN_PRIORITY);
        t2.setPriority(Thread.MAX_PRIORITY);

        t1.start();
        t2.start();
    }
}
