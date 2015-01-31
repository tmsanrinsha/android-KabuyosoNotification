package jp.lolipop.sanrinsha.KabuyosoNotification;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class KabuyosoNotificationActivity extends Activity implements View.OnClickListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn = (Button) findViewById(R.id.StartButton);
        btn.setOnClickListener(this);//リスナの登録

        Button btn1  = (Button) findViewById(R.id.StopButton);
        btn1.setOnClickListener(this);//リスナの登録

        CheckKabuyosoAlarm.setSchedule(getBaseContext());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.StartButton:
                CheckKabuyosoAlarm.setSchedule(getBaseContext());
                break;
            case R.id.StopButton:
                CheckKabuyosoAlarm.cancel(getBaseContext());
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
}
