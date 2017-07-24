package com.tatait.tatamusic.application;

import android.util.Log;

import com.tatait.tatamusic.utils.FileUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 异常捕获
 * Created by Lynn on 2016/1/25.
 */
class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
    private static SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault());

    private Thread.UncaughtExceptionHandler mDefaultHandler;

    public static CrashHandler getInstance() {
        return SingletonHolder.instance;
    }

    private static class SingletonHolder {
        private static CrashHandler instance = new CrashHandler();
    }

    public void init() {
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        saveCrashInfo(ex);
        mDefaultHandler.uncaughtException(thread, ex);
    }

    private void saveCrashInfo(Throwable ex) {
        String stackTrace = Log.getStackTraceString(ex);
        Date date = new Date();
        String dateStr = DATE_FORMAT.format(date);
        String filePath = FileUtils.getLogDir() + String.format("log_%s.log", dateStr);
        String time = TIME_FORMAT.format(date);
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true));
            bw.write(String.format(Locale.getDefault(), "*** crash at %s *** ", time));
            bw.newLine();
            bw.write(stackTrace);
            bw.newLine();
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}