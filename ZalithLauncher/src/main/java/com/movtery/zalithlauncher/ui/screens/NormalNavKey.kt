package com.movtery.zalithlauncher.ui.screens

import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.ui.AndroidStringText
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.ui.screens.content.FirstLoginMenu
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

sealed interface NormalNavKey : TitledNavKey {
    @Contextual override val title: AndroidStringText?
        get() = null

    @Serializable data object UnpackDeps: NormalNavKey
    @Serializable data object LauncherMain : NormalNavKey
    @Serializable data class AccountManager(
        val loginMenu: FirstLoginMenu = FirstLoginMenu.NONE
    ) : NormalNavKey {
        @Contextual override val title: AndroidStringText = androidText(R.string.page_title_account_list)
    }
    @Serializable data object McSkinLibrary : NormalNavKey
    @Serializable data class WebScreen(val url: String) : NormalNavKey
    @Serializable data object VersionsManager : NormalNavKey {
        @Contextual override val title: AndroidStringText = androidText(R.string.page_title_version_list)
    }
    @Serializable data class FileSelector(
        val startPath: String,
        val selectFile: Boolean,
        val saveKey: TitledNavKey,
        val onSelected: (path: String) -> Unit
    ) : NormalNavKey {
        @Contextual override val title: AndroidStringText = androidText(R.string.page_title_select_files)
    }
    @Serializable data object Multiplayer: NormalNavKey {
        @Contextual override val title: AndroidStringText = androidText(R.string.terracotta_terracotta)
    }
    /** 内置文件管理器屏幕 */
    @Serializable data class BuiltInFileManager(
        val startPath: String? = null
    ) : NormalNavKey {
        @Contextual override val title: AndroidStringText = androidText(R.string.page_title_file_manager)
    }
    /** 文件编辑器屏幕 */
    @Serializable data class FileEditor(
        val filePath: String
    ) : NormalNavKey {
        @Contextual override val title: AndroidStringText = androidText(R.string.page_title_file_editor)
    }

    /** 查看日志屏幕 */
    @Serializable data class LogView(
        val logPath: String
    ) : NormalNavKey {
        @Contextual override val title: AndroidStringText = androidText(R.string.versions_overview_log)
    }

    /** 游戏日志屏幕 */
    @Serializable data object GameLog : NormalNavKey {
        @Contextual override val title: AndroidStringText = androidText(R.string.page_title_game_log)
    }

