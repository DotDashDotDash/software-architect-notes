package design;

import java.util.ArrayList;
import java.util.List;

public class BuilderPrinciple {

    public static void main(String[] args) {
        Meal meal = new BuilderPrinciple().new Meal();
        meal.addItem(new BuilderPrinciple().new CNCake());
        meal.addItem(new BuilderPrinciple().new CNSuger());

        meal.showItems();
    }

    interface Packing {
        String packing();
    }

    interface Item {
        String name();

        String packing();

        float price();
    }

    class Wrapper implements Packing {
        @Override
        public String packing() {
            return "Wrapper";
        }
    }

    class Bottle implements Packing {
        @Override
        public String packing() {
            return "Bottle";
        }
    }

    abstract class Suger implements Item {
        @Override
        public String packing() {
            return new Wrapper().packing();
        }

        @Override
        public abstract float price();
    }

    abstract class Cake implements Item {
        @Override
        public String packing() {
            return new Bottle().packing();
        }

        @Override
        public abstract float price();
    }

    class CNSuger extends Suger {
        @Override
        public String name() {
            return "CNSuger";
        }

        @Override
        public float price() {
            return 50f;
        }
    }

    class CNCake extends Cake {
        @Override
        public float price() {
            return 70f;
        }

        @Override
        public String name() {
            return "CNCake";
        }


    }

    class Meal {
        List<Item> meal = new ArrayList<>();

        public void addItem(Item item) {
            meal.add(item);
        }

        public void showItems() {
            System.out.println("total price: " + getPrice());
            for (Item m : meal) {
                System.out.printf("name: %s, packing: %s, price: %f\n", m.name(), m.packing(), m.price());
            }
        }

        public float getPrice() {
            float result = 0f;
            for (Item m : meal) {
                result += m.price();
            }
            return result;
        }
    }

}
