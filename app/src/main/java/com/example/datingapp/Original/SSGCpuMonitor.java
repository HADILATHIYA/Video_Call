package com.example.datingapp.Original;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build.VERSION;
import android.os.SystemClock;
import android.util.Log;

import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class SSGCpuMonitor {
    private static final int CPU_STAT_LOG_PERIOD_MS = 6000;
    private static final int CPU_STAT_SAMPLE_PERIOD_MS = 2000;
    private static final int MOVING_AVERAGE_SAMPLES = 5;
    private static final String TAG = "RIVCCpuMonitor";
    private final Context np_appContext;
    private final MovingAverage np_frequencyScale;
    private final MovingAverage np_systemCpuUsage;
    private final MovingAverage np_totalCpuUsage;
    private final MovingAverage np_userCpuUsage;
    private int np_actualCpusPresent;
    private long[] np_cpuFreqMax;
    private boolean np_cpuOveruse;
    private int np_cpusPresent;
    private double[] np_curFreqScales;
    private String[] np_curPath;
    private ScheduledExecutorService np_executor;
    private boolean np_initialized;
    private ProcStat np_lastProcStat;
    private long np_lastStatLogTimeMs;
    private String[] np_maxPath;

    public SSGCpuMonitor(Context context) {
        if (isSupported()) {
            this.np_appContext = context.getApplicationContext();
            this.np_userCpuUsage = new MovingAverage(5);
            this.np_systemCpuUsage = new MovingAverage(5);
            this.np_totalCpuUsage = new MovingAverage(5);
            this.np_frequencyScale = new MovingAverage(5);
            this.np_lastStatLogTimeMs = SystemClock.elapsedRealtime();
            scheduleCpuUtilizationTask();
            return;
        }
        throw new RuntimeException("RIVCCpuMonitor is not supported on this Android version.");
    }

    public static boolean isSupported() {
        return VERSION.SDK_INT >= 19 && VERSION.SDK_INT < 24;
    }

    private static long parseLong(String str) {
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            Log.e(TAG, "parseLong error.", e);
            return 0;
        }
    }

    private int doubleToPercent(double d) {
        return (int) ((100.0d * d) + 0.5d);
    }

    public void pause() {
        ScheduledExecutorService scheduledExecutorService = this.np_executor;
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdownNow();
            this.np_executor = null;
        }
    }

    public synchronized int getCpuUsageCurrent() {
        return doubleToPercent(this.np_userCpuUsage.getCurrent() + this.np_systemCpuUsage.getCurrent());
    }

    public synchronized int getCpuUsageAverage() {
        return doubleToPercent(this.np_userCpuUsage.getAverage() + this.np_systemCpuUsage.getAverage());
    }

    public synchronized int getFrequencyScaleAverage() {
        return doubleToPercent(this.np_frequencyScale.getAverage());
    }

    private void scheduleCpuUtilizationTask() {
        ScheduledExecutorService scheduledExecutorService = this.np_executor;
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdownNow();
            this.np_executor = null;
        }
        ScheduledExecutorService newSingleThreadScheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        this.np_executor = newSingleThreadScheduledExecutor;
        newSingleThreadScheduledExecutor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                SSGCpuMonitor.this.cpuUtilizationTask();
            }
        }, 0, 2000, TimeUnit.MILLISECONDS);
    }

    public void cpuUtilizationTask() {
        try {
            if (sampleCpuUtilization() && SystemClock.elapsedRealtime() - this.np_lastStatLogTimeMs >= 6000) {
                this.np_lastStatLogTimeMs = SystemClock.elapsedRealtime();
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    private void init() {
        BufferedReader bufferedReader = null;
        String str = "/sys/devices/system/cpu/cpu";
        try {
            FileInputStream fileInputStream = new FileInputStream("/sys/devices/system/cpu/present");
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, Charset.forName("UTF-8"));
            try {
                bufferedReader = new BufferedReader(inputStreamReader);
            } catch (Throwable th11) {
                Object obj = th11;
            }
            try {
                Scanner useDelimiter = new Scanner(bufferedReader).useDelimiter("[-\n]");
                try {
                    useDelimiter.nextInt();
                    this.np_cpusPresent = useDelimiter.nextInt() + 1;
                    useDelimiter.close();
                    if (useDelimiter != null) {
                        useDelimiter.close();
                    }
                    bufferedReader.close();
                    inputStreamReader.close();
                    fileInputStream.close();
                    int i = this.np_cpusPresent;
                    this.np_cpuFreqMax = new long[i];
                    this.np_maxPath = new String[i];
                    this.np_curPath = new String[i];
                    this.np_curFreqScales = new double[i];
                    for (int i2 = 0; i2 < this.np_cpusPresent; i2++) {
                        this.np_cpuFreqMax[i2] = 0;
                        this.np_curFreqScales[i2] = 0.0d;
                        String[] strArr = this.np_maxPath;
                        StringBuilder sb = new StringBuilder();
                        sb.append(str);
                        sb.append(i2);
                        sb.append("/cpufreq/cpuinfo_max_freq");
                        strArr[i2] = sb.toString();
                        String[] strArr2 = this.np_curPath;
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append(str);
                        sb2.append(i2);
                        sb2.append("/cpufreq/scaling_cur_freq");
                        strArr2[i2] = sb2.toString();
                    }
                    ProcStat procStat = new ProcStat(0, 0, 0);
                    this.np_lastProcStat = procStat;
                    resetStat();
                    this.np_initialized = true;
                } catch (Throwable th) {
                }
            } catch (Throwable th9) {
                Object obj2 = th9;
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Cannot do CPU stats since /sys/devices/system/cpu/present is missing");
        } catch (Throwable th2) {
        }
    }

    private synchronized void resetStat() {
        this.np_userCpuUsage.reset();
        this.np_systemCpuUsage.reset();
        this.np_totalCpuUsage.reset();
        this.np_frequencyScale.reset();
        this.np_lastStatLogTimeMs = SystemClock.elapsedRealtime();
    }

    private int getBatteryLevel() {
        Intent registerReceiver = this.np_appContext.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        int intExtra = registerReceiver.getIntExtra("scale", 100);
        if (intExtra > 0) {
            return (int) ((((float) registerReceiver.getIntExtra("level", 0)) * 100.0f) / ((float) intExtra));
        }
        return 0;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:101:0x0175, code lost:
        r0 = th;
     */
    private synchronized boolean sampleCpuUtilization() throws Throwable {
        double d5;
        long j;
        synchronized (this) {
            try {
                synchronized (this) {
                    try {
                        if (!this.np_initialized) {
                            init();
                        }
                        if (this.np_cpusPresent == 0) {
                            return false;
                        }
                        this.np_actualCpusPresent = 0;
                        long j2 = 0;
                        long j22 = 0;
                        long j3 = 0;
                        for (int i = 0; i < this.np_cpusPresent; i++) {
                            this.np_curFreqScales[i] = 0.0d;
                            long[] jArr = this.np_cpuFreqMax;
                            if (jArr[i] == 0) {
                                long readFreqFromFile = readFreqFromFile(this.np_maxPath[i]);
                                if (readFreqFromFile > 0) {
                                    this.np_cpuFreqMax[i] = readFreqFromFile;
                                    this.np_maxPath[i] = null;
                                    j3 = readFreqFromFile;
                                }
                            } else {
                                j3 = jArr[i];
                            }
                            long readFreqFromFile2 = readFreqFromFile(this.np_curPath[i]);
                            if (readFreqFromFile2 != 0 || j3 != 0) {
                                if (readFreqFromFile2 > 0) {
                                    this.np_actualCpusPresent++;
                                }
                                long j4 = j2 + readFreqFromFile2;
                                j22 += j3;
                                if (j3 > 0) {
                                    double[] dArr = this.np_curFreqScales;
                                    double d = (double) readFreqFromFile2;
                                    j = j4;
                                    double d2 = (double) j3;
                                    Double.isNaN(d);
                                    Double.isNaN(d2);
                                    Double.isNaN(d);
                                    Double.isNaN(d2);
                                    dArr[i] = d / d2;
                                } else {
                                    j = j4;
                                }
                                j2 = j;
                            }
                        }
                        if (j2 == 0) {
                            long j5 = j2;
                            long j6 = j22;
                            long j7 = j3;
                        } else if (j22 == 0) {
                            long j8 = j2;
                            long j9 = j22;
                            long j10 = j3;
                        } else {
                            double d3 = (double) j2;
                            double d4 = (double) j22;
                            Double.isNaN(d3);
                            Double.isNaN(d4);
                            Double.isNaN(d3);
                            Double.isNaN(d4);
                            double d52 = d3 / d4;
                            if (this.np_frequencyScale.getCurrent() > 0.0d) {
                                d5 = (this.np_frequencyScale.getCurrent() + d52) * 0.5d;
                            } else {
                                d5 = d52;
                            }
                            ProcStat readProcStat = readProcStat();
                            if (readProcStat == null) {
                                return false;
                            }
                            try {
                                try {
                                    long j11 = j2;
                                    long j42 = readProcStat.userTime - this.np_lastProcStat.userTime;
                                    long j12 = j22;
                                    long j52 = readProcStat.systemTime - this.np_lastProcStat.systemTime;
                                    long j13 = j3;
                                    double d6 = d4;
                                    long j62 = j42 + j52 + (readProcStat.idleTime - this.np_lastProcStat.idleTime);
                                    double d53 = d5;
                                    if (d53 == 0.0d) {
                                        long j14 = j52;
                                        long j15 = j62;
                                    } else if (j62 != 0) {
                                        this.np_frequencyScale.addValue(d53);
                                        double d62 = (double) j42;
                                        long j16 = j42;
                                        double d7 = (double) j62;
                                        Double.isNaN(d62);
                                        Double.isNaN(d7);
                                        Double.isNaN(d62);
                                        Double.isNaN(d7);
                                        long j17 = j62;
                                        double d8 = d62 / d7;
                                        this.np_userCpuUsage.addValue(d8);
                                        double d9 = d62;
                                        double d92 = (double) j52;
                                        Double.isNaN(d92);
                                        Double.isNaN(d7);
                                        Double.isNaN(d92);
                                        Double.isNaN(d7);
                                        long j18 = j52;
                                        double d10 = d92 / d7;
                                        this.np_systemCpuUsage.addValue(d10);
                                        double d11 = d7;
                                        this.np_totalCpuUsage.addValue((d8 + d10) * d53);
                                        this.np_lastProcStat = readProcStat;
                                        return true;
                                    } else {
                                        long j19 = j52;
                                        long j20 = j62;
                                    }
                                    return false;
                                } catch (Throwable th) {
                                    th = th;
                                    while (true) {
                                        try {
                                            break;
                                        } catch (Throwable th2) {
                                            th = th2;
                                        }
                                    }
                                    throw th;
                                }
                            } catch (Throwable th3) {

                                while (true) {
                                    break;
                                }
                                throw th3;
                            }
                        }
                        Log.e(TAG, "Could not read max or current frequency for any CPU");
                        return false;
                    } catch (Throwable th4) {

                        while (true) {
                            break;
                        }
                        throw th4;
                    }
                }
            } catch (Throwable th5) {

                throw th5;
            }
        }
    }

    private synchronized String getStatString() {
        StringBuilder sb;
        sb = new StringBuilder();
        sb.append("CPU User: ");
        sb.append(doubleToPercent(this.np_userCpuUsage.getCurrent()));
        sb.append("/");
        sb.append(doubleToPercent(this.np_userCpuUsage.getAverage()));
        sb.append(". System: ");
        sb.append(doubleToPercent(this.np_systemCpuUsage.getCurrent()));
        sb.append("/");
        sb.append(doubleToPercent(this.np_systemCpuUsage.getAverage()));
        sb.append(". Freq: ");
        sb.append(doubleToPercent(this.np_frequencyScale.getCurrent()));
        sb.append("/");
        sb.append(doubleToPercent(this.np_frequencyScale.getAverage()));
        sb.append(". Total usage: ");
        sb.append(doubleToPercent(this.np_totalCpuUsage.getCurrent()));
        sb.append("/");
        sb.append(doubleToPercent(this.np_totalCpuUsage.getAverage()));
        sb.append(". Cores: ");
        sb.append(this.np_actualCpusPresent);
        sb.append("( ");
        for (int i = 0; i < this.np_cpusPresent; i++) {
            sb.append(doubleToPercent(this.np_curFreqScales[i]));
            sb.append(MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR);
        }
        sb.append("). Battery: ");
        sb.append(getBatteryLevel());
        if (this.np_cpuOveruse) {
            sb.append(". Overuse.");
        }
        return sb.toString();
    }

    private long readFreqFromFile(String str) {
        long j = 0;
        try {
            FileInputStream fileInputStream = new FileInputStream(str);
            Throwable th = null;
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, Charset.forName("UTF-8"));
            try {
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                try {
                    j = parseLong(bufferedReader.readLine());
                } catch (Throwable th4) {
                    Object obj = th4;
                }
                bufferedReader.close();
                try {
                    inputStreamReader.close();
                    fileInputStream.close();
                    return j;
                } catch (Throwable th2) {
                    throw th;
                }
            } catch (Throwable th3) {
                throw th;
            }
        } catch (Throwable th5) {
            return j;
        }
    }

    private ProcStat readProcStat() {
        BufferedReader bufferedReader;
        long j;
        long j2;
        String str = TAG;
        try {
            FileInputStream fileInputStream = new FileInputStream("/proc/stat");
            try {
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, Charset.forName("UTF-8"));
                try {
                    bufferedReader = new BufferedReader(inputStreamReader);
                } catch (Throwable th) {
                    return null;
                }
                try {
                    String[] split = bufferedReader.readLine().split("\\s+");
                    int length = split.length;
                    long j3 = 0;
                    if (length >= 5) {
                        j3 = parseLong(split[1]) + parseLong(split[2]);
                        j2 = parseLong(split[3]);
                        j = parseLong(split[4]);
                    } else {
                        j2 = 0;
                        j = 0;
                    }
                    if (length >= 8) {
                        j3 += parseLong(split[5]);
                        j2 = parseLong(split[6]) + j2 + parseLong(split[7]);
                    }
                    long j4 = j3;
                    long j5 = j2;
                    bufferedReader.close();
                    inputStreamReader.close();
                    fileInputStream.close();
                    ProcStat procStat = new ProcStat(j4, j5, j);
                    return procStat;
                } catch (Throwable th2) {
                    return null;
                }
            } catch (Throwable th3) {
                return null;
            }
        } catch (FileNotFoundException e) {
            Log.e(str, "Cannot open /proc/stat for reading", e);
            return null;
        } catch (Exception e2) {
            Log.e(str, "Problems parsing /proc/stat", e2);
            return null;
        }
    }

    private static class MovingAverage {
        private final int np_size;
        private double[] np_circBuffer;
        private int np_circBufferIndex;
        private double np_currentValue;
        private double np_sum;

        public MovingAverage(int i) {
            if (i > 0) {
                this.np_size = i;
                this.np_circBuffer = new double[i];
                return;
            }
            throw new AssertionError("np_size value in MovingAverage ctor should be positive.");
        }

        public void reset() {
            Arrays.fill(this.np_circBuffer, 0.0d);
            this.np_circBufferIndex = 0;
            this.np_sum = 0.0d;
            this.np_currentValue = 0.0d;
        }

        public void addValue(double d) {
            double d2 = this.np_sum - this.np_circBuffer[this.np_circBufferIndex];
            this.np_sum = d2;
            double[] dArr = this.np_circBuffer;
            int i = this.np_circBufferIndex;
            int i2 = i + 1;
            this.np_circBufferIndex = i2;
            dArr[i] = d;
            this.np_currentValue = d;
            this.np_sum = d2 + d;
            if (i2 >= this.np_size) {
                this.np_circBufferIndex = 0;
            }
        }

        public double getCurrent() {
            return this.np_currentValue;
        }

        public double getAverage() {
            double d = this.np_sum;
            double d2 = (double) this.np_size;
            Double.isNaN(d2);
            Double.isNaN(d2);
            return d / d2;
        }
    }

    private static class ProcStat {
        final long idleTime;
        final long systemTime;
        final long userTime;

        ProcStat(long j, long j2, long j3) {
            this.userTime = j;
            this.systemTime = j2;
            this.idleTime = j3;
        }
    }
}
