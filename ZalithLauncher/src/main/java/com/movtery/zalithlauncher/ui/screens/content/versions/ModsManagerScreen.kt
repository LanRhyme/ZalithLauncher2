package com.movtery.zalithlauncher.ui.screens.content.versions

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformVersion
import com.movtery.zalithlauncher.game.download.assets.utils.getMcmodTitle
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionFolders
import com.movtery.zalithlauncher.game.version.mod.AllModReader
import com.movtery.zalithlauncher.game.version.mod.LocalMod
import com.movtery.zalithlauncher.game.version.mod.RemoteMod
import com.movtery.zalithlauncher.game.version.mod.isDisabled
import com.movtery.zalithlauncher.game.version.mod.isEnabled
import com.movtery.zalithlauncher.game.version.mod.update.ModData
import com.movtery.zalithlauncher.game.version.mod.update.ModUpdater
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.EdgeDirection
import com.movtery.zalithlauncher.ui.components.IconTextButton
import com.movtery.zalithlauncher.ui.components.LittleTextLabel
import com.movtery.zalithlauncher.ui.components.ScalingLabel
import com.movtery.zalithlauncher.ui.components.SimpleTextInputField
import com.movtery.zalithlauncher.ui.components.TooltipIconButton
import com.movtery.zalithlauncher.ui.components.fadeEdge
import com.movtery.zalithlauncher.ui.components.itemLayoutColor
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.AssetsIcon
import com.movtery.zalithlauncher.ui.screens.content.elements.ImportFileButton
import com.movtery.zalithlauncher.ui.screens.content.versions.elements.ByteArrayIcon
import com.movtery.zalithlauncher.ui.screens.content.versions.elements.DeleteAllOperation
import com.movtery.zalithlauncher.ui.screens.content.versions.elements.LoadingState
import com.movtery.zalithlauncher.ui.screens.content.versions.elements.ModsConfirmOperation
import com.movtery.zalithlauncher.ui.screens.content.versions.elements.ModsOperation
import com.movtery.zalithlauncher.ui.screens.content.versions.elements.ModsUpdateOperation
import com.movtery.zalithlauncher.ui.screens.content.versions.elements.filterMods
import com.movtery.zalithlauncher.ui.screens.content.versions.layouts.VersionSettingsBackground
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState
import com.movtery.zalithlauncher.utils.file.formatFileSize
import com.movtery.zalithlauncher.utils.string.isNotEmptyOrBlank
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.LinkedList
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

