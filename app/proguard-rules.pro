# ProGuard rules for KidShield
-keep class com.kidshield.agent.data.local.entity.** { *; }
-keep class com.kidshield.agent.domain.model.** { *; }
-keep class com.kidshield.agent.data.remote.api.** { *; }
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
