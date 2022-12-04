package com.example.hellojni;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.os.Environment;
import android.provider.Settings;

import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.Script;
import android.support.v8.renderscript.Type;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.ActivityCompat;


import android.view.MenuItem;

import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


import android.net.Uri;
import android.database.Cursor;
import android.provider.MediaStore;
import android.widget.Toast;

import com.xxxyyy.testcamera2.ScriptC_yuv420888;

import umich.cse.yctung.androidlibsvm.LibSVM;


public class Yolo extends AppCompatActivity {
    private static final int COPY_FALSE = -1;
    private static final int DETECT_FINISH = 1;
    private static final int WRITE_EXTERNAL_STORAGE = 2;
    private static final int RESULT_LOAD_IMAGE = 3;
    private static final int RECORD_AUDIO = 4;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final String TAG = "Yolo";

    TextureView view_srcimg;
    ImageView view_dstimg;
    TextView view_status;

    Bitmap dstimg;
    Bitmap srcimg;
    Bitmap rgbbitmap;
    Bitmap result_bitmap;
    Bitmap rotatedBitmap;
    byte[] bytearr_srcimg;

    String srcimgpath;
    String appFolderPath = "/sdcard/yolo/";
    String dataPredictPath = appFolderPath+"capturedVector.txt ";
    String dataPredictPath_Scale = appFolderPath+"capturedVector_scale.txt ";
    String modelPath = appFolderPath + "data/dictionary_train.model ";
    String outputPath = appFolderPath + "predict.txt";
    String[] testArgs = {dataPredictPath, modelPath, outputPath};
    String[] catergories = { "dummy", "bathroom", "bedroom", "bookstore", "childrenroom", "diningroom", "kitchen" , "livingroom" , "lobby" , "office" , "restaurant" };
    String[] objectCat = {"person", "bench", "backpack", "umbrella", "handbag", "bottle", "wine glass", "cup", "fork", "knife",
            "spoon", "bowl", "chair", "couch", "bed", "dining table", "toilet", "tv", "laptop", "remote",
            "cell phone", "microwave", "oven", "toaster", "sink", "refrigerator", "scissors", "hair drier", "toothbrush",  "pen",
            "carton", "comb"};
    String[] objectCat_COCOC = {"人", "單車", "汽車", "電單車", "飛機", "巴士", "火車", "貨車", "船", "紅綠燈",
            "消防栓", "停止標誌", "咪錶", "長凳", "雀", "貓", "狗", "馬", "羊", "牛",
            "象", "熊", "斑馬", "長頸鹿", "長頸鹿", "雨傘", "手提包", "領帶", "手提箱", "飛盤",
            "滑雪板", "單板滑雪", "波", "風箏", "棒球棒", "棒球手套", "滑板", "衝浪板", "網球拍", "樽",
            "紅酒杯", "杯", "叉", "刀", "勺", "碗", "香蕉", "蘋果", "三明治", "橙",
            "西蘭花", "胡蘿蔔", "熱狗", "比薩", "甜甜圈", "蛋糕", "椅子", "沙發", "盆栽", "床",
            "飯檯", "馬桶", "電視", "手提電腦", "滑鼠", "遙控", "鍵盤", "手機", "微波爐", "焗爐",
            "多士機", "洗手盆", "雪櫃", "書", "鐘", "花瓶", "剪刀", "泰迪熊", "風筒", "牙刷"};
    String[] objectCat_COCO = {"person", "bicycle", "car", "motorbike", "aeroplane", "bus", "train", "truck", "boat", "traffic light",
            "fire hydrant", "stop sign", "parking meter", "bench", "bird", "cat", "dog", "horse", "sheep", "cow",
            "elephant", "bear", "zebra", "giraffe", "backpack", "umbrella", "handbag", "tie", "suitcase", "frisbee",
            "skis", "snowboard", "sports ball", "kite", "baseball bat", "baseball glove", "skateboard", "surfboard", "tennis racket", "bottle",
            "wine glass", "cup", "fork", "knife", "spoon", "bowl", "banana", "apple", "sandwich", "orange",
            "broccoli", "carrot", "hot dog", "pizza", "donut", "cake", "chair", "sofa", "pottedplant", "bed",
            "diningtable", "toilet", "monitor", "laptop", "mouse", "remote", "keyboard", "phone", "microwave", "oven",
            "toaster", "sink", "refrigerator", "book", "clock", "vase", "scissors", "teddy bear", "hair drier", "toothbrush"};
    String[] positionC = {"左上", "上", "右上", "左", "中間", "右", "左下", "下", "右下"};
    String[] position = {"upper left", "up", "upper right", "left", "middle", "right", "lower left", "down", "lower right"};
    private String cameraId;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSessions;
    private Integer sensorOrientation;
    private Integer screenOrientation;
    private CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private ImageReader imageReader;
    private Handler mBackgroundHandler;
    private File file;

