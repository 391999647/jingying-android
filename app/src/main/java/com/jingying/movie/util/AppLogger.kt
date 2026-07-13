package com.jingying.movie.util

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AppLogger {
    private const val TAG = "Jingying"
    private const val LOG_FILE_NAME = "app_debug.log"
    private const val MAX_LOG_SIZE = 2 * 1024 * 1024L // 2MB

    private lateinit var logDir: File
    private var isInitialized = false
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

    fun init(context: Context) {
        logDir = File(context.filesDir, "logs").apply { mkdirs() }
        isInitialized = true
        i("AppLogger", "日志系统初始化完成，日志目录: ${logDir.absolutePath}")
    }

    fun v(tag: String, msg: String) {
        Log.v(TAG, "[$tag] $msg")
        writeLog("V", tag, msg)
    }

    fun d(tag: String, msg: String) {
        Log.d(TAG, "[$tag] $msg")
        writeLog("D", tag, msg)
    }

    fun i(tag: String, msg: String) {
        Log.i(TAG, "[$tag] $msg")
        writeLog("I", tag, msg)
    }

    fun w(tag: String, msg: String) {
        Log.w(TAG, "[$tag] $msg")
        writeLog("W", tag, msg)
    }

    fun e(tag: String, msg: String, throwable: Throwable? = null) {
        Log.e(TAG, "[$tag] $msg", throwable)
        writeLog("E", tag, msg + (throwable?.let { "\n${it.stackTraceToString()}" } ?: ""))
    }

    fun getLogDir(): File = if (isInitialized) logDir else File("")

    fun getLogFile(): File = File(logDir, LOG_FILE_NAME)

    fun getLogContent(maxLines: Int = 200): String {
        return try {
            val file = getLogFile()
            if (file.exists()) {
                val lines = file.readLines()
                lines.takeLast(maxLines).joinToString("\n")
            } else {
                "暂无日志"
            }
        } catch (e: Exception) {
            "读取日志失败: ${e.message}"
        }
    }

    fun clearLogs() {
        try {
            getLogFile().delete()
            i("AppLogger", "日志已清除")
        } catch (e: Exception) {
            Log.e(TAG, "清除日志失败", e)
        }
    }

    fun getDataDirInfo(context: Context): String {
        return buildString {
            appendLine("=== 应用数据目录 ===")
            appendLine("Files: ${context.filesDir.absolutePath}")
            appendLine("Cache: ${context.cacheDir.absolutePath}")
            appendLine("Database: ${context.getDatabasePath("jingying_db")?.absolutePath ?: "N/A"}")
            appendLine("SharedPrefs: ${context.filesDir.parent}/shared_prefs")
            appendLine()
            appendLine("=== 缓存大小 ===")
            appendLine("图片缓存: ${formatSize(context.cacheDir.resolve("image_cache").totalSize())}")
            appendLine("网络缓存: ${formatSize(context.cacheDir.resolve("http_cache").totalSize())}")
            appendLine("日志大小: ${formatSize(logDir.totalSize())}")
            appendLine()
            appendLine("=== 最近日志 (最新50行) ===")
            append(getLogContent(50))
        }
    }

    private fun writeLog(level: String, tag: String, msg: String) {
        if (!isInitialized) return
        try {
            val file = getLogFile()
            if (file.exists() && file.length() > MAX_LOG_SIZE) {
                val lines = file.readLines()
                file.writeText(lines.takeLast(lines.size / 2).joinToString("\n"))
            }
            FileWriter(file, true).use { writer ->
                writer.appendLine("${dateFormat.format(Date())} $level/$TAG [$tag] $msg")
            }
        } catch (_: Exception) {}
    }

    private fun File.totalSize(): Long {
        return if (isDirectory) {
            listFiles()?.sumOf { it.totalSize() } ?: 0L
        } else {
            length()
        }
    }

    private fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "${bytes}B"
            bytes < 1024 * 1024 -> "${bytes / 1024}KB"
            else -> "${"%.1f".format(bytes / (1024.0 * 1024.0))}MB"
        }
    }
}
