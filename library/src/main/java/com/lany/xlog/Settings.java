package com.lany.xlog;

public final class Settings {
    private int methodCount = 2;
    private boolean showThreadInfo = false;
    private int methodOffset = 0;

    public Settings showThreadInfo() {
        showThreadInfo = true;
        return this;
    }

    public Settings methodCount(int methodCount) {
        if (methodCount < 0) {
            methodCount = 0;
        }
        this.methodCount = methodCount;
        return this;
    }

    public Settings methodOffset(int offset) {
        this.methodOffset = offset;
        return this;
    }

    public int getMethodCount() {
        return methodCount;
    }

    public boolean isShowThreadInfo() {
        return showThreadInfo;
    }

    public int getMethodOffset() {
        return methodOffset;
    }

    public void reset() {
        methodCount = 2;
        methodOffset = 0;
        showThreadInfo = true;
    }
}
