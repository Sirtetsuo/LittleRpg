# Rules for Little RPG Data Models (JSON Serialization / Deserialization)
-keep class com.example.littlerpg.** { *; }
-keepclassmembers class com.example.littlerpg.** { *; }

# Keep ViewModels and Lifecycle State
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel { *; }

# Room Keep Rules
-keep class * extends androidx.room.RoomDatabase
-keep class * extends com.example.littlerpg.data.RpgDatabase
-dontwarn androidx.room.paging.**

# Jetpack Compose Rules
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}
