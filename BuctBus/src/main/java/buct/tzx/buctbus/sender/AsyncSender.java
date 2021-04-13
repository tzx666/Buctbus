package buct.tzx.buctbus.sender;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

import buct.tzx.buctbus.BuctBus;
import buct.tzx.buctbus.Subscription;
import buct.tzx.buctbus.service.Sender;

// 异步线程的执行，对每一个post到此线程到事件来讲，都将新建一个线程立刻执行
// 大量在此线程执行将造成大量资源占用！！！
// 对于线性的后台任务请走background模式或者谷歌官方的workmanager
public class AsyncSender extends Sender.Stub implements Runnable {
    private Queue<Subscription> queue = new ConcurrentLinkedDeque<>();


    @Override
    public void enqueue(Subscription subscription) {
        queue.offer(subscription);
        BuctBus.getInstance().getService().execute(this);
    }

    @Override
    public void run() {
        Subscription subscription = queue.poll();
        if (subscription == null) {
            throw new IllegalStateException("没有注册的事件被执行");
        }
        BuctBus.getInstance().invoke(subscription);
    }
}
