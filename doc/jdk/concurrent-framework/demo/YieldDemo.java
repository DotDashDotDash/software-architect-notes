
public class YieldDemo implements Runnable{
    @Override
    public void run(){
        try {
            Thread.sleep(100);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for(int i = 0; i < 100; i++){
            System.out.println("Thread: " + Thread.currentThread().getName() + " -> " + i);
            Thread.yield();
        }
    }

    public static void main(String[] args){
        YieldDemo demo = new YieldDemo();

        //Thread t1 = new Thread(demo, "T1");
        //Thread t2 = new Thread(demo, "T2");

        Thread t1 = new Thread(new YieldDemo(), "T1");
        Thread t2 = new Thread(new YieldDemo(), "T2");

        t1.start();
        t2.start();
    }
}