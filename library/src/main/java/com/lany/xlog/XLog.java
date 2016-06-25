package com.lany.xlog;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.UnknownHostException;

public final class XLog {
    private static String TAG = "XLog";
    private static boolean isDebug = true;

    private static int methodCount = 2;
    private static boolean showThreadInfo = false;
    private static int methodOffset = 0;

    private static final int DEBUG = 3;
    private static final int ERROR = 6;
    private static final int ASSERT = 7;
    private static final int INFO = 4;
    private static final int VERBOSE = 2;
    private static final int WARN = 5;

    /**
     * Android's max limit for a log entry is ~4076 bytes,
     * so 4000 bytes is used as chunk size since default charset
     * is UTF-8
     */
    private static final int CHUNK_SIZE = 4000;

    /**
     * The minimum stack trace index, starts at this class after two native calls.
     */
    private static final int MIN_STACK_OFFSET = 3;

    /**
     * Drawing toolbox
     */
    private static final char TOP_LEFT_CORNER = '╔';
    private static final char BOTTOM_LEFT_CORNER = '╚';
    private static final char MIDDLE_CORNER = '╟';
    private static final char HORIZONTAL_DOUBLE_LINE = '║';
    private static final String DOUBLE_DIVIDER = "════════════════════════════════════════════";
    private static final String SINGLE_DIVIDER = "────────────────────────────────────────────";
    private static final String TOP_BORDER = TOP_LEFT_CORNER + DOUBLE_DIVIDER + DOUBLE_DIVIDER;
    private static final String BOTTOM_BORDER = BOTTOM_LEFT_CORNER + DOUBLE_DIVIDER + DOUBLE_DIVIDER;
    private static final String MIDDLE_BORDER = MIDDLE_CORNER + SINGLE_DIVIDER + SINGLE_DIVIDER;

    /**
     * Localize single tag and method count for each thread
     */
    private static final ThreadLocal<String> localTag = new ThreadLocal<>();
    private static final ThreadLocal<Integer> localMethodCount = new ThreadLocal<>();

    public XLog() {
        initTag(TAG);
    }

    /**
     * It is used to change the tag
     *
     * @param tag1 is the given string which will be used in XLog
     */
    public static void initTag(String tag1) {
        if (tag1 == null) {
            throw new NullPointerException("tag may not be null");
        }
        if (tag1.trim().length() == 0) {
            throw new IllegalStateException("tag may not be empty");
        }
        TAG = tag1;
    }

    public XLog t(String tag, int methodCount) {
        if (tag != null) {
            localTag.set(tag);
        }
        localMethodCount.set(methodCount);
        return this;
    }

    public static void d(String message, Object... args) {
        log(DEBUG, message, args);
    }

    public static void e(String message, Object... args) {
        e(null, message, args);
    }

    public static void e(Throwable throwable, String message, Object... args) {
        if (throwable != null && message != null) {
            message += " : " + getStackTraceString(throwable);
        }
        if (throwable != null && message == null) {
            message = getStackTraceString(throwable);
        }
        if (message == null) {
            message = "No message/exception is set";
        }
        log(ERROR, message, args);
    }

    public static void w(String message, Object... args) {
        log(WARN, message, args);
    }

    public static void i(String message, Object... args) {
        log(INFO, message, args);
    }

    public static void v(String message, Object... args) {
        log(VERBOSE, message, args);
    }

    public static void wtf(String message, Object... args) {
        log(ASSERT, message, args);
    }

    /**
     * Formats the json content and print it
     *
     * @param json the json content
     */
    public static void json(String json) {
        if (TextUtils.isEmpty(json)) {
            d("Empty/Null json content");
            return;
        }
        try {
            int jsonIndent = 2;
            json = json.trim();
            if (json.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(json);
                String message = jsonObject.toString(jsonIndent);
                d(message);
                return;
            }
            if (json.startsWith("[")) {
                JSONArray jsonArray = new JSONArray(json);
                String message = jsonArray.toString(jsonIndent);
                d(message);
                return;
            }
            e("Invalid Json");
        } catch (JSONException e) {
            e("Invalid Json");
        }
    }

