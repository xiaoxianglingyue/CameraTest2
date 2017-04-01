package com.example.cameratest;

import android.app.Application;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static java.security.AccessController.getContext;

/**
 * Created by fengyin on 16-8-26.
 */
public class AppApplication extends Application {

    public static int sScreenWidth = 1920;
    public static int sScreenHeight;

    @Override
    public void onCreate() {
        super.onCreate();

        WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        //sScreenWidth = point.x;
        //sScreenWidth /=2;
        sScreenHeight = point.y;
        if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            Log.i("Debug", "has camera");
        } else {
            Log.i("Debug", "no camera");
        }

        TelephonyManager manager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        Log.i("Debug", "sn:" + manager.getSimSerialNumber());

        try {
            Method method = Class.forName("android.os.SystemProperties").getMethod("get", String.class);
            String sn = (String) method.invoke(null, "ro.serialno");
            Log.i("Debug", "sn:" + sn);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }



}
