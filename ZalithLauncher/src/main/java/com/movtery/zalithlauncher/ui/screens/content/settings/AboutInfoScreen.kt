package com.movtery.zalithlauncher.ui.screens.content.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Copyright
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.movtery.zalithlauncher.BuildConfig
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.plugin.ApkPlugin
import com.movtery.zalithlauncher.game.plugin.PluginLoader
import com.movtery.zalithlauncher.game.plugin.appCacheIcon
import com.movtery.zalithlauncher.info.InfoDistributor
import com.movtery.zalithlauncher.library.LibraryInfo
import com.movtery.zalithlauncher.library.libraryData
import com.movtery.zalithlauncher.path.URL_COMMUNITY
import com.movtery.zalithlauncher.path.URL_MCMOD
import com.movtery.zalithlauncher.path.URL_PROJECT
import com.movtery.zalithlauncher.path.URL_SUPPORT
import com.movtery.zalithlauncher.path.URL_WEBLATE
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.AnimatedLazyColumn
import com.movtery.zalithlauncher.ui.components.itemLayoutColor
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsBackground

@Composable
fun AboutInfoScreen(
    key: NestedNavKey.Settings,
    settingsScreenKey: NavKey?,
    mainScreenKey: NavKey?,
    openLicense: (raw: Int) -> Unit,
    openLink: (url: String) -> Unit
) {
    BaseScreen(
        Triple(key, mainScreenKey, false),
        Triple(NormalNavKey.Settings.AboutInfo, settingsScreenKey, false)
    ) { isVisible ->
        AnimatedLazyColumn(
            modifier = Modifier.fillMaxSize(),
            isVisible = isVisible,
            contentPadding = PaddingValues(all = 12.dp)
        ) { scope ->
            animatedItem(scope) { yOffset ->
                ChunkLayout(
                    modifier = Modifier.offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
                    title = stringResource(R.string.about_launcher_title)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ButtonIconItem(
                            icon = painterResource(R.drawable.img_launcher),
                            title = InfoDistributor.LAUNCHER_NAME,
                            text = stringResource(R.string.about_launcher_version, BuildConfig.VERSION_NAME),
                            buttonText = stringResource(R.string.about_launcher_project_link),
                            onButtonClick = { openLink(URL_PROJECT) }
                        )

                        ButtonIconItem(
                            icon = painterResource(R.drawable.img_movtery),
                            title = stringResource(R.string.about_launcher_author_movtery_title),
                            text = stringResource(R.string.about_launcher_author_movtery_text, InfoDistributor.LAUNCHER_NAME),
                            buttonText = stringResource(R.string.about_sponsor),
                            onButtonClick = { openLink(URL_SUPPORT) }
                        )
                    }
                }
            }

            animatedItem(scope) { yOffset ->
                ChunkLayout(
                    modifier = Modifier.offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
                    title = stringResource(R.string.about_acknowledgements_title)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ButtonIconItem(
                            icon = painterResource(R.drawable.img_bangbang93),
                            title = "bangbang93",
                            text = stringResource(R.string.about_acknowledgements_bangbang93_text, InfoDistributor.LAUNCHER_SHORT_NAME),
                            buttonText = stringResource(R.string.about_sponsor),
                            onButtonClick = { openLink("https://afdian.com/a/bangbang93") }
                        )
                        LinkIconItem(
                            icon = painterResource(R.drawable.img_launcher_fcl),
                            title = "Fold Craft Launcher",
                            text = stringResource(R.string.about_acknowledgements_fcl_text, InfoDistributor.LAUNCHER_SHORT_NAME),
                            openLicense = { openLicense(R.raw.fcl_license) },
                            openLink = { openLink("https://github.com/FCL-Team/FoldCraftLauncher") }
                        )
                        LinkIconItem(
                            icon = painterResource(R.drawable.img_launcher_hmcl),
                            title = "Hello Minecraft! Launcher",
                            text = stringResource(R.string.about_acknowledgements_hmcl_text, InfoDistributor.LAUNCHER_SHORT_NAME),
                            openLicense = { openLicense(R.raw.hmcl_license) },
                            openLink = { openLink("https://github.com/HMCL-dev/HMCL") }
                        )
                        LinkIconItem(
                            icon = painterResource(R.drawable.img_platform_mcmod),
                            title = stringResource(R.string.about_acknowledgements_mcmod),
                            text = stringResource(R.string.about_acknowledgements_mcmod_text, InfoDistributor.LAUNCHER_SHORT_NAME),
                            openLink = { openLink(URL_MCMOD) }
                        )
                        LinkIconItem(
                            icon = painterResource(R.drawable.img_launcher_pcl2),
                            title = "Plain Craft Launcher 2",
                            text = stringResource(R.string.about_acknowledgements_pcl_text, InfoDistributor.LAUNCHER_SHORT_NAME),
                            openLink = { openLink("https://github.com/Meloong-Git/PCL") }
                        )
                        LinkIconItem(
                            icon = painterResource(R.drawable.img_launcher_pojav),
                            title = "PojavLauncher",
                            text = stringResource(R.string.about_acknowledgements_pojav_text, InfoDistributor.LAUNCHER_SHORT_NAME),
                            openLicense = { openLicense(R.raw.lgpl_3_license) },
                            openLink = { openLink("https://github.com/PojavLauncherTeam/PojavLauncher") }
                        )
                        LinkIconItem(
                            icon = painterResource(R.drawable.ic_github),
                            title = stringResource(R.string.about_acknowledgements_github_community),
                            text = stringResource(R.string.about_acknowledgements_github_community_text),
                            openLink = { openLink(URL_COMMUNITY) },
                            useImage = false
                        )
                        LinkIconItem(
                            icon = painterResource(R.drawable.img_weblate),
                            title = stringResource(R.string.about_acknowledgements_weblate_community),
                            text = stringResource(R.string.about_acknowledgements_weblate_community_text),
                            openLink = { openLink(URL_WEBLATE) }
                        )
                    }
                }
            }

            //额外依赖库板块
            animatedItem(scope) { yOffset ->
                ChunkLayout(
                    modifier = Modifier.offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
                    title = stringResource(R.string.about_library_title)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        libraryData.forEach { info ->
                            LibraryInfoItem(info = info, openLicense = openLicense, openLink = openLink)
                        }
                    }
                }
            }

            //已加载插件板块
            PluginLoader.allPlugins.takeIf { it.isNotEmpty() }?.let { allPlugins ->
                animatedItem(scope) { yOffset ->
                    ChunkLayout(
                        modifier = Modifier.offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
                        title = stringResource(R.string.about_plugin_title)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            allPlugins.forEach { apkPlugin ->
                                PluginInfoItem(apkPlugin = apkPlugin)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChunkLayout(
    modifier: Modifier = Modifier,
    title: String,
    content: @Composable () -> Unit
) {
    SettingsBackground(
        modifier = modifier,
        contentPadding = 0.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(width = 8.dp))
            HorizontalDivider(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurface
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 12.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun LinkIconItem(
    modifier: Modifier = Modifier,
    icon: Painter,
    title: String,
    text: String,
    openLicense: (() -> Unit)? = null,
    openLink: (() -> Unit)? = null,
    color: Color = itemLayoutColor(),
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    useImage: Boolean = true
) {
    Surface(
        modifier = modifier,
        color = color,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.large,
        shadowElevation = 1.dp,
        onClick = {}
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val iconModifier = Modifier
                .size(34.dp)
                .clip(shape = RoundedCornerShape(6.dp))
            if (useImage) {
                Image(
                    modifier = iconModifier,
                    painter = icon,
                    contentDescription = null,
                    contentScale = ContentScale.Fit
                )
            } else {
                Icon(
                    modifier = iconModifier,
                    painter = icon,
                    contentDescription = null
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    modifier = Modifier.alpha(0.7f),
                    text = text,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row {
                openLicense?.let {
                    IconButton(
                        onClick = it
                    ) {
                        Icon(
                            modifier = Modifier.size(22.dp),
                            imageVector = Icons.Outlined.Copyright,
                            contentDescription = "License"
                        )
                    }
                }
                openLink?.let {
                    IconButton(
                        onClick = it
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Link,
                            contentDescription = stringResource(R.string.generic_open_link)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ButtonIconItem(
    modifier: Modifier = Modifier,
    icon: Painter,
    title: String,
    text: String,
    buttonText: String,
    onButtonClick: () -> Unit,
    color: Color = itemLayoutColor(),
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Surface(
        modifier = modifier,
        color = color,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.large,
        shadowElevation = 1.dp,
        onClick = {}
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                modifier = Modifier
                    .size(34.dp)
                    .clip(shape = RoundedCornerShape(6.dp)),
                painter = icon,
                contentDescription = null,
                contentScale = ContentScale.Fit
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    modifier = Modifier.alpha(0.7f),
                    text = text,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            OutlinedButton(
                onClick = onButtonClick
            ) {
                Text(text = buttonText)
            }
        }
    }
}

@Composable
private fun PluginInfoItem(
    apkPlugin: ApkPlugin,
    modifier: Modifier = Modifier,
    color: Color = itemLayoutColor(),
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Surface(
        modifier = modifier,
        color = color,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.large,
        shadowElevation = 1.dp,
        onClick = {}
    ) {
        val context = LocalContext.current
        Row(
            modifier = Modifier
                .padding(all = 12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val iconFile = appCacheIcon(apkPlugin.packageName)
            if (iconFile.exists()) {
                val model = remember(context, iconFile) {
                    ImageRequest.Builder(context)
                        .data(iconFile)
                        .build()
                }
                AsyncImage(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(shape = RoundedCornerShape(8.dp)),
                    model = model,
                    contentDescription = null,
                    contentScale = ContentScale.Fit
                )
            } else {
                Image(
                    modifier = Modifier.size(34.dp),
                    painter = painterResource(R.drawable.ic_unknown_icon),
                    contentDescription = null,
                    contentScale = ContentScale.Fit
                )
            }

            Column(
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Text(
                    text = apkPlugin.appName,
                    style = MaterialTheme.typography.titleSmall
                )
                Row(
                    modifier = Modifier.alpha(0.7f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = apkPlugin.packageName,
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (apkPlugin.appVersion.isNotEmpty()) {
                        Text(
                            text = apkPlugin.appVersion,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryInfoItem(
    info: LibraryInfo,
    modifier: Modifier = Modifier,
    color: Color = itemLayoutColor(),
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    openLicense: (Int) -> Unit,
    openLink: (url: String) -> Unit
) {
    Surface(
        modifier = modifier,
        color = color,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.large,
        shadowElevation = 1.dp,
        onClick = {}
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = info.name,
                    style = MaterialTheme.typography.titleSmall
                )
                Column(
                    modifier = Modifier.alpha(0.7f)
                ) {
                    info.copyrightInfo?.let { copyrightInfo ->
                        Text(
                            text = copyrightInfo,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(
                        modifier = Modifier.clickable(
                            onClick = {
                                openLicense(info.license.raw)
                            }
                        ),
                        text = "Licensed under the ${info.license.name}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            textDecoration = TextDecoration.Underline
                        )
                    )
                }
            }
            IconButton(
                modifier = Modifier.align(Alignment.CenterVertically),
                onClick = {
                    openLink(info.webUrl)
                }
            ) {
                Icon(
                    imageVector = Icons.Outlined.Link,
                    contentDescription = null
                )
            }
        }
    }
}