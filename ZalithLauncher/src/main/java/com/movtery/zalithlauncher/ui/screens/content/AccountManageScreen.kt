package com.movtery.zalithlauncher.ui.screens.content

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.context.copyLocalFile
import com.movtery.zalithlauncher.context.getFileName
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.coroutine.TaskSystem
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.account.addOtherServer
import com.movtery.zalithlauncher.game.account.auth_server.AuthServerHelper
import com.movtery.zalithlauncher.game.account.auth_server.ResponseException
import com.movtery.zalithlauncher.game.account.isAuthServerAccount
import com.movtery.zalithlauncher.game.account.isLocalAccount
import com.movtery.zalithlauncher.game.account.isMicrosoftAccount
import com.movtery.zalithlauncher.game.account.isMicrosoftLogging
import com.movtery.zalithlauncher.game.account.localLogin
import com.movtery.zalithlauncher.game.account.microsoft.MINECRAFT_SERVICES_URL
import com.movtery.zalithlauncher.game.account.microsoft.MinecraftProfileException
import com.movtery.zalithlauncher.game.account.microsoft.NotPurchasedMinecraftException
import com.movtery.zalithlauncher.game.account.microsoft.XboxLoginException
import com.movtery.zalithlauncher.game.account.microsoft.toLocal
import com.movtery.zalithlauncher.game.account.microsoftLogin
import com.movtery.zalithlauncher.game.account.wardrobe.EmptyCape
import com.movtery.zalithlauncher.game.account.wardrobe.SkinModelType
import com.movtery.zalithlauncher.game.account.wardrobe.capeTranslatedName
import com.movtery.zalithlauncher.game.account.wardrobe.getLocalUUIDWithSkinModel
import com.movtery.zalithlauncher.game.account.yggdrasil.changeCape
import com.movtery.zalithlauncher.game.account.yggdrasil.getPlayerProfile
import com.movtery.zalithlauncher.game.account.yggdrasil.uploadSkin
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.IconTextButton
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.ScalingActionButton
import com.movtery.zalithlauncher.ui.components.ScalingLabel
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.SimpleEditDialog
import com.movtery.zalithlauncher.ui.components.SimpleListDialog
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.content.elements.AccountItem
import com.movtery.zalithlauncher.ui.screens.content.elements.AccountOperation
import com.movtery.zalithlauncher.ui.screens.content.elements.AccountSkinOperation
import com.movtery.zalithlauncher.ui.screens.content.elements.LocalLoginDialog
import com.movtery.zalithlauncher.ui.screens.content.elements.LocalLoginOperation
import com.movtery.zalithlauncher.ui.screens.content.elements.LoginItem
import com.movtery.zalithlauncher.ui.screens.content.elements.MicrosoftChangeCapeOperation
import com.movtery.zalithlauncher.ui.screens.content.elements.MicrosoftChangeSkinOperation
import com.movtery.zalithlauncher.ui.screens.content.elements.MicrosoftLoginOperation
import com.movtery.zalithlauncher.ui.screens.content.elements.MicrosoftLoginTipDialog
import com.movtery.zalithlauncher.ui.screens.content.elements.OtherLoginOperation
import com.movtery.zalithlauncher.ui.screens.content.elements.OtherServerLoginDialog
import com.movtery.zalithlauncher.ui.screens.content.elements.SelectSkinModelDialog
import com.movtery.zalithlauncher.ui.screens.content.elements.ServerItem
import com.movtery.zalithlauncher.ui.screens.content.elements.ServerOperation
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState
import com.movtery.zalithlauncher.utils.logging.Logger.lError
import com.movtery.zalithlauncher.utils.string.StringUtils.Companion.getMessageOrToString
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.apache.commons.io.FileUtils
import java.io.File
import java.net.ConnectException
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException
import java.util.UUID

