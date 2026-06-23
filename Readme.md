#### Hilt引入配置

###### 根目录 build.gradle 配置
```
plugins {
	alias(libs.plugins.android.dagger.hilt) apply false
}
```
###### app模块 build.gradle 配置
```
plugins {
	alias(libs.plugins.android.dagger.hilt)
	alias(libs.plugins.kotlin.kapt)
}

dependencies {
	implementation(libs.google.dagger.hilt)
	kapt(libs.google.dagger.hilt.compiler)
}
```
###### Gradle版本目录配置 (libs.versions.toml)
```
[versions]
hilt = "2.58"

[libraries]
google-dagger-hilt = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
google-dagger-hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }

[plugins]
android-dagger-hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
kotlin-kapt = { id = "kotlin-kapt" }
```
#### 在 Android 应用中使用 Hilt：https://developer.android.google.cn/codelabs/android-hilt?authuser=1&hl=zh-cn#0