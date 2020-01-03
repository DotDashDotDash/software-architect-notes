import java.util.concurrent.atomic.AtomicStampedReference;
import java.util.concurrent.locks.LockSupport;

public class AtomicStampedReferenceDemo{
    public static void main(String[] args){
        AtomicStampedReference<Integer> atomicStampedReference = new AtomicStampedReference<>(1, 1);

        new Thread(()->{
            int[] stampHolder = new int[1];
            int value = atomicStampedReference.get(stampHolder);
            int stamp = stampHolder[0];
            System.out.println("Thread 1 read value: " + value + ", stamp: " + stamp);

            LockSupport.parkNanos(1000000000L);

            if(atomicStampedReference.compareAndSet(value, 3, stamp, stamp + 1)){
                System.out.println("Thread 1 update from " + value + " to 3");
            }else{
                System.out.println("Thread 1 update failed");
            }
        }).start();

        new Thread(()->{
            int[] stampHolder = new int[1];
            int value = atomicStampedReference.get(stampHolder);
            int stamp = stampHolder[0];
            System.out.println("Thread 2 read value: " + value + ", stamp: " + stamp);

            if(atomicStampedReference.compareAndSet(value, 2, stamp, stamp + 1)){
                System.out.println("Thread 2 update from " + value + " to 2");

                value = atomicStampedReference.get(stampHolder);
                stamp = stampHolder[0];
                System.out.println("Thread 2 read value: " + value + ", stamp: " + stamp);
                if(atomicStampedReference.compareAndSet(value, 1, stamp, stamp + 1)){
                    System.out.println("Thread 2 update value: " + value + " to 1");
                }
            }
        }).start();
    }
}