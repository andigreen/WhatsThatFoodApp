package com.wtf.whatsthatfoodapp.camera;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Size;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.TextureView;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.wtf.whatsthatfoodapp.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

/**
 * Created by Aitor on 03/02/2017.
 */

// The API check is performed before starting the Activity in WelcomeActivity
@TargetApi(21)
public class TakePhotoAPI21Activity extends AppCompatActivity {
    FrameLayout frameLayout;
    TextureView previewView;
    TextureView.SurfaceTextureListener previewViewListener;
    SurfaceTexture previewTexture;
    Surface jpegCaptureSurface;
    CaptureRequest.Builder previewRequest;
    Size[] jpegSizes;
    CameraCharacteristics cc;
    int previewOrientation; // Rotation needed in the sensor in order to be upright
    int correctOrientation; // Rotation needed in the image in order to be saved upright (takes into account previewOrientation and rotation mode)
    CameraManager cameraManager;
    CameraDevice cameraDevice;
    CameraCaptureSession mSession;
    String cameraId;
    Handler handler;
    HandlerThread cameraThread;
    TotalCaptureResult captureResult;
    Button takePhotoBtn;
    Button acceptPhotoBtn;
    Image capturedImage; // Need to pass the capturedImage from camera thread to main
    Size previewSize;
    Size capturedImageSize;
    Bitmap bitmapImage;
    double initialDistance;
    double lengthScreen;
    int pointer1;
    int pointer2;
    Rect zoom;
    int flash = CaptureRequest.CONTROL_AE_MODE_ON; // Flash set to OFF
    OrientationEventListener orientListener;
    IOImage ioImage;

    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.take_photo_api21);
        frameLayout = (FrameLayout) findViewById(R.id.frameLayout);

        orientListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_UI) {
            private int lastOrientation;
            private boolean landscapeMode;
            private Button toggleFlashBtn = (Button)findViewById(R.id.toogleFlashBtn);

            @Override
            public void onOrientationChanged(int i) {
                // Orientation changes at 70 (Portrait to Landscape Right) , 180 (Landscape to Landscape) and 290 (Portrait to Landscape Left) degrees
                // Give 20 degrees to detect changes from one mode to another
                if (i == -1){
                    return;
                }
                if (i >= 80 && i < 170 && !(lastOrientation >= 80 && lastOrientation < 170)){
                    // Landscape Right
                    correctOrientation += 90;
                    lastOrientation = i;
                    landscapeMode = true;

                    RotateAnimation animation = new RotateAnimation(0, -90, takePhotoBtn.getPivotX(), takePhotoBtn.getPivotY());
                    animation.setDuration(100);
                    animation.setRepeatCount(0);
                    animation.setFillAfter(true);
                    takePhotoBtn.startAnimation(animation);
                    animation = new RotateAnimation(0, -90, toggleFlashBtn.getPivotX(), toggleFlashBtn.getPivotY());
                    animation.setDuration(100);
                    animation.setRepeatCount(0);
                    animation.setFillAfter(true);
                    toggleFlashBtn.startAnimation(animation);

                } else if (i >= 190 && i < 280 && !(lastOrientation >= 190 && lastOrientation < 280)){
                    // Landscape Left
                    correctOrientation -= 90;
                    lastOrientation = i;
                    landscapeMode = true;

                    RotateAnimation animation = new RotateAnimation(0, 90, takePhotoBtn.getPivotX(), takePhotoBtn.getPivotY());
                    animation.setDuration(100);
                    animation.setRepeatCount(0);
                    animation.setFillAfter(true);
                    takePhotoBtn.startAnimation(animation);
                    animation = new RotateAnimation(0, 90, toggleFlashBtn.getPivotX(), toggleFlashBtn.getPivotY());
                    animation.setDuration(100);
                    animation.setRepeatCount(0);
                    animation.setFillAfter(true);
                    toggleFlashBtn.startAnimation(animation);

                } else if (landscapeMode && i >= 300 && !(lastOrientation >= 300)){
                    // Portrait coming from Landscape Left
                    landscapeMode = false;
                    correctOrientation = 0;
                    lastOrientation = i;

                    RotateAnimation animation = new RotateAnimation(90, 0, takePhotoBtn.getPivotX(), takePhotoBtn.getPivotY());
                    animation.setDuration(100);
                    animation.setRepeatCount(0);
                    animation.setFillAfter(true);
                    takePhotoBtn.startAnimation(animation);
                    animation = new RotateAnimation(90, 0, toggleFlashBtn.getPivotX(), toggleFlashBtn.getPivotY());
                    animation.setDuration(100);
                    animation.setRepeatCount(0);
                    animation.setFillAfter(true);
                    toggleFlashBtn.startAnimation(animation);

                } else if (landscapeMode && i < 60 &&  !(lastOrientation < 60)){
                    // Portrait coming from Landscape Right
                    landscapeMode = false;
                    correctOrientation = 0;
                    lastOrientation = i;

                    RotateAnimation animation = new RotateAnimation(-90, 0, takePhotoBtn.getPivotX(), takePhotoBtn.getPivotY());
                    animation.setDuration(100);
                    animation.setRepeatCount(0);
                    animation.setFillAfter(true);
                    takePhotoBtn.startAnimation(animation);
                    animation = new RotateAnimation(-90, 0, toggleFlashBtn.getPivotX(), toggleFlashBtn.getPivotY());
                    animation.setDuration(100);
                    animation.setRepeatCount(0);
                    animation.setFillAfter(true);
                    toggleFlashBtn.startAnimation(animation);
                }
            }
        };
        orientListener.enable();

        acceptPhotoBtn = (Button) findViewById(R.id.acceptBtn);
        takePhotoBtn = (Button)findViewById(R.id.takePhotoBtn);
        takePhotoBtn.setTag(0);
        takePhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (takePhotoBtn.getTag().equals(0)){
                    // Take Picture
                    orientListener.disable();
                    // Not clickable until the preview is shown
                    view.setEnabled(false);
                    takePhoto();
                    takePhotoBtn.setTag(1);
                } else {
                    // User cancelled the picture taken
                    openCamera();
                    orientListener.enable();
                    takePhotoBtn.setTag(0);
                    acceptPhotoBtn.clearAnimation();
                    acceptPhotoBtn.setVisibility(View.INVISIBLE);
                    previewView.setVisibility(View.VISIBLE);
                }
            }
        });
        previewView = (TextureView) findViewById(R.id.texture_view);

        final View.OnTouchListener previewViewTouchListener = new View.OnTouchListener() {
            // Handles Camera Zoom
            int zoomLevel = 1;
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN){
                    // First pointer touching the screen
                    // Save pointer pos and id
                    int index = event.getActionIndex();
                    pointer1 = event.getPointerId(index);

                } else if (event.getActionMasked() == MotionEvent.ACTION_UP){
                    // Only pointer goes up, reset data
                    pointer1 = 0;
                } else if (event.getActionMasked() == MotionEvent.ACTION_POINTER_UP){
                    // One of the pointers goes up, if it was pointer1, set pointer2 to be pointer1 now
                    int index = event.getActionIndex();
                    if (pointer1 == event.getPointerId(index)){
                        pointer1 = pointer2;
                    }
                    pointer2 = 0;
                }
                if (event.getPointerCount() == 2){
                    // 2 pointers touching the screen

                    if (event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN){
                        // Second Pointer touching the screen
                        // Save pointer id and calculate the initial euclidean distance between pointers
                        int index = event.getActionIndex();
                        pointer2 = event.getPointerId(index);

                        float X1 = event.getX(pointer1);
                        float Y1 = event.getY(pointer1);
                        float X2 = event.getX(pointer2);
                        float Y2 = event.getY(pointer2);

                        initialDistance = Math.sqrt(Math.pow(X1-X2,2)+Math.pow(Y1-Y2,2))/lengthScreen;
                    }
                    if (event.getActionMasked() == MotionEvent.ACTION_MOVE){
                        // Pointer Moving
                        float X1 = event.getX(pointer1);
                        float Y1 = event.getY(pointer1);
                        float X2 = event.getX(pointer2);
                        float Y2 = event.getY(pointer2);
                        // Calculate the real Euclidean Distance ; real (distance starts 0 (initialDistance) and unrelated to screen size)
                        double distance = (Math.sqrt(Math.pow(X1-X2,2)+Math.pow(Y1-Y2,2)))/lengthScreen - initialDistance;

                        float maxzoom = 0;
                        try{
                            maxzoom = (cc.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM))*10;
                        } catch (NullPointerException e){
                            // No camera characteristics, no zoom, exit listener
                            return false;
                        }


                        boolean update = false;
                        if (distance > 0.01 && zoomLevel<maxzoom){
                            initialDistance += distance;
                            zoomLevel ++;
                            update = true;

                        } else if (distance < -0.01 && zoomLevel > 1){
                            initialDistance += distance;
                            zoomLevel --;
                            update = true;

                        }
                        if (update) {
                            Rect m = cc.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
                            int minW = (int) (m.width() / maxzoom);
                            int minH = (int) (m.height() / maxzoom);
                            int difW = m.width() - minW;
                            int difH = m.height() - minH;
                            int cropW = difW / 100 * zoomLevel;
                            int cropH = difH / 100 * zoomLevel;
                            cropW -= cropW & 3;
                            cropH -= cropH & 3;
                            zoom = new Rect(cropW, cropH, m.width() - cropW, m.height() - cropH);

                            try {

                                previewRequest.set(CaptureRequest.SCALER_CROP_REGION, zoom);
                                mSession.setRepeatingRequest(previewRequest.build(), new CameraCaptureSession.CaptureCallback() {
                                    @Override
                                    public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                                    }
                                }, handler);
                            } catch (CameraAccessException ignored) {
                            }
                        }

                    }
                }
                return true;
            }
        };
        previewViewListener = new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                previewTexture = surfaceTexture;
                try{
                    cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

                    String[] cameraIdList = cameraManager.getCameraIdList();
                    for (String aCameraIdList : cameraIdList) {
                        cc = cameraManager.getCameraCharacteristics(
                                aCameraIdList);
                        if (cc.get(
                                CameraCharacteristics.LENS_FACING) ==
                                CameraCharacteristics.LENS_FACING_BACK) {
                            cameraId = aCameraIdList;
                            break;
                        }
                    }

                    StreamConfigurationMap streamConfigs = cc.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    jpegSizes = streamConfigs.getOutputSizes(ImageFormat.JPEG);



                    capturedImageSize = chooseOptimalSize(jpegSizes,width,height);

                    previewSize = chooseOptimalSize(streamConfigs.getOutputSizes(SurfaceTexture.class),width,height);


                    // Rotation needed in the captured image in order to be upright;
                    // Function correctImageRotation will use this variable
                    try {
                        previewOrientation = cc.get(CameraCharacteristics.SENSOR_ORIENTATION);

                    } catch (NullPointerException e){
                        previewOrientation = 0;
                    }

                    // Preview View Size (ViewTexture width and height) has to follow the ratio from the captured Image size
                    // so the preview does not look distorted and does not differ from the captured image.
                    float imageRatio = (float)(capturedImageSize.getWidth())/capturedImageSize.getHeight();
                    int previewViewWIDTH;
                    int previewViewHEIGHT;

                    previewViewWIDTH = previewView.getWidth();
                    previewViewHEIGHT = (int) (previewViewWIDTH*imageRatio);

                    previewView.setLayoutParams(new FrameLayout.LayoutParams(previewViewWIDTH, previewViewHEIGHT));

                    setButtonsSize(width,height,previewViewWIDTH,previewViewHEIGHT);

                    ImageReader jpegImageReader = ImageReader.newInstance(capturedImageSize.getWidth(), capturedImageSize.getHeight(), ImageFormat.JPEG, 1);
                    jpegCaptureSurface = jpegImageReader.getSurface();

                    ImageReader.OnImageAvailableListener imageReaderListener = new ImageReader.OnImageAvailableListener() {
                        @Override
                        public void onImageAvailable(ImageReader reader) {
                            capturedImage  = reader.acquireLatestImage();

                            // Sent a message using the handler from the Camera Thread to Main Thread
                            // to Display the Preview Image
                            Bundle bundle = new Bundle();
                            bundle.putString("function","photoTaken");
                            Message msg = new Message();
                            msg.setData(bundle);
                            handler.sendMessage(msg);

                        }
                    };
                    jpegImageReader.setOnImageAvailableListener(imageReaderListener,handler);
                    openCamera();
                    lengthScreen = Math.sqrt(Math.pow(previewView.getWidth(),2)+ Math.pow(previewView.getHeight(),2));
                    previewView.setOnTouchListener(previewViewTouchListener);



                }catch(CameraAccessException e){
                    handleCameraAccessNotAccepted();
                }

            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

            }
        };
        previewView.setSurfaceTextureListener(previewViewListener);
    }
    protected void onResume(){
        super.onResume();
        startThread();
        if (previewView.isAvailable()) {
            openCamera();
        } else {
            previewView.setSurfaceTextureListener(previewViewListener);

        }

    }
    protected void onPause(){
        if (cameraDevice != null){
            cameraDevice.close();
        }
        stopThread();
        super.onPause();
    }
    private void openCamera(){
        try {
            cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(CameraDevice camera) {
                    cameraDevice = camera;
                    setButtonsSize(frameLayout.getWidth(),frameLayout.getHeight(),previewView.getWidth(),previewView.getHeight());

                    final Surface previewSurface = new Surface(previewTexture);
                    List<Surface> surfaces = Arrays.asList(previewSurface, jpegCaptureSurface);
                    try {
                        cameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
                            @Override
                            public void onConfigured(CameraCaptureSession session) {
                                mSession = session;

                                try {
                                    previewRequest = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                                    previewRequest.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                    previewRequest.addTarget(previewSurface);

                                    mSession.setRepeatingRequest(previewRequest.build(), new CameraCaptureSession.CaptureCallback() {
                                        @Override
                                        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                                        }
                                    },handler);
                                } catch (CameraAccessException e) {
                                    handleCameraAccessNotAccepted();
                                }
                            }
                            public void onConfigureFailed(CameraCaptureSession session){
                                handleCameraAccessNotAccepted();
                            }

                        },handler);
                    } catch(CameraAccessException e){
                        handleCameraAccessNotAccepted();
                    }
                }
                public void onDisconnected(CameraDevice camera){
                    camera.close();
                }
                public void onError(CameraDevice camera, int error){
                    camera.close();
                }
            },handler);
        } catch (SecurityException | CameraAccessException e){
            // User Didn't Accept Camera Usage
            handleCameraAccessNotAccepted();
        }
    }
    public void takePhoto(){
        try{
            mSession.setRepeatingRequest(previewRequest.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                }
            }, handler);
        } catch(CameraAccessException e){
            handleCameraAccessNotAccepted();
        }
        try{
            CaptureRequest.Builder request = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);

            request.addTarget(jpegCaptureSurface);
            request.set(CaptureRequest.SCALER_CROP_REGION, zoom);
            request.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START);
            request.set(CaptureRequest.CONTROL_AE_MODE, flash);

            mSession.capture(request.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    captureResult = result;
                    cameraDevice.close();
                }
            }, handler);
        } catch (CameraAccessException e){
            handleCameraAccessNotAccepted();
        }
    }
    public void acceptPhoto(View v){
        // Set correct orientation, not preview orientation
        bitmapImage = ioImage.getBitmapImage();
        bitmapImage = ioImage.correctImageRotation(bitmapImage, correctOrientation);
        takePhotoBtn.setVisibility(View.INVISIBLE);
        acceptPhotoBtn.setVisibility(View.INVISIBLE);

        String path = ioImage.saveImage();

        Intent resultIntent = new Intent();
        resultIntent.putExtra("data", path);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    public void handlePhotoTaken(){
        ioImage = new IOImage(this,capturedImage,previewOrientation);

        takePhotoBtn.setEnabled(true);
        acceptPhotoBtn.setVisibility(View.VISIBLE); //bring to front
        // Change the rotation of the button according to the current orientation of the screen
        // The rotate animation starts and finishes at the same degrees (-correctOrientation), it does not move
        RotateAnimation animation = new RotateAnimation(-correctOrientation, -correctOrientation, acceptPhotoBtn.getPivotX(), acceptPhotoBtn.getPivotY());
        animation.setDuration(0);
        animation.setRepeatCount(0);
        animation.setFillAfter(true);
        acceptPhotoBtn.startAnimation(animation);
    }

    public void toggleFlash(View v){
        if (flash == CaptureRequest.CONTROL_AE_MODE_ON){
            flash = CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH;
            ((Button)v).setText("Flash On");
        } else {
            ((Button)v).setText("Flash Off");
            flash = CaptureRequest.CONTROL_AE_MODE_ON;
        }
        previewRequest.set(CaptureRequest.CONTROL_AE_MODE, flash);
        // Trigger needed for flash
        previewRequest.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);

    }
    private static Size chooseOptimalSize(Size[] choices, int width, int height) {
        List<Size> bigEnough = new ArrayList<>();
        for(Size option : choices) {
            if(option.getHeight() == option.getWidth() * height / width &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }
        if(bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizeByArea());
        } else {
            return choices[0];
        }
    }
    private static class CompareSizeByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum( (long)(lhs.getWidth() * lhs.getHeight()) -
                    (long)(rhs.getWidth() * rhs.getHeight()));
        }
    }

    private void handleCameraAccessNotAccepted(){
        Toast.makeText(this,"Could Not Access The Camera",Toast.LENGTH_LONG).show();
        finish();
    }

    private void setButtonsSize(int parentWidth,int parentHeight, int previewWidth, int previewHeight){

        // Adjust Size of the buttons according to preview width and height
        int buttonsWidth = (parentWidth - previewWidth)/3;
        int buttonHeight = (parentHeight - previewHeight);
        Button acceptBtn = (Button)findViewById(R.id.acceptBtn);
        Button takePhotoBtn = (Button)findViewById(R.id.takePhotoBtn);
        Button toggleFlashBtn = (Button)findViewById(R.id.toogleFlashBtn);

        acceptBtn.setWidth(buttonsWidth);
        acceptBtn.setHeight(buttonHeight);
        takePhotoBtn.setWidth(buttonsWidth);
        takePhotoBtn.setHeight(buttonHeight);
        toggleFlashBtn.setWidth(buttonsWidth);
        toggleFlashBtn.setHeight(buttonHeight);
    }
    private class MyHandler extends Handler{
        // This Handler handles the messages sent from the Camera Thread to the Main UI Thread
        // We cannot modify the Views of the Main Thread from the Camera Thread
        private final WeakReference<TakePhotoAPI21Activity> weakReference;

        public MyHandler (Looper looper, TakePhotoAPI21Activity takePicActivity){
            weakReference = new WeakReference<>(takePicActivity);
        }
        @Override
        public void handleMessage(Message msg){
            try{
                if (msg.getData().getString("function").equals("photoTaken")){
                    weakReference.get().handlePhotoTaken();
                }
            } catch (NullPointerException ignored){

            }
        }
    }

    private void startThread(){
        cameraThread = new HandlerThread("Camera Thread",THREAD_PRIORITY_BACKGROUND);
        cameraThread.start();
        handler = new MyHandler(cameraThread.getLooper(),this);

        }

    private void stopThread() {
        cameraThread.quitSafely();
        try {
            cameraThread.join();
            cameraThread = null;
            handler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}