package com.demo_download_notification.demo.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileUtil {


    /**
     * 将Bitmap 图片保存到本地路径，并返回路径
     *
     * @param c
     * @param mType    资源类型，参照  MultimediaContentType 枚举，根据此类型，保存时可自动归类
     * @param fileName 文件名称
     * @param bitmap   图片
     * @return
     */
    public static String saveFile(Context c, String fileName, Bitmap bitmap) {
        return saveFile(c, "", fileName, bitmap);
    }

    public static String saveFile(Context c, String filePath, String fileName, Bitmap bitmap) {
        byte[] bytes = bitmapToBytes(bitmap);
        return saveFile(c, filePath, fileName, bytes);
    }


    public static boolean saveFile(InputStream inputStream, String path, String fileName, Handler mHandler, long fileLength) throws IOException {
        File newFile = new File(path);
        if (!newFile.exists()) {
            newFile.mkdir();
        }
        newFile = new File(path + "/" + fileName);
        OutputStream outs = new FileOutputStream(newFile);
        int byteWritten = 0;
        int byteCount = 0;
        byte[] bytes = new byte[1024];
        int percentage = 0; //保存百分比信息
        int count = 0;
        int nowCount = 1;
        while ((byteCount = inputStream.read(bytes)) != -1) {
            count += byteCount;
            percentage = (int) (((float) count / fileLength) * 100);

            if (percentage > 0 && percentage == nowCount) { //每完成百分之一 通知Handler一次
                nowCount = percentage + 1;
                Message msg = mHandler.obtainMessage();
                msg.what = 1;
                msg.obj = percentage + "";
                mHandler.sendMessage(msg);
            }
            outs.write(bytes, 0, byteCount);
            byteWritten += byteCount;
        }
        //写入完成,通知Handler 可以进行安装,改变通知栏ui
        Message msg = mHandler.obtainMessage();
        msg.what = 2;
        msg.obj = percentage + "";
        mHandler.sendMessage(msg);
        inputStream.close();
        outs.close();
        if (byteWritten > 0) {
            return true;
        } else {
            return false;
        }
    }

    public static byte[] bitmapToBytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

    public static String saveFile(Context c, String filePath, String fileName, byte[] bytes) {
        String fileFullName = "";
        FileOutputStream fos = null;
        String dateFolder = new SimpleDateFormat("yyyyMMdd", Locale.CHINA)
                .format(new Date());
        try {
            String suffix = "";
            File file = new File(getPath(filePath));
            if (!file.exists()) {
                file.mkdirs();
            }
            File fullFile = new File(filePath, fileName + suffix);
            fileFullName = fullFile.getPath();
            fos = new FileOutputStream(new File(filePath, fileName + suffix));
            fos.write(bytes);
        } catch (Exception e) {
            fileFullName = "";
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    fileFullName = "";
                }
            }
        }
        return fileFullName;
    }


    public static String getPath(String filePath) {
        if (filePath == null || filePath.trim().length() == 0) {
            filePath = Environment.getExternalStorageDirectory() + "/JiaXT/";
        } else {
            filePath = "";
        }
        return filePath;
    }

    public static String getPath() {
        return Environment.getExternalStorageDirectory() + "/TsingpuAkp/";
    }


    /**
     * 删除单个文件
     *
     * @param filePath 被删除文件的文件名
     * @return 文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            return file.delete();
        }
        return false;
    }

    /**
     * 删除文件夹以及目录下的文件
     *
     * @param filePath 被删除目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(String filePath) {
        boolean flag = false;
        //如果filePath不以文件分隔符结尾，自动添加文件分隔符
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator;
        }
        File dirFile = new File(filePath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        flag = true;
        File[] files = dirFile.listFiles();
        //遍历删除文件夹下的所有文件(包括子目录)
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                //删除子文件
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag) break;
            } else {
                //删除子目录
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag) break;
            }
        }
        if (!flag) return false;
        //删除当前空目录
        return dirFile.delete();
    }

    /**
     * 根据路径删除指定的目录或文件，无论存在与否
     *
     * @param filePath 要删除的目录或文件
     * @return 删除成功返回 true，否则返回 false。
     */
    public static boolean DeleteFolder(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return false;
        } else {
            if (file.isFile()) {
                // 为文件时调用删除文件方法
                return deleteFile(filePath);
            } else {
                // 为目录时调用删除目录方法
                return deleteDirectory(filePath);
            }
        }
    }


    /**
     * 保存文件
     *
     * @param bm
     * @param fileName
     * @throws IOException
     */
    public static File getFile(Bitmap bm, String fileName, String filePath) {
        try {
            String path = getPath(filePath);
            File dirFile = new File(path);
            if (!dirFile.exists()) {
                dirFile.mkdir();
            }
            File myCaptureFile = new File(path + fileName);
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
            bm.compress(CompressFormat.JPEG, 80, bos);
            bos.flush();
            bos.close();
            return myCaptureFile;
        } catch (IOException e) {
        }
        return null;
    }


}
