package com.movtery.zalithlauncher.game.download.assets.platform

import java.time.Instant

/**
 * 平台版本项实现，使用前，需要注意使用[initFile]函数进行初始化
 */
interface PlatformVersion {
    /**
     * 初始化版本数据，一些版本可能需要额外的操作来完成初始化
     * @param currentProjectId 当前版本所属的项目Id，辅助完成初始化
     * @return 是否初始化成功，若未成功可以考虑跳过该版本
     */
    suspend fun initFile(currentProjectId: String): Boolean

    /**
     * 所属平台
     */
    fun platform(): Platform

    /**
     * 该版本在平台上的Id
     */
    fun platformId(): String

    /**
     * 该版本在平台上的显示名称
     */
    fun platformDisplayName(): String

    /**
     * 该版本的文件名称
     */
    fun platformFileName(): String

    /**
     * 该版本兼容的游戏主版本
     */
    fun platformGameVersion(): Array<String>

    /**
     * 该版本在平台上标注的支持的加载器列表
     */
    fun platformLoaders(): List<PlatformDisplayLabel>

    /**
     * 该版本在平台上的发布类型
     */
    fun platformReleaseType(): PlatformReleaseType

    /**
     * 该版本在平台上所依赖的项目
     */
    fun platformDependencies(): List<PlatformDependency>

    /**
     * 该版本在平台上的总下载量
     */
    fun platformDownloadCount(): Long

    /**
     * 该版本的下载链接
     */
    fun platformDownloadUrl(): String

    /**
     * 该版本在平台上的发布日期
     */
    fun platformDatePublished(): Instant

    /**
     * 该版本的文件sha1值
     */
    fun platformSha1(): String?

    /**
     * 该版本的文件总大小
     */
    fun platformFileSize(): Long

    /**
     * 该版本在平台上标注的版本号名称
     */
    fun platformVersion(): String

    /**
     * 平台版本依赖项目类，保存依赖项关键信息
     * @param type 依赖类型
     */
    class PlatformDependency(
        val platform: Platform,
        val projectId: String,
        val type: PlatformDependencyType
    )
}