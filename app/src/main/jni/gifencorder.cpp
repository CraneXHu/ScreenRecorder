#include "com_pkhope_screenrecorder_NativeGifEncorder.h"
#include "gif.h"
#include <android/bitmap.h>

static GifWriter g_gifWriter;
static int g_iDelay;

JNIEXPORT void JNICALL Java_com_pkhope_screenrecorder_Recorder_NativeGifEncorder_test
        (JNIEnv *env, jclass obj){

   env->NewStringUTF("This just a test for Android Studio NDK JNI developer!");
}
JNIEXPORT jboolean JNICALL Java_com_pkhope_screenrecorder_Recorder_NativeGifEncorder_open
        (JNIEnv *env, jclass obj, jstring pathObj, jint width, jint height, jint delay){
    g_iDelay = delay;
    const char *path = env->GetStringUTFChars(pathObj,NULL);
    bool  result = GifBegin(&g_gifWriter,path,width,height,8);
    env->ReleaseStringUTFChars(pathObj,path);
    return  result;
}

JNIEXPORT jboolean JNICALL Java_com_pkhope_screenrecorder_Recorder_NativeGifEncorder_addFrame
        (JNIEnv *env, jclass obj, jobject bmpObj){
    AndroidBitmapInfo bmpInfo={0};
    if(AndroidBitmap_getInfo(env,bmpObj,&bmpInfo) < 0){
        return false;
    }

    int width = bmpInfo.width;
    int height = bmpInfo.height;

    unsigned char *pixels = 0;
    if (AndroidBitmap_lockPixels(env,bmpObj,(void**)&pixels) < 0){
        return false;
    }

    bool result = GifWriteFrame(&g_gifWriter,pixels,width,height,g_iDelay);

    AndroidBitmap_unlockPixels(env,bmpObj);

    return result;
}

JNIEXPORT jboolean JNICALL Java_com_pkhope_screenrecorder_Recorder_NativeGifEncorder_save
        (JNIEnv *env, jclass obj){
    bool result = GifEnd(&g_gifWriter);
    return result;
}

