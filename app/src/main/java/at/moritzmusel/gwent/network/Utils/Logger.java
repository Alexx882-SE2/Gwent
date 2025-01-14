package at.moritzmusel.gwent.network.Utils;

import android.util.Log;

public class Logger {
    public static void i(String TAG, String message){
        Log.i(TAG, message);
    }
    public static void v(String TAG, String message){
        Log.v(TAG, message);
    }
    public static void w(String TAG, String message){
        Log.w(TAG, message);
    }
    public static void e(String TAG, String message){
        Log.e(TAG, message);
    }
    public static void d(String TAG, String message){
        Log.d(TAG, message);
    }
    public static void wtf(String TAG, String message){
        Log.wtf(TAG, message);
    }
}
