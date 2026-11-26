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

# Keep line numbers for Play Console crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Kotlinx serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class ** {
    @kotlinx.serialization.SerialName <fields>;
}

# Ktor + OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn io.ktor.**
-keep class io.ktor.** { *; }

# App models used in JSON
-keep class com.mylaptopmarket.** { *; }
-keep class com.google.firebase.messaging.** { *; }

-keep class com.mylaptopmarket.util.VendorNotificationWorker { *; }

# Compose / Coil / Media3
-dontwarn androidx.compose.**
-keep class coil3.** { *; }
-dontwarn coil3.**
-keep class androidx.media3.** { *; }
-keep class com.pierfrancescosoffritti.** { *; }