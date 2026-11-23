# Keeps models and metadata needed when shrinking a release build.
# Keep FormUp data models used by JSON serialization.
-keep class com.formup.app.data.** { *; }

# Preserve generic signatures and runtime annotations.
-keepattributes Signature
-keepattributes *Annotation*

# Preserve Gson serialized field names.
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}