@Composable
fun AccountManageScreen(
    backStackViewModel: ScreenBackStackViewModel,
    backToMainScreen: () -> Unit,
    openLink: (url: String) -> Unit,
    summitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    var microsoftLoginOperation by remember { mutableStateOf<MicrosoftLoginOperation>(MicrosoftLoginOperation.None) }
    var microsoftChangeSkinOperation by remember { mutableStateOf<MicrosoftChangeSkinOperation>(MicrosoftChangeSkinOperation.None) }
    var microsoftChangeCapeOperation by remember { mutableStateOf<MicrosoftChangeCapeOperation>(MicrosoftChangeCapeOperation.None) }
    var localLoginOperation by remember { mutableStateOf<LocalLoginOperation>(LocalLoginOperation.None) }
    var otherLoginOperation by remember { mutableStateOf<OtherLoginOperation>(OtherLoginOperation.None) }
    var serverOperation by remember { mutableStateOf<ServerOperation>(ServerOperation.None) }

    BaseScreen(
        screenKey = NormalNavKey.AccountManager,
        currentKey = backStackViewModel.mainScreen.currentKey
    ) { isVisible ->
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            ServerTypeMenu(
                isVisible = isVisible,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(all = 12.dp)
                    .weight(2.5f),
                updateMicrosoftOperation = { microsoftLoginOperation = it },
                updateLocalLoginOperation = { localLoginOperation = it },
                updateOtherLoginOperation = { otherLoginOperation = it },
                updateServerOperation = { serverOperation = it }
            )
            AccountsLayout(
                isVisible = isVisible,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(top = 12.dp, end = 12.dp, bottom = 12.dp)
                    .weight(7.5f),
                summitError = summitError,
                onAddAuthClicked = {
                    //打开添加认证服务器的对话框
                    serverOperation = ServerOperation.AddNew
                },
                swapToDownloadScreen = { projectId, platform, classes ->
                    backStackViewModel.navigateToDownload(
                        targetScreen = backStackViewModel.downloadModScreen.apply {
                            navigateTo(
                                NormalNavKey.DownloadAssets(platform, projectId, classes)
                            )
                        }
                    )
                },
                onMicrosoftChangeSkin = { account, result ->
                    microsoftChangeSkinOperation = MicrosoftChangeSkinOperation.ImportFile(account, result)
                },
                onMicrosoftChangeCape = { account ->
                    microsoftChangeCapeOperation = MicrosoftChangeCapeOperation.FetchProfiles(account)
                }
            )
        }
    }

    //微软账号操作逻辑
    MicrosoftLoginOperation(
        checkIfInWebScreen = {
            backStackViewModel.mainScreen.currentKey is NormalNavKey.WebScreen
        },
        navigateToWeb = { url ->
            backStackViewModel.mainScreen.backStack.navigateToWeb(url)
        },
        backToMainScreen = backToMainScreen,
        microsoftLoginOperation = microsoftLoginOperation,
        updateOperation = { microsoftLoginOperation = it },
        openLink = openLink,
        summitError = summitError
    )

    //微软账号更改皮肤操作逻辑
    MicrosoftChangeSkinOperation(
        operation = microsoftChangeSkinOperation,
        updateOperation = { microsoftChangeSkinOperation = it },
        summitError = summitError
    )

    //微软账号更改披风操作逻辑
    MicrosoftChangeCapeOperation(
        operation = microsoftChangeCapeOperation,
        updateOperation = { microsoftChangeCapeOperation = it },
        summitError = summitError
    )

    //离线账号操作逻辑
    LocalLoginOperation(
        localLoginOperation = localLoginOperation,
        updateOperation = { localLoginOperation = it },
        openLink = openLink
    )

    //外置账号操作逻辑
    OtherLoginOperation(
        otherLoginOperation = otherLoginOperation,
        updateOperation = { otherLoginOperation = it },
        summitError = summitError,
        openLink = openLink
    )

    //外置服务器操作逻辑
    ServerTypeOperation(
        serverOperation = serverOperation,
        updateServerOperation = { serverOperation = it },
        summitError = summitError
    )
}

@Composable
private fun ServerTypeMenu(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    updateMicrosoftOperation: (MicrosoftLoginOperation) -> Unit,
    updateLocalLoginOperation: (LocalLoginOperation) -> Unit,
    updateOtherLoginOperation: (OtherLoginOperation) -> Unit,
    updateServerOperation: (ServerOperation) -> Unit
) {
    val xOffset by swapAnimateDpAsState(
        targetValue = (-40).dp,
        swapIn = isVisible,
        isHorizontal = true
    )

    Card(
        modifier = modifier
            .offset {
                IntOffset(
                    x = xOffset.roundToPx(),
                    y = 0
                )
            }
            .fillMaxHeight(),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column {
            Column(
                modifier = Modifier
                    .padding(all = 12.dp)
                    .verticalScroll(state = rememberScrollState())
                    .weight(1f)
            ) {
                LoginItem(
                    modifier = Modifier.fillMaxWidth(),
                    serverName = stringResource(R.string.account_type_microsoft),
                ) {
                    if (!isMicrosoftLogging()) {
                        updateMicrosoftOperation(MicrosoftLoginOperation.Tip)
                    }
                }
                LoginItem(
                    modifier = Modifier.fillMaxWidth(),
                    serverName = stringResource(R.string.account_type_local)
                ) {
                    updateLocalLoginOperation(LocalLoginOperation.Edit)
                }

                val authServers by AccountsManager.authServersFlow.collectAsState()
                authServers.forEach { server ->
                    ServerItem(
                        server = server,
                        onClick = { updateOtherLoginOperation(OtherLoginOperation.OnLogin(server)) },
                        onDeleteClick = { updateServerOperation(ServerOperation.Delete(server)) }
                    )
                }
            }

            ScalingActionButton(
                modifier = Modifier
                    .padding(PaddingValues(horizontal = 12.dp, vertical = 8.dp))
                    .fillMaxWidth(),
                onClick = { updateServerOperation(ServerOperation.AddNew) }
            ) {
                MarqueeText(text = stringResource(R.string.account_add_new_server_button))
            }
        }
    }
}

