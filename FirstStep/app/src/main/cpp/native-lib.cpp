#include <jni.h>
#include <string>
#include <android/log.h>
#include <spdlog/sinks/android_sink.h>
#include <spdlog/spdlog.h>

#define LOG_INFO(...) __android_log_print(ANDROID_LOG_INFO, "first_step_ndk", __VA_ARGS__)
#define SLOG_INFO(...) android_logger->info( __VA_ARGS__ )


auto android_logger = spdlog::android_logger_mt("android", "first_step_ndk");

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_firststep_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    android_logger ->set_pattern(">>>What a lovely %A of %B! %v <<<");
    LOG_INFO("Hello from c++ %d", 2022);
    SLOG_INFO("Hello from spdlog {}", 2022);

    return env->NewStringUTF(hello.c_str());
}