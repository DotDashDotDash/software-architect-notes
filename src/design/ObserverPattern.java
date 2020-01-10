package design;

import java.util.ArrayList;
import java.util.List;

public class ObserverPattern {

    public static void main(String[] args) {
        Subject subject = new ObserverPattern().new Subject();

        new ObserverPattern().new ObserverA(subject);
        new ObserverPattern().new ObserverB(subject);

        subject.setState(1);
    }

    class Subject {
        List<Observer> observers = new ArrayList<>();
        int state;

        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
            notifyAllObservers();
        }

        private void notifyAllObservers() {
            for (Observer o : observers) {
                o.update();
            }
        }

        public void attach(Observer observer) {
            this.observers.add(observer);
        }
    }

    abstract class Observer {
        protected Subject subject;

        abstract void update();
    }

    class ObserverA extends Observer {

        public ObserverA(Subject subject) {
            this.subject = subject;
            subject.attach(this);
        }

        @Override
        public void update() {
            System.out.println("observer A: " + subject.getState());
        }
    }

    class ObserverB extends Observer {

        public ObserverB(Subject subject) {
            this.subject = subject;
            subject.attach(this);
        }

        @Override
        public void update() {
            System.out.println("observer B: " + subject.getState());
        }
    }
}
