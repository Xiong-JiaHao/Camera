package com.gin.xjh.camera;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private static int REQ_1 = 1;
    private static int REQ_2 = 2;
    private ImageView mImageView;
    private Button mBtn1, mBtn2, mBtn3;
    private String mFilePath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageView = findViewById(R.id.iv);
        mBtn1 = findViewById(R.id.StartCamera1);
        mBtn2 = findViewById(R.id.StartCamera2);
        mBtn3 = findViewById(R.id.CustomCamera);
        mBtn1.setOnClickListener(this);
        mBtn2.setOnClickListener(this);
        mBtn3.setOnClickListener(this);
        isPermissionAllGranted(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},3);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {//判断是返回的事数据
            if (requestCode == REQ_1) {//判断是我们之前的intent返回的数据
                Bundle bundle = data.getExtras();
                Bitmap bitmap = (Bitmap) bundle.get("data");//从data中获取数据
                mImageView.setImageBitmap(bitmap);
            } else if (requestCode == REQ_2) {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(mFilePath);
                    Bitmap bitmap = BitmapFactory.decodeStream(fis);
                    mImageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 800, 800, true));//压缩图片
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.StartCamera1:
                Intent intent1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent1, REQ_1);
                break;
            case R.id.StartCamera2:
                Uri uri;
                mFilePath = Environment.getExternalStorageDirectory().getPath();//获得SD卡路径
                mFilePath = mFilePath + "/temp.jpg";
                Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (Build.VERSION.SDK_INT >= 24) {
                    uri = FileProvider.getUriForFile(this,"com.gin.xjh.camera.provider", new File(mFilePath));
                } else {
                    uri = Uri.fromFile(new File(mFilePath));
                }
                intent2.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(intent2, REQ_2);
                break;
            case R.id.CustomCamera:
                startActivity(new Intent(this,CustomCamera.class));
                break;
        }
    }
}
