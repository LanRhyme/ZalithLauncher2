package com.movtery.zalithlauncher.game.addons.modloader.optifine

import com.movtery.zalithlauncher.game.addons.mirror.MirrorSource
import com.movtery.zalithlauncher.game.addons.mirror.SourceType
import com.movtery.zalithlauncher.game.addons.mirror.runMirrorable
import com.movtery.zalithlauncher.game.addons.modloader.ResponseTooShortException
import com.movtery.zalithlauncher.path.UrlManager.Companion.GLOBAL_CLIENT
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.enums.MirrorSourceType
import com.movtery.zalithlauncher.utils.logging.Logger.lDebug
import com.movtery.zalithlauncher.utils.logging.Logger.lWarning
import com.movtery.zalithlauncher.utils.string.compareVersion
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.utils.io.charsets.Charset
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

/**
 * [Some logic refers to PCL2](https://github.com/Hex-Dragon/PCL2/blob/44aea3e/Plain%20Craft%20Launcher%202/Modules/Minecraft/ModDownload.vb#L375-L409)
 */
object OptiFineVersions {
    private const val OPTIFINE_URL = "https://optifine.net"
    private const val OPTIFINE_DOWNLOAD_URL = "$OPTIFINE_URL/downloads"
    private const val OPTIFINE_ADLOADX_URL = "$OPTIFINE_URL/adloadx"

    private var cacheResult: List<OptiFineVersion>? = null

    /**
     * 获取 OptiFine 版本列表
     */
    suspend fun fetchOptiFineList(
        force: Boolean = false,
        gameVersion: String
    ): List<OptiFineVersion>? = withContext(Dispatchers.IO) {
        runMirrorable(
            when (AllSettings.fetchModLoaderSource.getValue()) {
                MirrorSourceType.OFFICIAL_FIRST -> listOf(
                    fetchListWithOfficial(force, 5),
                    fetchListWithBMCLAPI(force, 5 + 30)
                )
                MirrorSourceType.MIRROR_FIRST -> listOf(
                    fetchListWithBMCLAPI(force, 30),
                    fetchListWithOfficial(force, 30 + 60)
                )
            }
        )?.filter {
            it.inherit == gameVersion
        }?.sortOptiFineVersions()
    }

    /**
     * 对OptiFine版本进行排序，正式版优先，预览版其次
     */
    private fun List<OptiFineVersion>.sortOptiFineVersions(): List<OptiFineVersion> {
        val comparator = Comparator<OptiFineVersion> { o1, o2 ->
            o2.realVersion.compareVersion(o1.realVersion)
        }
        //拆分预览版和正式版
        val (previews, releases) = this.partition { it.isPreview }
        //两个集合单独进行排序，合并时正式版优先，预览版其次
        return releases.sortedWith(comparator) + previews.sortedWith(comparator)
    }