/**
 * 微软账号登陆操作逻辑
 */
@Composable
private fun MicrosoftLoginOperation(
    checkIfInWebScreen: () -> Boolean,
    navigateToWeb: (url: String) -> Unit,
    backToMainScreen: () -> Unit,
    microsoftLoginOperation: MicrosoftLoginOperation,
    updateOperation: (MicrosoftLoginOperation) -> Unit,
    openLink: (url: String) -> Unit,
    summitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    val context = LocalContext.current

    when (microsoftLoginOperation) {
        is MicrosoftLoginOperation.None -> {}
        is MicrosoftLoginOperation.Tip -> {
            MicrosoftLoginTipDialog(
                onDismissRequest = { updateOperation(MicrosoftLoginOperation.None) },
                onConfirm = { updateOperation(MicrosoftLoginOperation.RunTask) },
                openLink = openLink
            )
        }
        is MicrosoftLoginOperation.RunTask -> {
            microsoftLogin(
                context = context,
                toWeb = navigateToWeb,
                backToMain = backToMainScreen,
                checkIfInWebScreen = checkIfInWebScreen,
                updateOperation = { updateOperation(it) },
                summitError = summitError
            )
            updateOperation(MicrosoftLoginOperation.None)
        }
    }
}

@Composable
private fun MicrosoftChangeSkinOperation(
    operation: MicrosoftChangeSkinOperation,
    updateOperation: (MicrosoftChangeSkinOperation) -> Unit,
    summitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    val context = LocalContext.current
    when (operation) {
        is MicrosoftChangeSkinOperation.None -> {}
        is MicrosoftChangeSkinOperation.ImportFile -> {
            val account = operation.account
            val uri = operation.uri

            val fileName = context.getFileName(uri) ?: UUID.randomUUID().toString().replace("-", "")
            val cacheFile = File(PathManager.DIR_IMAGE_CACHE, fileName)

            val importCacheSkin = Task.runTask(
                id = account.uniqueUUID,
                dispatcher = Dispatchers.IO,
                task = {
                    context.copyLocalFile(uri, cacheFile)
                    //导入成功后，检查图片文件像素尺寸
                    val options = BitmapFactory.Options()
                    options.inJustDecodeBounds = true
                    BitmapFactory.decodeFile(cacheFile.absolutePath, options)

                    val width = options.outWidth
                    val height = options.outHeight
                    if ((width == 64 && height == 32) || (width == 64 && height == 64)) {
                        //像素尺寸满足 64x64 或 32x32
                        updateOperation(MicrosoftChangeSkinOperation.SelectSkinModel(account, cacheFile))
                    } else {
                        //像素尺寸不符合要求
                        summitError(
                            ErrorViewModel.ThrowableMessage(
                                title = context.getString(R.string.generic_warning),
                                message = context.getString(R.string.account_change_skin_invalid)
                            )
                        )
                        updateOperation(MicrosoftChangeSkinOperation.None)
                    }
                },
                onError = { th ->
                    summitError(
                        ErrorViewModel.ThrowableMessage(
                            title = context.getString(R.string.generic_error),
                            message = context.getString(R.string.account_change_skin_failed_to_import) + "\r\n" + th.getMessageOrToString()
                        )
                    )
                    updateOperation(MicrosoftChangeSkinOperation.None)
                },
                onCancel = {
                    updateOperation(MicrosoftChangeSkinOperation.None)
                }
            )

            TaskSystem.submitTask(importCacheSkin)
        }
        is MicrosoftChangeSkinOperation.SelectSkinModel -> {
            val account = operation.account
            val skinFile = operation.file
            SelectSkinModelDialog(
                onDismissRequest = {
                    updateOperation(MicrosoftChangeSkinOperation.None)
                },
                onSelected = { modelType ->
                    updateOperation(
                        MicrosoftChangeSkinOperation.RunTask(
                            account = account,
                            file = skinFile,
                            skinModel = modelType
                        )
                    )
                }
            )
        }
        is MicrosoftChangeSkinOperation.RunTask -> {
            val account = operation.account
            val skinFile = operation.file
            val skinModel = operation.skinModel

            val task = Task.runTask(
                id = account.uniqueUUID,
                dispatcher = Dispatchers.IO,
                task = { task ->
                    task.updateMessage(R.string.account_change_skin_uploading)
                    uploadSkin(
                        apiUrl = MINECRAFT_SERVICES_URL,
                        accessToken = account.accessToken,
                        file = skinFile,
                        modelType = skinModel
                    )
                    //刷新本地皮肤
                    task.updateMessage(R.string.account_change_skin_update_local)
                    runCatching {
                        account.downloadSkin()
                    }.onFailure { th ->
                        summitError(
                            ErrorViewModel.ThrowableMessage(
                                title = context.getString(R.string.account_logging_in_failed),
                                message = context.formatAccountError(th)
                            )
                        )
                    }
                    //刷新本地皮肤后，使用Toast反馈给用户
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.account_change_skin_update_toast),
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    updateOperation(MicrosoftChangeSkinOperation.None)
                },
                onError = { th ->
                    val (title, message) = when {
                        th is io.ktor.client.plugins.ResponseException -> {
                            val response = th.response
                            val code = response.status.value
                            val body = response.body<JsonObject>()
                            val message = body["errorMessage"]?.jsonPrimitive?.contentOrNull
                            context.getString(R.string.account_change_skin_failed_to_upload, code) to (message ?: th.getMessageOrToString())
                        }
                        else -> context.getString(R.string.generic_error) to context.formatAccountError(th)
                    }

                    summitError(
                        ErrorViewModel.ThrowableMessage(
                            title = title,
                            message = message
                        )
                    )
                    updateOperation(MicrosoftChangeSkinOperation.None)
                },
                onCancel = {
                    updateOperation(MicrosoftChangeSkinOperation.None)
                }
            )

            TaskSystem.submitTask(task)
        }
    }
}

