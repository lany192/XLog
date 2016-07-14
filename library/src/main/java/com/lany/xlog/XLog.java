package com.lany.xlog;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public final class XLog {
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final String NULL_TIPS = "Log with null object";
    private static final String mTag = "XLog";
    private static final int V = 0x1;
    private static final int D = 0x2;
    private static final int I = 0x3;
    private static final int W = 0x4;
    private static final int E = 0x5;
    private static final int A = 0x6;
    private static final int J = 0x7;//json
    private static final int X = 0x8;//xml
    private static boolean DEBUG = true;
    private static Context mContext;

    /**
     * init XLog
     *
     * @param application
     * @param debug
     */
    public static void init(Application application, boolean debug) {
        mContext = application.getApplicationContext();
        DEBUG = debug;
        deleteExpiredLogs(7);//7七天过期删除
    }

    public static void v(Object msg) {
        printLog(V, null, msg);
    }

    public static void v(String tag, Object... objects) {
        printLog(V, tag, objects);
    }

    public static void d(Object msg) {
        printLog(D, null, msg);
    }

    public static void d(String tag, Object... objects) {
        printLog(D, tag, objects);
    }

    public static void i(Object msg) {
        printLog(I, null, msg);
    }

    public static void i(String tag, Object... objects) {
        printLog(I, tag, objects);
    }

    public static void w(Object msg) {
        printLog(W, null, msg);
    }

    public static void w(String tag, Object... objects) {
        printLog(W, tag, objects);
    }

    public static void e(Object msg) {
        printLog(E, null, msg);
    }

    public static void e(String tag, Object... objects) {
        printLog(E, tag, objects);
    }

    public static void a(Object msg) {
        printLog(A, null, msg);
    }

    public static void a(String tag, Object... objects) {
        printLog(A, tag, objects);
    }

    public static void json(String jsonFormat) {
        printLog(J, null, jsonFormat);
    }

    public static void json(String tag, String jsonFormat) {
        printLog(J, tag, jsonFormat);
    }

    public static void xml(String xml) {
        printLog(X, null, xml);
    }

    public static void xml(String tag, String xml) {
        printLog(X, tag, xml);
    }

    public static void file(File targetDirectory, Object msg) {
        printFile(null, targetDirectory, null, msg);
    }

    public static void file(String tag, File targetDirectory, Object msg) {
        printFile(tag, targetDirectory, null, msg);
    }

    public static void file(String tag, File targetDirectory, String fileName, Object msg) {
        printFile(tag, targetDirectory, fileName, msg);
    }

    private static void printLog(int type, String tagStr, Object... objects) {
        if (!DEBUG) {
            return;
        }
        String[] contents = wrapperContent(tagStr, objects);
        String tag = contents[0];
        String msg = contents[1];
        String headString = contents[2];
        switch (type) {
            case V:
            case D:
            case I:
            case W:
            case E:
            case A:
                printDefault(type, tag, headString + msg);
                break;
            case J:
                printJson(tag, msg, headString);
                break;
            case X:
                printXml(tag, msg, headString);
                break;
        }
    }

    private static void printFile(String tagStr, File targetDirectory, String fileName, Object objectMsg) {
        if (!DEBUG) {
            return;
        }
        String[] contents = wrapperContent(tagStr, objectMsg);
        String tag = contents[0];
        String msg = contents[1];
        String headString = contents[2];
        printFile(tag, targetDirectory, fileName, headString, msg);
    }

    private static String[] wrapperContent(String tagStr, Object... objects) {
        final int STACK_TRACE_INDEX = 5;
        final String SUFFIX = ".java";
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StackTraceElement targetElement = stackTrace[STACK_TRACE_INDEX];
        String className = targetElement.getClassName();
        String[] classNameInfo = className.split("\\.");
        if (classNameInfo.length > 0) {
            className = classNameInfo[classNameInfo.length - 1] + SUFFIX;
        }
        if (className.contains("$")) {
            className = className.split("\\$")[0] + SUFFIX;
        }
        String methodName = targetElement.getMethodName();
        int lineNumber = targetElement.getLineNumber();
        if (lineNumber < 0) {
            lineNumber = 0;
        }
        String methodNameShort = methodName.substring(0, 1).toUpperCase() + methodName.substring(1);
        String tag = (tagStr == null ? className : tagStr);
        if (TextUtils.isEmpty(tag)) {
            tag = mTag;
        }
        String msg = (objects == null) ? NULL_TIPS : getObjectsString(objects);
        String headString = "[ (" + className + ":" + lineNumber + ")#" + methodNameShort + " ] ";
        return new String[]{tag, msg, headString};
    }

    private static String getObjectsString(Object... objects) {
        if (objects.length > 1) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("\n");
            for (int i = 0; i < objects.length; i++) {
                Object object = objects[i];
                if (object == null) {
                    stringBuilder.append("Param").append("[").append(i).append("]").append(" = ").append("null").append("\n");
                } else {
                    stringBuilder.append("Param").append("[").append(i).append("]").append(" = ").append(object.toString()).append("\n");
                }
            }
            return stringBuilder.toString();
        } else {
            Object object = objects[0];
            return object == null ? "null" : object.toString();
        }
    }

    private static void printDefault(int type, String tag, String msg) {
        int index = 0;
        int maxLength = 4000;
        int countOfSub = msg.length() / maxLength;
        if (countOfSub > 0) {
            for (int i = 0; i < countOfSub; i++) {
                String sub = msg.substring(index, index + maxLength);
                print(type, tag, sub);
                index += maxLength;
            }
            print(type, tag, msg.substring(index, msg.length()));
        } else {
            print(type, tag, msg);
        }
    }

    private static void print(int type, String tag, String msg) {
        switch (type) {
            case V:
                log2File("V", tag, msg, null);
                Log.v(tag, msg);
                break;
            case D:
                log2File("D", tag, msg, null);
                Log.d(tag, msg);
                break;
            case I:
                log2File("I", tag, msg, null);
                Log.i(tag, msg);
                break;
            case W:
                log2File("W", tag, msg, null);
                Log.w(tag, msg);
                break;
            case E:
                log2File("E", tag, msg, null);
                Log.e(tag, msg);
                break;
            case A:
                log2File("A", tag, msg, null);
                Log.wtf(tag, msg);
                break;
        }
    }

    private static void printJson(String tag, String msg, String headString) {
        String message;
        try {
            final int JSON_INDENT = 4;
            if (msg.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(msg);
                message = jsonObject.toString(JSON_INDENT);
            } else if (msg.startsWith("[")) {
                JSONArray jsonArray = new JSONArray(msg);
                message = jsonArray.toString(JSON_INDENT);
            } else {
                message = msg;
            }
        } catch (JSONException e) {
            message = msg;
        }
        Log.d(tag, "╔═══════════════════════════════════════════════════════════════════════════════════════");
        message = headString + LINE_SEPARATOR + message;
        String[] lines = message.split(LINE_SEPARATOR);
        for (String line : lines) {
            Log.d(tag, "║ " + line);
        }
        Log.d(tag, "╚═══════════════════════════════════════════════════════════════════════════════════════");
    }

    private static void printXml(String tag, String xml, String headString) {
        if (xml != null) {
            xml = formatXML(xml);
            xml = headString + "\n" + xml;
        } else {
            xml = headString + NULL_TIPS;
        }
        Log.d(tag, "╔═══════════════════════════════════════════════════════════════════════════════════════");
        String[] lines = xml.split(LINE_SEPARATOR);
        for (String line : lines) {
            if (!(TextUtils.isEmpty(line) || line.equals("\n") || line.equals("\t") || TextUtils.isEmpty(line.trim()))) {
                Log.d(tag, "║ " + line);
            }
        }
        Log.d(tag, "╚═══════════════════════════════════════════════════════════════════════════════════════");
    }

    private static String formatXML(String inputXML) {
        try {
            Source xmlInput = new StreamSource(new StringReader(inputXML));
            StreamResult xmlOutput = new StreamResult(new StringWriter());
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(xmlInput, xmlOutput);
            return xmlOutput.getWriter().toString().replaceFirst(">", ">\n");
        } catch (Exception e) {
            e.printStackTrace();
            return inputXML;
        }
    }

    private static void printFile(String tag, File targetDirectory, String fileName, String headString, String msg) {
        fileName = (fileName == null) ? getLogFileName(new Date()) : fileName;
        if (save(targetDirectory, fileName, msg)) {
            Log.d(tag, headString + " save log success ! location is >>>" + targetDirectory.getAbsolutePath() + "/" + fileName);
        } else {
            Log.e(tag, headString + " save log fails !");
        }
    }

    private static boolean save(File dic, String fileName, String msg) {
        File file = new File(dic, fileName);
        try {
            OutputStream outputStream = new FileOutputStream(file);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-8");
            outputStreamWriter.write(msg);
            outputStreamWriter.flush();
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static String getLogFileName(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(
                "yyyy-MM-dd", Locale.getDefault());
        return mTag + sdf.format(date) + ".txt";
    }

    private static Context getAppContext() {
        if (mContext == null) {
            throw new IllegalArgumentException("XLog has not been initialized");
        }
        return mContext;
    }

    private static synchronized void log2File(String level, String tag,
                                              String msg, Throwable tr) {
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(
                "HH:mm:ss", Locale.getDefault());
        String fileName = getLogFileName(now);
        FileOutputStream outputStream = null;
        try {
            outputStream = getAppContext().openFileOutput(fileName,
                    Context.MODE_PRIVATE | Context.MODE_APPEND);
            StringBuilder sb = new StringBuilder();
            sb.append(level);
            sb.append(" ");
            sb.append(sdf.format(now));
            sb.append(" ");
            sb.append(tag);
            sb.append(" ");
            sb.append(msg);
            if (tr != null) {
                sb.append("\n");
                StringWriter sw = new StringWriter();
                tr.printStackTrace(new PrintWriter(sw, true));
                sb.append(sw.toString());
            }
            sb.append("\n");
            outputStream.write(sb.toString().getBytes());
            outputStream.flush();
        } catch (Exception e) {
            Log.e(mTag, "添加日志异常", e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    Log.e(mTag, "关闭日志文件异常", e);
                }
            }
        }
    }

    /**
     * 获取指定日期日志
     *
     * @param date
     * @return
     */
    public static synchronized String getLogText(Date date) {
        String fileName = getLogFileName(date);
        FileInputStream inputStream = null;
        StringBuilder sb = new StringBuilder();
        try {
            inputStream = getAppContext().openFileInput(fileName);
            byte[] buffer = new byte[10240];
            while (true) {
                int len = inputStream.read(buffer);
                if (len <= 0) {
                    break;
                }
                sb.append(new String(buffer, 0, len));
            }
            inputStream.close();
        } catch (FileNotFoundException e) {
            return "未找到对应的日志文件";
        } catch (Exception e) {
            Log.e(mTag, "获取日志文本异常", e);
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw, true));
            return sw.toString();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e(mTag, "获取日志文本异常", e);
                }
            }
        }
        return sb.toString();
    }

    /**
     * 清除指定日志日志文件
     *
     * @param date
     */
    public static synchronized void clearLogText(Date date) {
        String fileName = getLogFileName(date);
        FileOutputStream outputStream = null;
        try {
            outputStream = getAppContext().openFileOutput(fileName,
                    Context.MODE_PRIVATE);
            outputStream.write("".getBytes());
            outputStream.flush();
        } catch (Exception e) {
            Log.e(mTag, "clear log file error", e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    Log.e(mTag, "close log file error", e);
                }
            }
        }
    }

    private static void deleteExpiredLogs(int expiredDays) {
        File dir = getAppContext().getFilesDir();
        File[] subFiles = dir.listFiles();
        if (subFiles != null) {
            int logFileCnt = 0;
            int expiredLogFileCnt = 0;
            final int DAY_MILLISECONDS = 24 * 60 * 60 * 1000;//one day
            long expiredTimeMillis = System.currentTimeMillis()
                    - (expiredDays * DAY_MILLISECONDS);
            for (File file : subFiles) {
                if (file.getName().startsWith(mTag)) {
                    ++logFileCnt;
                    if (file.lastModified() < expiredTimeMillis) {
                        ++expiredLogFileCnt;
                        boolean deleteResult = file.delete();
                        if (deleteResult) {
                            i(mTag, "Delete expired log files successfully:" + file.getName());
                        } else {
                            e(mTag, "Delete expired log files failure:" + file.getName());
                        }
                    }
                }
            }
            Log.i(mTag, "删除过期日志:文件总数=" + (subFiles.length) + ", 日志文件数=" + logFileCnt
                    + ", 过期日志文件数=" + expiredLogFileCnt);
        }
    }
}