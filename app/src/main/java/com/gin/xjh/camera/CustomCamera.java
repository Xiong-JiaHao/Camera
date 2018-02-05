package com.gin.xjh.camera;

import android.Manifest;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Gin on 2018/2/5.
 */

public class CustomCamera extends BaseActivity implements View.OnClickListener,SurfaceHolder.Callback{
    private Button mBtn;
    private Camera mCamera;
    private SurfaceView mPreview;
    private SurfaceHolder mHolder;
    private Camera.PictureCallback mPictureCallback=new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {//data是完整的图片数据，并非是缩略图
            File tempFile = new File("/sdcard/temp.png");
            try {
                FileOutputStream fos = new FileOutputStream(tempFile);
                fos.write(data);//把data数据写入输出流
                fos.close();//关闭输出流
                Intent intent = new Intent(CustomCamera.this,ResultAty.class);
                intent.putExtra("picPath",tempFile.getAbsolutePath());//把文件的绝对路径写入intent的picPath字段中
                startActivity(intent);
                CustomCamera.this.finish();//关闭
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom);
        mBtn=findViewById(R.id.Capture);
        mPreview=findViewById(R.id.preview);
        mHolder=mPreview.getHolder();
        mHolder.addCallback(this);
        mBtn.setOnClickListener(this);
        //点击自动对焦
        mPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.autoFocus(null);
            }
        });
        isPermissionAllGranted(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA},4);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mCamera==null){
            mCamera=getCamera();
            if(mHolder!=null){
                setStartPreview(mCamera,mHolder);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();

    }

    @Override
    public void onClick(View v) {
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPictureFormat(ImageFormat.JPEG);//图片格式
        parameters.setPictureSize(800,400);//图片大小
        parameters.setFocusMode(Camera.Parameters.FLASH_MODE_AUTO);//设置相机对焦（自动对焦要相机是支持自动对焦的）
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if(success){//success对焦是否准确
                    mCamera.takePicture(null,null,mPictureCallback);
                }
            }
        });
    }

    /**
     * 获取Camera对象
     * @return
     */
    private Camera getCamera() {
        Camera camera;
        try {
            camera = Camera.open();

        } catch (Exception e){
            camera=null;
            e.printStackTrace();
        }
        return camera;
    }
    /**
     * 开始预览相机内容
     */
    private void setStartPreview(Camera camera,SurfaceHolder holder){
        try {
            camera.setPreviewDisplay(holder);//绑定操作
            camera.setDisplayOrientation(90);//最初系统的Camera的预览角度是横屏状态，把他旋转90度就是竖屏状态
            camera.startPreview();//开始预览相机
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 释放相机资源
     */
    private void releaseCamera(){
        if(mCamera!=null){
            mCamera.setPreviewCallback(null);//将相机的回调置空，取消绑定
            mCamera.stopPreview();//取消相机的取景功能
            mCamera.release();//释放相机所占有的系统资源
            mCamera=null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        setStartPreview(mCamera,mHolder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mCamera.stopPreview();
        setStartPreview(mCamera,mHolder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
    }
}
