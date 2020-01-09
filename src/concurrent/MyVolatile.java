package concurrent;

public class MyVolatile {
    public volatile int data = 0;

    public static void main(String[] args) {

        MyVolatile myVolatile = new MyVolatile();
        //create two threads
        Thread t1 = new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + " changes 'data' to 1");
            myVolatile.data = 1;
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + " get 'data': " + myVolatile.data);
        });

        Thread t2 = new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + " changes 'data' to 2");
            myVolatile.data = 2;
        });

        t1.start();
        t2.start();
    }
}
