#include <jni.h>
#include "darknet.h"
#include "scene.h"
#include "Header.h"
#include <android/bitmap.h>
#include <android/asset_manager_jni.h>
#include <android/asset_manager.h>

#define true JNI_TRUE
#define false JNI_FALSE

//define global param for thread
float image_fv[NO_OF_OBJECTS];      //feature vector - object occurrence
DetectionResult predict_image_result;       //detection result - detected object array

network *net;
char **names;
image **alphabet;

char *cfgfile_str = "/sdcard/yolo/cfg/yolov3-tiny.cfg";
char *datacfg_str = "/sdcard/yolo/cfg/coco.data";
//char *imgfile_str = "/sdcard/yolo/data/dog.jpg";
char *name_list = "/sdcard/yolo/data/coco.names";
char *outimgfile_str = "/sdcard/yolo/out";
char *weightfile_str = "/sdcard/yolo/weights/yolov3-tiny.weights";

char *voc_names[] = {"aeroplane", "bicycle", "bird", "boat", "bottle", "bus", "car", "cat", "chair", "cow", "diningtable", "dog", "horse", "motorbike", "person", "pottedplant", "sheep", "sofa", "train", "tvmonitor"};

double detection_time;

//rewrite test demo for android
image test_detector(char *datacfg, char *cfgfile, char *weightfile, char *filename, float thresh, float hier_thresh, char *outfile, image im)
{
    LOGD("data=%s",datacfg);
    LOGD("cfg=%s",cfgfile);
    LOGD("wei=%s",weightfile);
    LOGD("img=%s",filename);
    LOGE("Begin detector ............");
//    list *options = read_data_cfg(datacfg);
//    char *name_list = "/sdcard/yolo/data/coco.names";//option_find_str(options, "names", "data/names.list");
//    names = get_labels(name_list);
//    alphabet = load_alphabet();
//    net = load_network(cfgfile, weightfile, 0);

    set_batch_network(net, 1);
    srand(2222222);

    char buff[256];
    char *input = buff;
    int j;
    int tsize = 640 * 480;
    float nms=.3;

    while(1){
        if(filename){
//            LOGE("read input file ............");
            strncpy(input, filename, 256);
            im = load_image_color(input,0,0);
        }
        LOGE("Start detector ............");

//        save_image(im, "/sdcard/yolo/out_before");
//        im= rotate_image(im, -1.57079633);
        image sized = letterbox_image(im, net->w, net->h);
        //image sized = resize_image(im, net->w, net->h);
        //image sized2 = resize_max(im, net->w);
        //image sized = crop_image(sized2, -((net->w - sized2.w)/2), -((net->h - sized2.h)/2), net->w, net->h);
        //resize_network(net, sized.w, sized.h);
        layer l = net->layers[net->n-1];
        box *boxes = calloc(l.w*l.h*l.n, sizeof(box));
        float **probs = calloc(l.w*l.h*l.n, sizeof(float *));
        for(j = 0; j < l.w*l.h*l.n; ++j) probs[j] = calloc(l.classes + 1, sizeof(float *));
        float **masks = 0;
        if (l.coords > 4){
            masks = calloc(l.w*l.h*l.n, sizeof(float*));
            for(j = 0; j < l.w*l.h*l.n; ++j) masks[j] = calloc(l.coords-4, sizeof(float *));
        }

        float *X = sized.data;
        detection_time = what_time_is_it_now();
        network_predict(net, X);
        detection_time = what_time_is_it_now() - detection_time;
        int nboxes = 0;

        detection *dets = get_network_boxes(net, im.w, im.h, thresh, hier_thresh, 0, 1, &nboxes);

        //printf("%d\n", nboxes);
        //if (nms) do_nms_obj(boxes, probs, l.w*l.h*l.n, l.classes, nms);
        if (nms) do_nms_sort(dets, nboxes, l.classes, nms);
        draw_detections_v3(im, dets, nboxes, thresh, names, alphabet, l.classes);
        free_detections(dets, nboxes);

        free_image(sized);
        free(boxes);
        free_ptrs((void **)probs, l.w*l.h*l.n);
//        free_network(net);
        break;
    }
    return im;
}



