/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.ui.screens.content.download.game

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.versioninfo.MinecraftVersion
import com.movtery.zalithlauncher.game.versioninfo.MinecraftVersions
import com.movtery.zalithlauncher.game.versioninfo.models.isType
import com.movtery.zalithlauncher.game.versioninfo.models.mapVersion
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.CheckChip
import com.movtery.zalithlauncher.ui.components.EdgeDirection
import com.movtery.zalithlauncher.ui.components.LittleTextLabel
import com.movtery.zalithlauncher.ui.components.ScalingLabel
import com.movtery.zalithlauncher.ui.components.SimpleTextInputField
import com.movtery.zalithlauncher.ui.components.backgroundLayoutColor
import com.movtery.zalithlauncher.ui.components.fadeEdge
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState
import com.movtery.zalithlauncher.utils.classes.Quadruple
import com.movtery.zalithlauncher.utils.formatDate
import com.movtery.zalithlauncher.utils.logging.Logger.lError
import com.movtery.zalithlauncher.utils.logging.Logger.lWarning
import com.movtery.zalithlauncher.utils.string.isEmptyOrBlank
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException

/** 版本列表加载状态 */
private sealed interface VersionState {
    /** 加载中 */
    data object Loading : VersionState
    /** 加载完成 */
    data class None(val versions: List<MinecraftVersion>) : VersionState
    /** 加载出现异常 */
    data class Failure(val message: Int, val args: Array<Any>? = null) : VersionState {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Failure

            if (message != other.message) return false
            if (args != null) {
                if (other.args == null) return false
                if (!args.contentEquals(other.args)) return false
            } else if (other.args != null) return false

            return true
        }

        override fun hashCode(): Int {
            var result = message
            result = 31 * result + (args?.contentHashCode() ?: 0)
            return result
        }
    }
}

/**
 * 版本过滤条件
 * @param release 是否保留正式版本
 * @param snapshot 是否保留快照版本
 * @param old 是否保留旧版本
 * @param id 搜索并过滤版本ID
 */
private data class VersionFilter(
    val release: Boolean = true,
    val snapshot: Boolean = false,
    val aprilFools: Boolean = false,
    val old: Boolean = false,
    val id: String = ""
)

private class VersionsViewModel(): ViewModel() {
    var versionState by mutableStateOf<VersionState>(VersionState.Loading)
        private set

    //简易版本类型过滤器
    var versionFilter by mutableStateOf(VersionFilter())
        private set

    private var allVersions by mutableStateOf<List<MinecraftVersion>>(emptyList())

    fun filterWith(filter: VersionFilter) {
        versionFilter = filter
        viewModelScope.launch {
            versionState = VersionState.None(allVersions.filterVersions(versionFilter))
        }
    }

    fun refresh(forceReload: Boolean = false) {
        viewModelScope.launch {
            versionState = VersionState.Loading
            versionState = runCatching {
                allVersions = MinecraftVersions.getVersionManifest(forceReload).versions.mapVersion()
                VersionState.None(allVersions.filterVersions(versionFilter))
            }.getOrElse { e ->
                lWarning("Failed to get version manifest!", e)
                val message: Pair<Int, Array<Any>?> = when(e) {
                    is HttpRequestTimeoutException -> R.string.error_timeout to null
                    is UnknownHostException, is UnresolvedAddressException -> R.string.error_network_unreachable to null
                    is ConnectException -> R.string.error_connection_failed to null
                    is ResponseException -> {
                        val statusCode = e.response.status
                        val res = when (statusCode) {
                            HttpStatusCode.Unauthorized -> R.string.error_unauthorized
                            HttpStatusCode.NotFound -> R.string.error_notfound
                            else -> R.string.error_client_error
                        }
                        res to arrayOf(statusCode)
                    }
                    else -> {
                        lError("An unknown exception was caught!", e)
                        val errorMessage = e.localizedMessage ?: e.message ?: e::class.qualifiedName ?: "Unknown error"
                        R.string.error_unknown to arrayOf(errorMessage)
                    }
                }
                VersionState.Failure(message.first, message.second)
            }
        }
    }

    init {
        //初始化后，刷新版本列表
        refresh()
    }

    override fun onCleared() {
        viewModelScope.cancel()
    }
}

