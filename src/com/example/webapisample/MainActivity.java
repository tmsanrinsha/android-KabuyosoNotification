package com.example.webapisample;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

// http://codezine.jp/article/detail/7276
public class MainActivity extends Activity implements LoaderCallbacks<String>{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bundleにはパラメータを保存する
        Bundle bundle = new Bundle();

        // 大阪府の天気予報を返すAPIのURLを格納する
        bundle.putString("url", "http://www.drk7.jp/weather/json/27.js");

        // LoaderManagerの初期化
        // 第一引数( int )             onCreateLoaderメソッドの第一引数に渡されます。
        // 第二引数( Bundle )          onCreateLoaderメソッドの第二引数に渡されます。
        // 第三引数 ( LoaderCallback ) LoaderCallbackインターフェースを継承したクラスを指定します
        getLoaderManager().initLoader(0, bundle, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }


    @Override
    public Loader<String> onCreateLoader(int id, Bundle bundle) {

        // HttpAsyncLoaderの生成
        HttpAsyncLoader loader = new HttpAsyncLoader(this, bundle.getString("url"));

        // Web APIの呼び出し
        loader.forceLoad();
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String body) {

        // 実行結果の書き出し
        if ( loader.getId() == 0 ) {
            if (body != null) {
                Log.d("ボディ",  body);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {
        // 今回は何も処理しない
    }
}
