package demo.face.comi.io.camerademogoogle.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import demo.face.comi.io.camerademogoogle.R;
import demo.face.comi.io.camerademogoogle.bean.CameraFacing;
import demo.face.comi.io.camerademogoogle.camera.CameraManager;
import demo.face.comi.io.camerademogoogle.camera.CameraUtils;
import demo.face.comi.io.camerademogoogle.view.CameraSurfaceView;

/**
 * Created by xijie on 2017/12/15.
 */

public class CameraActivity extends Activity {
    private final static String TAG="CameraActivity";

    private FrameLayout camera_preview;
    private CameraSurfaceView cameraSurfaceView;
    private CameraManager mCameraManager;

    private FrameLayout border_ar;
    private ImageView image_view,orgin_image_view;
    boolean isFirstCamera =true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        camera_preview = (FrameLayout) findViewById(R.id.camera_preview);
        orgin_image_view = findViewById(R.id.orgin_image_view);
        border_ar = findViewById(R.id.border_ar);

        image_view = findViewById(R.id.image_view);

        findViewById(R.id.take_picture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if(!isFirstCamera){
                        mCameraManager.startPreview();
                    }
                    isFirstCamera=false;
                    //if (safeToTakePicture) {
                        mCameraManager.tackPicture(new Camera.PictureCallback() {
                            @Override
                            public void onPictureTaken(byte[] bytes, Camera camera) {

                                Bitmap bm = BitmapFactory.decodeByteArray(bytes,0,bytes.length);

                                orgin_image_view.setImageBitmap(bm);
                                int startX =(int) (bm.getWidth()*0.25);
                                int startY = (int) (bm.getHeight()*0.25);

                                int width = (int) (bm.getWidth()*0.75)-startX;
                                int height = (int) (bm.getHeight()*0.75)-startY;

                                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                                Bitmap bitmap2 = Bitmap.createBitmap(bm,startX,startY,width,height);

                                bitmap2.compress(Bitmap.CompressFormat.JPEG,100,baos);
                                image_view.setImageBitmap(bitmap2);
                                mCameraManager.stopPreview();
                                mCameraManager.startPreview();
                                //safeToTakePicture=true;
                            }
                        });
                        //safeToTakePicture = false;
                    //}

                }catch(Exception e){
                    e.printStackTrace();
                }

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG,"onResume...");
        if(CameraUtils.checkCameraHardware(this)){
            openCamera();//需要在子线程中操作
            relayout();
            cameraSurfaceView.onResume();
        }else{
            Toast.makeText(this,"该手机不支持摄像头！",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG,"onPause...");
        cameraSurfaceView.onPause();
    }

    /**
     * 初始化相机，主要是打开相机，并设置相机的相关参数，并将holder设置为相机的展示平台
     */
    private void openCamera() {
        mCameraManager = new CameraManager(this);
        if (mCameraManager.isOpen()) {
            Log.w(TAG, "surfaceCreated: 相机已经被打开了");
            return;
        }
        try {
            mCameraManager.openCamera(CameraFacing.BACK);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 设置界面展示大小
     */
    private void relayout() {
        // Create our Preview view and set it as the content of our activity.
        cameraSurfaceView = new CameraSurfaceView(this,mCameraManager);
        Point previewSizeOnScreen = mCameraManager.getConfigurationManager().getPreviewSizeOnScreen();//相机预览尺寸
        Point screentPoint=mCameraManager.getConfigurationManager().getScreenResolution();//自己展示相机预览控件所能设置最大值
        Point point = CameraUtils.calculateViewSize(previewSizeOnScreen, screentPoint);
        FrameLayout.LayoutParams layoutParams=new FrameLayout.LayoutParams(point.x,point.y);
        layoutParams.gravity= Gravity.CENTER;
        cameraSurfaceView.setLayoutParams(layoutParams);
        camera_preview.addView(cameraSurfaceView);


    }
}