@Composable
fun SelectGameVersionScreen(
    mainScreenKey: NavKey?,
    downloadScreenKey: NavKey?,
    downloadGameScreenKey: NavKey?,
    eventViewModel: EventViewModel,
    onVersionSelect: (String) -> Unit = {}
) {
    val viewModel = viewModel(
        key = NormalNavKey.DownloadGame.SelectGameVersion.toString()
    ) {
        VersionsViewModel()
    }

    BaseScreen(
        levels1 = listOf(
            Pair(NestedNavKey.Download::class.java, mainScreenKey),
            Pair(NestedNavKey.DownloadGame::class.java, downloadScreenKey)
        ),
        Triple(NormalNavKey.DownloadGame.SelectGameVersion, downloadGameScreenKey, false)
    ) { isVisible ->
        val yOffset by swapAnimateDpAsState(
            targetValue = (-40).dp,
            swapIn = isVisible
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
        ) {
            when (val state = viewModel.versionState) {
                is VersionState.Loading -> {
                    Box(Modifier.fillMaxSize()) {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }
                }

                is VersionState.Failure -> {
                    Box(Modifier.fillMaxSize()) {
                        val message = if (state.args != null) {
                            stringResource(state.message, *state.args)
                        } else {
                            stringResource(state.message)
                        }

                        ScalingLabel(
                            modifier = Modifier.align(Alignment.Center),
                            text = stringResource(R.string.download_game_failed_to_get_versions, message),
                            onClick = {
                                viewModel.refresh(true)
                            }
                        )
                    }
                }

                is VersionState.None -> {
                    Column {
                        VersionHeader(
                            modifier = Modifier.fillMaxWidth(),
                            versionFilter = viewModel.versionFilter,
                            onVersionFilterChange = { viewModel.filterWith(it) },
                            itemContainerColor = backgroundLayoutColor(),
                            itemContentColor = MaterialTheme.colorScheme.onSurface,
                            onRefreshClick = {
                                viewModel.refresh(true)
                            }
                        )

                        VersionList(
                            modifier = Modifier.weight(1f),
                            itemContainerColor = backgroundLayoutColor(),
                            itemContentColor = MaterialTheme.colorScheme.onSurface,
                            versions = state.versions,
                            onVersionSelect = onVersionSelect,
                            openLink = { url ->
                                eventViewModel.sendEvent(EventViewModel.Event.OpenLink(url))
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 简易过滤器，过滤特定类型的版本
 */
private fun List<MinecraftVersion>.filterVersions(
    versionFilter: VersionFilter
) = this.filter { version ->
    val type = version.isType(
        release = versionFilter.release,
        snapshot = versionFilter.snapshot,
        aprilFools = versionFilter.aprilFools,
        old = versionFilter.old
    )
    val versionId = versionFilter.id
    val id = (versionId.isEmptyOrBlank()) || version.version.id.contains(versionId)
    (type && id)
}

@Composable
private fun VersionHeader(
    modifier: Modifier = Modifier,
    versionFilter: VersionFilter,
    onVersionFilterChange: (VersionFilter) -> Unit,
    itemContainerColor: Color,
    itemContentColor: Color,
    onRefreshClick: () -> Unit = {}
) {
    Column(
        modifier = modifier.padding(horizontal = 12.dp)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fadeEdge(
                            state = scrollState,
                            direction = EdgeDirection.Horizontal
                        )
                        .widthIn(max = this@BoxWithConstraints.maxWidth / 5 * 3) //3/5
                        .horizontalScroll(scrollState),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    //版本筛选条件
                    VersionTypeItem(
                        selected = versionFilter.release,
                        onClick = {
                            onVersionFilterChange(versionFilter.copy(release = versionFilter.release.not()))
                        },
                        text = stringResource(R.string.download_game_type_release)
                    )
                    VersionTypeItem(
                        selected = versionFilter.snapshot,
                        onClick = {
                            onVersionFilterChange(versionFilter.copy(snapshot = versionFilter.snapshot.not()))
                        },
                        text = stringResource(R.string.download_game_type_snapshot)
                    )
                    VersionTypeItem(
                        selected = versionFilter.aprilFools,
                        onClick = {
                            onVersionFilterChange(versionFilter.copy(aprilFools = versionFilter.aprilFools.not()))
                        },
                        text = stringResource(R.string.download_game_type_april_fools)
                    )
                    VersionTypeItem(
                        selected = versionFilter.old,
                        onClick = {
                            onVersionFilterChange(versionFilter.copy(old = versionFilter.old.not()))
                        },
                        text = stringResource(R.string.download_game_type_old)
                    )
                }

                //搜索、刷新
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SimpleTextInputField(
                        modifier = Modifier.weight(1f),
                        value = versionFilter.id,
                        onValueChange = { onVersionFilterChange(versionFilter.copy(id = it)) },
                        color = itemContainerColor,
                        contentColor = itemContentColor,
                        singleLine = true,
                        hint = {
                            Text(
                                text = stringResource(R.string.generic_search),
                                style = TextStyle(color = itemContentColor).copy(fontSize = 12.sp)
                            )
                        }
                    )

                    IconButton(
                        onClick = onRefreshClick
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
private fun VersionTypeItem(
    selected: Boolean,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CheckChip(
        modifier = modifier,
        selected = selected,
        onClick = onClick,
        label = {
            Text(text)
        },
    )
}

@Composable
private fun VersionList(
    modifier: Modifier = Modifier,
    itemContainerColor: Color,
    itemContentColor: Color,
    versions: List<MinecraftVersion>,
    onVersionSelect: (String) -> Unit,
    openLink: (url: String) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
    ) {
        items(versions) { version ->
            VersionItemLayout(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                version = version,
                onClick = {
                    onVersionSelect(version.version.id)
                },
                onAccessWiki = { wikiUrl ->
                    openLink(wikiUrl)
                },
                color = itemContainerColor,
                contentColor = itemContentColor
            )
        }
    }
}

@Composable
private fun VersionItemLayout(
    modifier: Modifier = Modifier,
    version: MinecraftVersion,
    onClick: () -> Unit = {},
    onAccessWiki: (String) -> Unit = {},
    color: Color,
    contentColor: Color,
) {
    val scale = remember { Animatable(initialValue = 0.95f) }
    LaunchedEffect(Unit) {
        scale.animateTo(targetValue = 1f, animationSpec = getAnimateTween())
    }

    val (icon, versionType, wikiUrl, summary) = getVersionComponents(version)

    Surface(
        modifier = modifier.graphicsLayer(scaleY = scale.value, scaleX = scale.value),
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = color,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier
                .clip(shape = MaterialTheme.shapes.large)
                .padding(all = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let { versionIcon ->
                Image(
                    modifier = Modifier.size(32.dp),
                    painter = versionIcon,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = version.version.id,
                        style = MaterialTheme.typography.labelLarge
                    )

                    LittleTextLabel(
                        text = versionType
                    )
                }

                summary?.let { text ->
                    Text(
                        modifier = Modifier.alpha(0.7f),
                        text = text,
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                Text(
                    modifier = Modifier.alpha(0.7f),
                    text = formatDate(
                        input = version.version.releaseTime,
                        pattern = stringResource(R.string.date_format)
                    ),
                    style = MaterialTheme.typography.labelMedium
                )
            }

            wikiUrl?.let { url ->
                IconButton(
                    modifier = Modifier.size(32.dp),
                    onClick = { onAccessWiki(url) }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Link,
                        contentDescription = "Wiki"
                    )
                }
            }
        }
    }
}

@Composable
private fun getVersionComponents(
    version: MinecraftVersion
): Quadruple<Painter?, String, String?, String?> {
    val vmVer = version.version
    val summary = version.summary?.let { stringResource(it) }

    return when (version.type) {
        MinecraftVersion.Type.Release -> {
            Quadruple(
                painterResource(R.drawable.img_minecraft),
                stringResource(R.string.download_game_type_release),
                stringResource(R.string.url_wiki_minecraft_game_release, vmVer.id),
                summary
            )
        }
        MinecraftVersion.Type.Snapshot -> {
            Quadruple(
                painterResource(R.drawable.img_command_block),
                stringResource(R.string.download_game_type_snapshot),
                stringResource(R.string.url_wiki_minecraft_game_snapshot, vmVer.id),
                summary
            )
        }
        MinecraftVersion.Type.AprilFools -> {
            Quadruple(
                painterResource(R.drawable.img_diamond_block),
                stringResource(R.string.download_game_type_april_fools),
                stringResource(R.string.url_wiki_minecraft_game_snapshot, vmVer.id),
                summary
            )
        }
        MinecraftVersion.Type.OldBeta -> {
            Quadruple(
                painterResource(R.drawable.img_old_cobblestone),
                stringResource(R.string.download_game_type_old_beta),
                null,
                summary
            )
        }
        MinecraftVersion.Type.OldAlpha -> {
            Quadruple(
                painterResource(R.drawable.img_old_grass_block),
                stringResource(R.string.download_game_type_old_alpha),
                null,
                summary
            )
        }
        else -> {
            Quadruple(
                null,
                stringResource(R.string.generic_unknown),
                null,
                version.summary?.let { stringResource(it) }
            )
        }
    }
}