# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.alertgia.app.data.remote.dto.** { *; }
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.alertgia.app.**$$serializer { *; }
-keepclassmembers class com.alertgia.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.alertgia.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}