/**
 * 微软账号更改披风操作逻辑
 */
@Composable
private fun MicrosoftChangeCapeOperation(
    operation: MicrosoftChangeCapeOperation,
    updateOperation: (MicrosoftChangeCapeOperation) -> Unit,
    summitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    val context = LocalContext.current
    when (operation) {
        is MicrosoftChangeCapeOperation.None -> {}
        is MicrosoftChangeCapeOperation.FetchProfiles -> {
            val account = operation.account
            val task = Task.runTask(
                id = account.uniqueUUID,
                dispatcher = Dispatchers.IO,
                task = { task ->
                    task.updateMessage(R.string.account_change_cape_fetch_all)
                    val profile = getPlayerProfile(
                        apiUrl = MINECRAFT_SERVICES_URL,
                        accessToken = account.accessToken
                    )
                    updateOperation(MicrosoftChangeCapeOperation.SelectCape(account, profile))
                },
                onError = { th ->
                    summitError(
                        ErrorViewModel.ThrowableMessage(
                            title = context.getString(R.string.generic_error),
                            message = context.getString(R.string.account_change_cape_fetch_all_failed) + "\r\n" + th.getMessageOrToString()
                        )
                    )
                    updateOperation(MicrosoftChangeCapeOperation.None)
                },
                onCancel = {
                    updateOperation(MicrosoftChangeCapeOperation.None)
                }
            )
            TaskSystem.submitTask(task)
        }
        is MicrosoftChangeCapeOperation.SelectCape -> {
            val account = operation.account
            val profile = operation.profile

            val capes = remember(profile.capes) {
                listOf(EmptyCape) + profile.capes
            }

            SimpleListDialog(
                title = stringResource(R.string.account_change_cape_select_cape),
                items = capes,
                itemTextProvider = { cape ->
                    cape.capeTranslatedName()
                },
                onItemSelected = { cape ->
                    updateOperation(MicrosoftChangeCapeOperation.RunTask(account, cape))
                },
                isCurrent = { cape ->
                    cape.state == "ACTIVE" //ACTIVE表示当前正在使用
                },
                onDismissRequest = { selected ->
                    if (!selected) {
                        updateOperation(MicrosoftChangeCapeOperation.None)
                    }
                }
            )
        }
        is MicrosoftChangeCapeOperation.RunTask -> {
            val account = operation.account
            val cape = operation.cape
            val capeName = cape.capeTranslatedName()
            val capeId: String? = cape.takeIf { it != EmptyCape }?.id

            val task = Task.runTask(
                dispatcher = Dispatchers.IO,
                task = { task ->
                    task.updateMessage(R.string.account_change_cape_apply)
                    changeCape(
                        apiUrl = MINECRAFT_SERVICES_URL,
                        accessToken = account.accessToken,
                        capeId = capeId
                    )
                    //已变更披风，展示一条Toast反馈用户
                    withContext(Dispatchers.Main) {
                        val text = if (cape == EmptyCape) {
                            context.getString(R.string.account_change_cape_apply_reset)
                        } else {
                            context.getString(R.string.account_change_cape_apply_success, capeName)
                        }

                        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
                    }
                    updateOperation(MicrosoftChangeCapeOperation.None)
                },
                onError = { th ->
                    val (title, message) = when {
                        th is io.ktor.client.plugins.ResponseException -> {
                            val response = th.response
                            val code = response.status.value
                            val body = response.body<JsonObject>()
                            val message = body["errorMessage"]?.jsonPrimitive?.contentOrNull
                            context.getString(R.string.account_change_cape_apply_failed, code) to (message ?: th.getMessageOrToString())
                        }
                        else -> context.getString(R.string.generic_error) to context.formatAccountError(th)
                    }

                    summitError(
                        ErrorViewModel.ThrowableMessage(
                            title = title,
                            message = message
                        )
                    )
                    updateOperation(MicrosoftChangeCapeOperation.None)
                },
                onCancel = {
                    updateOperation(MicrosoftChangeCapeOperation.None)
                }
            )
            TaskSystem.submitTask(task)
        }
    }
}

