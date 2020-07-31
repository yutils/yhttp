package com.yutils.http;

import com.yutils.http.contract.YRun;

/**
 * Runnable线程，里面包含try，解决Runnable.run包含try过于复杂问题
 */
public class YRunnable implements Runnable {
    private final YRun yRun;

    YRunnable(YRun yRun) {
        this.yRun = yRun;
    }

    @Override
    public void run() {
        if (yRun != null) {
            try {
                yRun.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}