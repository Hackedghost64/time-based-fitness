# --- Optimization & Obfuscation Controls -----------------------------------------
# Enable access modification to permit R8 to widen visibility (e.g. private -> public)
# for aggressive cross-class inlining and devirtualization.
-allowaccessmodification

# Repackage all non-kept classes into a single flat obfuscated package.
-repackageclasses 'com.timebasedfitness.app.o'

# Overload class and member names aggressively.
-overloadaggressively

# Rename source file strings in stack traces to a generic placeholder to prevent reverse engineering
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# --- Belt-and-suspenders for Room ------------------------------------------------
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keepclassmembers class * {
    @androidx.room.* <methods>;
}

# --- Belt-and-suspenders for kotlinx.serialization -------------------------------
-keep @kotlinx.serialization.Serializable class * { *; }
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

# --- Strip Logging & Debug Overhead ----------------------------------------------
# Strip all android.util.Log and System.out/err calls from release bytecode
-assumenosideeffects class android.util.Log {
    public static *** *(...);
}
-assumenosideeffects class java.io.PrintStream {
    public static *** print*(...);
}

# Strip Kotlin runtime null-check parameter strings in release builds
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static void checkNotNullParameter(...);
    public static void checkExpressionValueIsNotNull(...);
    public static void checkParameterIsNotNull(...);
}

