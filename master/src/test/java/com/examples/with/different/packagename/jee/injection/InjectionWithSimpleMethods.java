package com.examples.with.different.packagename.jee.injection;

import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicLong;

public class InjectionWithSimpleMethods {

    private String aMessage = "Hi";
    private AtomicLong count = new AtomicLong(0);

    @Inject
    StringGetter stringGetter;

    public InjectionWithSimpleMethods() {
        super();
    }

    public long getCount() {
        return count.get();
    }

    public void setMessage(String message) {
        if (message != null && message.trim().length() > 0)
            aMessage = message;
    }

    public String getMessage() {
        return aMessage;
    }

    public String doSomethingWithInjected(String name) {
        count.incrementAndGet();
        return stringGetter.getA() + name;
    }

}
