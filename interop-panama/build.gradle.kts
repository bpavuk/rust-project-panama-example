import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
    id("buildsrc.convention.kotlin-jvm")
    id("java")
}

val rustSources = objects.sourceDirectorySet("rust", "Rust sources").apply {
    srcDir("src/main/rust")
    include("**/*.rs", "Cargo.toml", "Cargo.lock")
}

val rustProjectDir = layout.projectDirectory.dir("src/main/rust")
val rustTargetDir = rustProjectDir.dir("target/debug")
val rustHeadersDir = rustProjectDir.dir("bindings")
val jextractOutputDir = layout.buildDirectory.dir("generated/jextract")

val rustLibFileName = providers.provider {
    when {
        OperatingSystem.current().isLinux -> "libsrc_rust.so"
        else -> error("Unsupported operating system: ${OperatingSystem.current()}")
    }
}

val buildRustCdylib by tasks.registering(Exec::class) {
    group = "interop"
    description = "Build src-rust cdylib with Cargo"
    workingDir = rustProjectDir.asFile
    commandLine("cargo", "build")
    inputs.files(rustSources)
    outputs.dir(layout.projectDirectory.dir("src/main/rust/target"))
}

val copyRustLib by tasks.registering(Copy::class) {
    group = "interop"
    description = "Copy Rust shared library into interop build output"
    dependsOn(buildRustCdylib)
    from(rustTargetDir.file(rustLibFileName))
    into(layout.buildDirectory.dir("native"))
    inputs.dir(rustTargetDir)
    outputs.dir(layout.buildDirectory.dir("native"))
}

val copyHeaders by tasks.registering(Copy::class) {
    group = "interop"
    description = "Copy generated headers into interop build output"
    dependsOn(buildRustCdylib)
    from(rustHeadersDir.file("jvm_interop.h"))
    into(layout.buildDirectory.dir("native"))
    inputs.dir(rustHeadersDir)
    outputs.dir(layout.buildDirectory.dir("native"))
}

// HACK: I'd rather interact with jextract as a build-time dependency, much like
//  Rust's cbindgen crate in rust-src/build.rs
val generateJextractBindings by tasks.registering(Exec::class) {
    group = "interop"
    description = "Generate bindings from headers"
    workingDir = layout.projectDirectory.asFile
    dependsOn(copyRustLib, copyHeaders)
    val nativeDir = layout.buildDirectory.dir("native").get().asFile
    val nativeLibPath = layout.buildDirectory.dir("native").get().file(rustLibFileName).get().asFile.absolutePath
    inputs.dir(nativeDir)
    outputs.dir(jextractOutputDir)
    commandLine(
        "jextract",
        "--include-dir", nativeDir.absolutePath,
        "--output", jextractOutputDir.get().asFile.absolutePath,
        "--target-package", "org.example.interop",
        "--library", ":$nativeLibPath",
        layout.buildDirectory.dir("native").get().file("jvm_interop.h").asFile.absolutePath
    )
}

tasks.named("classes") {
    dependsOn(generateJextractBindings)
}

sourceSets {
    named("main") {
        java.srcDir(jextractOutputDir)
    }
}

tasks.withType<KotlinCompile>().configureEach {
    dependsOn(generateJextractBindings)
}

dependencies {
    testImplementation(kotlin("test"))
}
