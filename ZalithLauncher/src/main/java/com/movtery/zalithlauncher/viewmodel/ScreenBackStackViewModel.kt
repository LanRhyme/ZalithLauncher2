package com.movtery.zalithlauncher.viewmodel

import androidx.lifecycle.ViewModel
import androidx.navigation3.runtime.NavKey
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.addIfEmpty
import kotlin.reflect.KClass

class ScreenBackStackViewModel : ViewModel() {
    /** 主屏幕 */
    val mainScreen = NestedNavKey.Main()
    /** 设置屏幕 */
    val settingsScreen = NestedNavKey.Settings()
    /** 下载屏幕 */
    val downloadScreen = NestedNavKey.Download()

    /** 下载游戏屏幕 */
    val downloadGameScreen = NestedNavKey.DownloadGame()
    /** 下载整合包屏幕 */
    val downloadModPackScreen = NestedNavKey.DownloadModPack()
    /** 下载模组屏幕 */
    val downloadModScreen = NestedNavKey.DownloadMod()
    /** 下载资源包屏幕 */
    val downloadResourcePackScreen = NestedNavKey.DownloadResourcePack()
    /** 下载存档屏幕 */
    val downloadSavesScreen = NestedNavKey.DownloadSaves()
    /** 下载光影包屏幕 */
    val downloadShadersScreen = NestedNavKey.DownloadShaders()

    init {
        mainScreen.backStack.addIfEmpty(NormalNavKey.LauncherMain)
        settingsScreen.backStack.addIfEmpty(NormalNavKey.Settings.Renderer)
        //下载嵌套子屏幕
        downloadGameScreen.backStack.addIfEmpty(NormalNavKey.DownloadGame.SelectGameVersion)
        downloadModPackScreen.backStack.addIfEmpty(NormalNavKey.SearchModPack)
        downloadModScreen.backStack.addIfEmpty(NormalNavKey.SearchMod)
        downloadResourcePackScreen.backStack.addIfEmpty(NormalNavKey.SearchResourcePack)
        downloadSavesScreen.backStack.addIfEmpty(NormalNavKey.SearchSaves)
        downloadShadersScreen.backStack.addIfEmpty(NormalNavKey.SearchShaders)
    }

    /**
     * 在跳转前，先将导航栈中所有属于 [clearBeforeNavKeys] 的页面全部移除
     * 这样可以避免用户在这几个页面间产生叠加栈或多层返回的情况
     */
    val clearBeforeNavKeys = listOf<KClass<out NavKey>>(settingsScreen::class, downloadScreen::class)
}