    int result_tts;
    LibSVM svm;
    TextToSpeech toSpeech;
    SpeechRecognizer recognizer;
    private static RenderScript rs;
    DetectionResult detectionResult;
    ImageObject[] imageObjects;

    //Runnable runnable;
    int delay = 2500;
    boolean all = false;
    int searchObj = -1;
    int scene = -1;
    int[] xInterval = {213, 426, 640};
    int[] yInterval = {160, 320, 480};

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static{
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("darknetlib");
    }

    CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            cameraDevice.close();
            cameraDevice=null;
        }
    };

    private Handler h = new Handler();
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == DETECT_FINISH) {
                //dstimg = BitmapFactory.decodeFile("/sdcard/yolo/out.png");
                view_dstimg.setImageBitmap(result_bitmap);
                //view_status.setText("run time = " + (double)msg.obj + "s");
            }
            else if (msg.what == COPY_FALSE) {

            }
        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            captureImage();
            h.postDelayed(this, delay);
        }
    };

    public Yolo() {
        //srcimgpath = "/sdcard/yolo/data/dog.jpg";
    }

    public void copyFilesFassets(Context context, String oldPath, String newPath) {
        try {
            String fileNames[] = context.getAssets().list(oldPath); //get all the assets filename in oldPath
            if (fileNames.length > 0) { //directory
                File file = new File(newPath);
                file.mkdirs();
                for (String fileName : fileNames) {
                    copyFilesFassets(context,oldPath + "/" + fileName,newPath+"/"+fileName);
                }
            } else {    //sinle file
                InputStream is = context.getAssets().open(oldPath);
                FileOutputStream fos = new FileOutputStream(new File(newPath));
                byte[] buffer = new byte[1024];
                int byteCount=0;
                while((byteCount=is.read(buffer))!=-1) {
                    fos.write(buffer, 0, byteCount);
                }
                fos.flush();
                is.close();
                fos.close();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            mHandler.sendEmptyMessage(COPY_FALSE);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yolo);

        //Result Array Initialization
        imageObjects = new ImageObject[15];
        for (int i = 0; i < 15; i++) imageObjects[i] = new ImageObject();
        detectionResult = new DetectionResult(imageObjects);

        rs = RenderScript.create(this);
        //svm = new LibSVM();

        checkPermission();

        //initialize TextToSpeech
        toSpeech = new TextToSpeech(Yolo.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS)
                    result_tts = toSpeech.setLanguage(Locale.UK);
                else
                    Toast.makeText(getApplicationContext(),"Feature not supported in your device",Toast.LENGTH_SHORT).show();
            }
        });

        //initialize SpeechRecognizer
        recognizer = SpeechRecognizer.createSpeechRecognizer(Yolo.this);
        final Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        recognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {}

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {}

            @Override
            public void onError(int error) {}

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results
                        .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String str = matches.get(0);
                view_status.setText(str);
                str = str.toLowerCase();
                Log.e("Speech", str);

                if(str.equals("開始") || str.equals("start")) {
                    toSpeech.speak(str, TextToSpeech.QUEUE_FLUSH, null, null);
                    h.postDelayed(runnable, delay);
                }
                else if (str.equals("停") || str.equals("stop")) {
                    h.removeCallbacks(runnable);
                    toSpeech.speak(str, TextToSpeech.QUEUE_FLUSH, null, null);
                }
                else if (str.equals("場景") || str.equals("scene")) {
                    if (scene == -1) scene = 1;
                    else if (scene == 1) scene = -1;
                    toSpeech.speak(str, TextToSpeech.QUEUE_FLUSH, null, null);
                }
                else if (str.equals("capture")) {
                    toSpeech.speak(str, TextToSpeech.QUEUE_FLUSH, null, null);
                    captureImage();
                }
                else if (str.equals("all")) {
                    toSpeech.speak(str, TextToSpeech.QUEUE_FLUSH, null, null);
                    all = !all;
                }
                for (int i = 0; i < objectCat_COCOC.length; i++) {
                    if(str.equals(objectCat_COCOC[i]) || str.equals(objectCat_COCO[i])) {
                        searchObj = i;
                        Toast.makeText(getApplicationContext(),str + i,Toast.LENGTH_SHORT).show();
                        toSpeech.speak(objectCat_COCO[i], TextToSpeech.QUEUE_FLUSH, null, null);
                    }
                }

            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });
        //recognizer.startListening(recognizerIntent);
        findViewById(R.id.voice_cm).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_UP:
                        recognizer.stopListening();
                        view_status.setText("Stop");
                        break;

                    case MotionEvent.ACTION_DOWN:
                        recognizer.startListening(recognizerIntent);
                        view_status.setText("Listening");
                        break;
                }
                return false;
            }
        });

        view_srcimg = findViewById(R.id.srcimg);
        view_dstimg = findViewById(R.id.dstimg);
        view_status = findViewById(R.id.status);
        assert view_srcimg != null;

        view_srcimg.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                openCamera();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });

