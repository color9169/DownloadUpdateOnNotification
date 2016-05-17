package com.demo_download_notification.demo;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        showNoticeDialog("http://wsdownload.hdslb.net/app/BiliPlayer3.apk");
    }


    /**
     * 显示软件更新对话框
     */
    public void showNoticeDialog(final String apkUrl) {
        // 构造对话框

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("软件更新");
        builder.setMessage("有新版本,建议更新!");
        // 更新
        builder.setPositiveButton("更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // 显示下载对话框
                Intent serviceIntent = new Intent(mContext, DownUpdateApk.class);
                serviceIntent.putExtra("url", apkUrl);
                startService(serviceIntent);
            }
        });
        // 稍后更新
        builder.setNegativeButton("稍后更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        Dialog noticeDialog = builder.create();
        noticeDialog.show();
    }
}
