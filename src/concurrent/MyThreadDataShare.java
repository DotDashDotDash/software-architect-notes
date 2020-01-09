package concurrent;

public class MyThreadDataShare {

    public static void main(String[] args) {
        Data data = new MyThreadDataShare().new Data();
        MyThreadDataShare threadDataShare = new MyThreadDataShare();

        Thread t1 = new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + " increase the shared data...");
            data.increase();
        });

        Thread t2 = new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + " decrease the shared data...");
            data.decrease();
        });

        t1.start();
        t2.start();

        //check the result, supposed to be 0
        System.out.println("data: " + data.data);
    }


    class Data {
        int data = 0;

        synchronized void increase() {
            this.data++;
        }

        synchronized void decrease() {
            this.data--;
        }
    }
}