// initialize the network
void
JNICALL
Java_com_example_hellojni_Yolo_inityolo(JNIEnv *env, jobject obj)
{
    LOGE("1");
    names = get_labels(name_list);
    LOGE("2");
    alphabet = load_alphabet();
    LOGE("3");
    net = load_network(cfgfile_str, weightfile_str, 0);
    LOGE("4");
}

jdouble
JNICALL
Java_com_example_hellojni_Yolo_testyolo(JNIEnv *env, jobject obj, jstring imgfile)
{
    double time;
    const char *imgfile_str = (*env)->GetStringUTFChars(env, imgfile, 0);

//    time = test_detector(datacfg_str, cfgfile_str,
//                  weightfile_str, imgfile_str,
//                  0.2f, 0.5f, outimgfile_str, empty);

    (*env)->ReleaseStringUTFChars(env, imgfile, imgfile_str);
    return time;
}

//JNI function to call test detector
JNIEXPORT
jobject
JNICALL
Java_com_example_hellojni_Yolo_testyolo_1bitmap(JNIEnv *env, jobject obj, jstring imgfile, jobject bitmap)
{

    int i,j,k;
    int w, h;
    int c = 3;

//    LOGE("Start testyolo_1bitmap ..................");
    AndroidBitmapInfo srcInfo;
    if (ANDROID_BITMAP_RESULT_SUCCESS != AndroidBitmap_getInfo(env, bitmap, &srcInfo)) {
        LOGE("get bitmap info failed");
        return false;
    }
    void *srcBuf;
    if (ANDROID_BITMAP_RESULT_SUCCESS != AndroidBitmap_lockPixels(env, bitmap, &srcBuf)) {
        LOGE("lock src bitmap failed");
        return false;
    }

    w = srcInfo.width;
    h = srcInfo.height;
    int32_t *srcPixs = (int32_t *) srcBuf;

    int r,g,b;
    int alpha = 0xFF << 24;

    //convert bitmap ARGB_8888 to YOLO image format
    image im = make_image(w, h, c);
    LOGE("make image 1");
    for (j = 0; j < h; j++) {
        for (i = 0; i < w; i++) {
            b = (srcPixs[j*w + i] & 0x00FF0000) >> 16;
            g = (srcPixs[j*w + i] & 0x0000FF00) >> 8;
            r = srcPixs[j*w + i] & 0x000000FF;
//            LOGE("make image 2 r: %x g: %x b: %x", r, g, b);
            im.data[0*h*w + j*w + i] = (float)r/255;        //YOLO image format: byte array r->g->b, range 0-1
            im.data[1*h*w + j*w + i] = (float)g/255;
            im.data[2*h*w + j*w + i] = (float)b/255;
//            LOGE("make image %f", im.data[0*h*w + j*w + i]);
        }
    }
//    LOGE("Before: width %d height %d", w, h);
    im = test_detector(datacfg_str, cfgfile_str, weightfile_str, NULL, 0.2f, 0.5f, outimgfile_str, im);

    w = 640;
    h = 480;
    LOGE("Time: %f", detection_time);

//    LOGE("creating new bitmap...");

    //Return a new bitmap of image containing the object bounding box
    jclass bitmapCls = (*env)->GetObjectClass(env, bitmap);
    jmethodID createBitmapFunction = (*env)->GetStaticMethodID(env, bitmapCls, "createBitmap", "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");
    jstring configName = (*env)->NewStringUTF(env, "ARGB_8888");
    jclass bitmapConfigClass = (*env)->FindClass(env, "android/graphics/Bitmap$Config");
    jmethodID valueOfBitmapConfigFunction = (*env)->GetStaticMethodID(env, bitmapConfigClass, "valueOf", "(Ljava/lang/String;)Landroid/graphics/Bitmap$Config;");
    jobject bitmapConfig = (*env)->CallStaticObjectMethod(env, bitmapConfigClass, valueOfBitmapConfigFunction, configName);
    jobject newBitmap = (*env)->CallStaticObjectMethod(env, bitmapCls, createBitmapFunction, w, h, bitmapConfig);

    void* bitmapPixels;
    if (ANDROID_BITMAP_RESULT_SUCCESS != AndroidBitmap_lockPixels(env, newBitmap, &bitmapPixels)) {
        LOGE("lock dst bitmap failed");
        return false;
    }

    uint32_t* newBitmapPixels = (uint32_t*) bitmapPixels;

    for(j = 0; j < h; ++j){
        for(i = 0; i < w; ++i){
            r = (int)(im.data[0*h*w + j*w + i]*255);
            g = ((int)(im.data[1*h*w + j*w + i]*255)) << 8;
            b = ((int)(im.data[2*h*w + j*w + i]*255)) << 16;
//            LOGE("make image 3 r g b %x %x %x", r,g ,b);
            uint32_t pixel = alpha | b | g | r;
//            LOGE("pixel %d %x", j*w + i, pixel);
            newBitmapPixels[j*w + i] = pixel;
//            LOGE("newBitmapPixels %d %x", j*w + i, newBitmapPixels[j*w + i]);
        }
    }

    memcpy((uint32_t *)bitmapPixels, newBitmapPixels, sizeof(uint32_t) * w * h);
    AndroidBitmap_unlockPixels(env, newBitmap);
//    AndroidBitmap_unlockPixels(env, bitmap);
    free_image(im);
    return newBitmap;
}

