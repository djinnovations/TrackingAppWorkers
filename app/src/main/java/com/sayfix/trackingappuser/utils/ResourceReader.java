package com.sayfix.trackingappuser.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

/**
 * Created by COMP on 2/9/2016.
 */
public class ResourceReader {

    public static final int CURRENT_API_LEVEL = Build.VERSION.SDK_INT;
    private static Context mContext;
    private static ResourceReader mResReader;

    public static ResourceReader getInstance(Context mContext) {

        if (mResReader == null){

            ResourceReader.mContext = mContext;
            mResReader = new ResourceReader();
        }
        return mResReader;
    }


    @TargetApi(Build.VERSION_CODES.M)
    public int getColorFromResource(int colorResId){

        if(CURRENT_API_LEVEL == 23)
            return mContext.getResources().getColor(colorResId, mContext.getTheme());
        else
            return mContext.getResources().getColor(colorResId);
    }
}
