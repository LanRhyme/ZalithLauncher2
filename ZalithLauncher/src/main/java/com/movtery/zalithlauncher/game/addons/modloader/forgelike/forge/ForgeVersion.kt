package com.movtery.zalithlauncher.game.addons.modloader.forgelike.forge

import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.addons.modloader.forgelike.ForgeBuildVersion
import com.movtery.zalithlauncher.game.addons.modloader.forgelike.ForgeLikeVersion

/**
 * [Reference PCL2](https://github.com/Hex-Dragon/PCL2/blob/44aea3e/Plain%20Craft%20Launcher%202/Modules/Minecraft/ModDownload.vb#L565-L599)
 */
class ForgeVersion(
    versionName: String,
    branch: String?,
    inherit: String,
    /** 发布时间，格式为“yyyy/MM/dd HH:mm” */
    val releaseTime: String,
    /** 文件的 MD5 或 SHA1 */
    val hash: String?,
    /** 是否为推荐版本 */
    val isRecommended: Boolean,
    /** 安装类型: installer、client、universal */
    val category: String,
    /** 用于下载的文件版本名。可能在 Version 的基础上添加了分支。 */
    val fileVersion: String
) : ForgeLikeVersion(
    loaderName = ModLoader.FORGE.displayName,
    forgeBuildVersion = parseVersion(versionName, branch, inherit),
    versionName = versionName,
    inherit = inherit,
    fileExtension = if (category == "installer") "jar" else "zip"
)

/**
 * [Reference PCL2](https://github.com/Hex-Dragon/PCL2/blob/44aea3e/Plain%20Craft%20Launcher%202/Modules/Minecraft/ModDownload.vb#L588-L598)
 */
private fun parseVersion(version: String, branch: String?, inherit: String): ForgeBuildVersion {
    val specialVersions = listOf("11.15.1.2318", "11.15.1.1902", "11.15.1.1890")
    val modifiedBranch = when {
        version in specialVersions -> "1.8.9"
        branch == null && inherit == "1.7.10" && version.split(".")[3].toInt() >= 1300 -> "1.7.10"
        else -> branch
    }
    return ForgeBuildVersion.parse(version + (modifiedBranch?.let { "-$it" } ?: ""))
}