    /** 设置嵌套子屏幕 */
    sealed interface Settings : NormalNavKey {
        @Serializable data object Renderer : Settings {
            @Contextual override val title: AndroidStringText = androidText(R.string.settings_tab_renderer)
        }
        /** Turnip 驱动下载屏幕 */
        @Serializable data object TurnipDrivers : Settings {
            @Contextual override val title: AndroidStringText = androidText(R.string.settings_renderer_download_turnip)
        }
        /** 游戏设置屏幕 */
        @Serializable data object Game : Settings {
            @Contextual override val title: AndroidStringText = androidText(R.string.settings_tab_game)
        }
        @Serializable data object Control : Settings {
            @Contextual override val title: AndroidStringText = androidText(R.string.settings_tab_control)
        }
        @Serializable data object Gamepad : Settings {
            @Contextual override val title: AndroidStringText = androidText(R.string.settings_tab_gamepad)
        }
        @Serializable data object Launcher : Settings {
            @Contextual override val title: AndroidStringText = androidText(R.string.settings_tab_launcher)
        }
        @Serializable data object JavaManager : Settings {
            @Contextual override val title: AndroidStringText = androidText(R.string.settings_tab_java_manage)
        }
        @Serializable data object ControlManager : Settings {
            @Contextual override val title: AndroidStringText = androidText(R.string.settings_tab_control_manage)
        }
        @Serializable data object AboutInfo : Settings {
            @Contextual override val title: AndroidStringText = androidText(R.string.settings_tab_info_about)
        }
        /** Quick Access 面板自定义屏幕 */
        @Serializable data object QuickAccessCustomization : Settings {
            @Contextual override val title: AndroidStringText = androidText(R.string.settings_launcher_quick_access_title)
        }
    }
    sealed interface Versions : NormalNavKey {
        @Serializable data object OverView : Versions {
            @Contextual override val title: AndroidStringText = androidText(R.string.versions_settings_overview)
        }
        @Serializable data object Config : Versions {
            @Contextual override val title: AndroidStringText = androidText(R.string.versions_settings_config)
        }
        /** 修改版本屏幕 */
        @Serializable data object ModifyVersion : Versions {
            @Contextual override val title: AndroidStringText = androidText(R.string.versions_modify_version)
        }
        /** 选择要修改的 Minecraft 版本屏幕 */
        @Serializable data object SelectGameVersion : Versions {
            @Contextual override val title: AndroidStringText = androidText(R.string.versions_modify_select_mc)
        }
        @Serializable data object ModsManager : Versions {
            @Contextual override var title: AndroidStringText = androidText(R.string.mods_manage)
        }
        @Serializable data object SavesManager : Versions {
            @Contextual override var title: AndroidStringText = androidText(R.string.saves_manage)
        }
        @Serializable data object ResourcePackManager : Versions {
            @Contextual override var title: AndroidStringText = androidText(R.string.resource_pack_manage)
        }
        @Serializable data object ShadersManager : Versions {
            @Contextual override var title: AndroidStringText = androidText(R.string.shader_pack_manage)
        }
        @Serializable data object ScreenshotsManager : Versions {
            @Contextual override var title: AndroidStringText = androidText(R.string.screenshots_manage)
        }
        @Serializable data object ServerList : Versions {
            @Contextual override val title: AndroidStringText = androidText(R.string.servers_list)
        }
    }
    sealed interface VersionExports : NormalNavKey {
        @Serializable data object SelectType : VersionExports
        @Serializable data object EditInfo : VersionExports
        @Serializable data object SelectFiles : VersionExports
    }
    sealed interface DownloadGame : NormalNavKey {
        @Serializable data object SelectGameVersion : Versions
        @Serializable data class Addons(val gameVersion: String) : Versions
    }
    @Serializable data object SearchModPack : NormalNavKey
    @Serializable data object SearchMod : NormalNavKey
    @Serializable data object SearchResourcePack : NormalNavKey
    @Serializable data object SearchSaves : NormalNavKey
    @Serializable data object SearchShaders : NormalNavKey
    @Serializable data object SearchId : NormalNavKey {
        @Contextual override val title: AndroidStringText = androidText(R.string.download_category_by_id)
    }
    @Serializable data object Favorites : NormalNavKey {
        @Contextual override val title: AndroidStringText = androidText(R.string.download_category_favorites)
    }
    @Serializable data class DownloadAssets(
        val platform: Platform,
        val projectId: String,
        val classes: PlatformClasses,
        val iconUrl: String? = null
    ) : NormalNavKey

    /** 综合统计屏幕 */
    @Serializable data object Stats : NormalNavKey {
        @Contextual override val title: AndroidStringText = androidText(R.string.page_title_stats)
    }

    /** 游戏统计屏幕 */
    @Serializable data object GameStats : NormalNavKey {
        @Contextual override val title: AndroidStringText = androidText(R.string.stats_game_stats)
    }

    /** 游戏时间统计屏幕 */
    @Serializable data object PlayTimeStats : NormalNavKey {
        @Contextual override val title: AndroidStringText = androidText(R.string.stats_play_time_title)
    }

    /** 协议展示屏幕 */
    @Serializable data class License(
        val raw: Int
    ): NormalNavKey

    /** 披风浏览屏幕 */
    @Serializable data class CapeGallery(
        val accountUUID: String
    ): NormalNavKey {
        @Contextual override val title: AndroidStringText = androidText(R.string.account_capes_labynet_title)
    }

    /** 录像管理屏幕 */
    @Serializable data object Recordings : NormalNavKey {
        @Contextual override val title: AndroidStringText = androidText(R.string.page_title_recordings)
    }
}
