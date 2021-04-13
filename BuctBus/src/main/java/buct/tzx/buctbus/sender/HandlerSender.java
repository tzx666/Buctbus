package buct.tzx.buctbus.sender;


import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

import buct.tzx.buctbus.BuctBus;
import buct.tzx.buctbus.Subscription;

// handler的代理类，将事件总线的消息全部发到handler上去执行
public class HandlerSender extends Handler {
    private Queue<Subscription> queue = new ConcurrentLinkedDeque<>();
    private boolean isHandlerRunning = false;

    public HandlerSender(Looper looper) {
        super(looper);
    }

    public void enqueue(Subscription subscription) {
        queue.offer(subscription);
        // 直接发送一个空消息，保证可以接入主线程的消息队列执行
        if (!isHandlerRunning) {
            isHandlerRunning = true;
            if (!sendMessage(obtainMessage())) {
                throw new IllegalStateException("handler 状态异常");
            }
        }
    }

    @Override
    public void handleMessage(Message msg) {
        try {
            Subscription subscription = queue.poll();
            if (subscription == null) {
                isHandlerRunning = false;
                return;
            }
            BuctBus.getInstance().invoke(subscription);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            isHandlerRunning = false;
        }
    }

    public boolean isMainThread() {
        return getLooper() == Looper.getMainLooper();
    }

}