//        dstimg = BitmapFactory.decodeFile("/sdcard/out.png");
//        view_dstimg.setImageBitmap(dstimg);
//        view_dstimg.setScaleType(ImageView.ScaleType.FIT_XY);

        inityolo();
        toSpeech.speak("ready", TextToSpeech.QUEUE_FLUSH, null, null);

    }


    private void checkPermission() {
        if(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CAMERA_PERMISSION);
        }
        if(ActivityCompat.checkSelfPermission(Yolo.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
        {
                ActivityCompat.requestPermissions(Yolo.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        WRITE_EXTERNAL_STORAGE);
        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)) {
//                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
//                        Uri.parse("package:" + getPackageName()));
//                startActivity(intent);
//                finish();
//            }
//        }
        if(ActivityCompat.checkSelfPermission(Yolo.this,
                Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED)
        {
                ActivityCompat.requestPermissions(Yolo.this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        RECORD_AUDIO);
        }

    }

//    private String getImagePath(Uri uri, String selection) {
//        String path = null;
//        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
//        if (cursor != null) {
//            if (cursor.moveToFirst()) {
//                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
//            }
//            cursor.close();
//        }
//        return path;
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //LOAD button
//        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
//            Uri uri = data.getData();
//            srcimgpath = getImagePath(uri, null);
//            view_status.setText("selectfile = " + srcimgpath);
//            srcimg = BitmapFactory.decodeFile(srcimgpath);
            //view_srcimg.setImageBitmap(srcimg);
//        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    //return direction of the object
    public String getObjPosition(float mid_x, float mid_y) {
        String s = null;
        if (mid_x <= xInterval[0]) {
            if (mid_y <= yInterval[0]) s = position[0];
            else if (mid_y <= yInterval[1]) s = position[3];
            else if (mid_y <= yInterval[2]) s = position[6];
        }
        else if (mid_x <= xInterval[1]) {
            if (mid_y <= yInterval[0]) s = position[1];
            else if (mid_y <= yInterval[1]) s = position[4];
            else if (mid_y <= yInterval[2]) s = position[7];
        }
        else if (mid_x <= xInterval[2]) {
            if (mid_y <= yInterval[0]) s = position[2];
            else if (mid_y <= yInterval[1]) s = position[5];
            else if (mid_y <= yInterval[2]) s = position[8];
        }
        return s;
    }
    public void yoloDetect(){

        new Thread(new Runnable() {
            public void run() {
                int i;
                long tStartDetection = System.currentTimeMillis();
                result_bitmap = testyolo_bitmap(srcimgpath, rgbbitmap);
                long tEndDetection = System.currentTimeMillis();
                Log.e("Object Detection Time", String.format("%f", (float)(tEndDetection - tStartDetection)/1000));
                passDetectionResult(detectionResult, imageObjects);
                for(i = 0; i < detectionResult.getSize(); i++){
                    imageObjects[i].calMidXY();
                }
//                Log.e("Prediction size", Integer.toString(detectionResult.getSize()));
//                for (i = 0; i < detectionResult.getSize(); i++){
//                    Log.e("Prediction result", String.format("Object:%d  Score:%f  left_x:%f  top_y:%f  width:%f  height:%f",
//                            imageObjects[i].getObject(), imageObjects[i].getScore(), imageObjects[i].getLeft_x(),
//                            imageObjects[i].getTop_y(), imageObjects[i].getWidth(), imageObjects[i].getHeight()));
//                }
//                try (FileOutputStream out = new FileOutputStream("/sdcard/yolo/out_result.jpg")) {
//                    result_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
//                    // PNG is a lossless format, the compression factor (100) is ignored
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                Log.e("Object result", String.format("searchObj: %d object: %d midx: %f midy: %f", searchObj, imageObjects[0].getObject(), imageObjects[0].getMid_x(), imageObjects[0].getMid_y()));
                String pos;
                boolean found = false;
                for (i = 0; i < detectionResult.getSize(); i++) {
                    if (imageObjects[i].getObject() == searchObj) {
                        found = true;
                        float mid_x = imageObjects[i].getMid_x();
                        float mid_y = imageObjects[i].getMid_y();
                        pos = getObjPosition(mid_x, mid_y);
                        toSpeech.speak(pos, TextToSpeech.QUEUE_ADD, null, null);    //output obj direction
                        Log.e("position", pos);
                    }
                }
                if (!found && searchObj != -1)
                    toSpeech.speak("no", TextToSpeech.QUEUE_ADD, null, null);   //output no if obj doesn't exist
                long tEndDetectionResult = System.currentTimeMillis();
                Log.e("Object Result Time", String.format("%f", (float)(tEndDetectionResult - tEndDetection)/1000));

                long tStartScene = System.currentTimeMillis();
                //SVM scene recognition
                float[] imageVector = getImageVector();
//                for(i =0; i< 80; i++)
//                {
//                    Log.e("Image Vector", String.format("[%d] %f", i, imageVector[i]));
//                }
                float fv_max = imageVector[0];
                float fv_min = imageVector[0];
//                Log.e("Initial Max Min", String.format("Max: %f Min: %f", fv_max, fv_min));
                //find min and max in the captured image feature vector
                for (i = 1; i < imageVector.length; i++)
                {
                    if ( Float.compare(imageVector[i], fv_max) > 0 ) fv_max = imageVector[i];
                    else if (Float.compare(imageVector[i], fv_min) < 0) fv_min = imageVector[i];
//                    Log.e("Max Min", String.format("imageVector[%d}: %f Max: %f Min: %f", i, imageVector[i], fv_max, fv_min));
                }
//                Log.e("Image Vector", String.format("Max: %f    Min: %f", fv_max, fv_min));
                //feature scaling for SVM
                for (i = 0; i < imageVector.length; i++)
                {
                    imageVector[i] = -1 + 2 * (imageVector[i] - fv_min) / (fv_max - fv_min);
                }
//                for(i =0; i< 80; i++)
//                {
//                    Log.e("Image Vector Normalized", String.format("[%d] %f", i, imageVector[i]));
//                }

                //output txt for LibSVM
                try {
                    FileWriter writer = new FileWriter("/sdcard/yolo/capturedVector.txt", false);
                    writer.write("2 ");

                    for(i=0; i<80; i++){
                        writer.write( (i+1) + ":" + imageVector[i] + " ");
                    }
                    writer.write('\n');
                    writer.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

                if (scene == 1) {
                    String dataPredictPath = "/sdcard/yolo/capturedVector.txt /sdcard/yolo/data/dictionary_train.model /sdcard/yolo/predict.txt";
                    String outputPath = "/sdcard/yolo/predict.txt";

//                    Log.e("Start scale", "............");
                    LibSVM svm = LibSVM.getInstance();
                    //svm.scale( dataPath, dataPath_Scale);
//                    Log.e("Start predict", "............");
                    //svm.predict(dataPredictPath + modelPath + outputPath);
                    svm.predict(dataPredictPath);
//                    Log.e("End predict", "............");
                    //read result file generated by LibSVM
                    File file_res = new File (outputPath);
                    StringBuilder text = new StringBuilder();
                    String result = "";

                    try
                    {
//                        Log.e("Start Reading", "..............");
                        BufferedReader bufferedReader = new BufferedReader(new FileReader(file_res));
                        result = bufferedReader.readLine();
                        bufferedReader.close();
                    }
                    catch (IOException e)
                    {
                        Log.e("Failed reading result", "Failed");
                    }

                    //output the result via speaker
                    if(result != null)
                    {
                        int result_category_no = Integer.parseInt(result);
                        String result_category_str = catergories[result_category_no];
                        long tEndScene = System.currentTimeMillis();
                        Log.e("Scene Result Time", String.format("%f", (float)(tEndScene - tStartScene)/1000));
                        view_status.setText("Result = " + result_category_str);
                        toSpeech.speak(result_category_str, TextToSpeech.QUEUE_ADD, null, null);

                    }
                    else
                    {
                        view_status.setText("Result = Fail");
                    }
                }


                Message msg = new Message();
                msg.what = DETECT_FINISH;
                //msg.obj = runtime;
                mHandler.sendMessage(msg);
            }
        }).start();

    }

    //ASSET button function
    public void assetClick(View v){
        view_status.setText("exact model, please wait");
        copyFilesFassets(this, "cfg", "/sdcard/yolo/cfg");
        copyFilesFassets(this, "data", "/sdcard/yolo/data");
        copyFilesFassets(this, "weights", "/sdcard/yolo/weights");
        view_status.setText("exact model finish");
    }

    public void captureClick(View v){
        captureImage();
    }

//    public void selectimgClick(View v){
//        Intent i = new Intent(
//                Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        startActivityForResult(i, RESULT_LOAD_IMAGE);
//    }

    private void createCameraPreview() {
        try{
            SurfaceTexture texture = view_srcimg.getSurfaceTexture();
            assert  texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(),imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    if(cameraDevice == null)
                        return;
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(Yolo.this, "Changed", Toast.LENGTH_SHORT).show();
                }
            },null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void updatePreview() {
        if(cameraDevice == null)
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE,CaptureRequest.CONTROL_MODE_AUTO);
        try{
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(),null,mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private int getScreenOrientation() {
        switch (getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_270:
                return 270;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_90:
                return 90;
            default:
                return 0;
        }
    }

    private void openCamera() {
        CameraManager manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        try{
            cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            Log.e("sensor orientation", Integer.toString(sensorOrientation));
            screenOrientation = getScreenOrientation();
            Log.e("screen orientation", Integer.toString(screenOrientation));
            //int orientation = getResources().getConfiguration().orientation;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            //Check realtime permission if run higher API 23

            manager.openCamera(cameraId,stateCallback,null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void captureImage () {
        if(cameraDevice == null)
            return;
        CameraManager manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        try{
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());

            //Capture image with custom size
            final int width = 640;
            final int height = 480;

            final ImageReader reader = ImageReader.newInstance(width,height,ImageFormat.YUV_420_888,1);
            List<Surface> outputSurface = new ArrayList<>(2);
            outputSurface.add(reader.getSurface());
            outputSurface.add(new Surface(view_srcimg.getSurfaceTexture()));

            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            //Check orientation base on device
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION,ORIENTATIONS.get(rotation));
//            file = new File(Environment.getExternalStorageDirectory()+"/capturedYolo.jpg");
            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener()
            {
                @Override
                public void onImageAvailable(ImageReader imageReader)
                {
                    Toast.makeText(Yolo.this, "Image Available", Toast.LENGTH_SHORT).show();
                    Image image = null;
                    image = reader.acquireLatestImage();
                    rgbbitmap = YUV_420_888_toRGB(image, width, height);
                    int orientation = sensorOrientation - screenOrientation;
                    Log.e("orientation", String.format("orientation: %d   sensorOrientation: %d   screenOrientation: %d",
                            orientation,sensorOrientation,screenOrientation));
                    Matrix matrix = new Matrix();
                    matrix.postRotate(orientation);
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(rgbbitmap, height, width, true);
                    rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
                    rgbbitmap = rotatedBitmap;
//                    try (FileOutputStream out = new FileOutputStream("/sdcard/yolo/out_Camera.png")) {
//                        rgbbitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
//                        // PNG is a lossless format, the compression factor (100) is ignored
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                    yoloDetect();
                    if(image != null) image.close();
                }
                private void save(byte[] bytes) throws IOException {
                    OutputStream outputStream = null;
                    try{
                        outputStream = new FileOutputStream(file);
                        outputStream.write(bytes);
                    }finally {
                        if(outputStream != null)
                            outputStream.close();
                    }
                }
            };

            reader.setOnImageAvailableListener(readerListener,mBackgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
//                    Toast.makeText(Yolo.this, "Saved "+file, Toast.LENGTH_SHORT).show();
                    createCameraPreview();
                }
            };

            cameraDevice.createCaptureSession(outputSurface, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {

                    try{
                        cameraCaptureSession.capture(captureBuilder.build(),captureListener,mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                }
            },mBackgroundHandler);


        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private Bitmap YUV_420_888_toRGB(Image image, int width, int height){
        // Get the three image planes
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();
        byte[] y = new byte[buffer.remaining()];
        buffer.get(y);

        buffer = planes[1].getBuffer();
        byte[] u = new byte[buffer.remaining()];
        buffer.get(u);

        buffer = planes[2].getBuffer();
        byte[] v = new byte[buffer.remaining()];
        buffer.get(v);

        // get the relevant RowStrides and PixelStrides
        // (we know from documentation that PixelStride is 1 for y)
        int yRowStride= planes[0].getRowStride();
        int uvRowStride= planes[1].getRowStride();  // we know from   documentation that RowStride is the same for u and v.
        int uvPixelStride= planes[1].getPixelStride();  // we know from   documentation that PixelStride is the same for u and v.


        // rs creation just for demo. Create rs just once in onCreate and use it again.

        RenderScript rs = Yolo.rs;
        ScriptC_yuv420888 mYuv420=new ScriptC_yuv420888 (rs);

        // Y,U,V are defined as global allocations, the out-Allocation is the Bitmap.
        // Note also that uAlloc and vAlloc are 1-dimensional while yAlloc is 2-dimensional.
        Type.Builder typeUcharY = new Type.Builder(rs, Element.U8(rs));
        typeUcharY.setX(yRowStride).setY(height);
        Allocation yAlloc = Allocation.createTyped(rs, typeUcharY.create());
        yAlloc.copyFrom(y);
        mYuv420.set_ypsIn(yAlloc);

        Type.Builder typeUcharUV = new Type.Builder(rs, Element.U8(rs));
        // note that the size of the u's and v's are as follows:
        //      (  (width/2)*PixelStride + padding  ) * (height/2)
        // =    (RowStride                          ) * (height/2)
        // but I noted that on the S7 it is 1 less...
        typeUcharUV.setX(u.length);
        Allocation uAlloc = Allocation.createTyped(rs, typeUcharUV.create());
        uAlloc.copyFrom(u);
        mYuv420.set_uIn(uAlloc);

        Allocation vAlloc = Allocation.createTyped(rs, typeUcharUV.create());
        vAlloc.copyFrom(v);
        mYuv420.set_vIn(vAlloc);

        // handover parameters
        mYuv420.set_picWidth(width);
        mYuv420.set_uvRowStride (uvRowStride);
        mYuv420.set_uvPixelStride (uvPixelStride);

        Bitmap outBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Allocation outAlloc = Allocation.createFromBitmap(rs, outBitmap, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);

        Script.LaunchOptions lo = new Script.LaunchOptions();
        lo.setX(0, width);  // by this we ignore the y’s padding zone, i.e. the right side of x between width and yRowStride
        lo.setY(0, height);

        mYuv420.forEach_doConvert(outAlloc,lo);
        outAlloc.copyTo(outBitmap);

        return outBitmap;
    }
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native void inityolo();
    public native double testyolo(String imgfile);
    public native Bitmap testyolo_bitmap(String imgfile, Bitmap bmp);
    public native float[] getImageVector();
    public native void passDetectionResult(DetectionResult detectionResult, ImageObject[] imageObjects);
    //public native int getInt();

}

