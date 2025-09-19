package com.movtery.zalithlauncher.ui.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.view.InputDevice
import android.view.KeyEvent
import android.view.Surface
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.lifecycleScope
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.bridge.LoggerBridge
import com.movtery.zalithlauncher.bridge.ZLBridge
import com.movtery.zalithlauncher.bridge.ZLBridgeStates
import com.movtery.zalithlauncher.game.keycodes.LwjglGlfwKeycode
import com.movtery.zalithlauncher.game.launch.GameLauncher
import com.movtery.zalithlauncher.game.launch.JvmLaunchInfo
import com.movtery.zalithlauncher.game.launch.JvmLauncher
import com.movtery.zalithlauncher.game.launch.Launcher
import com.movtery.zalithlauncher.game.launch.handler.AbstractHandler
import com.movtery.zalithlauncher.game.launch.handler.GameHandler
import com.movtery.zalithlauncher.game.launch.handler.HandlerType
import com.movtery.zalithlauncher.game.launch.handler.JVMHandler
import com.movtery.zalithlauncher.game.multirt.RuntimesManager
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.base.BaseComponentActivity
import com.movtery.zalithlauncher.ui.theme.ZalithLauncherTheme
import com.movtery.zalithlauncher.utils.device.PhysicalMouseChecker
import com.movtery.zalithlauncher.utils.getDisplayFriendlyRes
import com.movtery.zalithlauncher.utils.getParcelableSafely
import com.movtery.zalithlauncher.utils.logging.Logger.lError
import com.movtery.zalithlauncher.utils.logging.Logger.lWarning
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.lwjgl.glfw.CallbackBridge
import java.io.File
import java.io.IOException

private const val INTENT_RUN_GAME = "BUNDLE_RUN_GAME"
private const val INTENT_RUN_JAR = "INTENT_RUN_JAR"
private const val INTENT_VERSION = "INTENT_VERSION"
private const val INTENT_JAR_INFO = "INTENT_JAR_INFO"
private var isRunning = false

class VMActivity : BaseComponentActivity(), SurfaceTextureListener {
    private val eventViewModel: EventViewModel by viewModels()

    private var mTextureView: TextureView? = null

    private lateinit var launcher: Launcher
    private lateinit var handler: AbstractHandler

    private fun runIfHandlerInitialized(
        block: (AbstractHandler) -> Unit
    ) {
        if (this::handler.isInitialized) block(this.handler)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //初始化物理鼠标连接检查器
        PhysicalMouseChecker.initChecker(this)

        val bundle = intent.extras ?: throw IllegalStateException("Unknown VM launch state!")

        val exitListener = { exitCode: Int, isSignal: Boolean ->
            if (exitCode != 0) {
                showExitMessage(this, exitCode, isSignal)
            } else {
                //重启启动器
                startActivity(Intent(this@VMActivity, MainActivity::class.java))
            }
        }

        val getWindowSize = {
            val displayMetrics = getDisplayMetrics()
            IntSize(displayMetrics.widthPixels, displayMetrics.heightPixels)
        }

        launcher = if (bundle.getBoolean(INTENT_RUN_GAME, false)) {
            val version: Version = bundle.getParcelableSafely(INTENT_VERSION, Version::class.java)
                ?: throw IllegalStateException("No launch version has been set.")
            GameLauncher(
                activity = this,
                version = version,
                getWindowSize = getWindowSize,
                onExit = exitListener
            ).also { launcher ->
                handler = GameHandler(
                    context = this,
                    version = version,
                    eventViewModel = eventViewModel,
                    getWindowSize = getWindowSize,
                    gameLauncher = launcher,
                ) { code ->
                    exitListener(code, false)
                }
            }
        } else if (bundle.getBoolean(INTENT_RUN_JAR, false)) {
            val jvmLaunchInfo: JvmLaunchInfo = bundle.getParcelableSafely(INTENT_JAR_INFO, JvmLaunchInfo::class.java)
                ?: throw IllegalStateException("No launch jar info has been set.")
            JvmLauncher(
                context = this,
                getWindowSize = getWindowSize,
                jvmLaunchInfo = jvmLaunchInfo,
                onExit = exitListener
            ).also { launcher ->
                handler = JVMHandler(
                    jvmLauncher = launcher,
                    eventViewModel = eventViewModel,
                    getWindowSize = getWindowSize
                ) { code ->
                    exitListener(code, false)
                }
            }
        } else {
            throw IllegalStateException("Unknown VM launch mode, or the launch mode was not set at all!")
        }

        refreshWindowSize()

        window?.apply {
            setBackgroundDrawable(Color.BLACK.toDrawable())
            if (AllSettings.sustainedPerformance.getValue()) {
                setSustainedPerformanceMode(true)
            }
            addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) // 防止系统息屏
        }

