package com.example.webapisample;

import android.content.Context;
import android.util.Log;

/**
 * 端末起動時の処理。
 * @author id:language_and_engineering
 *
 */
public class OnBootReceiver extends BaseOnBootReceiver
{
    @Override
    protected void onDeviceBoot(Context context)
    {
        Log.d("service", "hoge");
        // サンプルのサービス常駐を開始
    }

}

