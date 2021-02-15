object Depends {

    object BuildPlugins {
        const val gradlePlugin = "com.android.tools.build:gradle:${Versions.Android.gradlePlugin}"
        const val mavenGradlePlugin = "com.github.dcendents:android-maven-gradle-plugin:${Versions.Android.mavenGradlePlugin}"
        const val kotlinPlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    }

    object Kotlin {
        const val stdlib =  "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
        const val extensions =  "org.jetbrains.kotlin:kotlin-android-extensions-runtime:${Versions.kotlin}"
        const val core = "androidx.core:core-ktx:${Versions.ktxVersion}"
    }

    object BaseAndroid {
        const val material = "com.google.android.material:material:${Versions.material}"
        const val appcompat = "androidx.appcompat:appcompat:${Versions.appcompat}"
    }

    object Paging {
        const val core = "androidx.paging:paging-runtime:${Versions.paging}"
    }

    object Coroutines {
        const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
        const val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}"
    }

    object Database {
        const val runtime = "androidx.room:room-runtime:${Versions.room}"
        const val ktx = "androidx.room:room-ktx:${Versions.room}"
    }

    object DatabasePlugin {
        const val plugin = "androidx.room:room-compiler:${Versions.room}"
    }

    object Api {
        const val okhttp = "com.squareup.okhttp3:okhttp:${Versions.okhttp}"
        const val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
        const val logging = "com.squareup.okhttp3:logging-interceptor:${Versions.logging}"
        const val gson = "com.squareup.retrofit2:converter-gson:${Versions.gson}"
    }

    object Test {
        const val runner = "androidx.test:runner:${Versions.runner}"
        const val rules = "androidx.test:rules:${Versions.rules}"
        const val junit = "androidx.test.ext:junit:${Versions.junit}"
        const val core_testing = "androidx.test:core:${Versions.core_testing}"
        const val arch_core_testing = "androidx.arch.core:core-testing:${Versions.arch_core_testing}"
        const val espresso = "androidx.test.espresso:espresso-core:${Versions.espresso}"
    }
}