/**
 * 离线账号登陆操作逻辑
 */
@Composable
private fun LocalLoginOperation(
    localLoginOperation: LocalLoginOperation,
    updateOperation: (LocalLoginOperation) -> Unit = {},
    openLink: (url: String) -> Unit = {}
) {
    when (localLoginOperation) {
        is LocalLoginOperation.None -> {}
        is LocalLoginOperation.Edit -> {
            LocalLoginDialog(
                onDismissRequest = { updateOperation(LocalLoginOperation.None) },
                onConfirm = { isUserNameInvalid, userName ->
                    val operation = if (isUserNameInvalid) {
                        LocalLoginOperation.Alert(userName)
                    } else {
                        LocalLoginOperation.Create(userName)
                    }
                    updateOperation(operation)
                },
                openLink = openLink
            )
        }
        is LocalLoginOperation.Create -> {
            localLogin(userName = localLoginOperation.userName)
            //复位
            updateOperation(LocalLoginOperation.None)
        }
        is LocalLoginOperation.Alert -> {
            SimpleAlertDialog(
                title = stringResource(R.string.account_supporting_username_invalid_title),
                text = {
                    Text(text = stringResource(R.string.account_supporting_username_invalid_local_message_hint1))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.account_supporting_username_invalid_local_message_hint2),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = stringResource(R.string.account_supporting_username_invalid_local_message_hint3))
                    Text(text = stringResource(R.string.account_supporting_username_invalid_local_message_hint4))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.account_supporting_username_invalid_local_message_hint5),
                        fontWeight = FontWeight.Bold
                    )
                },
                confirmText = stringResource(R.string.account_supporting_username_invalid_still_use),
                onConfirm = {
                    updateOperation(LocalLoginOperation.Create(localLoginOperation.userName))
                },
                onCancel = {
                    updateOperation(LocalLoginOperation.None)
                }
            )
        }
    }
}

