# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the Android SDK.

# Keep Room entities
-keep class com.farmai.core.data.local.entity.** { *; }

# Keep Hilt
-keep class dagger.hilt.** { *; }
-keep class hilt_aggregated_deps.** { *; }
-keep class javax.inject.** { *; }

# Keep ML Kit
-keep class com.google.mlkit.** { *; }