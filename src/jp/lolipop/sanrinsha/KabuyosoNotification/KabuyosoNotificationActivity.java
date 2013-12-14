package jp.lolipop.sanrinsha.KabuyosoNotification;

import jp.lolipop.sanrinsha.KabuyosoNotification.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

/**
 * @see <a href="http://codezine.jp/article/detail/7276">Web APIの基本的な使い方 （1/3）：CodeZine</a>
 */
public class KabuyosoNotificationActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Button btn = (Button) findViewById(R.id.StartButton);
        btn.setOnClickListener(this);//リスナの登録
       
        Button btn1  = (Button) findViewById(R.id.StopButton);
        btn1.setOnClickListener(this);//リスナの登録

        startService(new Intent(this, CheckKabuyosoService.class));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.StartButton:
                startService(new Intent(this, CheckKabuyosoService.class));
                break;
            case R.id.StopButton:
                CheckKabuyosoService.stopResidentIfActive(this);;
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
}