//get feature vector of the image after detection
JNIEXPORT
jfloatArray
JNICALL
Java_com_example_hellojni_Yolo_getImageVector(JNIEnv *env, jobject yolo) {

//    for(int i =0 ; i < NO_OF_OBJECTS; i++){
//        LOGE("image_fv_java %d: %f", i, image_fv[i]);
//    }

    jfloatArray jArray;
    jArray = (*env)->NewFloatArray(env, 80);

    if(jArray != NULL) {
        (*env)->SetFloatArrayRegion(env, jArray, 0, 80, image_fv);
    }

    return jArray;
}

//get detection result after detection
JNIEXPORT
void
JNICALL
Java_com_example_hellojni_Yolo_passDetectionResult(JNIEnv *env, jobject obj, jobject detectionResult, jobjectArray imageObjects) {

    int i;

//    LOGE("Start passing result.........");
    jclass deteciontResultClass = (*env)->GetObjectClass(env, detectionResult);
    jfieldID size = (*env)->GetFieldID(env, deteciontResultClass, "size", "I");

    jclass imageObjectClass = (*env)->FindClass(env, "com/example/hellojni/ImageObject");
    jfieldID  object = (*env)->GetFieldID(env, imageObjectClass, "object", "I");
    jfieldID  score = (*env)->GetFieldID(env, imageObjectClass, "score", "F");
    jfieldID  left_x = (*env)->GetFieldID(env, imageObjectClass, "left_x", "F");
    jfieldID  top_y = (*env)->GetFieldID(env, imageObjectClass, "top_y", "F");
    jfieldID  width = (*env)->GetFieldID(env, imageObjectClass, "width", "F");
    jfieldID  height = (*env)->GetFieldID(env, imageObjectClass, "height", "F");

    (*env)->SetIntField(env, detectionResult, size, predict_image_result.size);

    for (i = 0; i < predict_image_result.size; i++){
        jobject imageObject = (*env)->GetObjectArrayElement(env, imageObjects, i);
        (*env)->SetIntField(env, imageObject, object, predict_image_result.image_object_list[i].object);
        (*env)->SetFloatField(env, imageObject, score, predict_image_result.image_object_list[i].score);
        (*env)->SetFloatField(env, imageObject, left_x, predict_image_result.image_object_list[i].left_x);
        (*env)->SetFloatField(env, imageObject, top_y, predict_image_result.image_object_list[i].top_y);
        (*env)->SetFloatField(env, imageObject, width, predict_image_result.image_object_list[i].width);
        (*env)->SetFloatField(env, imageObject, height, predict_image_result.image_object_list[i].height);
    }
    return;
}

//JNIEXPORT
//jint
//JNICALL
//Java_com_example_hellojni_Yolo_getInt(JNIEnv *env, jobject yolo) {
//
//    jint result;
//    result = testing_integer_log;
//    return result;
//}


