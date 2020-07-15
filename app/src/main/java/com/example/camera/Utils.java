package com.example.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.widget.ImageView;

import java.io.IOException;

public class Utils {
    public static Bitmap rotateBitmap(Bitmap bitmap, String path) {
        try {
            ExifInterface srcExif = new ExifInterface(path);
            Matrix matrix = new Matrix();
            int angle = 0;
            int orientation = srcExif.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL);
            switch (orientation){
                case ExifInterface.ORIENTATION_ROTATE_90:{
                    angle = 90;
                    break;
                }
                case ExifInterface.ORIENTATION_ROTATE_180:{
                    angle = 180;
                    break;
                }
                case ExifInterface.ORIENTATION_ROTATE_270:{
                    angle = 270;
                    break;
                }
                default:
                    break;
            }
            matrix.postRotate(angle);
            return Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void displayImage(ImageView imageView,String path){
        int targetWidth = imageView.getWidth();
        int targetHeight = imageView.getHeight();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path,options);
        int photoWidth = options.outWidth;
        int photoHeight = options.outHeight;
        int scaleFactor = Math.min(photoHeight/targetHeight,photoWidth/targetWidth);
        options.inJustDecodeBounds = false;
        options.inSampleSize = scaleFactor;
        Bitmap bitmap = Utils.rotateBitmap(BitmapFactory.decodeFile(path,options),path);
        imageView.setImageBitmap(bitmap);
    }
}
