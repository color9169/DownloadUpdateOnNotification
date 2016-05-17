package com.demo_download_notification.demo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.widget.RemoteViews;
import com.demo_download_notification.demo.utils.FileUtil;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by color on 16/5/16.
 */
public class DownUpdateApk extends Service {

    NotificationManager notificationManager;
    Notification myNotify;

    private String url;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            int progress = Integer.parseInt(message.obj.toString());
            RemoteViews rv;
            switch (message.what) {
                case 1:  //更改通知栏UI布局
                    rv = new RemoteViews(getPackageName(),
                            R.layout.my_notification);
                    rv.setTextViewText(R.id.text_title, "正在下载中");
                    rv.setProgressBar(R.id.progress, 100, progress, false);
                    rv.setTextViewText(R.id.text_content, progress + "%");
                    myNotify.contentView = rv;
                    notificationManager.notify(0, myNotify);
                    break;
                case 2:
                    rv = new RemoteViews(getPackageName(),
                            R.layout.my_notification);
                    rv.setTextViewText(R.id.text_title, "下载完成,点击安装");
                    rv.setProgressBar(R.id.progress, 100, progress, false);
                    rv.setTextViewText(R.id.text_content, progress + "%");
                    myNotify.contentView = rv;


                    //下载完成,点击可以去安装文件
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);// android.intent.action.VIEW
                    intent.setDataAndType(Uri.fromFile(new File(FileUtil.getPath() + "/Tsingpu.apk")),
                            "application/vnd.android.package-archive");
                    myNotify.flags = Notification.FLAG_AUTO_CANCEL;
                    myNotify.contentIntent = PendingIntent.getActivity(DownUpdateApk.this, 1, intent, 0);
                    notificationManager.notify(0, myNotify);


                    break;
            }
            return false;
        }
    });

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        myNotify = new Notification();
        myNotify.icon = R.mipmap.ic_launcher;
        myNotify.tickerText = "准备下载...";
        myNotify.when = System.currentTimeMillis();

        myNotify.flags = Notification.FLAG_NO_CLEAR;// 不能够自动清除

        RemoteViews rv = new RemoteViews(getPackageName(),
                R.layout.my_notification);
        rv.setProgressBar(R.id.progress, 100, 0, false);
        rv.setTextViewText(R.id.text_content, "开始下载"); //这里就是使用自定义布局了 初始化的时候不设置Intent,点击的时候就不会有反应了,亏得我还找了好久  T-T

        myNotify.contentView = rv;
        notificationManager.notify(0, myNotify);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        url = null;
        myNotify = null;
        notificationManager = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        url = intent.getStringExtra("url");
        init();

        return super.onStartCommand(intent, flags, startId);
    }


    public void init() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient(); //使用okhttp下载文件
                Request request = new Request.Builder()
                        .url(url)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        String s = e.getMessage();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (!response.isSuccessful())
                            throw new IOException("Unexpected code " + response);
                        InputStream is = response.body().byteStream(); //成功的回调中拿到字节流
                        String path = FileUtil.getPath();
                        long fileLength = response.body().contentLength(); //获取文件长度
                        FileUtil.saveFile(is, path, "test.apk", mHandler, fileLength); //保存下载的apk文件
                    }

                });


            }
        }).start();
    }
}
