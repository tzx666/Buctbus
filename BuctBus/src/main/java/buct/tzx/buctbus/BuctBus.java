/**
 * @autuor tianzexin
 * @descption 事件总线的实现方法
 * @time 2021.4.12
 */
package buct.tzx.buctbus;

import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import buct.tzx.buctbus.annotation.Subscribe;
import buct.tzx.buctbus.sender.AsyncSender;
import buct.tzx.buctbus.sender.BackgroundSender;
import buct.tzx.buctbus.sender.MainSender;
import buct.tzx.buctbus.service.MsgEmail;
import buct.tzx.buctbus.utils.Logger;

public class BuctBus {
    private volatile static BuctBus buctBus;
    // 一个缓存根据类找到对应方法的class，加快速度
    private ConcurrentHashMap<Object, CopyOnWriteArrayList<SubscriberMethod>> getMethodByClass;
    // 一个事件和对应类的索引
    private Map<Class<?>, CopyOnWriteArrayList<Subscription>> getClassByEventType;
    // 粘性事件
    private CopyOnWriteArrayList<Object> StickyEventList;
    // 异步和后台线程共用的线程池
    private ExecutorService service = Executors.newCachedThreadPool();

    private AsyncSender asyncSender;
    private MainSender mainSender;
    private BackgroundSender backgroundSender;
    private boolean isDebugMode;
    private boolean enablePostSticky;
    private boolean enableThread;
    private List<Long>costCount = new ArrayList<>();
    private ThreadLocal<MsgEmail> emailThreadLocal = new ThreadLocal<MsgEmail>() {
        @Override
        protected MsgEmail initialValue() {
            return new MsgEmail();
        }
    };
    private Map<Class<?>, Object> eventMap;

    private BuctBus() {
    }

    public ExecutorService getService() {
        return service;
    }

    public static BuctBus getInstance() {
        if (buctBus == null) {
            synchronized (BuctBus.class) {
                if (buctBus == null) {
                    buctBus = new BuctBus();
                }
            }
        }
        return buctBus;
    }

    public void init() {
        getMethodByClass = new ConcurrentHashMap<>();
        getClassByEventType = new ConcurrentHashMap<>();
        StickyEventList = new CopyOnWriteArrayList<>();
        eventMap = new ConcurrentHashMap<>();
        asyncSender = new AsyncSender();
        mainSender = new MainSender(Looper.getMainLooper());
        backgroundSender = new BackgroundSender();
    }

    /**
     * 事件的注册类，首先根据类找到对应的所有方法，然后判断方法是否有注解
     * 使用方式 RealBus.getInstance.register(this)
     */
    public long register(Object obj) {
        long cur =System.currentTimeMillis();
        //首先先把这个类的所有方法都找到并注册
        List<SubscriberMethod> methodMods = getMethodMods(obj);
        // 对所有找到对方法进行注册
        Logger.log("找到了" + methodMods.size() + "个方法");
        for (SubscriberMethod mod : methodMods) {
            subscribe(obj, mod);
        }
        costCount.add(System.currentTimeMillis()-cur);
        return System.currentTimeMillis()-cur;
    }

    public void unregister(Object obj) {
        CopyOnWriteArrayList<SubscriberMethod> methodMods = getMethodByClass.getOrDefault(obj, null);
        if (methodMods == null) return;
        for (SubscriberMethod mod : methodMods) {
            List<Subscription> subscribers = getClassByEventType.getOrDefault(mod.type, null);
            if (subscribers != null) {
                subscribers.removeIf(subscriber -> subscriber.subscriberMethod.equals(obj) && subscriber.subscriberMethod.type.equals(mod.type));
            }
        }
    }

    // 前置流程保证了这里一定不会空指针
    private void subscribe(Object obj, SubscriberMethod mod) {
        CopyOnWriteArrayList<Subscription> methodMods = getClassByEventType.getOrDefault(mod.type, null);
        if (methodMods == null) {
            methodMods = new CopyOnWriteArrayList<>();
            getClassByEventType.put(mod.type, methodMods);
        }
        Subscribe annotation = mod.method.getAnnotation(Subscribe.class);
        boolean isSticky = annotation.isSticky();
        Subscription subscriber = new Subscription(obj, mod);
        if (!methodMods.contains(subscriber)) {
            Logger.log("添加" + subscriber.subscriberMethod.method.getName());
            methodMods.add(subscriber);
        }
        if (isSticky) {
            for (Object event : StickyEventList) {
                Logger.log("比较粘性事件");
                Logger.log(event.getClass().getName());
                Logger.log(subscriber.subscriberMethod.type.getName());
                Logger.log("结果" + event.getClass().equals(subscriber.subscriberMethod.type));
                if (event.getClass().equals(subscriber.subscriberMethod.type)) {
                    // 在订阅时碰到粘性事件则直接执行
                    Logger.log("执行粘性事件");
                    post(event);
                }
            }
        }
    }

