# ReTube (NOVA Tube) ProGuard / R8 Configuration
-keepattributes *Annotation*,InnerClasses,Signature,EnclosingMethod

# Kotlinx Serialization
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class app.retube.**$$serializer { *; }
-keepclassmembers class app.retube.** {
    *** Companion;
}
-keepclasseswithmembers class app.retube.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Room Database
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**