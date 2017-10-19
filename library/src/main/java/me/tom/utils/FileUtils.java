package me.tom.utils;

import android.content.Context;
import android.os.Environment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {

    public static String getTempDirectoryPath(Context context) {
        File cache;
        // SD Card Mounted
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            StringBuffer buffer = new StringBuffer();
            buffer.append(Environment.getExternalStorageDirectory().getAbsolutePath());
            buffer.append("/Android/data/");
            buffer.append(context.getPackageName());
            buffer.append("/cache/");
            cache = new File(buffer.toString());
        } else {
            // Use internal storage
            cache = context.getCacheDir();
        }

        cache.mkdirs();
        return cache.getAbsolutePath();
    }

    public static String getTempFilePath(Context context, String ext) {
        return getTempFilePathWithSpecifiedName(context, System.currentTimeMillis() + ext);
    }

    public static String getTempFilePathWithSpecifiedName(Context context, String name) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(FileUtils.getTempDirectoryPath(context));
        buffer.append("/");
        buffer.append(name);
        return buffer.toString();
    }

    public static byte[] getFileData(String filePath) {
        byte[] data = null;
        try {
            InputStream inputStream = new FileInputStream(new File(filePath.replace("file://", "")));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            int count;
            byte[] buffer = new byte[1024];
            while ((count = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, count);
            }
            data = outputStream.toByteArray();
        } catch (IOException e) {
        }
        return data;
    }

    public static String getFileName(String filePath) {
        return filePath.substring(filePath.lastIndexOf('/') + 1, filePath.length());
    }
}