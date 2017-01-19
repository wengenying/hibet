package com.hibet.hibet;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.umeng.analytics.MobclickAgent;
import com.umeng.analytics.MobclickAgent.EScenarioType;


public class MainActivity extends AppCompatActivity {

    private final static String TAG = "MainActivity";

    //Umeng page count name
    private final String mPageName = "MainActivity";

    private Context mContext;

    private boolean isAdd = false;

    private String mUrl;
    private WebView mWebView;

    private ImageView mGoBack;
    private ImageView mGoForward;
    private ImageView mRefresh;

    private RelativeLayout mMainView;

    private RelativeLayout progressBar_circle = null;

    private UpdateInfo info;
    private ProgressDialog progressDialog;
    UpdateInfoService updateInfoService;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        mUrl = "http://www.hibet1.com";
        mWebView = (WebView) findViewById(R.id.webview);
        mGoBack = (ImageView) findViewById(R.id.go_back);
        mGoForward = (ImageView) findViewById(R.id.go_forward);

        initUmeng();

        if (mWebView != null) {
            mGoBack.setEnabled(mWebView.canGoBack());
            mGoForward.setEnabled(mWebView.canGoForward());
        }
        mRefresh = (ImageView) findViewById(R.id.refresh);
        mGoBack.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (mWebView != null && mWebView.canGoBack()) {
                    mWebView.goBack();
                }
            }
        });
        mGoForward.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (mWebView != null && mWebView.canGoForward()) {
                    mWebView.goForward();
                }
            }
        });
        mRefresh.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (mWebView != null) {
                    mWebView.reload();
                }
            }
        });
        mMainView = (RelativeLayout) findViewById(R.id.activity_main);
        initWebViewSettings();
        showWebView(mUrl, null, null, null);
        checkUpdate();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mClient = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @SuppressLint("HandlerLeak")
    private Handler handler1 = new Handler() {
        public void handleMessage(Message msg) {
            System.out.println("============== isNeedUpdate ================="+updateInfoService.isNeedUpdate());
            if (updateInfoService.isNeedUpdate()) {
                showUpdateDialog();
            }
        }

        ;
    };

    private void checkUpdate() {
        Toast.makeText(MainActivity.this, "���ڼ��汾����..", Toast.LENGTH_SHORT).show();
        new Thread() {
            public void run() {
                try {
                    updateInfoService = new UpdateInfoService(MainActivity.this);
                    info = updateInfoService.getUpDateInfo();
                    handler1.sendEmptyMessage(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            ;
        }.start();
        System.out.println("====================== check update !!!!! ======================");
    }


    private void initUmeng() {
        MobclickAgent.setDebugMode(true);
        MobclickAgent.openActivityDurationTrack(false);
        MobclickAgent.setScenarioType(mContext, EScenarioType.E_UM_NORMAL);
    }


    private void showWebView(String url, String username, String password, String result) {
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        mWebView.setWebViewClient(new webViewClient());
        progressBar_circle = (RelativeLayout) LayoutInflater.from(getApplicationContext()).inflate(R.layout.progress_circle, null);
        if (mMainView != null) {
            mMainView.addView(progressBar_circle, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        }
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                Log.d(TAG, "newProgress =" + newProgress);
                super.onProgressChanged(view, newProgress);
                if (newProgress == 100) {
                    if (progressBar_circle != null) {
                        progressBar_circle.setVisibility(View.GONE);
                    }
                    isAdd = false;
                } else {
                    if (!isAdd) {
                        progressBar_circle.setVisibility(View.VISIBLE);
                    }
                    isAdd = true;
                }
            }
        });
        mWebView.loadUrl(mUrl);
    }

    /**
     * init WebView Settings
     */
    private void initWebViewSettings() {
        //adjust zoom display
        //mWebView.getSettings().setSupportZoom(true);
        //mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDefaultFontSize(12);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.getSettings().setJavaScriptEnabled(true);
        //mWebView.getSettings().setUserAgentString(this.getApplication().getUserAgent());
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.getSettings().setAppCacheEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setDatabaseEnabled(true);
        mWebView.setDrawingCacheEnabled(true);
    }

    private boolean isExit = false;

    @Override
    public void onBackPressed() {
        if (!isExit) {
            Toast.makeText(getApplicationContext(), "再按一次退出海博",
                    Toast.LENGTH_SHORT).show();
            isExit = true;
        } else {
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(mPageName);
        MobclickAgent.onResume(mContext);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(mPageName);
        MobclickAgent.onPause(mContext);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MobclickAgent.onKillProcess(mContext);
        mWebView = null;
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mClient.connect();
        AppIndex.AppIndexApi.start(mClient, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(mClient, getIndexApiAction());
        mClient.disconnect();
    }

    private class webViewClient extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (view != null) {
                mGoBack.setEnabled(view.canGoBack());
                mGoForward.setEnabled(view.canGoForward());
            }
            view.loadUrl("javascript:setWebViewFlag()");
            if (url != null && url.endsWith("/index.html")) {
                MobclickAgent.onPageStart("index.html");
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        public void onProgressChanged(WebView view, int newProgress) {
        }

    }

    //��ʾ�Ƿ�Ҫ���µĶԻ���
    private void showUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setTitle("������APP���汾" + info.getVersion());
        builder.setMessage(info.getDescription());
        builder.setCancelable(false);
        builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (Environment.getExternalStorageState().equals(
                        Environment.MEDIA_MOUNTED)) {
                    downFile(info.getUrl());
                } else {
                    Toast.makeText(MainActivity.this, "SD�������ã������SD��",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.create().show();
    }

    void downFile(final String url) {
        progressDialog = new ProgressDialog(MainActivity.this);    //�������������ص�ʱ��ʵʱ���½��ȣ�����û��Ѻö�
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle("��������");
        progressDialog.setMessage("���Ժ�...");
        progressDialog.setProgress(0);
        progressDialog.show();
        updateInfoService.downLoadFile(url, progressDialog,handler1);
    }

}
