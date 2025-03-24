/*
 * Copyright © 2024-2025 Apple Inc. and the Pkl project authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
plugins {
  pklAllProjects
  pklGraalVm
  pklJavaLibrary
  pklJavaExecutable
  pklNativeLifecycle
}

// assumes that `pklJavaExecutable` is also applied
val executableSpec = project.extensions.getByType<ExecutableSpec>()

val stagedMacAmd64NativeLibrary: Configuration by configurations.creating
val stagedMacAarch64NativeLibrary: Configuration by configurations.creating
val stagedLinuxAmd64NativeLibrary: Configuration by configurations.creating
val stagedLinuxAarch64NativeLibrary: Configuration by configurations.creating
val stagedAlpineLinuxAmd64NativeLibrary: Configuration by configurations.creating
val stagedWindowsAmd64NativeLibrary: Configuration by configurations.creating

dependencies {
  implementation(projects.pklCore)
  implementation(projects.pklServer)

  implementation(libs.msgpack)
  implementation(libs.truffleApi)

  testImplementation(projects.pklCommonsTest)

  fun sharedLibrary(osAndArch: String) = files(nativeLibraryOutputFiles(osAndArch))

  stagedMacAarch64NativeLibrary(sharedLibrary("macos-aarch64"))
  stagedMacAmd64NativeLibrary(sharedLibrary("macos-amd64"))
  stagedLinuxAmd64NativeLibrary(sharedLibrary("linux-amd64"))
  stagedLinuxAarch64NativeLibrary(sharedLibrary("linux-aarch64"))
  stagedAlpineLinuxAmd64NativeLibrary(sharedLibrary("alpine-linux-amd64"))
  stagedWindowsAmd64NativeLibrary(sharedLibrary("windows-amd64.exe"))
}

executable {
  name = "libpkl"

  // TODO(kushal): Why is all of this necessary now? Can it be stripped back?
  javaName = "libpkl"
  documentationName = "Pkl Native Library"
  publicationName = "libpkl"
  javaPublicationName = "libpkl"
  mainClass = "org.pkl.libpkl.LibPkl"
  website = "TODO"
}

private fun extension(osAndArch: String) =
  when (osAndArch.split("-").first()) {
    "linux" -> "so"
    "macos" -> "dylib"
    "unix" -> "so"
    "windows" -> "dll"
    else -> {
      throw StopExecutionException(
        "Don't know how to construct library extension for OS: ${osAndArch.split("-").first()}"
      )
    }
  }

private fun nativeLibraryOutputFiles(osAndArch: String) =
  project.layout.buildDirectory.dir("libs/$osAndArch").map { outputDir ->
    // TODO(kushal): dashes/underscores for library files? C convention assumes underscores.
    val libraryName = "libpkl-$osAndArch"
    val libraryOutputFiles =
      listOf(
        "${libraryName}.${extension(osAndArch)}",
        "${libraryName}_dynamic.h",
        "${libraryName}.h",

        // GraalVM shared headers.
        "graal_isolate.h",
        "graal_isolate_dynamic.h",
      )

    libraryOutputFiles.map { filename -> outputDir.file(filename) }
  }

private fun NativeImageBuild.setOutputFiles(osAndArch: String) {
  outputs.files(nativeLibraryOutputFiles(osAndArch))
}

private fun NativeImageBuild.amd64() {
  arch = Architecture.AMD64
  dependsOn(":installGraalVmAmd64")
}

private fun NativeImageBuild.aarch64() {
  arch = Architecture.AARCH64
  dependsOn(":installGraalVmAarch64")
}

private fun NativeImageBuild.setClasspath() {
  classpath.from(sourceSets.main.map { it.output })
  classpath.from(
    project(":pkl-commons-cli").extensions.getByType(SourceSetContainer::class)["svm"].output
  )
  classpath.from(configurations.runtimeClasspath)
}

val macNativeLibraryAmd64 by
  tasks.registering(NativeImageBuild::class) {
    outputDir = project.layout.buildDirectory.dir("libs/macos-amd64")
    imageName = executableSpec.name.map { "$it-macos-amd64" }
    mainClass = executableSpec.mainClass
    amd64()
    setClasspath()
    extraNativeImageArgs =
      listOf(
        "--initialize-at-run-time=org.pkl.core.EvaluatorImpl",
        "--initialize-at-run-time=org.pkl.server.BinaryEvaluator",
        "--initialize-at-run-time=org.pkl.server.Server",
        "--shared",
      )

    setOutputFiles("macos-amd64")
  }

val macNativeLibraryAarch64 by
  tasks.registering(NativeImageBuild::class) {
    outputDir = project.layout.buildDirectory.dir("libs/macos-aarch64")
    imageName = executableSpec.name.map { "$it-macos-aarch64" }
    mainClass = executableSpec.mainClass
    aarch64()
    setClasspath()
    extraNativeImageArgs =
      listOf(
        "--initialize-at-run-time=org.pkl.core.EvaluatorImpl",
        "--initialize-at-run-time=org.pkl.server.BinaryEvaluator",
        "--initialize-at-run-time=org.pkl.server.Server",
        "--shared",
      )

    setOutputFiles("macos-aarch64")
  }

val linuxNativeLibraryAmd64 by
  tasks.registering(NativeImageBuild::class) {
    outputDir = project.layout.buildDirectory.dir("libs/linux-amd64")
    imageName = executableSpec.name.map { "$it-linux-amd64" }
    mainClass = executableSpec.mainClass
    amd64()
    setClasspath()
    extraNativeImageArgs =
      listOf(
        "--initialize-at-run-time=org.pkl.core.EvaluatorImpl",
        "--initialize-at-run-time=org.pkl.server.BinaryEvaluator",
        "--initialize-at-run-time=org.pkl.server.Server",
        "--shared",
      )

    setOutputFiles("linux-amd64")
  }

val linuxNativeLibraryAarch64 by
  tasks.registering(NativeImageBuild::class) {
    outputDir = project.layout.buildDirectory.dir("libs/linux-aarch64")
    imageName = executableSpec.name.map { "$it-linux-aarch64" }
    mainClass = executableSpec.mainClass
    aarch64()
    setClasspath()

    extraNativeImageArgs =
      listOf(
        "--initialize-at-run-time=org.pkl.core.EvaluatorImpl",
        "--initialize-at-run-time=org.pkl.server.BinaryEvaluator",
        "--initialize-at-run-time=org.pkl.server.Server",
        "--shared",
        // Ensure compatibility for kernels with page size set to 4k, 16k and 64k
        // (e.g. Raspberry Pi 5, Asahi Linux)
        "-H:PageSize=65536",
      )

    setOutputFiles("linux-aarch64")
  }

val alpineNativeLibraryAmd64 by
  tasks.registering(NativeImageBuild::class) {
    outputDir = project.layout.buildDirectory.dir("libs/alpine-linux-amd64")
    imageName = executableSpec.name.map { "$it-alpine-linux-amd64" }
    mainClass = executableSpec.mainClass
    amd64()
    setClasspath()

    extraNativeImageArgs =
      listOf(
        "--initialize-at-run-time=org.pkl.core.EvaluatorImpl",
        "--initialize-at-run-time=org.pkl.server.BinaryEvaluator",
        "--initialize-at-run-time=org.pkl.server.Server",
        "--shared",
        // TODO(kushal): https://github.com/oracle/graal/issues/3053
        "--libc=musl",
      )

    setOutputFiles("alpine-linux-amd64")
  }

val windowsNativeLibraryAmd64 by
  tasks.registering(NativeImageBuild::class) {
    outputDir = project.layout.buildDirectory.dir("libs/windows-amd64")
    imageName = executableSpec.name.map { "$it-windows-amd64" }
    mainClass = executableSpec.mainClass
    amd64()
    setClasspath()
    extraNativeImageArgs =
      listOf(
        "--initialize-at-run-time=org.pkl.core.EvaluatorImpl",
        "--initialize-at-run-time=org.pkl.server.BinaryEvaluator",
        "--initialize-at-run-time=org.pkl.server.Server",
        "--shared",
        "-Dfile.encoding=UTF-8",
      )

    setOutputFiles("windows-amd64")
  }

val assembleNative by
  tasks.existing {
    // TODO(kushal): Remove this later. Only exists to debug output files are in the graph.
    finalizedBy(tasks.named("validateNativeLibraryFiles"))
  }

// TODO(kushal): Remove this later. Only exists to debug output files are in the graph.
tasks.register("validateNativeLibraryFiles") {
  val assembleTasks = mutableSetOf<TaskProvider<NativeImageBuild>>()

  when {
    buildInfo.os.isMacOsX -> {
      assembleTasks.add(macNativeLibraryAmd64)
      if (buildInfo.arch == "aarch64") {
        assembleTasks.add(macNativeLibraryAarch64)
      }
    }

    buildInfo.os.isWindows -> {
      assembleTasks.add(windowsNativeLibraryAmd64)
    }

    buildInfo.os.isLinux && buildInfo.arch == "aarch64" -> {
      assembleTasks.add(linuxNativeLibraryAarch64)
    }

    buildInfo.os.isLinux && buildInfo.arch == "amd64" -> {
      assembleTasks.add(linuxNativeLibraryAmd64)
      if (buildInfo.hasMuslToolchain) {
        assembleTasks.add(alpineNativeLibraryAmd64)
      }
    }
  }

  dependsOn(assembleTasks)

  doLast {
    for (taskProvider in assembleTasks) {
      val task = taskProvider.get()
      val outputFiles = task.outputs.files.files

      println("==== Validating Native Library Files Exist ====")
      println("${task.name} outputs:")
      outputFiles.forEach { file -> println("- ${file.absolutePath} (exists: ${file.exists()})") }
    }
  }
}

// Expose underlying task's outputs
private fun <T : Task> Task.wraps(other: TaskProvider<T>) {
  dependsOn(other)
  outputs.files(other)
}

val assembleNativeMacOsAarch64 by tasks.existing { wraps(macNativeLibraryAarch64) }

val assembleNativeMacOsAmd64 by tasks.existing { wraps(macNativeLibraryAmd64) }

val assembleNativeLinuxAarch64 by tasks.existing { wraps(linuxNativeLibraryAarch64) }

val assembleNativeLinuxAmd64 by tasks.existing { wraps(linuxNativeLibraryAmd64) }

val assembleNativeAlpineLinuxAmd64 by tasks.existing { wraps(alpineNativeLibraryAmd64) }

val assembleNativeWindowsAmd64 by tasks.existing { wraps(windowsNativeLibraryAmd64) }
