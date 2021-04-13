package buct.tzx.buctbus.sender;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

import buct.tzx.buctbus.BuctBus;
import buct.tzx.buctbus.Subscription;
import buct.tzx.buctbus.service.Sender;

// 线性的连续后台post线程，非安卓的应用默认走此线程,和async的区别是：async会对任何新到的事件立刻新开线程执行
// 而后台线程会排队执行
public class BackgroundSender extends Sender.Stub implements Runnable {
    private Queue<Subscription> queue = new ConcurrentLinkedDeque<>();
    // 判断线程的启动状态
    private volatile boolean isRunning = false;

    @Override
    public void enqueue(Subscription subscription) {
        queue.offer(subscription);
        if (!isRunning) {
            isRunning = true;
            BuctBus.getInstance().getService().execute(this);
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                Subscription subscription = queue.poll();
                if (subscription == null) {
                    isRunning = false;
                    return;
                }
                BuctBus.getInstance().invoke(subscription);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            isRunning = false;
        }
    }
}
