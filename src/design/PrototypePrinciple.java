package design;

import java.util.HashMap;

public class PrototypePrinciple {

    public static void main(String[] args) {
        ShapeCache cache = new PrototypePrinciple().new ShapeCache();
        cache.loadCache();

        cache.show();
    }

    abstract class Shape implements Cloneable {
        String name;

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public Object clone() {
            Object obj = null;
            try {
                obj = super.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            return obj;
        }
    }

    class Circle extends Shape {
    }

    class Rectangular extends Shape {
    }

    class ShapeCache {
        private HashMap<String, Shape> map = new HashMap<>();

        public void loadCache() {
            Circle circle = new Circle();
            circle.setName("circle");
            map.put("circle", circle);

            Rectangular rectangular = new Rectangular();
            rectangular.setName("rectangular");
            map.put("rectangular", rectangular);
        }

        public HashMap<String, Shape> getCache() {
            return this.map;
        }

        public void show() {
            Circle circle = (Circle) map.get("circle");
            Rectangular rectangular = (Rectangular) map.get("rectangular");

            System.out.println("circle: " + circle.getName());
            System.out.println("rectangular: " + rectangular.getName());
        }
    }
}
