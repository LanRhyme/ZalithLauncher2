package com.movtery.zalithlauncher.ui.screens.content

import android.content.Context
import android.os.Environment
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.path.GamePathManager
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.game.version.installed.cleanup.GameAssetCleaner
import com.movtery.zalithlauncher.state.MutableStates
import com.movtery.zalithlauncher.ui.activities.MainActivity
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.IconTextButton
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.ScalingActionButton
import com.movtery.zalithlauncher.ui.components.ScalingLabel
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.content.elements.CleanupOperation
import com.movtery.zalithlauncher.ui.screens.content.elements.GamePathItemLayout
import com.movtery.zalithlauncher.ui.screens.content.elements.GamePathOperation
import com.movtery.zalithlauncher.ui.screens.content.elements.VersionCategory
import com.movtery.zalithlauncher.ui.screens.content.elements.VersionCategoryItem
import com.movtery.zalithlauncher.ui.screens.content.elements.VersionItemLayout
import com.movtery.zalithlauncher.ui.screens.content.elements.VersionsOperation
import com.movtery.zalithlauncher.utils.StoragePermissionsUtils
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel

private class VersionsScreenViewModel() : ViewModel() {
    /** 版本类别分类 */
    var versionCategory by mutableStateOf(VersionCategory.ALL)

    /** 清理游戏文件操作 */
    var cleanupOperation by mutableStateOf<CleanupOperation>(CleanupOperation.None)

    /** 游戏无用资源清理者 */
    var cleaner by mutableStateOf<GameAssetCleaner?>(null)

    fun cleanUnusedFiles(context: Context) {
        cleaner = GameAssetCleaner(context, viewModelScope).also {
            it.start(
                onEnd = { count, size ->
                    cleaner = null
                    cleanupOperation = CleanupOperation.Success(count, size)
                },
                onThrowable = { th ->
                    cleaner = null
                    cleanupOperation = CleanupOperation.Error(th)
                }
            )
        }
    }

    fun cancelCleaner() {
        cleaner?.cancel()
        cleaner = null
    }

    override fun onCleared() {
        cancelCleaner()
    }
}

@Composable
private fun rememberVersionViewModel() : VersionsScreenViewModel {
    return viewModel(
        key = NormalNavKey.VersionsManager.toString()
    ) {
        VersionsScreenViewModel()
    }
}

@Composable
fun VersionsManageScreen(
    backScreenViewModel: ScreenBackStackViewModel,
    navigateToVersions: (Version) -> Unit,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    val viewModel = rememberVersionViewModel()
    val context = LocalContext.current

    BaseScreen(
        screenKey = NormalNavKey.VersionsManager,
        currentKey = backScreenViewModel.mainScreen.currentKey
    ) { isVisible ->
        Row {
            LeftMenu(
                isVisible = isVisible,
                swapToFileSelector = { path ->
                    backScreenViewModel.mainScreen.backStack.navigateToFileSelector(
                        startPath = path,
                        selectFile = false,
                        saveKey = NormalNavKey.VersionsManager
                    )
                },
                onCleanupGameFiles = {
                    viewModel.cleanupOperation = CleanupOperation.Tip
                },
                submitError = submitError,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(2.5f)
            )

            VersionsLayout(
                isVisible = isVisible,
                versionCategory = viewModel.versionCategory,
                onCategoryChange = { viewModel.versionCategory = it },
                navigateToVersions = navigateToVersions,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(7.5f)
                    .padding(vertical = 12.dp)
                    .padding(end = 12.dp),
                submitError = submitError,
                onRefresh = {
                    if (!VersionsManager.isRefreshing) {
                        VersionsManager.refresh()
                    }
                },
                onInstall = {
                    backScreenViewModel.navigateToDownload()
                }
            )

            CleanupOperation(
                operation = viewModel.cleanupOperation,
                changeOperation = { viewModel.cleanupOperation = it },
                cleaner = viewModel.cleaner,
                onClean = {
                    viewModel.cleanUnusedFiles(context)
                },
                onCancel = {
                    viewModel.cancelCleaner()
                },
                submitError = submitError
            )
        }
    }
}

