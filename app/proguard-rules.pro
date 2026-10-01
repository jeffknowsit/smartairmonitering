# Default ProGuard rules for SmartAir
-keepattributes Signature
-keepattributes *Annotation*

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.smartair.data.model.** { *; }

# USB Serial
-keep class com.hoho.android.usbserial.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
