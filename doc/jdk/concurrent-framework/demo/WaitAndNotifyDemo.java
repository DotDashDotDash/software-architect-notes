/**
 * @apiNote wait() and notify()
 * 
 * @apiNote wait允许我们将线程置于睡眠状态同时又积极响应外部条件，例如notify
 *          并且只能在sychronized块里面使用，当notify的时候，sychronized块
 *          会放弃持有的锁(对象锁)，java允许每一个对象都持有锁!!!!!
 * 
 */
public class WaitAndNotifyDemo{
    public static void main(String[] args){
        
        //会产生一个问题，wait()/notify()之能在同步块里面使用
        try{
            new Object().wait();
        }catch(Exception e){
            e.printStackTrace();
        }
        
        Thread_A ta = new WaitAndNotifyDemo().new Thread_A();
        ta.start();
        synchronized(ta){
            try{
                System.out.println("Thread ta is waiting....");
                ta.wait();
            }catch(Exception e){
                e.printStackTrace();
            }
            System.out.println("Back to main thread...");
        }
    }

    class Thread_A extends Thread{
        int total;

        @Override
        public void run(){
            synchronized(this){
                for(int i = 0; i < 100; i++)
                    total += i;
                System.out.println("total is: " + total);
                notify();
            }
        }
    }
    
}