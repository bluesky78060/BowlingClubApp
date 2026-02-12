# ============================================
# BowlingClubApp ProGuard Rules
# ============================================
# Comprehensive obfuscation and optimization
# rules for release builds

# --- General Android & Core ---
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep BuildConfig for BuildConfig.DEBUG checks
-keep class com.bowlingclub.app.BuildConfig { *; }

# --- Kotlin ---
-dontwarn kotlin.**
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# Keep Kotlin standard library exceptions

# --- Kotlin Coroutines ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# --- Room Database ---
# Keep all entity classes
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers @androidx.room.Entity class * { *; }

# Keep all DAO interfaces
-keep @androidx.room.Dao interface *
-keepclassmembers @androidx.room.Dao interface * { *; }

# Keep database classes
-keep class com.bowlingclub.app.data.local.BowlingClubDatabase { *; }
-keep class com.bowlingclub.app.data.local.BowlingClubDatabase_Impl { *; }

# Keep all DAO implementations
-keep class com.bowlingclub.app.data.local.dao.*Dao_Impl { *; }

# --- Hilt/Dagger ---
# Keep Hilt-related classes
-keep class com.google.dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class dagger.** { *; }

# Keep Hilt generated classes
-keep class * extends dagger.internal.Binding
-keep class * extends dagger.internal.ModuleAdapter
-keep class * extends dagger.internal.StaticInjection
-keep class **_Factory { *; }
-keep class **_Factory$InstanceHolder { *; }
-keep class **_Provide* { *; }
-keep class **_MembersInjector { *; }

# Keep @HiltViewModel
-keepclasseswithmembers class * {
    @dagger.hilt.android.lifecycle.HiltViewModel <init>(...);
}

# Keep Hilt module classes
-keep class * extends dagger.Module
-keepclassmembers class * {
    @dagger.* <fields>;
}
-keepclassmembers class * {
    @dagger.* <methods>;
}

# --- Retrofit/OkHttp ---
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations

# Keep Retrofit service interfaces and annotations
-keep interface * {
    @retrofit2.http.* <methods>;
}
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Keep Response class
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class retrofit2.Call { *; }

# Keep converter classes
-keep class retrofit2.converter.gson.** { *; }
-keep class retrofit2.adapter.** { *; }

# Retrofit warnings suppression
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# --- Gson ---
# Keep all @SerializedName annotations
-keepattributes SerializedName

# Keep Gson classes
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep fields with @SerializedName
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep entity classes used with Gson (for OCR/JSON parsing)
-keep class com.bowlingclub.app.data.local.entity.** { *; }
-keep class com.bowlingclub.app.data.model.** { *; }
-keep class com.bowlingclub.app.data.remote.** { *; }

# --- Jetpack Compose ---
-dontwarn androidx.compose.**
-keep class androidx.compose.runtime.** { *; }
-keep interface androidx.compose.runtime.** { *; }

# --- Jetpack Core ---
-keep class androidx.lifecycle.** { *; }
-keep interface androidx.lifecycle.** { *; }
-keep class androidx.navigation.** { *; }
-keep class androidx.activity.** { *; }

# --- CameraX ---
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

# --- WorkManager ---
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class androidx.work.** { *; }

# Keep BackupWorker class
-keep class com.bowlingclub.app.work.** { *; }

# --- Security Crypto ---
-keep class androidx.security.crypto.** { *; }

# --- Coil Image Loading ---
-dontwarn coil.**
-keep class coil.** { *; }
-keep interface coil.** { *; }

# --- SplashScreen ---
-keep class androidx.core.splashscreen.** { *; }

# --- MPAndroidChart ---
-keep class com.github.mikephil.charting.** { *; }
-dontwarn com.github.mikephil.charting.**

# --- App-Specific Classes ---

# Keep all ViewModel classes
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class com.bowlingclub.app.viewmodel.** {
    public <methods>;
}

# Keep UI State classes
-keep class com.bowlingclub.app.viewmodel.*UiState { *; }
-keepclassmembers class com.bowlingclub.app.viewmodel.*UiState {
    *;
}

# Keep enum classes used in ViewModels
-keep enum com.bowlingclub.app.viewmodel.PinLockMode { *; }
-keep enum com.bowlingclub.app.viewmodel.PinSetupStep { *; }

# Keep Repository classes
-keep class * extends com.bowlingclub.app.data.repository.** {
    public <methods>;
}
-keep interface com.bowlingclub.app.data.repository.** { *; }

# Keep all entity classes (critical for Room)
-keep class com.bowlingclub.app.data.local.entity.Member { *; }
-keep class com.bowlingclub.app.data.local.entity.Tournament { *; }
-keep class com.bowlingclub.app.data.local.entity.TournamentParticipant { *; }
-keep class com.bowlingclub.app.data.local.entity.GameScore { *; }
-keep class com.bowlingclub.app.data.local.entity.Team { *; }
-keep class com.bowlingclub.app.data.local.entity.TeamMember { *; }
-keep class com.bowlingclub.app.data.local.entity.Setting { *; }

# Keep DAO classes
-keep class com.bowlingclub.app.data.local.dao.** { *; }

# Keep data converter classes
-keep class com.bowlingclub.app.data.local.converter.** { *; }

# Keep utility classes
-keep class com.bowlingclub.app.util.** {
    public <methods>;
}

# Keep OCR-related classes
-keep class com.bowlingclub.app.data.remote.OcrConfig { *; }
-keep class com.bowlingclub.app.data.remote.OcrModels** { *; }
-keep class com.bowlingclub.app.data.remote.OcrApiService { *; }
-keep class com.bowlingclub.app.data.remote.OcrRepository { *; }
-keep class com.bowlingclub.app.data.remote.OcrResultParser { *; }

# Keep DI module classes
-keep class com.bowlingclub.app.di.** {
    public <methods>;
}

# Keep main application class
-keep class com.bowlingclub.app.BowlingClubApp { *; }

# --- Suppress Warnings ---
-dontwarn java.lang.invoke.LambdaForm
-dontwarn sun.misc.**
-dontwarn com.sun.**

# --- Optimization Rules ---
# Optimization level
-optimizationpasses 5

# Keep main method entries
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep constructor of enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    public static final ** *;
}

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep Serializable implementations
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# --- Debugging Info ---
# Uncomment for debugging (only in debug builds, not release)
# -printmapping mapping.txt
# -printseeds seeds.txt
# -printusage usage.txt
# -printconfiguration configuration.txt

# --- Strip Debug Logs in Release ---
-assumenosideeffects class android.util.Log {
    public static int d(...);
    public static int v(...);
}

# ============================================
# END OF PROGUARD RULES
# ============================================
