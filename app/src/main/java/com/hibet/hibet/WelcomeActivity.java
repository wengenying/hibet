package com.hibet.hibet;

import android.app.Activity;

/**
 * Created by mkt on 2017/1/14.
 */

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class WelcomeActivity extends Activity {

    private static String FIXD_URL ="http://www.hibet1.com";

    // 声明控件对象//
    private TextView textView;
    private int count = 5;
    private Animation mAnimation;
    private String mUrl = null;

    //get update info
    UpdateInfoService mUpdateInfoService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // dismiss titlebar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_welcome);
        // init
        textView = (TextView) findViewById(R.id.textView);
        mAnimation = AnimationUtils.loadAnimation(this, R.anim.animation_text);
        handler.sendEmptyMessageDelayed(0, 1000);
        mUpdateInfoService = new UpdateInfoService(WelcomeActivity.this);
        new Thread(new Runnable() {
            public void run() {
                try{
                    mUrl = mUpdateInfoService.getNewUrl();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private int getCount() {
        count--;
        if (count == 0) {
            //start main activity
            Intent intent = new Intent(this, MainActivity.class);
            if(mUrl != null){
                intent.putExtra("URL", mUrl);
            }else{
                intent.putExtra("URL", FIXD_URL);
            }
            startActivity(intent);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
        return count;
    }

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 0) {
                textView.setText(getCount()+"");
                handler.sendEmptyMessageDelayed(0, 1000);
                mAnimation.reset();
                textView.startAnimation(mAnimation);
            }
        }

    };

}