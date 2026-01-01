# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /usr/local/android-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.

# Keep the Application class
-keep class com.ghostbattery.App { *; }

# Keep Activity classes for XML inflation
-keep class com.ghostbattery.ui.** { *; }

# Keep Data classes
-keep class com.ghostbattery.data.model.** { *; }

# Keep Service
-keep class com.ghostbattery.service.GhostAccessibilityService { *; }

# Obfuscate everything else
-repackageclasses 'a'
-allowaccessmodification
