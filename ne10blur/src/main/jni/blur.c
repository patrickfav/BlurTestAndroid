#include <jni.h>
#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <android/bitmap.h>
#include <android/log.h>
#ifdef __ARM_NEON__
#include <cpu-features.h>
#endif // __ARM_NEON__

#include "NE10_types.h"
#include "NE10_macros.h"

#define LOG_TAG "ne10blurlib"
#define LOGV(...)  __android_log_print(ANDROID_LOG_VERBOSE,LOG_TAG,__VA_ARGS__)
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

#define CLASS_RU0XDC_NE10_BLUR "ru0xdc/ne10/Blur"

extern void ne10_img_boxfilter_rgba8888_c (const ne10_uint8_t *src,
        ne10_uint8_t *dst,
        ne10_size_t src_size,
        ne10_int32_t src_stride,
        ne10_int32_t dst_stride,
        ne10_size_t kernel_size);
#ifdef __ARM_NEON__
extern void ne10_img_boxfilter_rgba8888_neon (const ne10_uint8_t *src,
        ne10_uint8_t *dst,
        ne10_size_t src_size,
        ne10_int32_t src_stride,
        ne10_int32_t dst_stride,
        ne10_size_t kernel_size);
#endif // __ARM_NEON__

static void (*ne10_img_boxfilter_rgba8888) (const ne10_uint8_t *src,
        ne10_uint8_t *dst,
        ne10_size_t src_size,
        ne10_int32_t src_stride,
        ne10_int32_t dst_stride,
        ne10_size_t kernel_size);

static int ne10_init() {
    ne10_result_t status, is_NEON_available;

#ifdef __ARM_NEON__
    uint64_t features;
    features = android_getCpuFeatures();
    if (features & ANDROID_CPU_ARM_FEATURE_NEON) {
        is_NEON_available = NE10_OK;
        ne10_img_boxfilter_rgba8888 = ne10_img_boxfilter_rgba8888_neon;
    } else
#endif // __ARM_NEON__
    {
        is_NEON_available = NE10_ERR;
        ne10_img_boxfilter_rgba8888 = ne10_img_boxfilter_rgba8888_c;
    }
    LOGI("Neon: %s", is_NEON_available ? "available" : "not available");

    return JNI_TRUE;
}

static void native_functionToBlur(JNIEnv* env, jclass clzz __attribute__((__unused__)), jobject bitmapOut, jint radius) {
    // Properties
    AndroidBitmapInfo   infoOut;
    void*               pixelsOut;
    int ret;
    ne10_size_t src_size, kernel_size;

    // Get image info
    if ((ret = AndroidBitmap_getInfo(env, bitmapOut, &infoOut)) != 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
        return;
    }

    // Check image
    if (infoOut.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Bitmap format is not RGBA_8888!");
        LOGE("==> %d", infoOut.format);
        return;
    }

    // Lock all images
    if ((ret = AndroidBitmap_lockPixels(env, bitmapOut, &pixelsOut)) != 0) {
        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
        return;
    }

    src_size.x = infoOut.width;
    src_size.y = infoOut.height;
    kernel_size.x = radius <= infoOut.width ? radius : infoOut.width;
    kernel_size.y = radius <= infoOut.height ? radius : infoOut.height;

    //LOGI("size: %ux%u kernel: %ux%u stride: %u", src_size.x, src_size.y, kernel_size.x,
    //        kernel_size.y, src_size.x * 4);

    ne10_img_boxfilter_rgba8888(pixelsOut,
            pixelsOut,
            src_size,
            src_size.x * 4,
            src_size.x * 4,
            kernel_size);

    // Unlocks everything
    AndroidBitmap_unlockPixels(env, bitmapOut);
}

static JNINativeMethod native_methods[] = {
    {"functionToBlur", "(Landroid/graphics/Bitmap;I)V", (void*)native_functionToBlur}
};

static int register_natives(JNIEnv* env) {
    jclass clazz = (*env)->FindClass(env, CLASS_RU0XDC_NE10_BLUR);

    if (clazz == NULL) {
        LOGE("Class " CLASS_RU0XDC_NE10_BLUR " not found");
        return JNI_FALSE;
    }

    if ((*env)->RegisterNatives(env, clazz, native_methods, sizeof(native_methods)
                / sizeof(native_methods[0])) != JNI_OK) {
        LOGE("RegisterNatives() error");
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    JNIEnv* env = NULL;
    jint result = -1;

    (void)reserved;

    LOGV("Entering JNI_OnLoad");

    if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_6) != JNI_OK) {
        LOGE("JNI_VERSION_1_6 not defined");
        goto bail;
    }

    if (!register_natives(env))
        goto bail;

    if (!ne10_init(env))
        goto bail;

    /* success -- return valid version number */
    result = JNI_VERSION_1_6;

bail:
    LOGV("Leaving JNI_OnLoad (result=0x%x)", result);
    return result;
}
