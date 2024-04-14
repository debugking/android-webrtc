# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile


# 保持WebRTCClient类不被混淆
-keep public class com.nightlight.webrtc.WebRTCClient {
    public <methods>;
    public <init>(...);
}

# 保持IPeerConnectionClient接口完全不被混淆
-keep public interface com.nightlight.webrtc.IPeerConnectionClient {
    *;
}

# 保持所有Observer和Adapter类不被混淆
-keep public class com.nightlight.webrtc.*ObserverAdapter {
    *;
}

# 保持HttpSignalingClient中的网络通信方法不被混淆
-keep class com.nightlight.webrtc.HttpSignalingClient {
    public *;
}

# 保持工具类及其静态方法不被混淆
-keep class com.nightlight.webrtc.SDPUtils {
    public static *;
}

# 避免混淆枚举
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 保持日志调用，可以在发布版本中去除
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
}

# 一般混淆规则，去除未使用的代码
-dontoptimize
-dontpreverify
-verbose
