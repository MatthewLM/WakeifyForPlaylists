#  Copyright (c) 2014 Matthew Mitchell
#
#  This file is part of Wakeify for Playlists. It is subject to the license terms
#  in the LICENSE file found in the top-level directory of this
#  distribution and at https://github.com/MatthewLM/WakeifyForPlaylists/blob/master/LICENSE
#  No part of Wakeify for Playlists, including this file, may be copied, modified,
#  propagated, or distributed except according to the terms contained in the LICENSE file.

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := spotify
LOCAL_SRC_FILES := libspotify.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE    := libspotifywrapper
LOCAL_SRC_FILES := run_loop.cpp tasks.cpp jni_glue.cpp logger.cpp sound_driver.cpp
LOCAL_LDLIBS += -llog -lOpenSLES
LOCAL_SHARED_LIBRARIES := libspotify
LOCAL_CPPFLAGS = -std=c++0x -D__STDC_INT64__ -gstabs+
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)

include $(BUILD_SHARED_LIBRARY)
