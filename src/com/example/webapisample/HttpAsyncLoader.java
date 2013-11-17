package com.example.webapisample;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.util.Log;

public class HttpAsyncLoader extends AsyncTaskLoader<String> {

    private String url = null; // WebAPIのURL
    private static final String TAG = "DEBUG";

    public HttpAsyncLoader(Context context, String url) {
        super(context);
        this.url = url;
    }

    @Override
    public String loadInBackground() {

        // HttpClient httpClient = new DefaultHttpClient();
        AndroidHttpClient httpClient = AndroidHttpClient.newInstance("Android UserAgent");
        httpClient.enableCurlLogging(TAG, Log.VERBOSE); // 何も出てこない
        DebugLogConfig.enable(); //出てくる

        try {
            String responseBody = httpClient.execute(new HttpGet(this.url),

                    // UTF-8でデコードするためhandleResponseをオーバーライドする
                    new ResponseHandler<String>() {

                        @Override
                        public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {

                            // レスポンスコードが、
                            // HttpStatus.SC_OK（HTTP 200）の場合のみ、結果を返す
                            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()){
                                return EntityUtils.toString(response.getEntity(), "UTF-8");
                            }
                            return null;
                        }
                    });

            return responseBody;
        }
        catch (Exception e) {
            Log.e(this.getClass().getSimpleName(),e.getMessage());
        }
        finally {
            // 通信終了時は、接続を閉じる
            httpClient.getConnectionManager().shutdown();
        }
        return null;
    }

}
