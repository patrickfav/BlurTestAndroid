LOCAL_PATH:= $(call my-dir)

ne10_neon_source := \
    NE10_boxfilter_neon.c

ne10_source_files := \
    NE10_boxfilter.c

include $(CLEAR_VARS)

LOCAL_CFLAGS   := -D_ARM_ASSEM_ -fvisibility=hidden -O3 -ffast-math
LOCAL_ARM_MODE := arm
LOCAL_LDLIBS   += -llog -ljnigraphics
LOCAL_SRC_FILES :=  \
    $(ne10_source_files) \
    blur.c

ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
    LOCAL_SRC_FILES += $(ne10_neon_source)
    LOCAL_STATIC_LIBRARIES += cpufeatures
    LOCAL_ARM_NEON := true
endif

LOCAL_MODULE := libne10blur

include $(BUILD_SHARED_LIBRARY)
$(call import-module,cpufeatures)
