plugins{id("com.android.application");id("org.jetbrains.kotlin.android")}
android{
namespace="com.sina.mbook";compileSdk=34
defaultConfig{
applicationId="com.sina.mbook";minSdk=24;targetSdk=34
versionCode=(project.findProperty("VC") as String?)?.toInt()?:1
versionName="1.0"
buildConfigField("String","REPO","\"${project.findProperty("REPO") ?: ""}\"")
buildConfigField("String","BRANCH","\"${project.findProperty("BRANCH") ?: "main"}\"")
}
buildFeatures{buildConfig=true}
signingConfigs{getByName("debug"){storeFile=file("debug.keystore");storePassword="android";keyAlias="androiddebugkey";keyPassword="android"}}
buildTypes{release{isMinifyEnabled=false}}
compileOptions{sourceCompatibility=JavaVersion.VERSION_17;targetCompatibility=JavaVersion.VERSION_17}
kotlinOptions{jvmTarget="17"}
}
