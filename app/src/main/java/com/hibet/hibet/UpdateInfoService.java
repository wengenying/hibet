package com.hibet.hibet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.content.FileProvider;

public class UpdateInfoService {

    private static final String AUTHORITY="com.hibet.hibet";

    ProgressDialog progressDialog;
    Handler handler;
    Context context;
    UpdateInfo updateInfo;

    public UpdateInfoService(Context context){
        this.context=context;
    }

    private String writeServerFileContent(String path) throws Exception {
        StringBuffer sb = new StringBuffer();
        String line = null;
        BufferedReader reader = null;
        try {
            URL url = new URL(path);
            HttpURLConnection urlConnection = (HttpURLConnection) url
                    .openConnection();
            reader = new BufferedReader(new InputStreamReader(
                    urlConnection.getInputStream()));
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public String getNewUrl() throws Exception{
        String path = GetServerUrl.getUrl() + "/URL.txt";
        String url = writeServerFileContent(path);
        return url;
    }

    public UpdateInfo getUpDateInfo() throws Exception {
        String path = GetServerUrl.getUrl() + "/UpdateVersion.txt";
        String info = writeServerFileContent(path);
        UpdateInfo updateInfo = new UpdateInfo();
        updateInfo.setVersion(info.split("&")[1]);
        updateInfo.setDescription(info.split("&")[2]);
        updateInfo.setUrl(info.split("&")[3]);
        this.updateInfo=updateInfo;
        return updateInfo;
    }


    public boolean isNeedUpdate(){
        String new_version = updateInfo.getVersion();
        String now_version="";
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            now_version= packageInfo.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return !new_version.equals(now_version);
    }


    public void downLoadFile(final String url,final ProgressDialog pDialog,Handler h){
        progressDialog=pDialog;
        handler=h;
        new Thread() {
            public void run() {
                HttpClient client = new DefaultHttpClient();
                HttpGet get = new HttpGet(url);
                HttpResponse response;
                try {
                    response = client.execute(get);
                    HttpEntity entity = response.getEntity();
                    int length = (int) entity.getContentLength();
                    progressDialog.setMax(length);
                    InputStream is = entity.getContent();
                    FileOutputStream fileOutputStream = null;
                    if (is != null) {
                        File file = new File(
                                Environment.getExternalStorageDirectory(),
                                "new.apk");
                        fileOutputStream = new FileOutputStream(file);
                        byte[] buf = new byte[1024];
                        int ch = -1;
                        int process = 0;
                        while ((ch = is.read(buf)) != -1) {
                            fileOutputStream.write(buf, 0, ch);
                            process += ch;
                            progressDialog.setProgress(process);
                        }

                    }
                    fileOutputStream.flush();
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    down();
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }.start();
    }
//
    void down() {
        handler.post(new Runnable() {
            public void run() {
                progressDialog.cancel();
                update();
            }
        });
    }

    void update() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        File file = new File(Environment
                .getExternalStorageDirectory(), "new.apk");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileProvider", file);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }
}