@Composable
private fun LeftMenu(
    isVisible: Boolean,
    swapToFileSelector: (path: String) -> Unit,
    onCleanupGameFiles: () -> Unit,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit,
    modifier: Modifier = Modifier
) {
    val surfaceXOffset by swapAnimateDpAsState(
        targetValue = (-40).dp,
        swapIn = isVisible,
        isHorizontal = true
    )

    var gamePathOperation by remember { mutableStateOf<GamePathOperation>(GamePathOperation.None) }
    LaunchedEffect(MutableStates.filePathSelector) {
        MutableStates.filePathSelector?.let {
            if (it.saveKey == NormalNavKey.VersionsManager) {
                gamePathOperation = GamePathOperation.AddNewPath(it.path)
                MutableStates.filePathSelector = null
            }
        }
    }
    GamePathOperation(
        gamePathOperation = gamePathOperation,
        changeState = { gamePathOperation = it },
        submitError = submitError
    )

    Column(
        modifier = modifier.offset { IntOffset(x = surfaceXOffset.roundToPx(), y = 0) },
    ) {
        val gamePaths by GamePathManager.gamePathData.collectAsState()
        val currentPath = GamePathManager.currentPath
        val context = LocalContext.current

        LazyColumn(
            modifier = Modifier
                .padding(all = 12.dp)
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(gamePaths) { pathItem ->
                GamePathItemLayout(
                    item = pathItem,
                    selected = currentPath == pathItem.path,
                    onClick = {
                        if (!VersionsManager.isRefreshing) { //避免频繁刷新，防止currentGameInfo意外重置
                            if (pathItem.id == GamePathManager.DEFAULT_ID) {
                                GamePathManager.saveDefaultPath()
                            } else {
                                (context as? MainActivity)?.let { activity ->
                                    StoragePermissionsUtils.checkPermissions(
                                        activity = activity,
                                        message = activity.getString(R.string.versions_manage_game_storage_permissions),
                                        messageSdk30 = activity.getString(R.string.versions_manage_game_storage_permissions_sdk30),
                                        hasPermission = {
                                            GamePathManager.saveCurrentPath(pathItem.id)
                                        }
                                    )
                                }
                            }
                        }
                    },
                    onDelete = {
                        gamePathOperation = GamePathOperation.DeletePath(pathItem)
                    },
                    onRename = {
                        gamePathOperation = GamePathOperation.RenamePath(pathItem)
                    }
                )
            }
        }

        ScalingActionButton(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .padding(top = 8.dp)
                .fillMaxWidth(),
            onClick = {
                (context as? MainActivity)?.let { activity ->
                    StoragePermissionsUtils.checkPermissions(
                        activity = activity,
                        message = activity.getString(R.string.versions_manage_game_path_storage_permissions),
                        messageSdk30 = activity.getString(R.string.versions_manage_game_path_storage_permissions_sdk30),
                        hasPermission = {
                            swapToFileSelector(Environment.getExternalStorageDirectory().absolutePath)
                        }
                    )
                }
            }
        ) {
            MarqueeText(text = stringResource(R.string.versions_manage_game_path_add_new))
        }

        ScalingActionButton(
            modifier = Modifier
                .padding(PaddingValues(horizontal = 12.dp, vertical = 8.dp))
                .fillMaxWidth(),
            onClick = onCleanupGameFiles
        ) {
            MarqueeText(text = stringResource(R.string.versions_manage_cleanup))
        }
    }
}

@Composable
private fun VersionsLayout(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    versionCategory: VersionCategory,
    onCategoryChange: (VersionCategory) -> Unit,
    navigateToVersions: (Version) -> Unit,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit,
    onRefresh: () -> Unit,
    onInstall: () -> Unit
) {
    val surfaceYOffset by swapAnimateDpAsState(
        targetValue = (-40).dp,
        swapIn = isVisible
    )

    Card(
        modifier = modifier
            .offset {
                IntOffset(x = 0, y = surfaceYOffset.roundToPx())
            },
        shape = MaterialTheme.shapes.extraLarge
    ) {
        if (VersionsManager.isRefreshing) { //版本正在刷新中
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        } else {
            val versions by when (versionCategory) { //在这里使用已经提前分类好的版本列表
                VersionCategory.ALL -> VersionsManager.versions
                VersionCategory.VANILLA -> VersionsManager.vanillaVersions
                VersionCategory.MODLOADER -> VersionsManager.modloaderVersions
            }.collectAsState()

            var versionsOperation by remember { mutableStateOf<VersionsOperation>(VersionsOperation.None) }
            VersionsOperation(
                versionsOperation = versionsOperation,
                updateVersionsOperation = { versionsOperation = it },
                submitError = submitError
            )

            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(state = rememberScrollState())
                        .padding(PaddingValues(horizontal = 16.dp, vertical = 8.dp)),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconTextButton(
                        onClick = onRefresh,
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = stringResource(R.string.generic_refresh),
                        text = stringResource(R.string.generic_refresh),
                    )
                    IconTextButton(
                        onClick = onInstall,
                        imageVector = Icons.Filled.Download,
                        contentDescription = stringResource(R.string.versions_manage_install_new),
                        text = stringResource(R.string.versions_manage_install_new),
                    )
                    //版本分类
                    VersionCategoryItem(
                        value = VersionCategory.ALL,
                        versionsCount = VersionsManager.allVersionsCount(),
                        selected = versionCategory == VersionCategory.ALL,
                        onClick = { onCategoryChange(VersionCategory.ALL) }
                    )
                    VersionCategoryItem(
                        value = VersionCategory.VANILLA,
                        versionsCount = VersionsManager.vanillaVersionsCount(),
                        selected = versionCategory == VersionCategory.VANILLA,
                        onClick = { onCategoryChange(VersionCategory.VANILLA) }
                    )
                    VersionCategoryItem(
                        value = VersionCategory.MODLOADER,
                        versionsCount = VersionsManager.modloaderVersionsCount(),
                        selected = versionCategory == VersionCategory.MODLOADER,
                        onClick = { onCategoryChange(VersionCategory.MODLOADER) }
                    )
                }

                HorizontalDivider(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (versions.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        items(versions) { version ->
                            VersionItemLayout(
                                version = version,
                                selected = version == VersionsManager.currentVersion,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                onSelected = {
                                    if (version.isValid() && version != VersionsManager.currentVersion) {
                                        VersionsManager.saveCurrentVersion(version.getVersionName())
                                    } else {
                                        //不允许选择无效版本
                                        versionsOperation = VersionsOperation.InvalidDelete(version)
                                    }
                                },
                                onSettingsClick = {
                                    navigateToVersions(version)
                                },
                                onRenameClick = { versionsOperation = VersionsOperation.Rename(version) },
                                onCopyClick = { versionsOperation = VersionsOperation.Copy(version) },
                                onDeleteClick = { versionsOperation = VersionsOperation.Delete(version) }
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        ScalingLabel(
                            modifier = Modifier.align(Alignment.Center),
                            text = stringResource(R.string.versions_manage_no_versions)
                        )
                    }
                }
            }
        }
    }
}