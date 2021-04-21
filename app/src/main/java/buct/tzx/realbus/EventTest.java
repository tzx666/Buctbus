package buct.tzx.realbus;

import buct.tzx.buctbus.BuctBus;
import buct.tzx.realbus.event.AsyncEvent;
import buct.tzx.realbus.event.BackgroundEvent;
import buct.tzx.realbus.event.BaseEvent;
import buct.tzx.realbus.event.MainEvent;

public class EventTest {
    public EventTest(){
        BuctBus.getInstance().register(this);
    }
    public void postEvent(){
        BuctBus.getInstance().post(new BaseEvent("777"));
    }
    public void postMainEvent(){
        BuctBus.getInstance().post(new MainEvent("777"));
    }
    public void postAsyncEvent(){
        BuctBus.getInstance().post(new AsyncEvent("777"));
    }
    public void postBackEvent(){
        BuctBus.getInstance().post(new BackgroundEvent("777"));
    }
    public void unregister(){
        BuctBus.getInstance().unregister(this);
    }
}
