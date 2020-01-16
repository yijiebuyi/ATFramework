package com.callme.platform.util.http;

public interface TaskHandler {

    boolean supportPause();

    boolean supportResume();

    boolean supportCancel();

    void pause();

    void resume();

    void cancel();

    boolean isPaused();

    boolean isCancelled();
}
