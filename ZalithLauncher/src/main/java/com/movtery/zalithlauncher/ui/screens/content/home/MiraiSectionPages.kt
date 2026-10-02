@Composable
fun MiraiSettingsPage(onRenderer: () -> Unit, onGame: () -> Unit, onControls: () -> Unit, onGamepad: () -> Unit, onLauncher: () -> Unit, onJava: () -> Unit, onControlLayouts: () -> Unit, onAccounts: () -> Unit, onAbout: () -> Unit, onExport: () -> Unit = {}, onSkins: () -> Unit = {}, onFiles: () -> Unit = {}, onLogs: () -> Unit = {}, onWeb: () -> Unit = {}, onTutorial: () -> Unit = {}, modifier: Modifier = Modifier) {
    SectionPage("Settings", "Launcher, game, controls, and accounts.", listOf(
        SectionAction("Renderer", "Resolution, renderer, and RAM", onRenderer),
        SectionAction("Game", "Game options", onGame),
        SectionAction("Controls", "Touch controls", onControls),
        SectionAction("Control layouts", "Manage control layouts", onControlLayouts),
        SectionAction("Gamepad", "Controller options", onGamepad),
        SectionAction("Launcher", "Launcher behavior", onLauncher),
        SectionAction("Java", "Java runtime", onJava),
        SectionAction("Accounts", "Offline and Microsoft login", onAccounts),
        SectionAction("Export", "Export the selected instance", onExport),
        SectionAction("Licenses", "Open source licenses", onAbout),
        SectionAction("Skins", "Browse and equip a skin", onSkins),
        SectionAction("Files", "Launcher files", onFiles),
        SectionAction("Logs", "Latest game log", onLogs),
        SectionAction("Web", "Open a page in the browser", onWeb),
        SectionAction("Tutorial", "How this launcher is laid out", onTutorial),
        SectionAction("About", "Version and licenses", onAbout)
    ), modifier)
}