    public void post(Object obj) {
        MsgEmail email = emailThreadLocal.get();
        Queue<Object> eventList = email.getEventList();
        eventList.add(obj);
        eventMap.put(obj.getClass(), obj);
        while (!eventList.isEmpty()) {
            Object event = eventList.poll();
            Logger.log(event.getClass().getName());
            Class<?> type = event.getClass();
            // 将事件按线程分发
            List<Subscription> subscribers = getClassByEventType.getOrDefault(type, null);
            if (subscribers != null) {
                for (Subscription sub : subscribers) {
                    dispatchEventByThread(sub);
                }
            } else {
                Logger.log("empty");
            }
        }
    }

    public void invoke(Subscription subscriber) {
        Object event = eventMap.get(subscriber.subscriberMethod.type);
        invokeMethod(subscriber, event);
    }

    public void postSticky(Object event) {
        CheckOrAdd(event);
        post(event);
    }

    private void unRegesiterSticky(Object event) {
        StickyEventList.remove(event);
    }

    // 根据注解对注册类进行过滤，获得所有正常的方法,这里的obj表示一个事件
    // 这里不考虑自己订阅自己的阴间操作
    private List<SubscriberMethod> getMethodMods(Object obj) {
        CopyOnWriteArrayList<SubscriberMethod> methodMods = getMethodByClass.getOrDefault(obj, null);
        if (methodMods == null) {
            methodMods = new CopyOnWriteArrayList<>();
            getMethodByClass.put(obj, methodMods);
        }
        // getmethods速度可能有问题，需要做一个速度监控来保证速度
        Method[] methods = obj.getClass().getMethods();
        List<SubscriberMethod> list = new ArrayList<>();
        for (Method method : methods) {
            // 对每一个函数，判断其是否有注解以及是否有一个事件的参数
            if (isTargetMethod(method)) {
                // 构建方法包装类

                Class<?>[] methodTypes = method.getParameterTypes();
                Subscribe annotation = method.getAnnotation(Subscribe.class);
                SubscriberMethod methodMod = new SubscriberMethod(method, annotation.threadMode(), methodTypes[0]);
                // 构建订阅包装类
                list.add(methodMod);
                if (!methodMods.contains(methodMod)) {
                    methodMods.add(methodMod);
                }
            }
        }
        return list;
    }

    // 判断某个方法是否属于应该被订阅的方法
    private boolean isTargetMethod(Method method) {
        try {
            Class<?>[] methodTypes = method.getParameterTypes();
            if (methodTypes.length == 1) {
                Subscribe annotation = method.getAnnotation(Subscribe.class);
                return annotation != null;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    // 通过反射调用执行对应订阅类的方法
    private void invokeMethod(Subscription subscriber, Object event) {
        if (subscriber == null || event == null) {
            return;
        }
        Method targetMethod = subscriber.subscriberMethod.method;
        try {
            targetMethod.invoke(subscriber.subscriber, event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void CheckOrAdd(Object obj) {
        if (!StickyEventList.contains(obj)) {
            StickyEventList.add(obj);
            Logger.log("添加sticky" + obj.getClass().getName());
        }
    }

    private void dispatchEventByThread(Subscription subscription) {
        switch (subscription.subscriberMethod.threadMode) {
            case MAIN: {
                if (isMainThread()) {
                    invoke(subscription);
                } else {
                    mainSender.enqueue(subscription);
                }
                break;
            }
            case ASYNC: {
                asyncSender.enqueue(subscription);
                break;
            }
            case DEFAULT: {
                // 直接反射执行即可
                invoke(subscription);
                break;
            }
            case BACKGROUND: {
                if (isMainThread()) {
                    backgroundSender.enqueue(subscription);
                } else {
                    invoke(subscription);
                }
                break;
            }
            default:
                break;
        }
    }

    private boolean isMainThread() {
        return mainSender.isMainThread();
    }

    public static class builder {
        private boolean isDebugMode = false;
        private boolean enablePostSticky = true;
        private boolean enableThread = true;
        private ExecutorService service = null;

        public builder setDebugMode(boolean debugMode) {
            isDebugMode = debugMode;
            return this;
        }


        public builder setEnablePostSticky(boolean enablePostSticky) {
            this.enablePostSticky = enablePostSticky;
            return this;
        }

        public builder setEnableThread(boolean enableThread) {
            this.enableThread = enableThread;
            return this;
        }

        public builder setService(ExecutorService service) {
            this.service = service;
            return this;
        }

        public void build() {
            BuctBus.getInstance().init();
            buctBus.enablePostSticky = this.enablePostSticky;
            buctBus.enableThread = this.enableThread;
            buctBus.isDebugMode = this.isDebugMode;
            if (this.service != null) {
                buctBus.service = this.service;
            }
        }
    }
    public void printList(Context context){
        if(!isDebugMode){
            throw new IllegalStateException("no permission! open debug mode first!");
        }
        StringBuilder builder = new StringBuilder("");
        for(Map.Entry<Class<?>, CopyOnWriteArrayList<Subscription>>entry:getClassByEventType.entrySet()){
            builder.append(entry.getKey().getCanonicalName());
            builder.append("\n");
            for(Subscription sb:entry.getValue()){
                builder.append(sb.toString());
                builder.append("\n");
            }
        }
        Toast.makeText(context,builder.toString(),Toast.LENGTH_SHORT).show();
    }
}
