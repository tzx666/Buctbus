package buct.tzx.realbus.event;

public class BaseEvent {
    private   String str;
    public BaseEvent(String str){
        this.str = str;
    }

    public String getStr() {
        return str;
    }
}
