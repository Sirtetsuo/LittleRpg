# ============================================================================
# ProGuard / R8 Rules - Little RPG
# ============================================================================

# 1. General Optimization & Attributes
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# 2. Domain Models & Data Classes
# Keep all data models, enums and sealed classes used across domain and UI
-keep class com.example.littlerpg.domain.model.** { *; }
-keepclassmembers class com.example.littlerpg.domain.model.** { *; }
-keepclassmembers enum com.example.littlerpg.domain.model.** { *; }

# 3. ViewModels & UI States
-keep class com.example.littlerpg.presentation.state.** { *; }
-keepclassmembers class com.example.littlerpg.presentation.state.** { *; }
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel { *; }

# 4. Data Layer & SQLite Database Helpers
-keep class com.example.littlerpg.data.** { *; }
-keepclassmembers class com.example.littlerpg.data.** { *; }

# 5. Jetpack Compose Rules
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}
-keep class androidx.compose.material3.** { *; }
-keep class androidx.compose.ui.** { *; }

# 6. Kotlin Coroutines & Flow
-keepclassmembers class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# 7. Android Lifecycle & Activity
-keep public class * extends androidx.activity.ComponentActivity
-keep public class * extends android.app.Application
