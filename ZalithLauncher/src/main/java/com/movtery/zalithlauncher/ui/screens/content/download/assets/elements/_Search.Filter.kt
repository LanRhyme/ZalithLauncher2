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

package com.movtery.zalithlauncher.ui.screens.content.download.assets.elements

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformDisplayLabel
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformFilterCode
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformSortField
import com.movtery.zalithlauncher.game.versioninfo.MinecraftVersions
import com.movtery.zalithlauncher.game.versioninfo.allGameVersions
import com.movtery.zalithlauncher.ui.components.LittleTextLabel
import com.movtery.zalithlauncher.ui.components.backgroundLayoutColor
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.utils.logging.Logger.lWarning

/**
 * 搜索资源过滤器UI
 * @param enablePlatform 是否允许更改目标平台
 * @param searchPlatform 目标平台
 * @param searchName 搜索名称
 * @param gameVersion 游戏版本
 * @param sortField 排序方式
 * @param allCategories 可用资源类别列表
 * @param categories 已选择的资源类别
 * @param enableModLoader 是否启用模组加载器过滤
 * @param modloaders 可用模组加载器列表
 * @param modloader 模组加载器
 */
@Composable
fun SearchFilter(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    enablePlatform: Boolean = true,
    searchPlatform: Platform,
    onPlatformChange: (Platform) -> Unit = {},
    searchName: String,
    onSearchNameChange: (String) -> Unit = {},
    gameVersion: String?,
    onGameVersionChange: (String?) -> Unit = {},
    sortField: PlatformSortField,
    onSortFieldChange: (PlatformSortField) -> Unit = {},
    allCategories: List<PlatformFilterCode>,
    categories: List<PlatformFilterCode>,
    onCategoryChanged: (List<PlatformFilterCode>) -> Unit = {},
    enableModLoader: Boolean = true,
    modloaders: List<PlatformDisplayLabel> = emptyList(),
    modloader: PlatformDisplayLabel? = null,
    onModLoaderChange: (PlatformDisplayLabel?) -> Unit = {}
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = searchName,
                onValueChange = onSearchNameChange,
                shape = MaterialTheme.shapes.large,
                label = {
                    Text(text = stringResource(R.string.download_assets_filter_search_name))
                },
                singleLine = true
            )
        }

        if (enablePlatform) {
            item {
                FilterListLayout(
                    modifier = Modifier.fillMaxWidth(),
                    items = Platform.entries,
                    selectionMode = FilterSelectionMode.Single,
                    selectedItems = listOfNotNull(searchPlatform),
                    onSelectionChange = { new ->
                        new.first().takeIf { it != searchPlatform }?.let { value ->
                            onPlatformChange(value)
                        }
                    },
                    getItemLabel = { item ->
                        item.displayName
                    },
                    selectedLabel = { item ->
                        PlatformIdentifier(
                            platform = item,
                            shape = MaterialTheme.shapes.small
                        )
                    },
                    itemLayout = { platform ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                modifier = Modifier.size(14.dp),
                                painter = painterResource(platform.getDrawable()),
                                contentDescription = platform.displayName
                            )
                            Text(
                                text = platform.displayName,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    },
                    title = stringResource(R.string.download_assets_filter_search_platform),
                    cancelable = false
                )
            }
        }

        item {
            val versions by MinecraftVersions.releasesFlow.collectAsState()
            //刷新真实的版本列表
            LaunchedEffect(Unit) {
                runCatching {
                    MinecraftVersions.refreshReleaseVersions(force = false)
                }.onFailure {
                    lWarning("Failed to refresh Minecraft versions")
                }
            }

            FilterListLayout(
                modifier = Modifier.fillMaxWidth(),
                items = versions ?: allGameVersions,
                selectionMode = FilterSelectionMode.Single,
                selectedItems = listOfNotNull(gameVersion),
                onSelectionChange = { new ->
                    val value = new.firstOrNull()
                    if (value != gameVersion) onGameVersionChange(value)
                },
                getItemLabel = { it },
                title = stringResource(R.string.download_assets_filter_game_version)
            )
        }

        item {
            FilterListLayout(
                modifier = Modifier.fillMaxWidth(),
                items = PlatformSortField.entries,
                selectionMode = FilterSelectionMode.Single,
                selectedItems = listOfNotNull(sortField),
                onSelectionChange = { new ->
                    new.first().takeIf { it != sortField }?.let { value ->
                        onSortFieldChange(value)
                    }
                },
                getItemLabel = { item ->
                    stringResource(item.getDisplayName())
                },
                title = stringResource(R.string.download_assets_filter_sort_field),
                cancelable = false
            )
        }

        item {
            FilterListLayout(
                modifier = Modifier.fillMaxWidth(),
                items = allCategories,
                selectionMode = FilterSelectionMode.Multiple,
                selectedItems = categories,
                onSelectionChange = { news ->
                    news.takeIf { it.toSet() != categories.toSet() }?.let { value ->
                        onCategoryChanged(value)
                    }
                },
                getItemLabel = { item ->
                    stringResource(item.getDisplayName())
                },
                title = stringResource(R.string.download_assets_filter_category)
            )
        }

        if (enableModLoader) {
            item {
                FilterListLayout(
                    modifier = Modifier.fillMaxWidth(),
                    items = modloaders,
                    selectionMode = FilterSelectionMode.Single,
                    selectedItems = listOfNotNull(modloader),
                    onSelectionChange = { new ->
                        val value = new.firstOrNull()
                        if (value != modloader) onModLoaderChange(value)
                    },
                    getItemLabel = { item ->
                        item.getDisplayName()
                    },
                    title = stringResource(R.string.download_assets_filter_modloader)
                )
            }
        }
    }
}



