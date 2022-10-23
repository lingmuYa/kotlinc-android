package com.lingmu.builder

import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.config.Services
import java.io.File
import java.util.concurrent.Executors

class Compiler{

  private var logs: String
  
  @JvmStatic
  fun main(val JarList: String[], val ClassOutput: String, val FileCompile: String){
    val handler = Handler(Looper.getMainLooper())
    Executors.newSingleThreadExecutor().execute {
      compileKt(JarList: String[], ClassOutput: String, FileCompile: String)
    }
  }

@JvmStatic
  private fun compileKt(val JarList: String[], val ClassOutput: String, val FileCompile: String) {
    val mKotlinHome = File(cacheDir, "Kotlin").apply { mkdirs() }
    val mClassOutput = File(ClassOutput).apply { mkdirs() }
    val mFileCompile = File(FileCompile)

    val compiler = K2JVMCompiler()
    val collector = object : MessageCollector {
      private val diagnostics = mutableListOf<Diagnostic>()

      override fun clear() { diagnostics.clear() }

      override fun hasErrors() = diagnostics.any { it.severity.isError }

      override fun report(
        severity: CompilerMessageSeverity,
        message: String,
        location: CompilerMessageSourceLocation?
      ) {
        diagnostics += Diagnostic(severity, message, location)
      }

      override fun toString() = diagnostics
      .joinToString(System.lineSeparator().repeat(2)) { it.toString() }
    }

    val arguments = mutableListOf<String>().apply {
      // Classpath
      for(File jar in JarList){
        add("-cp")
        add(jar.absolutePath)
      }

      // Sources (.java & .kt)
      add(mFileCompile.absolutePath)
    }

    val args = K2JVMCompilerArguments().apply {
      compileJava = false
      includeRuntime = false
      noJdk = true
      noReflect = true
      noStdlib = true
      kotlinHome = mKotlinHome.absolutePath
      destination = mClassOutput.absolutePath
    }

    Log.d("TAG", "Running kotlinc with these arguments: $arguments")

    compiler.parseArguments(arguments.toTypedArray(), args)
    compiler.exec(collector, Services.EMPTY, args)

    logs = collector.toString()
  }

  @JvmStatic
  fun getLogs(): String {
    return logs
  }

  private data class Diagnostic(
    val severity: CompilerMessageSeverity,
    val message: String,
    val location: CompilerMessageSourceLocation?
  )

}
