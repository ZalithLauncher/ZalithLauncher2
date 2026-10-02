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
    @Serializable data class LogView(
        val logPath: String
    ) : NormalNavKey {
        @Contextual override val title: AndroidStringText = androidText(R.string.versions_overview_log)
    }
    sealed interface Settings : NormalNavKey {
        @Serializable data object Renderer : Settings {
            @Contextual override val title: AndroidStringText = androidText(R.string.settings_tab_renderer)
        }
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
    }
    sealed interface Versions : NormalNavKey {
        @Serializable data object OverView : Versions {
            @Contextual override val title: AndroidStringText = androidText(R.string.versions_settings_overview)
        }
        @Serializable data object Config : Versions {
            @Contextual override val title: AndroidStringText = androidText(R.string.versions_settings_config)
        }
        @Serializable data object UpdateLoader : Versions {
            @Contextual override var title: AndroidStringText = androidText(R.string.versions_update_loader)
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
    @Serializable data class License(
        val raw: Int
    ): NormalNavKey
}