    private static String getStackTraceString(Throwable tr) {
        if (tr == null) {
            return "";
        }

        // This is to reduce the amount of log spew that apps do in the non-error
        // condition of the network being unavailable.
        Throwable t = tr;
        while (t != null) {
            if (t instanceof UnknownHostException) {
                return "";
            }
            t = t.getCause();
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        tr.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }

    public void clear() {
        reset();
    }

    /**
     * This method is synchronized in order to avoid messy of logs' order.
     */
    private static synchronized void log(int logType, String msg, Object... args) {
        String tag = getTag();
        String message = createMessage(msg, args);
        int methodCount = getMethodCount();

        if (TextUtils.isEmpty(message)) {
            message = "Empty/NULL log message";
        }

        logTopBorder(logType, tag);
        logHeaderContent(logType, tag, methodCount);

        //get bytes of message with system's default charset (which is UTF-8 for Android)
        byte[] bytes = message.getBytes();
        int length = bytes.length;
        if (length <= CHUNK_SIZE) {
            if (methodCount > 0) {
                logDivider(logType, tag);
            }
            logContent(logType, tag, message);
            logBottomBorder(logType, tag);
            return;
        }
        if (methodCount > 0) {
            logDivider(logType, tag);
        }
        for (int i = 0; i < length; i += CHUNK_SIZE) {
            int count = Math.min(length - i, CHUNK_SIZE);
            //create a new String with system's default charset (which is UTF-8 for Android)
            logContent(logType, tag, new String(bytes, i, count));
        }
        logBottomBorder(logType, tag);
    }

    private static void logTopBorder(int logType, String tag) {
        logChunk(logType, tag, TOP_BORDER);
    }

    private static void logHeaderContent(int logType, String tag, int methodCount) {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        if (showThreadInfo) {
            logChunk(logType, tag, HORIZONTAL_DOUBLE_LINE + " Thread: " + Thread.currentThread().getName());
            logDivider(logType, tag);
        }
        String level = "";

        int stackOffset = getStackOffset(trace) + methodOffset;

        //corresponding method count with the current stack may exceeds the stack trace. Trims the count
        if (methodCount + stackOffset > trace.length) {
            methodCount = trace.length - stackOffset - 1;
        }

        for (int i = methodCount; i > 0; i--) {
            int stackIndex = i + stackOffset;
            if (stackIndex >= trace.length) {
                continue;
            }
            StringBuilder builder = new StringBuilder();
            builder.append("║ ")
                    .append(level)
                    .append(getSimpleClassName(trace[stackIndex].getClassName()))
                    .append(".")
                    .append(trace[stackIndex].getMethodName())
                    .append(" ")
                    .append(" (")
                    .append(trace[stackIndex].getFileName())
                    .append(":")
                    .append(trace[stackIndex].getLineNumber())
                    .append(")");
            level += "   ";
            logChunk(logType, tag, builder.toString());
        }
    }

    private static void logBottomBorder(int logType, String tag) {
        logChunk(logType, tag, BOTTOM_BORDER);
    }

    private static void logDivider(int logType, String tag) {
        logChunk(logType, tag, MIDDLE_BORDER);
    }

    private static void logContent(int logType, String tag, String chunk) {
        String[] lines = chunk.split(System.getProperty("line.separator"));
        for (String line : lines) {
            logChunk(logType, tag, HORIZONTAL_DOUBLE_LINE + " " + line);
        }
    }

    private static void logChunk(int logType, String tag, String chunk) {
        String finalTag = formatTag(tag);
        switch (logType) {
            case ERROR:
                Log.e(finalTag, chunk);
                break;
            case INFO:
                Log.i(finalTag, chunk);
                break;
            case VERBOSE:
                Log.v(finalTag, chunk);
                break;
            case WARN:
                Log.w(finalTag, chunk);
                break;
            case ASSERT:
                Log.wtf(finalTag, chunk);
                break;
            case DEBUG:
                // Fall through, log debug by default
            default:
                Log.d(finalTag, chunk);
                break;
        }
    }

    private static String getSimpleClassName(String name) {
        int lastIndex = name.lastIndexOf(".");
        return name.substring(lastIndex + 1);
    }

    private static String formatTag(String tag1) {
        if (!TextUtils.isEmpty(tag1) && !TextUtils.equals(TAG, tag1)) {
            return TAG + "-" + tag1;
        }
        return TAG;
    }

    /**
     * @return the appropriate tag based on local or global
     */
    private static String getTag() {
        String tag1 = localTag.get();
        if (tag1 != null) {
            localTag.remove();
            return tag1;
        }
        return TAG;
    }

    private static String createMessage(String message, Object... args) {
        return args == null || args.length == 0 ? message : String.format(message, args);
    }

    private static int getMethodCount() {
        Integer count = localMethodCount.get();
        int result = methodCount;
        if (count != null) {
            localMethodCount.remove();
            result = count;
        }
        if (result < 0) {
            throw new IllegalStateException("methodCount cannot be negative");
        }
        return result;
    }

    /**
     * Determines the starting index of the stack trace, after method calls made by this class.
     *
     * @param trace the stack trace
     * @return the stack offset
     */
    private static int getStackOffset(StackTraceElement[] trace) {
        for (int i = MIN_STACK_OFFSET; i < trace.length; i++) {
            StackTraceElement e = trace[i];
            String name = e.getClassName();
            if (!name.equals(XLog.class.getName())) {
                return --i;
            }
        }
        return -1;
    }

    public void reset() {
        methodCount = 2;
        methodOffset = 0;
        showThreadInfo = true;
    }

    public static void setSettings(boolean isShowThreadInfo, int methodCount1, int offset) {
        showThreadInfo = isShowThreadInfo;
        if (methodCount1 < 0) {
            methodCount1 = 0;
        }
        methodCount = methodCount1;
        methodOffset = offset;
    }

}
