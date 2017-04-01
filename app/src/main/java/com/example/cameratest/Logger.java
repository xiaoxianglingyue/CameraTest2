package com.example.cameratest;/**
 * Created by fengyin on 16/8/25.
 */

import android.util.Log;

/**
 * @author fengyin(email:594601408@qq.com)
 * @date 2016-08-25 14:06
 * @package com.example.cameratest
 * @description Logger
 * @params
 */
public class Logger {
    private static boolean sDebug = true;

    private static final String DEBUG_TAG = "Debug";

    private static Logger sInstance;

    private Logger(){

    }

    public static Logger getInstance(){
        if(sInstance == null){
            synchronized (Logger.class){
                if(sInstance == null){
                    sInstance = new Logger();
                }
            }
        }
        return sInstance;
    }

    public void logI(String msg){
        if(sDebug){
            Log.i(DEBUG_TAG, msg);
        }
    }
}
