package com.lany.xlog;

public final class XLog {
    private static final String TAG = "XLog";

    private static Printer printer = new XLogPrinter();

    private XLog() {
    }

    public static Settings init() {
        return init(TAG);
    }

    public static Settings init(String tag) {
        printer = new XLogPrinter();
        return printer.init(tag);
    }

    public static void clear() {
        printer.clear();
    }

    public static Printer t(String tag) {
        return printer.t(tag, printer.getSettings().getMethodCount());
    }

    public static Printer t(int methodCount) {
        return printer.t(null, methodCount);
    }

    public static Printer t(String tag, int methodCount) {
        return printer.t(tag, methodCount);
    }

    public static void d(String message, Object... args) {
        printer.d(message, args);
    }

    public static void e(String message, Object... args) {
        printer.e(null, message, args);
    }

    public static void e(Throwable throwable, String message, Object... args) {
        printer.e(throwable, message, args);
    }

    public static void i(String message, Object... args) {
        printer.i(message, args);
    }

    public static void v(String message, Object... args) {
        printer.v(message, args);
    }

    public static void w(String message, Object... args) {
        printer.w(message, args);
    }

    public static void wtf(String message, Object... args) {
        printer.wtf(message, args);
    }

    public static void json(String json) {
        printer.json(json);
    }

}