@Composable
private fun OtherLoginOperation(
    otherLoginOperation: OtherLoginOperation,
    updateOperation: (OtherLoginOperation) -> Unit,
    summitError: (ErrorViewModel.ThrowableMessage) -> Unit,
    openLink: (link: String) -> Unit
) {
    val context = LocalContext.current
    when (otherLoginOperation) {
        is OtherLoginOperation.None -> {}
        is OtherLoginOperation.OnLogin -> {
            OtherServerLoginDialog(
                server = otherLoginOperation.server,
                onRegisterClick = { url ->
                    openLink(url)
                    updateOperation(OtherLoginOperation.None)
                },
                onDismissRequest = { updateOperation(OtherLoginOperation.None) },
                onConfirm = { email, password ->
                    updateOperation(OtherLoginOperation.None)
                    AuthServerHelper(
                        otherLoginOperation.server, email, password,
                        onSuccess = { account, task ->
                            task.updateMessage(R.string.account_logging_in_saving)
                            account.downloadSkin()
                            AccountsManager.suspendSaveAccount(account)
                        },
                        onFailed = { th ->
                            updateOperation(OtherLoginOperation.OnFailed(th))
                        }
                    ).createNewAccount(context) { availableProfiles, selectedFunction ->
                        updateOperation(
                            OtherLoginOperation.SelectRole(
                                availableProfiles,
                                selectedFunction
                            )
                        )
                    }
                }
            )
        }
        is OtherLoginOperation.OnFailed -> {
            val message: String = when (val th = otherLoginOperation.th) {
                is ResponseException -> th.responseMessage
                is HttpRequestTimeoutException -> stringResource(R.string.error_timeout)
                is UnknownHostException, is UnresolvedAddressException -> stringResource(R.string.error_network_unreachable)
                is ConnectException -> stringResource(R.string.error_connection_failed)
                is io.ktor.client.plugins.ResponseException -> {
                    val statusCode = th.response.status
                    val res = when (statusCode) {
                        HttpStatusCode.Unauthorized -> R.string.error_unauthorized
                        HttpStatusCode.NotFound -> R.string.error_notfound
                        else -> R.string.error_client_error
                    }
                    stringResource(res, statusCode)
                }
                else -> {
                    lError("An unknown exception was caught!", th)
                    val errorMessage = th.localizedMessage ?: th.message ?: th::class.qualifiedName ?: "Unknown error"
                    stringResource(R.string.error_unknown, errorMessage)
                }
            }

            summitError(
                ErrorViewModel.ThrowableMessage(
                    title = stringResource(R.string.account_logging_in_failed),
                    message = message
                )
            )
            updateOperation(OtherLoginOperation.None)
        }
        is OtherLoginOperation.SelectRole -> {
            SimpleListDialog(
                title = stringResource(R.string.account_other_login_select_role),
                items = otherLoginOperation.profiles,
                itemTextProvider = { it.name },
                onItemSelected = { otherLoginOperation.selected(it) },
                onDismissRequest = { updateOperation(OtherLoginOperation.None) }
            )
        }
    }
}

@Composable
private fun ServerTypeOperation(
    serverOperation: ServerOperation,
    updateServerOperation: (ServerOperation) -> Unit,
    summitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    when (serverOperation) {
        is ServerOperation.AddNew -> {
            var serverUrl by rememberSaveable { mutableStateOf("") }
            SimpleEditDialog(
                title = stringResource(R.string.account_add_new_server),
                value = serverUrl,
                onValueChange = { serverUrl = it.trim() },
                label = { Text(text = stringResource(R.string.account_label_server_url)) },
                singleLine = true,
                onDismissRequest = { updateServerOperation(ServerOperation.None) },
                onConfirm = {
                    if (serverUrl.isNotEmpty()) {
                        updateServerOperation(ServerOperation.Add(serverUrl))
                    }
                }
            )
        }
        is ServerOperation.Add -> {
            addOtherServer(
                serverUrl = serverOperation.serverUrl,
                onThrowable = { updateServerOperation(ServerOperation.OnThrowable(it)) }
            )
            updateServerOperation(ServerOperation.None)
        }
        is ServerOperation.Delete -> {
            val server = serverOperation.server
            SimpleAlertDialog(
                title = stringResource(R.string.account_other_login_delete_server_title),
                text = stringResource(
                    R.string.account_other_login_delete_server_message,
                    server.serverName
                ),
                onDismiss = { updateServerOperation(ServerOperation.None) },
                onConfirm = {
                    AccountsManager.deleteAuthServer(server)
                    updateServerOperation(ServerOperation.None)
                }
            )
        }
        is ServerOperation.OnThrowable -> {
            summitError(
                ErrorViewModel.ThrowableMessage(
                    title = stringResource(R.string.account_other_login_adding_failure),
                    message = serverOperation.throwable.getMessageOrToString()
                )
            )
            updateServerOperation(ServerOperation.None)
        }
        is ServerOperation.None -> {}
    }
}

