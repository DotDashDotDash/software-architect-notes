/**
 * JoinDemo
 * 
 * join()是为了让异步线程达到同步的目的
 * 例如所有线程全部执行完毕，再执行"Finished"
 */
public class JoinDemo implements Runnable{
    @Override
    public void run() {
        try {
            System.out.println(Thread.currentThread().getName() + " start-----");
            Thread.sleep(1000);
            System.out.println(Thread.currentThread().getName() + " end------");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        for (int i=0;i<5;i++) {
            Thread test = new Thread(new JoinDemo());
            test.start();
            try {
                test.join();    //调用join方法
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Finished~~~");
    }
}