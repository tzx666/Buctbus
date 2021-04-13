package buct.tzx.buctbus.sender;

import android.os.Looper;

import buct.tzx.buctbus.Subscription;
import buct.tzx.buctbus.service.Sender;

// 这里使用一个委派代理模式，将事件全部抛给主线程对应的handler去执行
public class MainSender extends Sender.Stub {
    private HandlerSender MainThreadSender;
    private Looper looper;

    public MainSender(Looper looper){
        this.looper = looper;
        MainThreadSender = new HandlerSender(looper);
    }

    @Override
    public void enqueue(Subscription subscription) {
        MainThreadSender.enqueue(subscription);
    }

    public boolean isMainThread() {
        return MainThreadSender.isMainThread();
    }
}