@Composable
private fun AccountsLayout(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    summitError: (ErrorViewModel.ThrowableMessage) -> Unit,
    onAddAuthClicked: () -> Unit,
    swapToDownloadScreen: (id: String, platform: Platform, classes: PlatformClasses) -> Unit,
    onMicrosoftChangeSkin: (Account, Uri) -> Unit,
    onMicrosoftChangeCape: (Account) -> Unit
) {
    val yOffset by swapAnimateDpAsState(
        targetValue = (-40).dp,
        swapIn = isVisible
    )

    val context = LocalContext.current

    val accounts by AccountsManager.accountsFlow.collectAsState()
    val currentAccount by AccountsManager.currentAccountFlow.collectAsState()

    var accountOperation by remember { mutableStateOf<AccountOperation>(AccountOperation.None) }
    AccountOperation(
        accountOperation = accountOperation,
        updateAccountOperation = { accountOperation = it },
        summitError = summitError
    )

    Card(
        modifier = modifier.offset {
            IntOffset(
                x = 0,
                y = yOffset.roundToPx()
            )
        },
        shape = MaterialTheme.shapes.extraLarge
    ) {
        if (accounts.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape = MaterialTheme.shapes.extraLarge),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                items(accounts) { account ->
                    var refreshAvatar by remember { mutableStateOf(false) }
                    var accountSkinOperation by remember { mutableStateOf<AccountSkinOperation>(AccountSkinOperation.None) }
                    AccountSkinOperation(
                        account = account,
                        accountSkinOperation = accountSkinOperation,
                        updateOperation = { accountSkinOperation = it },
                        summitError = summitError,
                        onAddAuthClicked = onAddAuthClicked,
                        onRefreshAvatar = { refreshAvatar = !refreshAvatar },
                        swapToDownloadScreen = swapToDownloadScreen
                    )

                    val skinPicker = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.OpenDocument()
                    ) { uri ->
                        uri?.let { result ->
                            when {
                                account.isLocalAccount() -> {
                                    accountSkinOperation = AccountSkinOperation.SelectSkinModel(result)
                                }
                                account.isMicrosoftAccount() -> {
                                    onMicrosoftChangeSkin(account, result)
                                }
                            }
                        }
                    }

                    AccountItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        currentAccount = currentAccount,
                        account = account,
                        refreshKey = refreshAvatar,
                        onSelected = { acc ->
                            AccountsManager.setCurrentAccount(acc)
                        },
                        onChangeSkin = {
                            if (!account.isAuthServerAccount()) {
                                skinPicker.launch(arrayOf("image/*"))
                            }
                        },
                        onChangeCape = {
                            if (account.isMicrosoftAccount()) {
                                onMicrosoftChangeCape(account)
                            }
                        },
                        onResetSkin = {
                            accountSkinOperation = AccountSkinOperation.PreResetSkin
                        },
                        onRefreshClick = {
                            AccountsManager.refreshAccount(
                                context = context,
                                account = account,
                                onFailed = { th ->
                                    accountOperation = AccountOperation.OnFailed(th)
                                }
                            )
                        },
                        onDeleteClick = { accountOperation = AccountOperation.Delete(account) }
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                ScalingLabel(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(R.string.account_no_account)
                )
            }
        }
    }
}

