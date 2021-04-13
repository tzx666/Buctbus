package buct.tzx.buctbus.annotation;

import buct.tzx.buctbus.ThreadMode;

public @interface Subscribe {
    ThreadMode threadMode() default ThreadMode.DEFAULT;
    boolean isSticky() default false;
}
