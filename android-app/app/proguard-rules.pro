# AutoMateIQ v5.0 — R8 / ProGuard rules for the DEBUG build
# (GitHub Actions builds assembleDebug; this is what shrinks the APK).
#
# Firebase, Firestore, Google Sign-In and Play services MUST survive
# shrinking: login and live leads depend on them. Firebase libraries
# also ship their own consumer rules; these explicit keeps are the
# safety net so R8 never strips a class the SDK reaches by reflection.
# No token, key, password or secret is stored in this file.
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod

# The app's own classes are small; keeping them avoids any reflection
# breakage and costs almost nothing in size.
-keep class com.automateiq.app.** { *; }

# Firebase Auth + Firestore + Google Sign-In + Play services.
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-keep class com.google.android.libraries.** { *; }
-keep class com.google.api.** { *; }

# Firestore field-name annotations, if any model ever uses them.
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <methods>;
}

# Keep R classes referenced by the google-services plugin output.
-keep class **.R { *; }
-keep class **.R$* { *; }

-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**
-dontwarn com.google.api.**