private class ModsManageViewModel(
    modsDir: File
) : ViewModel() {
    val modReader = AllModReader(modsDir)

    var nameFilter by mutableStateOf("")

    var allMods by mutableStateOf<List<RemoteMod>>(emptyList())
        private set
    var filteredMods by mutableStateOf<List<RemoteMod>?>(null)
        private set

    /**
     * 已选择的模组
     */
    val selectedMods = mutableStateListOf<RemoteMod>()

    /**
     * 删除所有已选择模组的操作流程
     */
    var deleteAllOperation by mutableStateOf<DeleteAllOperation>(DeleteAllOperation.None)

    /** 作为标记，记录哪些模组已被加载 */
    private val modsToLoad = mutableListOf<RemoteMod>()
    private val loadQueue = LinkedList<Pair<RemoteMod, Boolean>>()
    private val semaphore = Semaphore(8) //一次最多允许同时加载8个模组
    private var initialQueueSize = 0
    private val queueMutex = Mutex()

    var modsState by mutableStateOf<LoadingState>(LoadingState.None)

    private var job: Job? = null

    fun refresh(context: Context? = null) {
        job?.cancel()
        job = viewModelScope.launch {
            modsState = LoadingState.Loading
            selectedMods.clear() //清空所有已选择的模组
            try {
                allMods = modReader.readAllMods()
                filterMods(context)
            } catch (_: CancellationException) {
                //已取消
            }
            modsState = LoadingState.None
        }
    }

    init {
        refresh()
        startQueueProcessor()
    }

    fun updateFilter(name: String, context: Context? = null) {
        this.nameFilter = name
        filterMods(context)
    }

    private fun filterMods(context: Context? = null) {
        filteredMods = allMods.takeIf { it.isNotEmpty() }?.filterMods(nameFilter, context)
    }

    /** 在ViewModel的生命周期协程内调用 */
    fun doInScope(block: suspend () -> Unit) {
        viewModelScope.launch {
            block()
        }
    }

    private fun startQueueProcessor() {
        viewModelScope.launch {
            while (true) {
                try {
                    ensureActive()
                } catch (_: Exception) {
                    break //取消
                }

                val task = queueMutex.withLock {
                    loadQueue.poll()
                } ?: run {
                    delay(100)
                    continue
                }

                val (mod, loadFromCache) = task
                semaphore.acquire()

                launch {
                    try {
                        //重载模组信息时，应从选择列表中清除
                        selectedMods.remove(mod)
                        mod.load(loadFromCache)
                    } finally {
                        semaphore.release()
                    }
                }
            }
        }
    }

    /** 加载模组远端信息 */
    fun loadMod(mod: RemoteMod, loadFromCache: Boolean = true) {
        //强制刷新：直接加入队列头部并清除旧任务
        if (!loadFromCache) {
            doInScope {
                queueMutex.withLock {
                    loadQueue.removeAll { it.first == mod }
                    loadQueue.addFirst(mod to false) //加入队头优先执行
                }
            }
            if (modsToLoad.contains(mod)) return //已在加载列表
            modsToLoad.add(mod)
            return
        }

        if (modsToLoad.contains(mod)) return

        modsToLoad.add(mod)
        doInScope {
            queueMutex.withLock {
                val canJoin = loadQueue.size <= (initialQueueSize / 2)
                if (canJoin || loadQueue.none { it.first == mod }) {
                    loadQueue.add(mod to true)
                    modsToLoad.add(mod)
                    //若当前是新一轮任务，更新初始队列总数
                    if (initialQueueSize == 0 || canJoin) {
                        initialQueueSize = loadQueue.size
                    }
                }
            }
        }
    }

    override fun onCleared() {
        viewModelScope.cancel()
    }
}


// ------------
// 模组更新
// ------------
private class ModsUpdaterViewModel(
    private val modsDir: File,
    private val version: Version
) : ViewModel() {
    var modsUpdateOperation by mutableStateOf<ModsUpdateOperation>(ModsUpdateOperation.None)
    var modsConfirmOperation by mutableStateOf<ModsConfirmOperation>(ModsConfirmOperation.None)

    //等待用户确认模组更新
    private var waitingUserContinuation: (Continuation<Boolean>)? = null
    suspend fun waitingForUserConfirm(map: Map<ModData, PlatformVersion>): Boolean {
        return suspendCancellableCoroutine { cont ->
            waitingUserContinuation = cont
            modsConfirmOperation = ModsConfirmOperation.WaitingConfirm(map)
        }
    }

    /**
     * 用户确认更新模组
     */
    fun modsUserConfirm(confirm: Boolean) {
        waitingUserContinuation?.resume(confirm)
        waitingUserContinuation = null
        modsConfirmOperation = ModsConfirmOperation.None
    }

    /**
     * 模组更新器
     */
    var modsUpdater by mutableStateOf<ModUpdater?>(null)

    fun update(
        context: Context,
        mods: List<RemoteMod>,
        refreshMods: () -> Unit
    ) {
        val minecraftVer = version.getVersionInfo()!!.minecraftVersion
        val modLoader = version.getVersionInfo()!!.loaderInfo!!.loader

        modsUpdater = ModUpdater(
            context = context,
            mods = mods,
            modsDir = modsDir,
            minecraft = minecraftVer,
            modLoader = modLoader,
            scope = viewModelScope,
            waitForUserConfirm = ::waitingForUserConfirm
        ).also {
            it.updateAll(
                onUpdated = {
                    modsUpdater = null
                    refreshMods()
                    modsUpdateOperation = ModsUpdateOperation.Success
                },
                onNoModUpdates = {
                    viewModelScope.launch(Dispatchers.Main) {
                        Toast.makeText(context, context.getString(R.string.mods_update_no_mods_update), Toast.LENGTH_SHORT).show()
                    }
                    modsUpdater = null
                    modsUpdateOperation = ModsUpdateOperation.None
                },
                onCancelled = {
                    modsUpdater = null
                    modsUpdateOperation = ModsUpdateOperation.None
                },
                onError = { th ->
                    modsUpdater = null
                    refreshMods()
                    modsUpdateOperation = ModsUpdateOperation.Error(th)
                }
            )
        }
    }

    fun cancel() {
        modsUpdater?.cancel()
        modsUpdater = null
    }

    override fun onCleared() {
        cancel()
    }
}

