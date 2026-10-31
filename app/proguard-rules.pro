# Rules for Little RPG Data Models (JSON Serialization / Deserialization)
-keep class com.example.littlerpg.model.** { *; }
-keepclassmembers class com.example.littlerpg.model.** { *; }

# Rules for Firebase AI / Gemini Generative AI
-keep class com.google.firebase.ai.** { *; }
-dontwarn com.google.firebase.ai.**

# Keep ViewModels and Lifecycle State
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel { *; }

# Jetpack Compose Rules
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}
