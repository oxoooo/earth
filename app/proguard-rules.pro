-keepattributes LineNumberTable

# retrolambda
-dontwarn java.lang.invoke.*

# okhttp
-dontwarn okio.**

# support library
-keep class android.support.v7.widget.LinearLayoutManager { *; }

# data binding
-keep class ooo.oxo.apps.earth.databinding.** { *; }

# play services
-dontwarn com.google.android.gms.**