@Composable
private fun AccountSkinOperation(
    account: Account,
    accountSkinOperation: AccountSkinOperation,
    updateOperation: (AccountSkinOperation) -> Unit,
    summitError: (ErrorViewModel.ThrowableMessage) -> Unit,
    onAddAuthClicked: () -> Unit = {},
    onRefreshAvatar: () -> Unit = {},
    swapToDownloadScreen: (id: String, platform: Platform, classes: PlatformClasses) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    when (accountSkinOperation) {
        is AccountSkinOperation.None -> {}
        is AccountSkinOperation.SaveSkin -> {
            val skinFile = account.getSkinFile()
            TaskSystem.submitTask(
                Task.runTask(
                    dispatcher = Dispatchers.IO,
                    task = {
                        context.copyLocalFile(accountSkinOperation.uri, skinFile)
                        AccountsManager.suspendSaveAccount(account)
                        onRefreshAvatar()
                        //警告用户关于自定义皮肤的一些注意事项
                        updateOperation(AccountSkinOperation.AlertModel)
                    },
                    onError = { th ->
                        FileUtils.deleteQuietly(skinFile)
                        summitError(
                            ErrorViewModel.ThrowableMessage(
                                title = context.getString(R.string.error_import_image),
                                message = th.getMessageOrToString()
                            )
                        )
                        onRefreshAvatar()
                        updateOperation(AccountSkinOperation.None)
                    }
                )
            )
        }
        is AccountSkinOperation.SelectSkinModel -> {
            SelectSkinModelDialog(
                onDismissRequest = {
                    updateOperation(AccountSkinOperation.None)
                },
                onSelected = { type ->
                    account.skinModelType = type
                    account.profileId = getLocalUUIDWithSkinModel(account.username, type)
                    updateOperation(AccountSkinOperation.SaveSkin(accountSkinOperation.uri))
                }
            )
        }
        is AccountSkinOperation.AlertModel -> {
            AlertDialog(
                onDismissRequest = {},
                title = {
                    Text(
                        text = stringResource(R.string.generic_warning),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text(text = stringResource(R.string.account_change_skin_select_model_alert_hint1))
                        Text(
                            text = stringResource(R.string.account_change_skin_select_model_alert_hint2),
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = stringResource(R.string.account_change_skin_select_model_alert_hint3))
                        Text(text = stringResource(R.string.account_change_skin_select_model_alert_hint4))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.account_change_skin_select_model_alert_hint5),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        IconTextButton(
                            onClick = {
                                onAddAuthClicked()
                                updateOperation(AccountSkinOperation.None)
                            },
                            imageVector = Icons.Outlined.Dns,
                            contentDescription = null,
                            text = stringResource(R.string.account_change_skin_select_model_alert_auth_server)
                        )
                        IconTextButton(
                            onClick = {
                                swapToDownloadScreen("idMHQ4n2", Platform.MODRINTH, PlatformClasses.MOD)
                                updateOperation(AccountSkinOperation.None)
                            },
                            imageVector = Icons.Outlined.Checkroom,
                            contentDescription = null,
                            text = stringResource(R.string.account_change_skin_select_model_alert_custom_skin_loader)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            updateOperation(AccountSkinOperation.None)
                        }
                    ) {
                        MarqueeText(text = stringResource(R.string.generic_go_it))
                    }
                }
            )
        }
        is AccountSkinOperation.PreResetSkin -> {
            SimpleAlertDialog(
                title = stringResource(R.string.generic_reset),
                text = stringResource(R.string.account_change_skin_reset_skin_message),
                onDismiss = { updateOperation(AccountSkinOperation.None) },
                onConfirm = { updateOperation(AccountSkinOperation.ResetSkin) }
            )
        }
        is AccountSkinOperation.ResetSkin -> {
            TaskSystem.submitTask(
                Task.runTask(
                    dispatcher = Dispatchers.IO,
                    task = {
                        account.apply {
                            FileUtils.deleteQuietly(getSkinFile())
                            skinModelType = SkinModelType.NONE
                            profileId = getLocalUUIDWithSkinModel(username, skinModelType)
                            AccountsManager.suspendSaveAccount(this)
                            onRefreshAvatar()
                        }
                    }
                )
            )
            updateOperation(AccountSkinOperation.None)
        }
    }
}

@Composable
private fun AccountOperation(
    accountOperation: AccountOperation,
    updateAccountOperation: (AccountOperation) -> Unit,
    summitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    val context = LocalContext.current
    when (accountOperation) {
        is AccountOperation.Delete -> {
            //删除账号前弹出Dialog提醒
            SimpleAlertDialog(
                title = stringResource(R.string.account_delete_title),
                text = stringResource(R.string.account_delete_message,
                    accountOperation.account.username),
                onConfirm = {
                    AccountsManager.deleteAccount(accountOperation.account)
                    updateAccountOperation(AccountOperation.None)
                },
                onDismiss = { updateAccountOperation(AccountOperation.None) }
            )
        }
        is AccountOperation.OnFailed -> {
            val message: String = context.formatAccountError(accountOperation.th)

            summitError(
                ErrorViewModel.ThrowableMessage(
                    title = stringResource(R.string.account_logging_in_failed),
                    message = message
                )
            )
            updateAccountOperation(AccountOperation.None)
        }
        is AccountOperation.None -> {}
    }
}

/**
 * 格式化账号登陆/刷新时遇到的各种错误
 */
private fun Context.formatAccountError(th: Throwable) = when (th) {
    is NotPurchasedMinecraftException -> toLocal(this)
    is MinecraftProfileException -> th.toLocal(this)
    is XboxLoginException -> th.toLocal(this)
    is ResponseException -> th.responseMessage
    is HttpRequestTimeoutException -> getString(R.string.error_timeout)
    is UnknownHostException, is UnresolvedAddressException -> getString(R.string.error_network_unreachable)
    is ConnectException -> getString(R.string.error_connection_failed)
    is io.ktor.client.plugins.ResponseException -> {
        val statusCode = th.response.status
        val res = when (statusCode) {
            HttpStatusCode.Unauthorized -> R.string.error_unauthorized
            HttpStatusCode.NotFound -> R.string.error_notfound
            else -> R.string.error_client_error
        }
        getString(res, statusCode)
    }
    else -> {
        lError("An unknown exception was caught!", th)
        val errorMessage = th.localizedMessage ?: th.message ?: th::class.qualifiedName ?: "Unknown error"
        getString(R.string.error_unknown, errorMessage)
    }
}