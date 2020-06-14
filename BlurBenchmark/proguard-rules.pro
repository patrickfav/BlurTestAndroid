# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:/Program Files/android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the ProGuard
# include property in project.properties.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}


#keep jackson
-keepnames class com.fasterxml.jackson.** { *; }
-dontwarn com.fasterxml.jackson.databind.**

#keep picasso
-keep class com.jjoe64.** { *; }
-keep interface com.jjoe64.** { *; }

#keep picasso
-keep class com.squareup.** { *; }
-keep interface com.squareup.** { *; }
-dontwarn com.squareup.okhttp.**

#keep renderscript
-keep class androidx.renderscript.** {
    native <methods>;
}

-keep class at.favre.app.blurbenchmark.models.** {*;}
-keep class at.favre.app.blurbenchmark.blur.** {*;}

#-keep interface at.favre.app.** {*;}
#-keep interface at.favre.app.** {*;}

# get rid of the logging stuff
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
}

-keep public class at.favre.app.blurbenchmark.BuildConfig {public static *;}
