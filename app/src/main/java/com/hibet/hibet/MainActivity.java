package com.hibet.hibet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
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
import android.Manifest;
import android.content.pm.PackageManager;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.umeng.analytics.MobclickAgent;
import com.umeng.analytics.MobclickAgent.EScenarioType;

import static android.net.ConnectivityManager.TYPE_MOBILE;
import static android.net.ConnectivityManager.TYPE_WIFI;


public class MainActivity extends AppCompatActivity {

    private final static String TAG = "MainActivity";

    private final static String PAGE_NAME = "MainActivity";

    private Context mContext;

    private boolean isAdd = false;

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
        int netCode = checkNetworkType(mContext);
        if (netCode > -1) {
            init();
        }
    }

    private int checkNetworkType(Context mContext) {
        try {
            final ConnectivityManager connectivityManager = (ConnectivityManager) mContext
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo mobNetInfoActivity = connectivityManager
                    .getActiveNetworkInfo();
            if (mobNetInfoActivity == null || !mobNetInfoActivity.isAvailable()) {
                Toast.makeText(MainActivity.this, "当前网络不可用，请检查网络",
                        Toast.LENGTH_SHORT).show();
                return -1;
            } else {
                // NetworkInfo不为null开始判断是网络类型
                int netType = mobNetInfoActivity.getType();
                if (netType == TYPE_WIFI) {
                    // wifi net处理
                    return TYPE_WIFI;
                } else if (netType == ConnectivityManager.TYPE_MOBILE) {
                    Toast.makeText(MainActivity.this, "当前使用的是移动网络",
                            Toast.LENGTH_SHORT).show();
                    return TYPE_MOBILE;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return -2;
        }
        return -3;
    }

    @SuppressLint("HandlerLeak")
    private Handler handler1 = new Handler() {
        public void handleMessage(Message msg) {
            if (updateInfoService.isNeedUpdate()) {
                showUpdateDialog();
            }
        }
    };

    private void showUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setTitle("请升级APP至版本" + info.getVersion());
        builder.setMessage(info.getDescription());
        builder.setCancelable(false);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (Environment.getExternalStorageState().equals(
                        Environment.MEDIA_MOUNTED)) {
                    downFile(info.getUrl());
                } else {
                    Toast.makeText(MainActivity.this, "SD卡不可用，请插入SD卡",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.create().show();
    }

    private void downFile(String url) {
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle("正在下载");
        progressDialog.setMessage("请稍候...");
        progressDialog.setProgress(0);
        progressDialog.show();
        updateInfoService.downLoadFile(url, progressDialog, handler1);
    }

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 0;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage
     * <p>
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private void init() {
        Intent intent = getIntent();
        String url = intent.getStringExtra("URL");
        checkUpdate();
        initUmeng();
        mWebView = (WebView) findViewById(R.id.webview);
        mGoBack = (ImageView) findViewById(R.id.go_back);
        mGoForward = (ImageView) findViewById(R.id.go_forward);

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
        showWebView(url, null, null, null);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mClient = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void checkUpdate() {
        Activity activity = MainActivity.this;
        //activity is destory if no use ?
        verifyStoragePermissions(activity);
        Toast.makeText(MainActivity.this, "正在检查版本更新...", Toast.LENGTH_SHORT).show();
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

        }.start();
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
        mWebView.loadUrl(url);
    }

    /**
     * init WebView Settings ass
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
        MobclickAgent.onPageStart(PAGE_NAME);
        MobclickAgent.onResume(mContext);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(PAGE_NAME);
        MobclickAgent.onPause(mContext);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MobclickAgent.onKillProcess(mContext);
        mWebView.clearCache(true);
        mWebView.clearFormData();
        mWebView.clearSslPreferences();
        mWebView.clearHistory();
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
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(mClient, getIndexApiAction());
        mClient.disconnect();
    }

    public class webViewClient extends WebViewClient {

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

    }
}



