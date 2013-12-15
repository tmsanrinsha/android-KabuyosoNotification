package jp.lolipop.sanrinsha.KabuyosoNotification;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class CheckKabuyosoAlarm {

    static void setSchedule(Context context){
        // 次のサービスの実行時刻の決定
        Calendar cal = Calendar.getInstance();

        Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 8);
        startTime.set(Calendar.MINUTE, 50);

        Calendar endTime = Calendar.getInstance();
        endTime.set(Calendar.HOUR_OF_DAY, 9);
        endTime.set(Calendar.MINUTE, 10);

        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

        if (dayOfWeek >= 2 && dayOfWeek <= 6 && cal.after(startTime) && cal.before(endTime)) {
            // 平日の08:50-09:09なら1分後
            cal.add(Calendar.MINUTE, 1);
        } else if (dayOfWeek == 1 || dayOfWeek == 7 || dayOfWeek == 6 && cal.after(endTime)) {
            // 日曜、土曜、もしくは金曜の終了時刻以降なら月曜08:50
            cal.add(Calendar.DATE, (9 - dayOfWeek) % 7);
            cal.set(Calendar.HOUR_OF_DAY, 8);
            cal.set(Calendar.MINUTE, 50);
            cal.set(Calendar.SECOND, 0);
        } else if (cal.after(endTime)) {
            // それ以外の終了時刻以降なら次の日の08:50
            cal.add(Calendar.DATE, 1);
            cal.set(Calendar.HOUR_OF_DAY, 8);
            cal.set(Calendar.MINUTE, 50);
            cal.set(Calendar.SECOND, 0);
        } else {
            // それ以外の開始時刻前ならその日の開始時刻
            cal.set(Calendar.HOUR_OF_DAY, 8);
            cal.set(Calendar.MINUTE, 50);
            cal.set(Calendar.SECOND, 0);
        }

        Intent intent = new Intent(context, CheckKabuyosoService.class);
        PendingIntent pendingIntent
            = PendingIntent.getService(
                    context, -1, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager
            = (AlarmManager)
            context.getSystemService(android.content.Context.ALARM_SERVICE);
        alarmManager.set(
                AlarmManager.RTC,
                cal.getTimeInMillis(), // 時刻の設定
                pendingIntent);
        Log.v("", cal.getTime().toString());
    }

    static void cancel(Context context){
        Intent intent = new Intent(context, CheckKabuyosoService.class);
        PendingIntent pendingIntent 
            = PendingIntent.getService(
                    context, -1, intent, 
                    PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager
            = (AlarmManager)
            context.getSystemService(android.content.Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}
