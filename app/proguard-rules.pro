# The optional module is loaded by name from META-INF/xposed/java_init.list.
-keep class com.realcts.app.SystemUiModule { *; }
-keepattributes SourceFile,LineNumberTable
# Preserve readable app stack traces on testers' devices.
-keepnames class com.realcts.app.**
