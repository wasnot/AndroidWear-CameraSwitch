package net.wasnot.wear.cameraswitch;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;


public class MainActivity extends Activity {

    private final static String TAG = MainActivity.class.getSimpleName();

    private Camera mCamera;
    private SurfaceView mSurfaceView;
    private SurfaceHolder holder;
    private int numberOfCameras;
    private int defaultCameraId;
    private int cameraCurrentlyLocked;

    private SurfaceHolder.Callback mSurfaceListener =
            new SurfaceHolder.Callback() {
                public void surfaceCreated(SurfaceHolder holder) {
                    mCamera = Camera.open();
                    try {
                        mCamera.setDisplayOrientation(90);
                        Camera.Parameters parameters = mCamera.getParameters();
                        List<Camera.Size> list = parameters.getSupportedPreviewSizes();
                        Toast.makeText(MainActivity.this, "test:" + list.size(), Toast.LENGTH_LONG)
                                .show();
                        for (Camera.Size size : list) {
                            Log.d("camera", "size:" + size.width + "," + size.height);
                        }
                        holder.setFixedSize(list.get(0).width, list.get(0).height);
                        mCamera.setPreviewDisplay(holder);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                public void surfaceDestroyed(SurfaceHolder holder) {
                    if (mCamera == null) {
                        return;
                    }
                    mCamera.release();
                    mCamera = null;
                }

                public void surfaceChanged(SurfaceHolder holder, int format, int width,
                        int height) {
                    if (mCamera == null) {
                        return;
                    }
//                    Camera.Parameters parameters = mCamera.getParameters();
//                    parameters.setPreviewSize(width, height);
//                    mCamera.setParameters(parameters);
                    setPreviewSize(width, height);
                    mCamera.startPreview();
                }
            };
    // シャッターが押されたときに呼ばれるコールバック
    private Camera.ShutterCallback mShutterListener =
            new Camera.ShutterCallback() {
                public void onShutter() {
//                    Toast.makeText(MainActivity.this, "tdst", Toast.LENGTH_SHORT).show();
                }
            };

    // JPEGイメージ生成後に呼ばれるコールバック
    private Camera.PictureCallback mPictureListener =
            new Camera.PictureCallback() {
                public void onPictureTaken(byte[] data, Camera camera) {
                    // SDカードにJPEGデータを保存する
                    if (data != null) {
//                        FileOutputStream myFOS = null;
//                        try {
//                            myFOS = new FileOutputStream(
//                                    "/sdcard/camera_test" + System.currentTimeMillis() + ".jpg");
//                            myFOS.write(data);
//                            myFOS.close();
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
                        Bitmap bmp = null;
                        bmp = BitmapFactory.decodeByteArray(data, 0, data.length);

//                        // 読み込む範囲
//                        int previewWidth = camera.getParameters().getPreviewSize().width;
//                        int previewHeight = camera.getParameters().getPreviewSize().height;
//
//                        // プレビューデータから Bitmap を生成
//                        Bitmap bmp = ImageManager.getBitmapImageFromYUV(data, previewWidth,
//                                previewHeight);
                        ImageManager.addImageAsCamera(getContentResolver(), bmp);

                        camera.startPreview();
                    }
                }
            };

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        if (event.getAction() == MotionEvent.ACTION_DOWN) {
//        shutter();
//        }
//        return true;
        return super.onTouchEvent(event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        holder = mSurfaceView.getHolder();
        holder.addCallback(mSurfaceListener);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shutter();
            }
        });

        // 利用可能なカメラの個数を取得
        numberOfCameras = Camera.getNumberOfCameras();

        // CameraInfoからバックフロントカメラのidを取得
        CameraInfo cameraInfo = new CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
                defaultCameraId = i;
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent == null) {
            return;
        }
        if (CameraShutterReceiver.ACTION_SHUTTER.equals(intent.getAction())) {
            shutter();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        menu.add(Menu.NONE, 1, 0, "camera");
        menu.add(Menu.NONE, 2, 0, "notify");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            // カメラが複数あるかチェック
            if (numberOfCameras == 1) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("no more camera")
                        .setNeutralButton("Close", null);
                AlertDialog alert = builder.create();
                alert.show();
                return true;
            }

            // 現在利用しているカメラを解放
            if (mCamera != null) {
                mCamera.stopPreview();
//                mPreview.setCamera(null);
                mCamera.release();
                mCamera = null;
            }

            // カメラを切り替え
            mCamera = Camera.open(
                    (cameraCurrentlyLocked + 1) % numberOfCameras);
            cameraCurrentlyLocked = (cameraCurrentlyLocked + 1)
                    % numberOfCameras;
//            mPreview.switchCamera(mCamera);
            try {
                mCamera.setDisplayOrientation(90);
                mCamera.setPreviewDisplay(holder);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // プレビュー再開
            mCamera.startPreview();
            return true;
        } else if (item.getItemId() == 1) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, 0);
            return true;
        } else if (item.getItemId() == 2) {
            NotificationUtil.showNotification(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setPreviewSize(int width, int height) {
        Camera.Parameters mParam = mCamera.getParameters();
        boolean portrait = true;
        // Set width & height
        int previewWidth = width;
        int previewHeight = height;
        if (portrait) {
            previewWidth = height;
            previewHeight = width;
        }

        List<Camera.Size> sizes = mParam.getSupportedPreviewSizes();
        int tmpHeight = 0;
        int tmpWidth = 0;
        for (Camera.Size size : sizes) {
            if ((size.width > previewWidth) || (size.height > previewHeight)) {
                continue;
            }
            if (tmpHeight < size.height) {
                tmpWidth = size.width;
                tmpHeight = size.height;
            }
        }
        previewWidth = tmpWidth;
        previewHeight = tmpHeight;

        mParam.setPreviewSize(previewWidth, previewHeight);

        // Adjust SurfaceView size
        ViewGroup.LayoutParams layoutParams = mSurfaceView.getLayoutParams();
        if (portrait) {
            layoutParams.height = previewWidth;
            layoutParams.width = previewHeight;
        } else {
            layoutParams.height = previewHeight;
            layoutParams.width = previewWidth;
        }
        mSurfaceView.setLayoutParams(layoutParams);

//        mCamera.setParameters(mParam);
    }

    private void shutter() {
        if (mCamera != null) {
            mCamera.takePicture(mShutterListener, null, mPictureListener);
        }
    }
}
