package buct.tzx.buctbus;

import java.lang.reflect.Method;

public class SubscriberMethod {
    final Method method;
    final ThreadMode threadMode;
    final Class<?> type;
    // 判重
    String methodString;
    public SubscriberMethod(Method method,ThreadMode threadMode,Class<?> type){
        this.method = method;
        this.threadMode = threadMode;
        this.type = type;
    }
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        } else if (other instanceof SubscriberMethod) {
            checkMethodString();
            SubscriberMethod otherSubscriberMethod = (SubscriberMethod)other;
            otherSubscriberMethod.checkMethodString();
            return methodString.equals(otherSubscriberMethod.methodString);
        } else {
            return false;
        }
    }

    private synchronized void checkMethodString() {
        if (methodString == null) {
            StringBuilder builder = new StringBuilder(64);
            builder.append(method.getDeclaringClass().getName());
            builder.append('#').append(method.getName());
            builder.append('(').append(type.getName());
            methodString = builder.toString();
        }
    }

    @Override
    public int hashCode() {
        return method.hashCode();
    }
}

