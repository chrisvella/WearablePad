package jp.tkgktyk.wearablepadlib;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by tkgktyk on 2015/05/02.
 */
public abstract class BaseApplication extends Application {
    private static final String PREF_KEY_VERSION_NAME = "key_version_name";
    private static Context sContext;
    private static boolean DEBUG;

    private static String getMethodName() {
        String method = Thread.currentThread().getStackTrace()[4].getClassName();
        method += "#" + Thread.currentThread().getStackTrace()[4].getMethodName();
        method = method.substring(method.lastIndexOf(".") + 1);
        return method;
    }

    public static void logD(String text) {
        if (DEBUG) {
            Log.d(getMethodName(), text);
        }
    }

    public static void logD() {
        if (DEBUG) {
            Log.d("LogD", getMethodName());
        }
    }

    public static void logE(String text) {
        Log.e(getMethodName(), text);
    }

    public static void logE(Throwable t) {
        t.printStackTrace();
        Log.e(getMethodName(), t.toString());
    }

    public static void showToast(@StringRes int id) {
        showToast(sContext.getString(id));
    }

    public static void showToast(String text) {
        Toast.makeText(sContext, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreate() {
        sContext = this;
        DEBUG = isDebug();

        logD("check version");
        // get last running version
        MyVersion old = new MyVersion(PreferenceManager.getDefaultSharedPreferences(this)
                .getString(PREF_KEY_VERSION_NAME, ""));
        // save current version
        MyVersion current = new MyVersion(this);

        if (current.isNewerThan(old)) {
            logD("updated");
            onVersionUpdated(current, old);

            // reload preferences and put new version name
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            prefs.edit()
                    .putString(PREF_KEY_VERSION_NAME, current.toString())
                    .apply();
        }
        logD("start application");

        super.onCreate();
    }

    protected abstract boolean isDebug();

    protected abstract void onVersionUpdated(MyVersion next, MyVersion old);

    public class MyVersion {
        public static final int BASE = 1000;

        int major = 0;
        int minor = 0;
        int revision = 0;

        public MyVersion(String version) {
            set(version);
        }

        public MyVersion(Context context) {
            // set current package's version
            PackageManager pm = context.getPackageManager();
            String version = null;
            try {
                PackageInfo info = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
                version = info.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            if (version == null) {
                version = "0.0.0";
            }
            set(version);
        }

        public void set(String version) {
            if (TextUtils.isEmpty(version)) {
                return;
            }

            String[] v = version.split("\\.");
            int n = v.length;
            if (n >= 1) {
                major = Integer.parseInt(v[0]);
            }
            if (n >= 2) {
                minor = Integer.parseInt(v[1]);
            }
            if (n >= 3) {
                revision = Integer.parseInt(v[2]);
            }
        }

        public int toInt() {
            return major * BASE * BASE + minor * BASE + revision;
        }

        public boolean isNewerThan(MyVersion v) {
            return toInt() > v.toInt();
        }

        public boolean isNewerThan(String v) {
            return isNewerThan(new MyVersion(v));
        }

        public boolean isOlderThan(MyVersion v) {
            return toInt() < v.toInt();
        }

        public boolean isOlderThan(String v) {
            return isOlderThan(new MyVersion(v));
        }

        @Override
        public String toString() {
            return Integer.toString(major)
                    + "." + Integer.toString(minor)
                    + "." + Integer.toString(revision);
        }
    }
}
