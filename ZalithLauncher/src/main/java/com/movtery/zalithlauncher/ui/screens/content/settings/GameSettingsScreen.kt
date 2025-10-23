package com.movtery.zalithlauncher.ui.screens.content.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.multirt.RuntimesManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.AnimatedColumn
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.content.elements.MemoryPreview
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsBackground
import com.movtery.zalithlauncher.utils.platform.getMaxMemoryForSettings

@Composable
fun GameSettingsScreen(
    key: NestedNavKey.Settings,
    settingsScreenKey: NavKey?,
    mainScreenKey: NavKey?
) {
    BaseScreen(
        Triple(key, mainScreenKey, false),
        Triple(NormalNavKey.Settings.Game, settingsScreenKey, false)
    ) { isVisible ->
        AnimatedColumn(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(state = rememberScrollState())
                .padding(all = 12.dp),
            isVisible = isVisible
        ) { scope ->
            AnimatedItem(scope) { yOffset ->
                SettingsBackground(
                    modifier = Modifier.offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
                ) {
                    SwitchSettingsLayout(
                        modifier = Modifier.fillMaxWidth(),
                        unit = AllSettings.versionIsolation,
                        title = stringResource(R.string.settings_game_version_isolation_title),
                        summary = stringResource(R.string.settings_game_version_isolation_summary)
                    )

                    SwitchSettingsLayout(
                        modifier = Modifier.fillMaxWidth(),
                        unit = AllSettings.skipGameIntegrityCheck,
                        title = stringResource(R.string.settings_game_skip_game_integrity_check_title),
                        summary = stringResource(R.string.settings_game_skip_game_integrity_check_summary)
                    )

                    TextInputSettingsLayout(
                        modifier = Modifier.fillMaxWidth(),
                        unit = AllSettings.versionCustomInfo,
                        title = stringResource(R.string.settings_game_version_custom_info_title),
                        summary = stringResource(R.string.settings_game_version_custom_info_summary)
                    )
                }
            }

            AnimatedItem(scope) { yOffset ->
                SettingsBackground(
                    modifier = Modifier.offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
                ) {
                    ListSettingsLayout(
                        modifier = Modifier.fillMaxWidth(),
                        unit = AllSettings.javaRuntime,
                        items = RuntimesManager.getRuntimes().filter { it.isCompatible },
                        title = stringResource(R.string.settings_game_java_runtime_title),
                        summary = stringResource(R.string.settings_game_java_runtime_summary),
                        getItemText = { it.name },
                        getItemId = { it.name }
                    )

                    SwitchSettingsLayout(
                        modifier = Modifier.fillMaxWidth(),
                        unit = AllSettings.autoPickJavaRuntime,
                        title = stringResource(R.string.settings_game_auto_pick_java_runtime_title),
                        summary = stringResource(R.string.settings_game_auto_pick_java_runtime_summary)
                    )

                    SliderSettingsLayout(
                        modifier = Modifier.fillMaxWidth(),
                        unit = AllSettings.ramAllocation,
                        title = stringResource(R.string.settings_game_java_memory_title),
                        summary = stringResource(R.string.settings_game_java_memory_summary),
                        valueRange = 256f..getMaxMemoryForSettings(LocalContext.current).toFloat(),
                        suffix = "MB",
                        fineTuningControl = true
                    )

                    MemoryPreview(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .padding(start = 2.dp, bottom = 12.dp),
                        preview = AllSettings.ramAllocation.state.toDouble(),
                        usedText = { usedMemory, totalMemory ->
                            stringResource(R.string.settings_game_java_memory_used_text, usedMemory.toInt(), totalMemory.toInt())
                        },
                        previewText = { preview ->
                            stringResource(R.string.settings_game_java_memory_allocation_text, preview.toInt())
                        }
                    )

                    TextInputSettingsLayout(
                        modifier = Modifier.fillMaxWidth(),
                        unit = AllSettings.jvmArgs,
                        title = stringResource(R.string.settings_game_jvm_args_title),
                        summary = stringResource(R.string.settings_game_jvm_args_summary)
                    )
                }
            }

            AnimatedItem(scope) { yOffset ->
                SettingsBackground(
                    modifier = Modifier.offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
                ) {
                    SwitchSettingsLayout(
                        modifier = Modifier.fillMaxWidth(),
                        unit = AllSettings.showLogAutomatic,
                        title = stringResource(R.string.settings_game_show_log_automatic_title),
                        summary = stringResource(R.string.settings_game_show_log_automatic_summary)
                    )

                    SliderSettingsLayout(
                        modifier = Modifier.fillMaxWidth(),
                        unit = AllSettings.logTextSize,
                        title = stringResource(R.string.settings_game_log_text_size_title),
                        summary = stringResource(R.string.settings_game_log_text_size_summary),
                        valueRange = 5f..20f,
                        suffix = "Sp",
                        fineTuningControl = true
                    )

                    SliderSettingsLayout(
                        modifier = Modifier.fillMaxWidth(),
                        unit = AllSettings.logBufferFlushInterval,
                        title = stringResource(R.string.settings_game_log_buffer_flush_interval_title),
                        summary = stringResource(R.string.settings_game_log_buffer_flush_interval_summary),
                        valueRange = 100f..1000f,
                        suffix = "ms",
                        fineTuningControl = true
                    )
                }
            }
        }
    }
}