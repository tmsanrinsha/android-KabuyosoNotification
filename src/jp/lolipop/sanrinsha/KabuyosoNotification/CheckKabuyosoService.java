package jp.lolipop.sanrinsha.KabuyosoNotification;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import jp.lolipop.sanrinsha.KabuyosoNotification.R;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.AndroidHttpClient;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * 非同期に処理をするサービス
 * @see <a href="http://stackoverflow.com/questions/5682632/android-httpclient-as-a-backgroundservice">service - Android - httpclient as a backgroundservice - Stack Overflow</a>
 * @see <a href="http://d.hatena.ne.jp/language_and_engineering/20120724/AndroidAutoStartingResidentServiceBatch">Androidで，自動起動する常駐型サービスのサンプルコード　（アプリの裏側で定期的にバッチ処理） - 主に言語とシステム開発に関して</a>
 */
public class CheckKabuyosoService extends IntentService {

    /** 画面から常駐を解除したい場合のために，常駐インスタンスを保持 **/
    public static CheckKabuyosoService checkKabuyosoService;

    public CheckKabuyosoService() {
        // Need this to name the service
        super ("ConnectionServices");
    }

    @Override
    protected void onHandleIntent(Intent arg0) {
        checkKabuyosoService = this;

        // Do stuff that you want to happen asynchronously here

        // ---- RSSの取得 ----
        // 株価予想のRSS
        String url = "http://info.finance.yahoo.co.jp/kabuyoso/rss/";
        AndroidHttpClient httpClient = AndroidHttpClient.newInstance("Android UserAgent");

        String responseBody = "";
        try {
            responseBody = httpClient.execute(new HttpGet(url),

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
        }
        catch (ClientProtocolException e) {
            Log.e(this.getClass().getSimpleName(),e.getMessage());
        }
        catch (IOException e) {
            Log.e(this.getClass().getSimpleName(),e.getMessage());
        }
        finally {
            // 通信終了時は、接続を閉じる
            httpClient.getConnectionManager().shutdown();
        }

        Log.v("hoge", responseBody);

        /**
         * XPathを使ったxmlのパース
         * @see <a href="http://d.hatena.ne.jp/d-kami/20110131/1296430088">AndroidでXPath - マイペースなプログラミング日記</a>
         */
        try{
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbFactory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(responseBody.getBytes()));

            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();

            //全てのlinkノードを取り出す
            NodeList textList = (NodeList)xpath.evaluate("/rss/channel/item/link", document, XPathConstants.NODESET);

            //linkノードを１つずつ取り出して更にXPathでアクセスする
            //ここではlinkからの相対パスとなっている
            for(int i = 0; i < textList.getLength(); i++){
                Node node = textList.item(i);

                // Log.d("link", xpath.evaluate("./text()", node));

                if (i == 0) {
                    sendNotification();
                }
            }

        } catch(Exception e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
        }

        scheduleNextTime();
    }

    /**
     * Notification
     * @see <a href="http://dev.classmethod.jp/smartphone/android/android-tips-23-android4-1-notification-style/">Android Tips #23 Android4.1 で追加された Notification のスタイルを使いこなす ｜ Developers.IO</a>
     */
    private void sendNotification() {
        Intent intent = new Intent(getApplicationContext(), KabuyosoNotificationActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

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

    /**
     * サービスの次回の起動を予約
     */
    private void scheduleNextTime() {

        long now = System.currentTimeMillis();

        // アラームをセット
        PendingIntent alarmSender = PendingIntent.getService(
            this,
            0,
            new Intent(this, this.getClass()),
            0
        );
        AlarmManager am = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        am.set(
            AlarmManager.RTC,
            // now + getIntervalMS(),
            now + 10 * 1000,
            alarmSender
        );
        // 次回登録が完了

    }

    /**
     * もし起動していたら，常駐を解除する
     */
    public static void stopResidentIfActive(Context context) {
        if( checkKabuyosoService != null )
        {
            checkKabuyosoService.stopResident(context);
        }
    }

    /**
     * サービスの定期実行を解除し，サービスを停止
     */
    public void stopResident(Context context) {
        // サービス名を指定
        // Intent intent = new Intent(context, this.getClass());

        // アラームを解除
        PendingIntent pendingIntent = PendingIntent.getService(
            this,
            0, // ここを-1にすると解除に成功しない
            new Intent(this, this.getClass()),
            PendingIntent.FLAG_UPDATE_CURRENT
        );
        AlarmManager alarmManager = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        // @see http://creadorgranoeste.blogspot.com/2011/06/alarmmanager.html

        // サービス自体を停止
        stopSelf();
    }
}
