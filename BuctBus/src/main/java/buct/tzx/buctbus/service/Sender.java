package buct.tzx.buctbus.service;

import buct.tzx.buctbus.Subscription;

// 所有非默认线程外的其他线程必须实现此类
public interface Sender {
    void dispatch();

    void enqueue(Subscription subscription);

    class Stub implements Sender {

        @Override
        public void dispatch() {

        }

        @Override
        public void enqueue(Subscription subscription) {

        }
    }
}
