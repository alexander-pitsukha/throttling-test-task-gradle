package com.throttling.task;

import com.throttling.task.aspect.RateLimit;

public class TestClass {

    @RateLimit
    public String testMethod() {
        return "";
    }
}