enum class FilterSelectionMode {
    /**
     * 一次性只能选择一个项
     */
    Single,

    /**
     * 支持选择更多项
     */
    Multiple
}

@Composable
private fun <E> FilterListLayout(
    title: String,
    items: List<E>,
    selectionMode: FilterSelectionMode,
    selectedItems: List<E>,
    onSelectionChange: (List<E>) -> Unit,
    getItemLabel: @Composable (E) -> String,
    modifier: Modifier = Modifier,
    selectedLabel: @Composable FlowRowScope.(E) -> Unit = { item ->
        LittleTextLabel(
            text = getItemLabel(item),
            shape = MaterialTheme.shapes.small
        )
    },
    itemLayout: @Composable (E) -> Unit = { item ->
        Text(
            text = getItemLabel(item),
            style = MaterialTheme.typography.labelMedium
        )
    },
    cancelable: Boolean = true,
    maxListHeight: Dp = 200.dp,
    shape: Shape = MaterialTheme.shapes.large,
    color: Color = backgroundLayoutColor(),
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    var expanded by remember { mutableStateOf(false) }

    val selected = selectedItems.isNotEmpty()
    val isSingle = selectionMode == FilterSelectionMode.Single

    Surface(
        modifier = modifier,
        shape = shape,
        color = color,
        contentColor = contentColor
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            FilterHeader(
                title = title,
                expanded = expanded,
                selected = selected,
                selectedLabels = {
                    if (selectedItems.isEmpty()) {
                        LittleTextLabel(
                            text = stringResource(R.string.download_assets_filter_none),
                            shape = MaterialTheme.shapes.small
                        )
                    } else {
                        selectedItems.fastForEach { item ->
                            selectedLabel(item)
                        }
                    }
                },
                cancelable = cancelable,
                onExpandToggle = { expanded = !expanded },
                onClear = { onSelectionChange(emptyList()) }
            )

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(animationSpec = getAnimateTween()),
                exit = shrinkVertically(animationSpec = getAnimateTween()) + fadeOut()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = maxListHeight)
                        .padding(vertical = 4.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(items) { item ->
                        val isSelected = selectedItems.contains(item)
                        FilterListItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(all = 4.dp),
                            selected = isSelected,
                            selectionMode = selectionMode,
                            onCheckedChange = { checked ->
                                val newSelection = when {
                                    isSingle && checked -> listOf(item)
                                    !isSingle && checked -> selectedItems + item
                                    !isSingle && !checked -> selectedItems - item
                                    else -> emptyList()
                                }
                                onSelectionChange(newSelection)
                            },
                            itemLayout = {
                                itemLayout(item)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterHeader(
    title: String,
    expanded: Boolean,
    selected: Boolean,
    selectedLabels: @Composable FlowRowScope.() -> Unit,
    cancelable: Boolean,
    onExpandToggle: () -> Unit,
    onClear: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onExpandToggle)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            FlowRow(
                modifier = Modifier.animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                content = selectedLabels
            )
        }

        Row(
            modifier = Modifier.padding(end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val rotation by animateFloatAsState(
                targetValue = if (expanded) -180f else 0f,
                animationSpec = getAnimateTween()
            )
            Icon(
                modifier = Modifier
                    .size(28.dp)
                    .rotate(rotation),
                imageVector = Icons.Rounded.ArrowDropDown,
                contentDescription = null
            )
            AnimatedVisibility(
                visible = selected && cancelable
            ) {
                IconButton(
                    onClick = {
                        if (selected && cancelable) onClear()
                    }
                ) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        imageVector = Icons.Outlined.Clear,
                        contentDescription = stringResource(R.string.generic_clear)
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterListItem(
    modifier: Modifier = Modifier,
    selected: Boolean,
    selectionMode: FilterSelectionMode,
    itemLayout: @Composable () -> Unit,
    onCheckedChange: (Boolean) -> Unit
) {
    val onClick = {
        val newValue = if (selectionMode == FilterSelectionMode.Multiple) !selected else true
        onCheckedChange(newValue)
    }

    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (selectionMode) {
            FilterSelectionMode.Single -> RadioButton(selected = selected, onClick = onClick)
            FilterSelectionMode.Multiple -> Checkbox(checked = selected, onCheckedChange = onCheckedChange)
        }
        itemLayout()
    }
}
