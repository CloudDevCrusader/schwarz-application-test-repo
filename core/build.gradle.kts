
plugins {
    alias(libs.plugins.kotlin.multiplatform)
}


kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            // OpenTelemetry dependencies removed
        }

        commonTest.dependencies {
            kotlin("test")
        }
    }
}