@Composable
private fun rememberModsManageViewModel(
    version: Version,
    modsDir: File
): ModsManageViewModel {
    return viewModel(
        key = version.toString() + "_" + VersionFolders.MOD.folderName
    ) {
        ModsManageViewModel(modsDir)
    }
}

@Composable
private fun rememberModsUpdaterViewModel(
    version: Version,
    modsDir: File
): ModsUpdaterViewModel {
    return viewModel(
        key = version.toString() + "_ModsUpdater"
    ) {
        ModsUpdaterViewModel(modsDir = modsDir, version = version)
    }
}

@Composable
fun ModsManagerScreen(
    mainScreenKey: NavKey?,
    versionsScreenKey: NavKey?,
    version: Version,
    backToMainScreen: () -> Unit,
    swapToDownload: () -> Unit,
    onSwapMoreInfo: (id: String, Platform) -> Unit,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    val context = LocalContext.current

    if (!version.isValid()) {
        backToMainScreen()
        return
    }

    BaseScreen(
        levels1 = listOf(
            Pair(NestedNavKey.VersionSettings::class.java, mainScreenKey)
        ),
        Triple(NormalNavKey.Versions.ModsManager, versionsScreenKey, false)
    ) { isVisible ->
        val modsDir = File(version.getGameDir(), VersionFolders.MOD.folderName)
        val viewModel = rememberModsManageViewModel(version, modsDir)
        val updaterViewModel = rememberModsUpdaterViewModel(version, modsDir)

        DeleteAllOperation(
            operation = viewModel.deleteAllOperation,
            changeOperation = { viewModel.deleteAllOperation = it },
            submitError = submitError,
            onRefresh = { viewModel.refresh(context) }
        )

        ModsUpdateOperation(
            operation = updaterViewModel.modsUpdateOperation,
            changeOperation = { updaterViewModel.modsUpdateOperation = it },
            modsUpdater = updaterViewModel.modsUpdater,
            onUpdate = { mods ->
                updaterViewModel.update(context, mods) {
                    //刷新模组
                    viewModel.refresh(context)
                }
            },
            onCancel = {
                updaterViewModel.cancel()
            }
        )

        ModsConfirmOperation(
            operation = updaterViewModel.modsConfirmOperation,
            changeOperation = { updaterViewModel.modsConfirmOperation = it },
            onCancel = {
                updaterViewModel.modsUserConfirm(false)
            },
            onConfirm = {
                updaterViewModel.modsUserConfirm(true)
            }
        )

        val yOffset by swapAnimateDpAsState(
            targetValue = (-40).dp,
            swapIn = isVisible
        )

        VersionSettingsBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(all = 12.dp)
                .offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
            paddingValues = PaddingValues()
        ) {
            when (viewModel.modsState) {
                LoadingState.None -> {
                    val itemColor = itemLayoutColor()
                    val itemContentColor = MaterialTheme.colorScheme.onSurface

                    var modsOperation by remember { mutableStateOf<ModsOperation>(ModsOperation.None) }
                    fun runProgress(task: () -> Unit) {
                        viewModel.doInScope {
                            withContext(Dispatchers.IO) {
                                modsOperation = ModsOperation.Progress
                                task()
                                modsOperation = ModsOperation.None
                                viewModel.refresh(context)
                            }
                        }
                    }
                    ModsOperation(
                        modsOperation = modsOperation,
                        updateOperation = { modsOperation = it },
                        onDelete = { mod ->
                            runProgress {
                                FileUtils.deleteQuietly(mod.file)
                            }
                        }
                    )

                    Column {
                        ModsActionsHeader(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .padding(top = 4.dp)
                                .fillMaxWidth(),
                            inputFieldColor = itemColor,
                            inputFieldContentColor = itemContentColor,
                            nameFilter = viewModel.nameFilter,
                            onNameFilterChange = { viewModel.updateFilter(it, context) },
                            //模组加载器信息已确认的情况下，才能够确保模组能够正常更新
                            canUpdateMods = version.getVersionInfo()?.loaderInfo != null,
                            onUpdateMods = {
                                if (
                                    updaterViewModel.modsUpdateOperation == ModsUpdateOperation.None &&
                                    viewModel.deleteAllOperation == DeleteAllOperation.None
                                ) {
                                    updaterViewModel.modsUpdateOperation = ModsUpdateOperation.Warning(viewModel.selectedMods)
                                }
                            },
                            modsDir = modsDir,
                            onDeleteAll = {
                                if (
                                    updaterViewModel.modsUpdateOperation == ModsUpdateOperation.None &&
                                    viewModel.deleteAllOperation == DeleteAllOperation.None &&
                                    viewModel.selectedMods.isNotEmpty()
                                ) {
                                    viewModel.deleteAllOperation = DeleteAllOperation.Warning(
                                        files = viewModel.selectedMods.map { mod ->
                                            mod.localMod.file
                                        }
                                    )
                                }
                            },
                            isModsSelected = viewModel.selectedMods.isNotEmpty(),
                            onClearModsSelected = { viewModel.selectedMods.clear() },
                            swapToDownload = swapToDownload,
                            refresh = { viewModel.refresh(context) },
                            submitError = submitError
                        )

                        ModsList(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            modsList = viewModel.filteredMods,
                            selectedMods = viewModel.selectedMods,
                            removeFromSelected = { mod ->
                                viewModel.selectedMods.remove(mod)
                            },
                            addToSelected = { mod ->
                                viewModel.selectedMods.add(mod)
                            },
                            onLoad = { mod ->
                                viewModel.loadMod(mod)
                            },
                            onForceRefresh = { mod ->
                                viewModel.loadMod(mod, loadFromCache = false)
                            },
                            onEnable = { mod ->
                                viewModel.doInScope {
                                    withContext(Dispatchers.IO) {
                                        mod.localMod.enable()
                                    }
                                }
                            },
                            onDisable = { mod ->
                                viewModel.doInScope {
                                    withContext(Dispatchers.IO) {
                                        mod.localMod.disable()
                                    }
                                }
                            },
                            onSwapMoreInfo = onSwapMoreInfo,
                            onDelete = { mod ->
                                modsOperation = ModsOperation.Delete(mod.localMod)
                            },
                            itemColor = itemColor,
                            itemContentColor = itemContentColor
                        )
                    }
                }
                LoadingState.Loading -> {
                    Box(Modifier.fillMaxSize()) {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    }
}

@Composable
private fun ModsActionsHeader(
    modifier: Modifier,
    inputFieldColor: Color,
    inputFieldContentColor: Color,
    nameFilter: String,
    onNameFilterChange: (String) -> Unit,
    canUpdateMods: Boolean,
    onUpdateMods: () -> Unit,
    modsDir: File,
    onDeleteAll: () -> Unit,
    isModsSelected: Boolean,
    onClearModsSelected: () -> Unit,
    swapToDownload: () -> Unit,
    refresh: () -> Unit,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit = {}
) {
    Column(modifier = modifier) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                SimpleTextInputField(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp),
                    value = nameFilter,
                    onValueChange = { onNameFilterChange(it) },
                    hint = {
                        Text(
                            text = stringResource(R.string.generic_search),
                            style = TextStyle(color = LocalContentColor.current).copy(fontSize = 12.sp)
                        )
                    },
                    color = inputFieldColor,
                    contentColor = inputFieldContentColor,
                    singleLine = true
                )

                AnimatedVisibility(
                    modifier = Modifier.height(IntrinsicSize.Min),
                    visible = isModsSelected
                ) {
                    Row {
                        if (canUpdateMods) {
                            IconButton(
                                onClick = onUpdateMods
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Update,
                                    contentDescription = null
                                )
                            }
                        }

                        IconButton(
                            onClick = onDeleteAll
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = null
                            )
                        }

                        IconButton(
                            onClick = {
                                if (isModsSelected) onClearModsSelected()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Deselect,
                                contentDescription = null
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        VerticalDivider(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                val scrollState = rememberScrollState()
                LaunchedEffect(Unit) {
                    scrollState.scrollTo(scrollState.maxValue)
                }
                Row(
                    modifier = Modifier
                        .fadeEdge(
                            state = scrollState,
                            length = 32.dp,
                            direction = EdgeDirection.Horizontal
                        )
                        .widthIn(max = this@BoxWithConstraints.maxWidth / 2)
                        .horizontalScroll(scrollState),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ImportFileButton(
                        extension = "jar",
                        targetDir = modsDir,
                        submitError = submitError,
                        onImported = refresh
                    )

                    IconTextButton(
                        onClick = swapToDownload,
                        imageVector = Icons.Default.Download,
                        text = stringResource(R.string.generic_download)
                    )

                    IconButton(
                        onClick = refresh
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.generic_refresh)
                        )
                    }
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ModsList(
    modifier: Modifier = Modifier,
    modsList: List<RemoteMod>?,
    selectedMods: List<RemoteMod>,
    removeFromSelected: (RemoteMod) -> Unit,
    addToSelected: (RemoteMod) -> Unit,
    onLoad: (RemoteMod) -> Unit,
    onForceRefresh: (RemoteMod) -> Unit,
    onEnable: (RemoteMod) -> Unit,
    onDisable: (RemoteMod) -> Unit,
    onSwapMoreInfo: (id: String, Platform) -> Unit,
    onDelete: (RemoteMod) -> Unit,
    itemColor: Color,
    itemContentColor: Color,
) {
    modsList?.let { list ->
        //如果列表是空的，则是由搜索导致的
        if (list.isNotEmpty()) {
            LazyColumn(
                modifier = modifier,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                items(list) { mod ->
                    ModItemLayout(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        mod = mod,
                        onLoad = {
                            onLoad(mod)
                        },
                        onForceRefresh = {
                            onForceRefresh(mod)
                        },
                        onClick = {
                            if (mod.isLoaded) {
                                //仅加载了项目信息的模组允许被选择
                                if (selectedMods.contains(mod)) {
                                    removeFromSelected(mod)
                                } else {
                                    addToSelected(mod)
                                }
                            }
                        },
                        onEnable = {
                            onEnable(mod)
                        },
                        onDisable = {
                            onDisable(mod)
                        },
                        onSwapMoreInfo = onSwapMoreInfo,
                        onDelete = {
                            onDelete(mod)
                        },
                        selected = selectedMods.contains(mod),
                        itemColor = itemColor,
                        itemContentColor = itemContentColor
                    )
                }
            }
        }
    } ?: run {
        //如果为null，则代表本身就没有模组可以展示
        Box(modifier = Modifier.fillMaxSize()) {
            ScalingLabel(
                modifier = Modifier.align(Alignment.Center),
                text = stringResource(R.string.mods_manage_no_mods)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModItemLayout(
    modifier: Modifier = Modifier,
    mod: RemoteMod,
    onLoad: () -> Unit = {},
    onForceRefresh: () -> Unit = {},
    onClick: () -> Unit = {},
    onEnable: () -> Unit,
    onDisable: () -> Unit,
    onSwapMoreInfo: (id: String, Platform) -> Unit,
    onDelete: () -> Unit,
    selected: Boolean,
    itemColor: Color,
    itemContentColor: Color,
    borderColor: Color = MaterialTheme.colorScheme.primary,
    shape: Shape = MaterialTheme.shapes.large,
    shadowElevation: Dp = 1.dp
) {
    val borderWidth by animateDpAsState(
        if (selected) 2.dp
        else (-1).dp
    )

    val scale = remember { Animatable(initialValue = 0.95f) }
    LaunchedEffect(Unit) {
        scale.animateTo(targetValue = 1f, animationSpec = getAnimateTween())
    }
    val context = LocalContext.current

    val projectInfo = mod.projectInfo

    LaunchedEffect(mod) {
        //尝试加载该模组文件在平台上所属的项目
        onLoad()
    }

    Surface(
        modifier = modifier
            .graphicsLayer(scaleY = scale.value, scaleX = scale.value)
            .border(
                width = borderWidth,
                color = borderColor,
                shape = shape
            ),
        onClick = onClick,
        shape = shape,
        color = itemColor,
        contentColor = itemContentColor,
        shadowElevation = shadowElevation
    ) {
        Row(
            modifier = Modifier.padding(all = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            //模组的封面图标
            ModIcon(
                modifier = Modifier.clip(shape = RoundedCornerShape(10.dp)),
                mod = mod,
                iconSize = 48.dp
            )

            //模组简要信息
            Crossfade(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(1f),
                //在本地是否为未知文件
                targetState = mod.localMod.notMod && projectInfo == null,
                label = "ModItemInfoCrossfade"
            ) { isUnknown ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val localMod = mod.localMod
                    when {
                        isUnknown -> {
                            //非模组，只展示文件名称
                            Text(
                                modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                                text = localMod.file.name,
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 1
                            )
                            if (localMod.loader != ModLoader.UNKNOWN) {
                                LittleTextLabel(
                                    text = localMod.loader.displayName,
                                    shape = MaterialTheme.shapes.small
                                )
                            }
                        }
                        else -> {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val displayTitle = if (projectInfo != null) {
                                    val title = projectInfo.title
                                    mod.mcMod?.getMcmodTitle(title, context) ?: title
                                } else {
                                    localMod.name
                                }
                                Text(
                                    modifier = Modifier
                                        .weight(1f, fill = false)
                                        .basicMarquee(iterations = Int.MAX_VALUE)
                                        .animateContentSize(),
                                    text = displayTitle,
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1
                                )
                                Row(
                                    modifier = Modifier
                                        .basicMarquee(iterations = Int.MAX_VALUE)
                                        .animateContentSize(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    val remoteLoaders = mod.remoteFile?.loaders
                                    if (remoteLoaders != null && remoteLoaders.isNotEmpty()) {
                                        remoteLoaders.forEach { loader ->
                                            LittleTextLabel(
                                                text = loader.getDisplayName(),
                                                shape = MaterialTheme.shapes.small
                                            )
                                        }
                                    } else if (localMod.loader != ModLoader.UNKNOWN) {
                                        LittleTextLabel(
                                            text = localMod.loader.displayName,
                                            shape = MaterialTheme.shapes.small
                                        )
                                    }
                                }
                            }

                            Text(
                                modifier = Modifier
                                    .alpha(0.7f)
                                    .basicMarquee(iterations = Int.MAX_VALUE),
                                text = stringResource(R.string.generic_file_name, localMod.file.name),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.align(Alignment.CenterVertically),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (mod.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(18.dp)
                            .alpha(0.7f),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                } else if (mod.isLoaded) {
                    IconButton(
                        modifier = Modifier.size(38.dp),
                        onClick = onForceRefresh
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = stringResource(R.string.generic_refresh)
                        )
                    }
                }

                //启用/禁用
                Checkbox(
                    checked = mod.localMod.file.isEnabled(),
                    onCheckedChange = { checked ->
                        if (checked) onEnable()
                        else onDisable()
                    }
                )

                //详细信息展示
                if (projectInfo == null) {
                    if (!mod.localMod.notMod) {
                        LocalModInfoTooltip(mod.localMod)
                    }
                } else {
                    IconButton(
                        modifier = Modifier.size(38.dp),
                        onClick = {
                            onSwapMoreInfo(projectInfo.id, projectInfo.platform)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = stringResource(R.string.mods_manage_info)
                        )
                    }
                }

                IconButton(
                    modifier = Modifier.size(38.dp),
                    onClick = onDelete
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.generic_delete)
                    )
                }
            }
        }
    }
}

@Composable
private fun ModIcon(
    modifier: Modifier = Modifier,
    mod: RemoteMod,
    iconSize: Dp,
    disableContainerSize: Dp = 28.dp
) {
    Box(modifier = modifier) {
        val colorMatrix = remember(mod, mod.localMod.file) { ColorMatrix() }
        colorMatrix.setToSaturation(
            if (mod.localMod.file.isDisabled()) 0f
            else 1f
        )

        val projectInfo = mod.projectInfo
        if (projectInfo == null) {
            ByteArrayIcon(
                modifier = Modifier.size(iconSize),
                triggerRefresh = mod,
                icon = mod.localMod.icon,
                colorFilter = ColorFilter.colorMatrix(colorMatrix)
            )
        } else {
            AssetsIcon(
                iconUrl = projectInfo.iconUrl,
                size = iconSize,
                colorFilter = ColorFilter.colorMatrix(colorMatrix)
            )
        }

        AnimatedVisibility(
            modifier = Modifier.align(Alignment.Center),
            visible = mod.localMod.file.isDisabled(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .padding(all = 4.dp)
                    .size(disableContainerSize),
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                shape = CircleShape,
                shadowElevation = 4.dp
            ) {
                Icon(
                    imageVector = Icons.Outlined.Block,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun LocalModInfoTooltip(
    mod: LocalMod
) {
    TooltipIconButton(
        modifier = Modifier.size(38.dp),
        tooltip = {
            RichTooltip(
                modifier = Modifier.padding(all = 3.dp),
                title = { Text(text = stringResource(R.string.mods_manage_info)) },
                shadowElevation = 3.dp
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                ) {
                    //文件大小
                    Text(text = stringResource(R.string.generic_file_size, formatFileSize(mod.fileSize)))
                    //模组版本
                    mod.version?.let { version ->
                        Text(text = stringResource(R.string.mods_manage_version, version))
                    }
                    //作者
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = stringResource(R.string.mods_manage_authors))
                        FlowRow(
                            modifier = Modifier.weight(1f, fill = false),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            mod.authors.forEach { author ->
                                Text(text = author)
                            }
                        }
                    }
                    //模组描述
                    mod.description?.takeIf { it.isNotEmptyOrBlank() }?.let { description ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = stringResource(R.string.mods_manage_description))
                            Text(
                                modifier = Modifier.weight(1f, fill = false),
                                text = description
                            )
                        }
                    }
                }
            }
        }
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = stringResource(R.string.mods_manage_info)
        )
    }
}

