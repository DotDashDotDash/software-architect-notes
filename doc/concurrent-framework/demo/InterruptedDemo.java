public class InterruptedDemo{
    public static void main(String[] args){
        Thread.currentThread().interrupt();
        Thread.currentThread().interrupt();

        /**
         * 两次打印结果不同，说明Thread.interrupted()会清除中断标志位
         * 底层实现是interrupted()会调用isInterrupted()
         */
        System.out.println("main thread is interrupted? " + Thread.interrupted());
        System.out.println("main thread is also interrupted? " + Thread.interrupted());
    }
}