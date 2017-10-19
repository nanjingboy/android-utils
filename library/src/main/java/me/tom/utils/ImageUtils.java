package me.tom.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ImageUtils {

    public static String saveToFile(Context context, Bitmap bitmap) {
        return saveToFile(FileUtils.getTempFilePath(context, ".jpg"), bitmap);
    }

    public static String saveToFile(String filePath, Bitmap bitmap) {
        try {
            OutputStream outputStream = new FileOutputStream(new File(filePath));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            return filePath;
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    public static String compress(Context context, String path) {
        String imagePath = path.replace("file://", "");
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inSampleSize = 1;
        BitmapFactory.decodeFile(imagePath, options);
        int width = options.outWidth;
        int height = options.outHeight;
        int thumbWidth = width % 2 == 1 ? width + 1 : width;
        int thumbHeight = height % 2 == 1 ? height + 1: height;
        if (thumbWidth > thumbHeight) {
            width = thumbHeight;
            height = thumbWidth;
        } else {
            width = thumbWidth;
            height = thumbHeight;
        }
        double scale = ((double) width / height);
        long originalSize = (new File(imagePath)).length() / 1024;
        if (scale <= 1 && scale > 0.5625) {
            if (height < 1664) {
                if (originalSize < 150) {
                    return imagePath;
                }
            } else if (height >= 1664 && height < 4990) {
                thumbWidth = width / 2;
                thumbHeight = height / 2;
            } else if (height >= 4990 && height < 10240) {
                thumbWidth = width / 4;
                thumbHeight = height / 4;
            } else {
                int multiple = height / 1280 == 0 ? 1 : height / 1280;
                thumbWidth = width / multiple;
                thumbHeight = height / multiple;
            }
        } else if (scale <= 0.5625 && scale > 0.5) {
            if (height < 1280 && originalSize < 200) {
                return imagePath;
            }
            int multiple = height / 1280 == 0 ? 1 : height / 1280;
            thumbWidth = width / multiple;
            thumbHeight = height / multiple;
        } else {
            int multiple = (int) Math.ceil(height / (1280.0 / scale));
            thumbWidth = width / multiple;
            thumbHeight = height / multiple;
        }
        return compress(context, imagePath, thumbWidth, thumbHeight);
    }

    public static String compress(Context context, String imagePath, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        int sourceWidth = options.outWidth;
        int sourceHeight = options.outHeight;
        int inSampleSize = 1;
        if (sourceWidth > width || sourceHeight > height) {
            int halfWidth = sourceWidth / 2;
            int halfHeight = sourceHeight / 2;
            while ((halfHeight / inSampleSize) > height && (halfWidth / inSampleSize) > width) {
                inSampleSize *= 2;
            }
        }
        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
        int heightRatio = (int) Math.ceil(options.outHeight / (float) height);
        int widthRatio = (int) Math.ceil(options.outWidth / (float) width);
        if (heightRatio > 1 || widthRatio > 1) {
            if (heightRatio > widthRatio) {
                options.inSampleSize = heightRatio;
            } else {
                options.inSampleSize = widthRatio;
            }
        }
        options.inJustDecodeBounds = false;
        Bitmap sourceBitmap = BitmapFactory.decodeFile(imagePath, options);
        String result = rotating(context, sourceBitmap, getSpinAngle(imagePath));
        sourceBitmap.recycle();
        return result;
    }

    public static String rotating(Context context, Bitmap bitmap, int angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        Bitmap destBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        String path = saveToFile(context, destBitmap);
        destBitmap.recycle();
        return path;
    }

    public static int getSpinAngle(String imagePath) {
        int angle = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(imagePath);
            switch (exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    angle = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    angle = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    angle = 270;
                    break;
                default:
                    break;
            }
        } catch (IOException e) {
        }
        return angle;
    }
}