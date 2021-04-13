package buct.tzx.buctbus.service;

import java.util.ArrayDeque;
import java.util.Queue;

import buct.tzx.buctbus.Subscription;

public class MsgEmail {
    private Subscription subscription;
    private Queue<Object> eventList = new ArrayDeque<>();

    public Queue<Object> getEventList() {
        return eventList;
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }

    public Subscription getSubscription() {
        return subscription;
    }
}
