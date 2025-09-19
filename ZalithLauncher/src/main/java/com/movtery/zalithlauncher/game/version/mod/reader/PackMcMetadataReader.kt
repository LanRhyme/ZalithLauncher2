package com.movtery.zalithlauncher.game.version.mod.reader

import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.version.mod.LocalMod
import com.movtery.zalithlauncher.game.version.mod.ModMetadataReader
import com.movtery.zalithlauncher.game.version.mod.isDisabled
import com.movtery.zalithlauncher.game.version.mod.meta.PackMcMeta
import com.movtery.zalithlauncher.utils.GSON
import com.movtery.zalithlauncher.utils.file.UnpackZipException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.zip.ZipFile
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.util.zip.ZipFile as JDKZipFile

/**
 * [Reference HMCL](https://github.com/HMCL-dev/HMCL/blob/4650287/HMCLCore/src/main/java/org/jackhuang/hmcl/mod/modinfo/PackMcMeta.java)
 */
object PackMcMetadataReader : ModMetadataReader {
    override suspend fun fromLocal(modFile: File): LocalMod = withContext(Dispatchers.IO) {
        try {
            JDKZipFile(modFile).use { zip ->
                try {
                    val entry = zip.getEntry("pack.mcmeta")
                        ?: throw IOException("pack.mcmeta not found in resource pack $modFile")

                    zip.getInputStream(entry).use { inputStream ->
                        InputStreamReader(inputStream).use { reader ->
                            val meta = GSON.fromJson(reader, PackMcMeta::class.java)

                            return@withContext LocalMod(
                                modFile = modFile,
                                fileSize = FileUtils.sizeOf(modFile),
                                id = getRawFileName(modFile),
                                loader = ModLoader.PACK,
                                name = getRawFileName(modFile),
                                description = meta.pack.description.toPlainText(),
                                version = "",
                                authors = emptyList(),
                                icon = null,
                                notMod = false
                            )
                        }
                    }
                } catch (e: Exception) {
                    throw UnpackZipException(e)
                }
            }
        } catch (e: Exception) {
            if (e !is UnpackZipException) return@withContext readWithApacheZip(modFile)
            else throw e
        }
    }

    private fun readWithApacheZip(modFile: File): LocalMod {
        val zipFile = ZipFile.Builder()
            .setFile(modFile)
            .get()

        val rawName = getRawFileName(modFile)

        zipFile.use { zip ->
            val entry = zip.getEntry("pack.mcmeta")
                ?: throw IOException("pack.mcmeta not found in resource pack $modFile")

            zip.getInputStream(entry).use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    val meta = GSON.fromJson(reader, PackMcMeta::class.java)

                    return LocalMod(
                        modFile = modFile,
                        fileSize = FileUtils.sizeOf(modFile),
                        id = rawName,
                        loader = ModLoader.PACK,
                        name = rawName,
                        description = meta.pack.description.toPlainText(),
                        version = "",
                        authors = emptyList(),
                        icon = null,
                        notMod = false
                    )
                }
            }
        }
    }

    /**
     * 获取原始文件名，移除 .disabled 后缀
     */
    private fun getRawFileName(file: File): String {
        val fileName = file.name
        return if (file.isDisabled()) fileName.removeSuffix(".disabled")
        else fileName
    }
}
