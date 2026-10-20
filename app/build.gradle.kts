plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.projectandroidstarwars"

    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.starwarsprojectandroid"

        minSdk = 24
        targetSdk = 37

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }
}

// Room будет сохранять описание структуры базы в app/schemas.
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Наблюдение за данными с учётом жизненного цикла экрана.
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Сетевые запросы.
    implementation("com.squareup.retrofit2:retrofit:3.0.0")

    // Преобразование JSON в Kotlin-объекты.
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")

    // Корутины.
    implementation(
        "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2"
    )

    // Навигация между экранами.
    implementation(
        "androidx.navigation:navigation-compose:2.9.0"
    )

    // ViewModel для Compose.
    implementation(
        "androidx.lifecycle:lifecycle-viewmodel-compose:" +
                libs.versions.lifecycleRuntimeKtx.get()
    )

    // Запуск корутин внутри ViewModel.
    implementation(
        "androidx.lifecycle:lifecycle-viewmodel-ktx:" +
                libs.versions.lifecycleRuntimeKtx.get()
    )

    // Сохранение состояния ViewModel.
    implementation(
        "androidx.lifecycle:lifecycle-viewmodel-savedstate:" +
                libs.versions.lifecycleRuntimeKtx.get()
    )

    // Разметка экрана деталей.
    implementation(
        "androidx.constraintlayout:constraintlayout-compose:1.1.1"
    )

    // Локальная база данных Room.
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)

    // Генерация кода для Room.
    ksp(libs.androidx.room.compiler)

    // Тесты.
    testImplementation(libs.junit)
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")

    androidTestImplementation(
        platform(libs.androidx.compose.bom)
    )
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}