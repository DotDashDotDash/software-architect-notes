import java.util.ArrayList;
import java.util.List;

public class AlibabaStackDemo {
    private List<String> list = new ArrayList<String>();

    public void push(String value) {
        synchronized (this) {
            list.add(value);
            notify();
        }
    }

    /**
     * @question:
     * 这里会产生一个问题，就是当线程x和线程y都被wait()阻塞的时候
     * 会卡在wait()方法，当线程x被唤醒执行，listsize=0之后锁被释放，
     * 线程y获得锁，此时已经无法再判断list.size()>=0，而此时等于0
     * 就会产生数组越界，报错产生
     */
    public String pop() throws InterruptedException {
        synchronized (this) {
            if (list.size() <= 0) {
                wait();
            }
            return list.remove(list.size() - 1);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        final AlibabaStackDemo myStack = new AlibabaStackDemo();
        for (int i = 0; i < 100; i++) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        String str1 = myStack.pop();
                        System.out.println("pop:" + str1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }

        for (int i = 0; i < 100; i++) {
            final int finalI = i;
            new Thread() {
                @Override
                public void run() {
                    myStack.push("aabb" + finalI);
                }
            }.start();
        }

    }
}