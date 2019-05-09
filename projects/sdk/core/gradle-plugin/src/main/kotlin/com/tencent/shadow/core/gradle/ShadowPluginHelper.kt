package com.tencent.shadow.core.gradle

import com.tencent.shadow.core.gradle.extensions.PackagePluginExtension
import com.tencent.shadow.core.gradle.extensions.PluginApkConfig
import com.tencent.shadow.core.gradle.extensions.PluginBuildType
import org.gradle.api.Project
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import kotlin.experimental.and

open class ShadowPluginHelper {
    companion object {
        fun getFileMD5(file: File): String? {
            if (!file.isFile) {
                return null
            }

            val buffer = ByteArray(1024)
            var len: Int
            var inStream: FileInputStream? = null
            val digest = MessageDigest.getInstance("MD5")
            try {
                inStream = FileInputStream(file)
                do {
                    len = inStream.read(buffer, 0, 1024)
                    if (len != -1) {
                        digest.update(buffer, 0, len)
                    }
                } while (len != -1)
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            } finally {
                inStream?.close()
            }
            return bytes2HexStr(digest.digest())
        }

        private fun bytes2HexStr(bytes: ByteArray?): String {
            val HEX_ARRAY = "0123456789ABCDEF".toCharArray()
            if (bytes == null || bytes.isEmpty()) {
                return ""
            }

            val buf = CharArray(2 * bytes.size)
            try {
                for (i in bytes.indices) {
                    var b = bytes[i]
                    buf[2 * i + 1] = HEX_ARRAY[(b and 0xF).toInt()]
                    b = b.toInt().ushr(4).toByte()
                    buf[2 * i + 0] = HEX_ARRAY[(b and 0xF).toInt()]
                }
            } catch (e: Exception) {
                return ""
            }

            return String(buf)
        }

        fun getRuntimeApkFile(project: Project, buildType: PluginBuildType, checkExist: Boolean): File {
            val packagePlugin = project.extensions.findByName("packagePlugin")
            val extension = packagePlugin as PackagePluginExtension

            val splitList = buildType.runtimeApkConfig.second.split(":")
            val runtimeFileParent = splitList[splitList.lastIndex].replace("assemble", "").toLowerCase()
            val runtimeApkName: String = buildType.runtimeApkConfig.first
            val runtimeFile = File("${project.rootDir}" +
                    "/${extension.runtimeApkProjectPath}/build/outputs/apk/$runtimeFileParent/$runtimeApkName")
            if (checkExist && !runtimeFile.exists()) {
                throw IllegalArgumentException(runtimeFile.absolutePath + " , runtime file not exist...")
            }
            println("runtimeFile = $runtimeFile")
            return runtimeFile
        }

        fun getLoaderApkFile(project: Project, buildType: PluginBuildType, checkExist: Boolean): File {
            val packagePlugin = project.extensions.findByName("packagePlugin")
            val extension = packagePlugin as PackagePluginExtension

            val loaderApkName: String = buildType.loaderApkConfig.first
            val splitList = buildType.loaderApkConfig.second.split(":")
            val loaderFileParent = splitList[splitList.lastIndex].replace("assemble", "").toLowerCase()
            val loaderFile = File("${project.rootDir}" +
                    "/${extension.loaderApkProjectPath}/build/outputs/apk/$loaderFileParent/$loaderApkName")
            if (checkExist && !loaderFile.exists()) {
                throw IllegalArgumentException(loaderFile.absolutePath + " , loader file not exist...")
            }
            println("loaderFile = $loaderFile")
            return loaderFile

        }

        fun getPluginFile(project: Project, pluginConfig: PluginApkConfig, checkExist: Boolean): File {
            val pluginFile = File(project.rootDir, pluginConfig.apkPath)
            if (checkExist && !pluginFile.exists()) {
                throw IllegalArgumentException(pluginFile.absolutePath + " , plugin file not exist...")
            }
            println("pluginFile = $pluginFile")
            return pluginFile
        }
    }
}