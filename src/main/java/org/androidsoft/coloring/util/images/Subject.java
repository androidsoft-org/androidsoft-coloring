package org.androidsoft.coloring.util.images;

import java.util.ArrayList;
import java.util.List;

/* See https://en.wikipedia.org/wiki/Observer_pattern
 *
 */
public class Subject {

    List<Observer> observers = new ArrayList<>();

    public interface Observer {
        void update();
    }

    public void attachObserver(Observer observer) {
        observers.add(observer);
    }

    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update();
        }
    }

}
