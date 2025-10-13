package com.movtery.zalithlauncher.game.addons.modloader.forgelike.neoforge

import com.movtery.zalithlauncher.game.addons.mirror.MirrorSource
import com.movtery.zalithlauncher.game.addons.mirror.SourceType
import com.movtery.zalithlauncher.game.addons.mirror.runMirrorable
import com.movtery.zalithlauncher.game.addons.modloader.forgelike.neoforge.models.BMCLAPIMaven
import com.movtery.zalithlauncher.game.addons.modloader.forgelike.neoforge.models.NeoForgedMaven
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.enums.MirrorSourceType
import com.movtery.zalithlauncher.utils.logging.Logger.lDebug
import com.movtery.zalithlauncher.utils.logging.Logger.lWarning
import com.movtery.zalithlauncher.utils.network.httpGet
import com.movtery.zalithlauncher.utils.network.withRetry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object NeoForgeVersions {
    private const val TAG = "NeoForgeVersions"
    private var cacheResult: List<NeoForgeVersion>? = null

    /**
     * 获取 NeoForge 版本列表
     * [Reference PCL2](https://github.com/Meloong-Git/PCL/blob/28ef67e/Plain%20Craft%20Launcher%202/Modules/Minecraft/ModDownload.vb#L811-L830)
     */
    suspend fun fetchNeoForgeList(
        force: Boolean = false,
        gameVersion: String
    ): List<NeoForgeVersion>? = withContext(Dispatchers.Default) {
        if (!force) cacheResult?.let {
            return@withContext it.outputVersionList(gameVersion)
        }

        runMirrorable(
            when (AllSettings.fetchModLoaderSource.getValue()) {
                MirrorSourceType.OFFICIAL_FIRST -> listOf(fetchListWithOfficial(5), fetchListWithBMCLAPI(5 + 30))
                MirrorSourceType.MIRROR_FIRST -> listOf(fetchListWithBMCLAPI(30), fetchListWithOfficial(30 + 60))
            }
        )?.also {
            cacheResult = it
        }?.outputVersionList(gameVersion)
    }

    private fun List<NeoForgeVersion>.outputVersionList(
        gameVersion: String
    ): List<NeoForgeVersion> {
        return this.filter {
            it.inherit == gameVersion
        }.sortedWith { o1, o2 ->
            o2.forgeBuildVersion.compareTo(o1.forgeBuildVersion)
        }
    }

    /**
     * 在官方源获取版本列表
     */
    private fun fetchListWithOfficial(delayMillis: Long): MirrorSource<List<NeoForgeVersion>?> = MirrorSource(
        delayMillis = delayMillis,
        type = SourceType.OFFICIAL
    ) {
        processVersionList {
            val neoforge = withRetry(TAG, maxRetries = 2) {
                httpGet<NeoForgedMaven>(url = "https://maven.neoforged.net/api/maven/versions/releases/net/neoforged/neoforge")
            }
            val legacyForge = withRetry(TAG, maxRetries = 2) {
                httpGet<NeoForgedMaven>(url = "https://maven.neoforged.net/api/maven/versions/releases/net/neoforged/forge")
            }.copy(isLegacy = true)

            neoforge + legacyForge
        }
    }

    /**
     * 在BMCL API源获取版本列表
     */
    private fun fetchListWithBMCLAPI(delayMillis: Long): MirrorSource<List<NeoForgeVersion>?> = MirrorSource(
        delayMillis = delayMillis,
        type = SourceType.BMCLAPI
    ) {
        processVersionList {
            val neoforge = withRetry(TAG, maxRetries = 2) {
                httpGet<BMCLAPIMaven>(url = "https://bmclapi2.bangbang93.com/neoforge/meta/api/maven/details/releases/net/neoforged/neoforge")
            }
            val legacyForge = withRetry(TAG, maxRetries = 2) {
                httpGet<BMCLAPIMaven>(url = "https://bmclapi2.bangbang93.com/neoforge/meta/api/maven/details/releases/net/neoforged/forge")
            }.copy(isLegacy = true)

            neoforge + legacyForge
        }
    }

    /**
     * 统一处理任务，处理异常、排序
     */
    private suspend fun processVersionList(
        block: suspend () -> List<NeoForgeVersion>
    ): List<NeoForgeVersion>? = withContext(Dispatchers.IO) {
        try {
            block()
                .sortedByDescending { it.forgeBuildVersion }
                .toList()
        } catch (_: CancellationException) {
            lDebug("Client cancelled.")
            null
        } catch (e: Exception) {
            lWarning("Failed to fetch neoforge list!", e)
            throw e
        }
    }

    /**
     * 获取 NeoForge 对应版本的下载链接
     */
    fun getDownloadUrl(version: NeoForgeVersion) = "${version.baseUrl}-installer.jar"
}