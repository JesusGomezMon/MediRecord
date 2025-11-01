# ProGuard rules optimizadas para MediRecord4

# Mantener información de línea para debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ===== Optimizaciones generales =====
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose

# ===== Retrofit y OkHttp =====
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# ===== Gson =====
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Mantener modelos de API
-keep class com.example.medirecord4.models.** { *; }
-keep class com.example.medirecord4.api.** { *; }

# ===== Firebase =====
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# ===== Coroutines =====
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# ===== WorkManager =====
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker {
    public <init>(...);
}

# ===== SQLite / Database =====
-keep class com.example.medirecord4.DatabaseHelper { *; }
-keep class android.database.** { *; }

# ===== Services y Receivers =====
-keep class com.example.medirecord4.SensorMonitorService { *; }
-keep class com.example.medirecord4.FCMService { *; }
-keep class com.example.medirecord4.NotificationHelper { *; }

# ===== Activities =====
-keep public class * extends android.app.Activity
-keep public class * extends androidx.appcompat.app.AppCompatActivity

# ===== Prevenir warnings =====
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
