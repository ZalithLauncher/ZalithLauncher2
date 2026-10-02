@Composable
fun MiraiPackPage(instanceName: String?, onPack: () -> Unit, onBack: () -> Unit, modifier: Modifier = Modifier) {
    var packName by remember { mutableStateOf(instanceName ?: "") }
    Page("Export", onBack, modifier) {
        Text(instanceName ?: "Select an instance on Play first.", color = Muted)
        if (instanceName != null) {
            Field(packName, { packName = it }, "Pack name")
            Text(if (packName.isBlank()) "Name the pack, then export it." else "Pack as $packName", color = Muted)
            GreenButton("Pack instance", onPack)
        }
    }
}