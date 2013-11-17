package com.example.webapisample;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;

// Web APIの基本的な使い方 （1/3）：CodeZine <http://codezine.jp/article/detail/7276>
public class MainActivity extends Activity implements LoaderCallbacks<String>{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bundleにはパラメータを保存する
        Bundle bundle = new Bundle();

        // 株価予想のRSS
        bundle.putString("url", "http://info.finance.yahoo.co.jp/kabuyoso/rss/");

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
    public void onLoadFinished(Loader<String> loader, String xml) {

        // 実行結果の書き出し
        if ( loader.getId() == 0 ) {
            if (xml != null) {
                Log.d("ボディ",  xml);

                // xmlのパース
                // AndroidでXPath - マイペースなプログラミング日記 <http://d.hatena.ne.jp/d-kami/20110131/1296430088>
                try{
                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = dbFactory.newDocumentBuilder();
                    Document document = builder.parse(new ByteArrayInputStream(xml.getBytes()));

                    XPathFactory factory = XPathFactory.newInstance();
                    XPath xpath = factory.newXPath();

                    //全てのlinkノードを取り出す
                    NodeList textList = (NodeList)xpath.evaluate("/rss/channel/item/link", document, XPathConstants.NODESET);

                    //linkノードを１つずつ取り出して更にXPathでアクセスする
                    //ここではlinkからの相対パスとなっている
                    for(int i = 0; i < textList.getLength(); i++){
                        Node node = textList.item(i);

                        Log.d("link", xpath.evaluate("./text()", node));

                        sendNotification();
                    }

                } catch(Exception e) {
                    Log.e(this.getClass().getSimpleName(), e.getMessage());
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {
        // 今回は何も処理しない
    }

    // Notification
    // Android Tips #23 Android4.1 で追加された Notification のスタイルを使いこなす ｜ Developers.IO
    // <http://dev.classmethod.jp/smartphone/android/android-tips-23-android4-1-notification-style/>
    private void sendNotification() {
        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext());
        builder.setContentIntent(contentIntent);
        builder.setTicker("Ticker");
        builder.setContentTitle("ContentTitle");
        builder.setContentText("ContentText");
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setLargeIcon(largeIcon);
        builder.setWhen(System.currentTimeMillis());
        builder.setDefaults(Notification.DEFAULT_SOUND
                | Notification.DEFAULT_VIBRATE
                | Notification.DEFAULT_LIGHTS);
        builder.setAutoCancel(true);

        // BigTextStyle を適用
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle(builder);
        bigTextStyle.bigText("BigText");
        bigTextStyle.setBigContentTitle("BigContentTitle");
        bigTextStyle.setSummaryText("SummaryText");

        // NotificationManagerを取得
        NotificationManager manager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
        // Notificationを作成して通知
        manager.notify(0, bigTextStyle.build());
    }
}
