package com.example.camera;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.VideoView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback{

    private Button takePhoto;
    private Button takeVideo;
    private SurfaceView surfaceView;
    private SurfaceHolder holder;
    private Camera mCamera;
    private ImageView imageView;
    private VideoView videoView;
    private MediaRecorder mMediaRecorder;
    private boolean isRecording;
    private static String[] permissions = new String[]{Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO};
    private static int PERMISSION_CODE = 1;
    private static String TAG = "CameraActivity";
    private String mp4Path;

    private void requestForPermission(){
        if(PackageManager.PERMISSION_GRANTED != getPackageManager().checkPermission(permissions[0], getPackageName())
        || PackageManager.PERMISSION_GRANTED != getPackageManager().checkPermission(permissions[1], getPackageName()))
            ActivityCompat.requestPermissions(this,permissions,PERMISSION_CODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestForPermission();
        initCamera();

        takePhoto = findViewById(R.id.takePhoto);
        takeVideo = findViewById(R.id.takeVideo);
        surfaceView = findViewById(R.id.surfaceView);
        holder = surfaceView.getHolder();
        imageView = findViewById(R.id.imageView);
        videoView = findViewById(R.id.videoView);
        holder.addCallback(this);

        final Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {
                FileOutputStream fos = null;
                String filePath = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath() + File.separator + "1.jpg";
                File file = new File(filePath);
                try {
                    fos = new FileOutputStream(file);
                    fos.write(bytes);
                    fos.close();
                    Utils.displayImage(imageView,filePath);
                    videoView.setVisibility(View.INVISIBLE);
                    imageView.setVisibility(View.VISIBLE);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    mCamera.startPreview();
                    if(fos != null){
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };

        takePhoto.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCamera.takePicture(null,null,mPictureCallback);
            }
        });

        takeVideo.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                record();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mCamera == null){
            initCamera();
        }
        mCamera.startPreview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCamera.stopPreview();
    }

    private void initCamera(){
        mCamera = Camera.open();
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPictureFormat(ImageFormat.JPEG);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        parameters.set("orientation","portrait");
        parameters.set("rotation",90);
        mCamera.setParameters(parameters);
        mCamera.setDisplayOrientation(90);
    }

    private boolean prepareVideoRecorder(){
        mMediaRecorder = new MediaRecorder();
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        mp4Path = getOutputMediaPath();
        mMediaRecorder.setOutputFile(mp4Path);
        mMediaRecorder.setOrientationHint(90);
        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            mMediaRecorder.release();
            return false;
        }
        return true;
    }

    public void record(){
        if(isRecording){
            takeVideo.setText("录制");
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            mCamera.lock();

            imageView.setVisibility(View.INVISIBLE);
            videoView.setVisibility(View.VISIBLE);
            videoView.setVideoPath(mp4Path);
            videoView.start();
        }else{
            if(prepareVideoRecorder()){
                takeVideo.setText("停止");
                mMediaRecorder.start();
            }else return;
        }
        isRecording = !isRecording;
    }


    public String getOutputMediaPath(){
        File mediaDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(mediaDir,"IMG_" + timeStamp + ".mp4");
        if(!mediaFile.exists()){
            mediaFile.getParentFile().mkdirs();
        }
        return mediaFile.getAbsolutePath();
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        if(holder.getSurface() == null) return;
        try {
            mCamera.stopPreview();
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }
}