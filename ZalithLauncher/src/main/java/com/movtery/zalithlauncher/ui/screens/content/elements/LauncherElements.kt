package com.movtery.zalithlauncher.ui.screens.content.elements

import android.app.Activity
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.gif.GifDecoder
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.crossfade
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.launch.LaunchGame
import com.movtery.zalithlauncher.game.plugin.renderer.RendererPluginManager
import com.movtery.zalithlauncher.game.renderer.RendererInterface
import com.movtery.zalithlauncher.game.renderer.Renderers
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.VideoPlayer
import com.movtery.zalithlauncher.utils.checkStoragePermissions
import com.movtery.zalithlauncher.utils.file.InvalidFilenameException
import com.movtery.zalithlauncher.utils.file.checkFilenameValidity
import com.movtery.zalithlauncher.utils.string.isBiggerTo
import com.movtery.zalithlauncher.utils.string.isLowerTo
import com.movtery.zalithlauncher.viewmodel.BackgroundViewModel
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

sealed interface LaunchGameOperation {
    data object None : LaunchGameOperation
    /** 没有安装版本/没有选中有效版本 */
    data object NoVersion : LaunchGameOperation
    /** 版本名称非法时 */
    data class InvalidVersionName(val th: InvalidFilenameException) : LaunchGameOperation
    /** 没有可用账号 */
    data object NoAccount : LaunchGameOperation

    /** 渲染器可配置，但需要用到文件管理权限 */
    data class RendererNoStoragePermission(
        val renderer: RendererInterface,
        val version: Version,
        val quickPlay: String?
    ) : LaunchGameOperation

    /** 当前渲染器不支持选中版本 */
    data class UnsupportedRenderer(
        val renderer: RendererInterface,
        val version: Version,
        val quickPlay: String?
    ): LaunchGameOperation

    /** 尝试启动：启动前检查一些东西 */
    data class TryLaunch(
        val version: Version?,
        val quickPlay: String? = null
    ) : LaunchGameOperation

    /** 正式启动 */
    data class RealLaunch(
        val version: Version,
        val quickPlay: String?
    ) : LaunchGameOperation
}