    /**
     * 从官方源获取版本列表
     */
    private fun fetchListWithOfficial(
        force: Boolean = false,
        delayMillis: Long
    ): MirrorSource<List<OptiFineVersion>?> = MirrorSource(
        delayMillis = delayMillis,
        type = SourceType.OFFICIAL
    ) {
        withContext(Dispatchers.Default) {
            if (!force) cacheResult?.let { return@withContext it }

            try {
                val response: HttpResponse = withContext(Dispatchers.IO) {
                    GLOBAL_CLIENT.get(OPTIFINE_DOWNLOAD_URL)
                }

                val bytes: ByteArray = response.body()
                val html = bytes.toString(Charset.defaultCharset())
                if (html.length < 100) {
                    throw ResponseTooShortException("Response too short")
                }
                val namePattern = Regex("<td class=['\"]colFile['\"]>([^<]+)</td>")
                val datePattern = Regex("<td class=['\"]colDate['\"]>([^<]+)</td>")
                val forgePattern = Regex("<td class=['\"]colForge['\"]>([^<]+)</td>")
                val jarPattern = Regex("adfoc\\.us[^>]+f=([^&]+\\.jar)")

                //提取所有匹配项
                val names = namePattern.findAll(html).map { it.groupValues[1].trim() }.toList()
                val dates = datePattern.findAll(html).map { it.groupValues[1].trim() }.toList()
                val forges = forgePattern.findAll(html).map { it.groupValues[1].trim() }.toList()
                val jars  = jarPattern.findAll(html).map { it.groupValues[1].trim() }.toList()

                if (names.size != dates.size || names.size != forges.size || names.size != jars.size) {
                    lWarning("The number of parsed fields is inconsistent.")
                    return@withContext emptyList()
                }

                val versions = mutableListOf<OptiFineVersion>()
                for (i in names.indices) {
                    ensureActive()

                    val rawName = jars[i].removeSuffix(".jar").removePrefix("preview_")
                    val rawNameSpaced = rawName.replace("_", " ")

                    val isPreview = jars[i].startsWith("preview_")

                    val displayName = rawNameSpaced
                        .replace("OptiFine ", "")
                        .replace("HD U ", "")
                        .replace(".0 ", " ")

                    val inherit = displayName.split(" ")[0]
                    val fileName = (if (isPreview) "preview_" else "") + "$rawName.jar"

                    val versionName = if (rawName.contains("$inherit.0_")) {
                        //OptiFine_1.9.0_HD_U_E7 -> 1.9-OptiFine_HD_U_E7
                        "${inherit}-${rawName.replace("$inherit.0_", "")}"
                    } else {
                        //OptiFine_1.10.2_HD_U_C1 -> 1.10.2-OptiFine_HD_U_C1
                        "${inherit}-${rawName.replace("${inherit}_", "")}"
                    }

                    val rawDate = dates[i]
                    val parts = rawDate.split('.')
                    val formattedDate = if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else rawDate

                    //提取Forge版本
                    val forgeVersion = forges[i]
                        .takeIf { !it.contains("N/A") }
                        ?.removePrefix("Forge ")
                        ?.replace("#", "")
                        ?.trim()

                    versions.add(
                        OptiFineVersion(
                            displayName = displayName,
                            fileName = fileName,
                            version = versionName,
                            inherit = inherit,
                            releaseDate = formattedDate,
                            forgeVersion = forgeVersion,
                            isPreview = isPreview
                        )
                    )
                }
                cacheResult = versions
                versions
            } catch(_: CancellationException) {
                lDebug("Client cancelled.")
                null
            } catch (e: Exception) {
                lWarning("Failed to fetch OptiFine list!", e)
                throw e
            }
        }
    }

    /**
     * 从镜像源获取版本列表
     */
    private fun fetchListWithBMCLAPI(
        force: Boolean = false,
        delayMillis: Long
    ): MirrorSource<List<OptiFineVersion>?> = MirrorSource(
        delayMillis = delayMillis,
        type = SourceType.BMCLAPI
    ) {
        withContext(Dispatchers.Default) {
            if (!force) cacheResult?.let { return@withContext it }

            try {
                val tokens: List<OptiFineVersionToken> = withContext(Dispatchers.IO) {
                    GLOBAL_CLIENT.get("https://bmclapi2.bangbang93.com/optifine/versionList").body()
                }

                tokens.map { token ->
                    val nameDisplay = (token.mcVersion + token.type.replace("HD_U", "").replace("_", " ") + " " + token.patch)
                        .replace(".0 ", " ")
                    val requiredForgeVersion = token.forge
                        ?.replace("Forge ", "")
                        ?.replace("#", "")
                        ?.takeUnless { it.contains("N/A", ignoreCase = true) }

                    val versionName = token.mcVersion + "-OptiFine_" +
                            (token.type + " " + token.patch)
                                .replace(".0 ", " ")
                                .replace(" ", "_")
                                .replace(token.mcVersion + "_", "")

                    OptiFineVersion(
                        displayName = nameDisplay,
                        fileName = token.fileName,
                        version = versionName,
                        inherit = token.mcVersion,
                        releaseDate = "",
                        forgeVersion = requiredForgeVersion,
                        isPreview = token.patch.contains("pre", ignoreCase = true)
                    )
                }.also {
                    cacheResult = it
                }
            } catch(_: CancellationException) {
                lDebug("Client cancelled.")
                null
            } catch (e: Exception) {
                lWarning("Failed to fetch OptiFine list!", e)
                throw e
            }
        }
    }

    /**
     * 获取 OF 对应文件下载链接
     */
    suspend fun fetchOptiFineDownloadUrl(fileName: String): String? = withContext(Dispatchers.Default) {
        try {
            val response: HttpResponse = withContext(Dispatchers.IO) {
                GLOBAL_CLIENT.get("${OPTIFINE_ADLOADX_URL}?f=$fileName") {
                    contentType(ContentType.Text.Html)
                }
            }

            val html = response.bodyAsText()

            val match = Regex("""downloadx\?f=[^"'<>]+""").find(html)
            val downloadPath = match?.value

            if (downloadPath != null) {
                val finalUrl = "$OPTIFINE_URL/$downloadPath"
                return@withContext finalUrl
            } else {
                return@withContext null
            }

        } catch (e: Exception) {
            lWarning("Failed to fetch $fileName download url!", e)
            return@withContext null
        }
    }
}