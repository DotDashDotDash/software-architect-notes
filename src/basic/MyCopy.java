package basic;

import java.io.*;

public class MyCopy {
    private MyCopyEntity myCopyEntity = new MyCopyEntity();

    public static void main(String[] args) {
        MyCopy myCopy = new MyCopy();
        myCopy.deepCopy();
    }

    public void deepCopy() {
        try {
            //serialization
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(myCopyEntity);

            //deserialization
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            MyCopyEntity test = (MyCopyEntity) ois.readObject();

            //do check deep copy
            System.out.println(doCheckDeepCopy(test, myCopyEntity));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public boolean doCheckDeepCopy(MyCopyEntity m1, MyCopyEntity m2) {
        return (m1 != m2) && (m1.getMyReflection() != m2.getMyReflection());
    }
}
