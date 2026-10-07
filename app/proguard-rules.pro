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
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

-keep class com.android.apksig.** { *; }
-dontwarn com.android.apksig.**

-keep class com.arkeos.arkclone.engine.CloneInstaller { *; }
-keep class com.arkeos.arkclone.engine.CloneInstallReceiver { *; }
-keep class * extends android.content.BroadcastReceiver { <init>(); }
-keep class * extends android.app.Service { <init>(); }

-keep class kotlin.Metadata { *; }
-keepattributes RuntimeVisibleAnnotations,Signature,InnerClasses,EnclosingMethod

-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }
-keepclassmembers class kotlin.coroutines.SafeContinuation { volatile <fields>; }
-dontwarn kotlinx.coroutines.debug.**
