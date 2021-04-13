package buct.tzx.realbus;

import buct.tzx.buctbus.BuctBus;
import buct.tzx.realbus.event.BaseEvent;

public class EventTest {
    public EventTest(){
        BuctBus.getInstance().register(this);
    }
    public void postEvent(){
        BuctBus.getInstance().post(new BaseEvent("777"));
    }
    public void unregister(){
        BuctBus.getInstance().unregister(this);
    }
}
