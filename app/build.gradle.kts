plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
    id("buildsrc.convention.kotlin-jvm")

    // Apply the Application plugin to add support for building an executable JVM application.
    application
}

dependencies {
    // Project "app" depends on project "utils". (Project paths are separated with ":", so ":utils" refers to the top-level "utils" project.)
    implementation(project(":interop-panama"))
}

application {
    // Define the Fully Qualified Name for the application main class
    // (Note that Kotlin compiles `App.kt` to a class with FQN `com.example.app.AppKt`.)
    mainClass = "org.example.app.AppKt"

    applicationDefaultJvmArgs = listOf(
        "--enable-native-access=ALL-UNNAMED",
    )
}

val rustLibName = providers.provider {
    when {
        org.gradle.internal.os.OperatingSystem.current().isLinux -> "libsrc_rust.so"
        else -> error("Unsupported operating system")
    }
}

tasks.named<JavaExec>("run") {
    dependsOn(":interop-panama:copyRustLib")
    val nativeDir = project(":interop-panama").layout.buildDirectory.dir("native")
    jvmArgs(
        "--enable-native-access=ALL-UNNAMED",
        "--illegal-native-access=deny",
        "-Drust.library.path=${nativeDir.get().file(rustLibName.get()).asFile.absolutePath}"
    )
}

tasks.withType<Test>().configureEach {
    dependsOn(":interop-panama:copyRustLib")
    val nativeDir = project(":interop-panama").layout.buildDirectory.dir("native")
    jvmArgs(
        "--enable-native-access=ALL-UNNAMED",
        "--illegal-native-access=deny",
        "-Drust.library.path=${nativeDir.get().file(rustLibName.get()).asFile.absolutePath}"
    )
}