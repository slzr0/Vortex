-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

-keepclassmembers class io.ktor.http.** { *; }

-keep class com.my.vortex.domain.model.** { <fields>; }

-keep class com.my.vortex.data.remote.** { <fields>; }

-keep class vortex.gateway.entities.** { <fields>; }

-dontwarn org.slf4j.impl.StaticLoggerBinder

-dontwarn java.lang.invoke.StringConcatFactory

-dontwarn com.my.vortex.resources.R$drawable