@Composable
fun LaunchGameOperation(
    activity: Activity,
    launchGameOperation: LaunchGameOperation,
    updateOperation: (LaunchGameOperation) -> Unit,
    exitActivity: () -> Unit,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit,
    toAccountManageScreen: () -> Unit = {},
    toVersionManageScreen: () -> Unit = {}
) {
    when (launchGameOperation) {
        is LaunchGameOperation.None -> {}
        is LaunchGameOperation.NoVersion -> {
            LaunchedEffect(Unit) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(activity, R.string.game_launch_no_version, Toast.LENGTH_SHORT).show()
                }
                toVersionManageScreen()
                updateOperation(LaunchGameOperation.None)
            }
        }
        is LaunchGameOperation.InvalidVersionName -> {
            val th = launchGameOperation.th
            SimpleAlertDialog(
                title = stringResource(R.string.versions_manage_invalid),
                text = th.getInvalidSummary(),
                confirmText = stringResource(R.string.generic_cancel),
                onDismiss = {
                    updateOperation(LaunchGameOperation.None)
                }
            )
        }
        is LaunchGameOperation.NoAccount -> {
            LaunchedEffect(Unit) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(activity, R.string.game_launch_no_account, Toast.LENGTH_SHORT).show()
                }
                toAccountManageScreen()
                updateOperation(LaunchGameOperation.None)
            }
        }
        is LaunchGameOperation.RendererNoStoragePermission -> {
            LaunchedEffect(Unit) {
                val renderer = launchGameOperation.renderer
                val version = launchGameOperation.version
                val quickPlay = launchGameOperation.quickPlay
                withContext(Dispatchers.Main) {
                    checkStoragePermissions(
                        activity = activity,
                        message = activity.getString(R.string.renderer_version_storage_permissions, renderer.getRendererName()),
                        messageSdk30 = activity.getString(R.string.renderer_version_storage_permissions_sdk30, renderer.getRendererName()),
                        onDialogCancel = {
                            //用户拒绝授权，但仍然允许启动（不过这会导致配置无法读取）
                            updateOperation(LaunchGameOperation.RealLaunch(version, quickPlay))
                        }
                    )
                }
                updateOperation(LaunchGameOperation.None)
            }
        }
        is LaunchGameOperation.UnsupportedRenderer -> {
            val renderer = launchGameOperation.renderer
            val version = launchGameOperation.version
            val quickPlay = launchGameOperation.quickPlay
            SimpleAlertDialog(
                title = stringResource(R.string.generic_warning),
                text = stringResource(R.string.renderer_version_unsupported_warning, renderer.getRendererName()),
                confirmText = stringResource(R.string.generic_anyway),
                onConfirm = {
                    updateOperation(LaunchGameOperation.RealLaunch(version, quickPlay))
                },
                onDismiss = {
                    updateOperation(LaunchGameOperation.None)
                }
            )
        }
        is LaunchGameOperation.TryLaunch -> {
            LaunchedEffect(Unit) {
                val version = launchGameOperation.version ?: run {
                    updateOperation(LaunchGameOperation.NoVersion)
                    return@LaunchedEffect
                }

                try {
                    checkFilenameValidity(version.getVersionName())
                } catch (th: InvalidFilenameException) {
                    updateOperation(LaunchGameOperation.InvalidVersionName(th))
                    return@LaunchedEffect
                }

                val quickPlay = launchGameOperation.quickPlay

                AccountsManager.getCurrentAccount() ?: run {
                    updateOperation(LaunchGameOperation.NoAccount)
                    return@LaunchedEffect
                }

                //开始检查渲染器的版本支持情况
                Renderers.setCurrentRenderer(activity, version.getRenderer())
                val currentRenderer = Renderers.getCurrentRenderer()
                val rendererMinVer = currentRenderer.getMinMCVersion()
                val rendererMaxVer = currentRenderer.getMaxMCVersion()

                val mcVer = version.getVersionInfo()!!.minecraftVersion

                val isUnsupported =
                    (rendererMinVer?.let { mcVer.isLowerTo(it) } ?: false) ||
                            (rendererMaxVer?.let { mcVer.isBiggerTo(it) } ?: false)

                if (isUnsupported) {
                    updateOperation(LaunchGameOperation.UnsupportedRenderer(currentRenderer, version, quickPlay))
                    return@LaunchedEffect
                }

                //为可配置的渲染器检查文件管理权限
                if (
                    !checkStoragePermissions() &&
                    RendererPluginManager.isConfigurablePlugin(version.getRenderer())
                ) {
                    updateOperation(LaunchGameOperation.RendererNoStoragePermission(currentRenderer, version, quickPlay))
                    return@LaunchedEffect
                }

                //正式启动游戏
                updateOperation(LaunchGameOperation.RealLaunch(version, quickPlay))
            }
        }
        is LaunchGameOperation.RealLaunch -> {
            LaunchedEffect(Unit) {
                val version = launchGameOperation.version
                val quickPlay = launchGameOperation.quickPlay
                version.apply {
                    offlineAccountLogin = false
                    quickPlaySingle = quickPlay
                }
                LaunchGame.launchGame(activity, version, exitActivity, submitError)
                updateOperation(LaunchGameOperation.None)
            }
        }
    }
}

@Composable
fun Background(
    viewModel: BackgroundViewModel,
    modifier: Modifier = Modifier
) {
    if (viewModel.isValid) {
        if (viewModel.isVideo) {
            VideoPlayer(
                videoUri = Uri.fromFile(viewModel.backgroundFile),
                modifier = modifier,
                refreshTrigger = viewModel.refreshTrigger
            )
        } else if (viewModel.isImage) {
            BackgroundImage(
                modifier = modifier,
                imageFile = viewModel.backgroundFile,
                refreshTrigger = viewModel.refreshTrigger
            )
        }
    }
}

@Composable
private fun BackgroundImage(
    refreshTrigger: Any,
    imageFile: File,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val imageLoader = remember(refreshTrigger) {
        ImageLoader.Builder(context)
            .components { add(GifDecoder.Factory()) }
            .build()
    }
    val request = remember(refreshTrigger) {
        ImageRequest.Builder(context)
            .data(imageFile)
            .allowHardware(false)
            .crossfade(false)
            .build()
    }

    AsyncImage(
        modifier = modifier,
        model = request,
        imageLoader = imageLoader,
        contentDescription = null,
        contentScale = ContentScale.Crop
    )
}