# App-specific R8 rules.
#
# Keep this file narrow. Room, Hilt, Compose, and kotlinx.serialization expose
# their own consumer rules. Add a keep rule only when a minified release has a
# verified runtime issue that reflection/code generation cannot otherwise solve.

# --- Belt-and-suspenders for Room ------------------------------------------------
# Room generates implementations for @Dao interfaces and uses reflection over
# @Entity field names. Consumer rules normally cover this, but an explicit keep
# ensures a minified release cannot strip a renamed @Entity field.
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keepclassmembers class * {
    @androidx.room.* <methods>;
}

# --- Belt-and-suspenders for kotlinx.serialization -------------------------------
# @Serializable data classes are accessed by generated $serializer companions and
# by name-based polymorphic lookup. Consumer rules normally cover this.
-keep @kotlinx.serialization.Serializable class * { *; }
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

# --- App-side Intent extras ------------------------------------------------------
# RoutineReminderReceiver reads the EXTRA_CATEGORY string. If R8 were to rename
# it, the receiver would silently receive null. The string constant is already
# pinned in NotificationScheduler.kt; no additional rule is required here.

# --- Logging hygiene -------------------------------------------------------------
# Strip android.util.Log.{v,d,i} calls from release to avoid leaking internal
# state into logcat. If verbose logging is ever needed, drop -assumenosideeffects
# or scope it to debug builds only.
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
}