        val logFile = File(PathManager.DIR_FILES_EXTERNAL, "${launcher.getLogName()}.log")
        if (!logFile.exists() && !logFile.createNewFile()) throw IOException("Failed to create a new log file")
        LoggerBridge.start(logFile.absolutePath)

        lifecycleScope.launch {
            //开始接收事件
            eventViewModel.events.collect { event ->
                when (event) {
                    is EventViewModel.Event.Game.RefreshSize -> {
                        refreshSize()
                    }
                    else -> { /* Ignore */ }
                }
            }
        }

        setContent {
            ZalithLauncherTheme {
                Screen {
                    handler.ComposableLayout()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        runIfHandlerInitialized { it.onResume() }
        CallbackBridge.nativeSetWindowAttrib(LwjglGlfwKeycode.GLFW_HOVERED, 1)
    }

    override fun onPause() {
        super.onPause()
        runIfHandlerInitialized { it.onPause() }
        CallbackBridge.nativeSetWindowAttrib(LwjglGlfwKeycode.GLFW_HOVERED, 0)
    }

    override fun onStart() {
        super.onStart()
        CallbackBridge.nativeSetWindowAttrib(LwjglGlfwKeycode.GLFW_HOVERED, 1)
    }

    override fun onStop() {
        super.onStop()
        CallbackBridge.nativeSetWindowAttrib(LwjglGlfwKeycode.GLFW_HOVERED, 0)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        refreshDisplayMetrics()
        refreshSize()
    }

    override fun onPostResume() {
        super.onPostResume()
        lifecycleScope.launch {
            delay(500)
            refreshSize()
        }
    }

    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val code = AllSettings.physicalKeyImeCode.state
        if (code != null && event.keyCode == code) {
            //用户按下了绑定呼出输入法的按键
            //向Compose端发送事件，调出输入法
            eventViewModel.sendEvent(EventViewModel.Event.Game.ShowIme)
            return true
        }
        event.device?.let {
            val source = event.source
            if (source and InputDevice.SOURCE_MOUSE_RELATIVE == InputDevice.SOURCE_MOUSE_RELATIVE ||
                source and InputDevice.SOURCE_MOUSE == InputDevice.SOURCE_MOUSE) {

                if (event.keyCode == KeyEvent.KEYCODE_BACK) {
                    //一些系统会将鼠标右键当成KEYCODE_BACK来处理，需要在这里进行拦截
                    val isPressed = event.action == KeyEvent.ACTION_DOWN
                    //然后发送真实的鼠标右键
                    runIfHandlerInitialized { it.sendMouseRight(isPressed) }
                    return false
                }
            }
        }
        if (this::handler.isInitialized && handler.shouldIgnoreKeyEvent(event)) {
            return super.dispatchKeyEvent(event)
        }
        return true
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        if (isRunning) {
            ZLBridge.setupBridgeWindow(Surface(surface))
            return
        }
        isRunning = true

        runIfHandlerInitialized { it.mIsSurfaceDestroyed = false }
        refreshSize()
        runIfHandlerInitialized { handler ->
            lifecycleScope.launch(Dispatchers.Default) {
                handler.execute(Surface(surface), lifecycleScope)
            }
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        refreshSize()
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        runIfHandlerInitialized { it.mIsSurfaceDestroyed = true }
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        runIfHandlerInitialized { it.onGraphicOutput() }
    }

    override fun shouldIgnoreNotch(): Boolean = AllSettings.gameFullScreen.getValue()

    @Composable
    private fun Screen(
        content: @Composable () -> Unit = {}
    ) {
        if (this::handler.isInitialized) {
            val imeInsets = WindowInsets.ime
            val density = LocalDensity.current
            val inputArea by handler.inputArea.collectAsState()
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .offset {
                        val area = inputArea ?: return@offset IntOffset.Zero
                        val imeHeight = imeInsets.getBottom(density)
                        val bottomDistance = CallbackBridge.windowHeight - area.bottom
                        val bottomPadding = (imeHeight - bottomDistance).coerceAtLeast(0)
                        IntOffset(0, -bottomPadding)
                    },
                factory = { context ->
                    TextureView(context).apply {
                        isOpaque = true
                        alpha = 1.0f

                        surfaceTextureListener = this@VMActivity
                    }.also { view ->
                        mTextureView = view
                    }
                }
            )

            content()
        }
    }

    private fun refreshWindowSize() {
        runIfHandlerInitialized { handler ->
            val displayMetrics = getDisplayMetrics()

            fun getDisplayPixels(pixels: Int): Int {
                return when (handler.type) {
                    HandlerType.GAME -> getDisplayFriendlyRes(pixels, AllSettings.resolutionRatio.state / 100f)
                    HandlerType.JVM -> getDisplayFriendlyRes(pixels, 0.8f)
                }
            }

            val width = getDisplayPixels(displayMetrics.widthPixels)
            val height = getDisplayPixels(displayMetrics.heightPixels)
            if (width < 1 || height < 1) {
                lError("Impossible resolution : $width x $height")
                return@runIfHandlerInitialized
            }
            CallbackBridge.windowWidth = width
            CallbackBridge.windowHeight = height
            ZLBridgeStates.onWindowChange()
        }
    }

    private fun refreshSize() {
        refreshWindowSize()
        mTextureView?.surfaceTexture?.apply {
            setDefaultBufferSize(CallbackBridge.windowWidth, CallbackBridge.windowHeight)
        } ?: run {
            lWarning("Attempt to refresh size on null surface")
            return
        }
        CallbackBridge.sendUpdateWindowSize(CallbackBridge.windowWidth, CallbackBridge.windowHeight)
    }
}

/**
 * 让VMActivity进入运行游戏模式
 * @param version 指定版本
 */
fun runGame(context: Context, version: Version) {
    val intent = Intent(context, VMActivity::class.java).apply {
        putExtra(INTENT_RUN_GAME, true)
        putExtra(INTENT_VERSION, version)
        addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }
    context.startActivity(intent)
}

/**
 * 让VMActivity进入运行Jar模式
 * @param jarFile 指定 jar 文件
 * @param jreName 指定使用的 Java 环境，null 则为自动选择
 * @param customArgs 指定 jvm 参数
 */
fun runJar(
    context: Context,
    jarFile: File,
    jreName: String? = null,
    customArgs: String? = null
) {
    RuntimesManager.getExactJreName(8) ?: run {
        Toast.makeText(context, R.string.multirt_no_java_8, Toast.LENGTH_SHORT).show()
        return
    }

    val jvmArgsPrefix = customArgs?.let { "$it " } ?: ""
    val jvmArgs = "$jvmArgsPrefix-jar ${jarFile.absolutePath}"

    val jvmLaunchInfo = JvmLaunchInfo(
        jvmArgs = jvmArgs,
        jreName = jreName
    )

    val intent = Intent(context, VMActivity::class.java).apply {
        putExtra(INTENT_RUN_JAR, true)
        putExtra(INTENT_JAR_INFO, jvmLaunchInfo)
        addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }
    context.startActivity(intent)
}