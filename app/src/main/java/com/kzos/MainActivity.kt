package com.kzos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { KzOsApp() }
    }
}

private enum class KzApp(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    COMPUTER("Meu Computador", Icons.Default.Computer), FILES("Arquivos", Icons.Default.Folder),
    NOTES("Bloco de notas", Icons.Default.Edit), CALCULATOR("Calculadora", Icons.Default.Calculate),
    SETTINGS("Configurações", Icons.Default.Settings), TERMINAL("Terminal", Icons.Default.Terminal),
    TASKS("Desempenho", Icons.Default.Speed), BROWSER("Navegador", Icons.Default.Language)
}
private data class DesktopWindow(val id: Int, val app: KzApp, val x: Int, val y: Int, val minimized: Boolean = false)

@Composable
private fun KzOsApp() {
    val context = LocalContext.current
    val fileSystem = remember { KzFileSystem(context) }
    val prefs = remember { context.getSharedPreferences("kzos", 0) }
    var dark by remember { mutableStateOf(prefs.getBoolean("dark", true)) }
    var wallpaper by remember { mutableIntStateOf(prefs.getInt("wallpaper", 0)) }
    var startOpen by remember { mutableStateOf(false) }
    var nextId by remember { mutableIntStateOf(1) }
    val windows = remember { mutableStateListOf<DesktopWindow>() }
    val colors = if (dark) listOf(Color(0xFF101A35), Color(0xFF254F78), Color(0xFF16233F))
                 else listOf(Color(0xFF8CC7E8), Color(0xFFDAEAF4), Color(0xFF779EC1))
    val backgrounds = listOf(
        listOf(Color(0xFF101A35), Color(0xFF254F78), Color(0xFF16233F)),
        listOf(Color(0xFF30234F), Color(0xFF8C4B78), Color(0xFF242044)),
        listOf(Color(0xFF174A46), Color(0xFF4F9B8B), Color(0xFF18304B))
    )
    val ink = if (dark) Color(0xFFF4F7FC) else Color(0xFF182235)
    MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
        BoxWithConstraints(Modifier.fillMaxSize().background(Brush.linearGradient(backgrounds[wallpaper]))) {
            val maxHeightPx = constraints.maxHeight
            Column(Modifier.fillMaxSize().padding(bottom = 62.dp)) {
                Row(Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    listOf(KzApp.COMPUTER, KzApp.FILES, KzApp.NOTES, KzApp.CALCULATOR).forEach { app ->
                        Column(Modifier.width(78.dp).clip(RoundedCornerShape(14.dp)).clickable {
                            windows.add(DesktopWindow(nextId++, app, 28 + windows.size * 18, 30 + windows.size * 18))
                        }.padding(9.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(app.icon, null, tint = ink, modifier = Modifier.size(32.dp))
                            Text(app.label, color = ink, fontSize = 11.sp, lineHeight = 14.sp)
                        }
                    }
                }
                windows.forEachIndexed { index, window ->
                    if (!window.minimized) {
                        val windowWidth = (maxWidth - 28.dp).coerceAtMost(560.dp)
                        Surface(
                            modifier = Modifier.offset { IntOffset(window.x, window.y) }.width(windowWidth)
                                .heightIn(min = 260.dp, max = (maxHeight - 100.dp).coerceAtLeast(300.dp))
                                .pointerInput(window.id) {
                                    detectDragGestures { change, drag ->
                                        change.consume()
                                        val updated = windows[index]
                                        windows[index] = updated.copy(x = updated.x + drag.x.roundToInt(), y = (updated.y + drag.y.roundToInt()).coerceAtLeast(0))
                                    }
                                },
                            shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                            shadowElevation = 18.dp
                        ) {
                            Column {
                                Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .7f)).padding(start = 16.dp, end = 8.dp, top = 7.dp, bottom = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(window.app.icon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                    Text(window.app.label, Modifier.weight(1f).padding(start = 8.dp), fontWeight = FontWeight.SemiBold)
                                    IconButton(onClick = { windows[index] = window.copy(minimized = true) }) { Icon(Icons.Default.Remove, "Minimizar") }
                                    IconButton(onClick = { windows.removeAt(index) }) { Icon(Icons.Default.Close, "Fechar") }
                                }
                                AppContent(window.app, fileSystem, ink = MaterialTheme.colorScheme.onSurface,
                                    onTheme = { dark = it; prefs.edit().putBoolean("dark", it).apply() },
                                    dark = dark, wallpaper = wallpaper, onWallpaper = { wallpaper = it; prefs.edit().putInt("wallpaper", it).apply() })
                            }
                        }
                    }
                }
            }
            if (startOpen) {
                Surface(Modifier.align(Alignment.BottomCenter).padding(bottom = 70.dp).widthIn(max = 420.dp).fillMaxWidth(.92f), shape = RoundedCornerShape(24.dp), shadowElevation = 20.dp, color = MaterialTheme.colorScheme.surface.copy(alpha = .97f)) {
                    Column(Modifier.padding(18.dp)) {
                        Text("KZ OS", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text("Aplicativos", Modifier.padding(top = 12.dp, bottom = 8.dp), color = MaterialTheme.colorScheme.primary)
                        KzApp.entries.chunked(2).forEach { row ->
                            Row(Modifier.fillMaxWidth()) { row.forEach { app ->
                                TextButton(onClick = { windows.add(DesktopWindow(nextId++, app, 24 + windows.size * 16, 24 + windows.size * 16)); startOpen = false }, modifier = Modifier.weight(1f)) {
                                    Icon(app.icon, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text(app.label, fontSize = 12.sp)
                                }
                            } }
                        }
                    }
                }
            }
            Taskbar(Modifier.align(Alignment.BottomCenter), dark, windows, onStart = { startOpen = !startOpen }, onApp = { app ->
                val minimized = windows.indexOfLast { it.app == app && it.minimized }
                if (minimized >= 0) windows[minimized] = windows[minimized].copy(minimized = false)
                else windows.add(DesktopWindow(nextId++, app, 30 + windows.size * 14, 30 + windows.size * 14))
            })
            if (windows.isEmpty()) {
                Text("KZ OS  •  seu espaço, do seu jeito", Modifier.align(Alignment.BottomStart).padding(start = 18.dp, bottom = 76.dp), color = ink.copy(alpha = .8f), fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun Taskbar(modifier: Modifier, dark: Boolean, windows: List<DesktopWindow>, onStart: () -> Unit, onApp: (KzApp) -> Unit) {
    var time by remember { mutableStateOf("--:--") }
    LaunchedEffect(Unit) { while (true) { time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()); delay(15_000) } }
    Surface(modifier.fillMaxWidth().height(62.dp), color = if (dark) Color(0xE6202B3F) else Color(0xEAF4F8FC), shadowElevation = 12.dp) {
        Row(Modifier.fillMaxSize().padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onStart, modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFF3B82F6))) {
                Text("KZ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            KzApp.entries.take(6).forEach { app ->
                IconButton(onClick = { onApp(app) }, modifier = Modifier.padding(horizontal = 1.dp)) { Icon(app.icon, app.label, modifier = Modifier.size(21.dp)) }
            }
            Spacer(Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                Text(time, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text(SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date()), fontSize = 10.sp)
            }
            Spacer(Modifier.width(8.dp))
        }
    }
}

@Composable
private fun AppContent(app: KzApp, fs: KzFileSystem, ink: Color, onTheme: (Boolean) -> Unit, dark: Boolean, wallpaper: Int, onWallpaper: (Int) -> Unit) {
    when (app) {
        KzApp.NOTES -> NotesApp(fs)
        KzApp.CALCULATOR -> CalculatorApp()
        KzApp.FILES -> FilesApp(fs)
        KzApp.SETTINGS -> SettingsApp(dark, onTheme, wallpaper, onWallpaper)
        KzApp.TERMINAL -> TerminalApp(fs)
        KzApp.COMPUTER -> ComputerApp(fs)
        KzApp.TASKS -> PerformanceApp(fs)
        KzApp.BROWSER -> BrowserApp()
    }
}

@Composable
private fun NotesApp(fs: KzFileSystem) {
    var text by remember { mutableStateOf(fs.readText("notes.txt")) }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Anotações salvas neste aparelho", style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(8.dp))
        BasicTextField(text, { text = it; fs.writeText("notes.txt", it) }, Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)).padding(12.dp), decorationBox = { inner -> if (text.isEmpty()) Text("Comece a escrever…", color = MaterialTheme.colorScheme.onSurfaceVariant); inner() })
    }
}

@Composable
private fun CalculatorApp() {
    var expression by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(expression.ifBlank { "0" }, Modifier.fillMaxWidth().padding(8.dp), fontSize = 25.sp, maxLines = 1)
        Text(result, Modifier.fillMaxWidth().padding(horizontal = 8.dp), color = MaterialTheme.colorScheme.primary, fontSize = 18.sp)
        listOf(listOf("7", "8", "9", "÷"), listOf("4", "5", "6", "×"), listOf("1", "2", "3", "−"), listOf("C", "0", ".", "+"), listOf("(", ")", "⌫", "=" )).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) { row.forEach { key ->
                Button(onClick = {
                    when (key) {
                        "C" -> { expression = ""; result = "" }
                        "⌫" -> expression = expression.dropLast(1)
                        "=" -> result = runCatching { CalculatorEngine.evaluate(expression) }.fold({ it.toString() }, { "Expressão inválida" })
                        else -> expression += when (key) { "÷" -> "/"; "×" -> "*"; "−" -> "-"; else -> key }
                    }
                }, modifier = Modifier.weight(1f), contentPadding = PaddingValues(vertical = 10.dp)) { Text(key) }
            } }
        }
    }
}

@Composable
private fun FilesApp(fs: KzFileSystem) {
    var refresh by remember { mutableIntStateOf(0) }
    var newName by remember { mutableStateOf("") }
    var createFolder by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(14.dp)) {
        Text("Armazenamento local • ${fs.usedBytes() / 1024} KB", style = MaterialTheme.typography.labelMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(newName, { newName = it }, Modifier.weight(1f), label = { Text("Nome do item") }, singleLine = true)
            IconButton(onClick = { createFolder = true }) { Icon(Icons.Default.CreateNewFolder, "Criar pasta") }
            IconButton(onClick = { if (newName.isNotBlank()) { fs.writeText(newName, ""); newName = ""; refresh++ } }) { Icon(Icons.Default.NoteAdd, "Criar arquivo") }
        }
        if (message.isNotBlank()) Text(message, color = MaterialTheme.colorScheme.primary)
        LazyColumn {
            items(fs.listItems().also { refresh }) { item ->
                Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (item.isDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile, null, tint = MaterialTheme.colorScheme.primary)
                    Text(item.name, Modifier.weight(1f).padding(start = 10.dp))
                    IconButton(onClick = { fs.rename(item.name, "${item.name}.renamed"); refresh++ }) { Icon(Icons.Default.DriveFileRenameOutline, "Renomear") }
                    IconButton(onClick = { fs.delete(item.name); refresh++ }) { Icon(Icons.Default.Delete, "Excluir") }
                }
            }
        }
    }
    if (createFolder) {
        AlertDialog(onDismissRequest = { createFolder = false }, title = { Text("Nova pasta") }, text = { OutlinedTextField(newName, { newName = it }, label = { Text("Nome") }) },
            confirmButton = { TextButton(onClick = { if (newName.isNotBlank()) { message = fs.createFolder(newName); newName = ""; refresh++ }; createFolder = false }) { Text("Criar") } },
            dismissButton = { TextButton(onClick = { createFolder = false }) { Text("Cancelar") } })
    }
}

@Composable
private fun SettingsApp(dark: Boolean, onTheme: (Boolean) -> Unit, wallpaper: Int, onWallpaper: (Int) -> Unit) {
    Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Personalização", style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically) { Text("Tema escuro", Modifier.weight(1f)); Switch(dark, onTheme) }
        Text("Papel de parede")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf(Color(0xFF254F78), Color(0xFF8C4B78), Color(0xFF4F9B8B)).forEachIndexed { index, color ->
            Box(Modifier.size(54.dp).clip(RoundedCornerShape(12.dp)).background(color).clickable { onWallpaper(index) }, contentAlignment = Alignment.Center) { if (wallpaper == index) Icon(Icons.Default.Check, null, tint = Color.White) }
        } }
        HorizontalDivider()
        Text("KZ OS 0.1.0 • Android ${android.os.Build.VERSION.RELEASE}")
        Text("Ambiente desktop e hardware são simulados dentro do Android.", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun TerminalApp(fs: KzFileSystem) {
    var command by remember { mutableStateOf("") }
    var output by remember { mutableStateOf("KZ Terminal 0.1\nDigite help para ver os comandos.\n") }
    Column(Modifier.fillMaxSize().background(Color(0xFF10151D)).padding(14.dp)) {
        Text(output, color = Color(0xFFB6F2C2), fontSize = 13.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("kz> ", color = Color(0xFF75D78C))
            BasicTextField(command, { command = it }, Modifier.weight(1f), textStyle = androidx.compose.ui.text.TextStyle(color = Color.White), singleLine = true)
            TextButton(onClick = {
                val response = when {
                    command == "help" -> "help  ls  pwd  clear  neofetch"
                    command == "ls" -> fs.listItems().joinToString("\n") { it.name }.ifBlank { "(vazio)" }
                    command == "pwd" -> "/KZ-OS"
                    command == "clear" -> ""
                    command == "neofetch" -> "KZ OS 0.1 • Android ${android.os.Build.VERSION.RELEASE}\nVirtual desktop environment"
                    command.startsWith("echo ") -> command.removePrefix("echo ")
                    else -> "Comando não encontrado: $command"
                }
                output = if (command == "clear") "" else "$output\nkz> $command\n$response\n"
                command = ""
            }) { Text("Executar", color = Color(0xFF75D78C)) }
        }
    }
}

@Composable
private fun ComputerApp(fs: KzFileSystem) {
    Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Este dispositivo", style = MaterialTheme.typography.titleLarge)
        InfoRow("Sistema", "KZ OS 0.1.0 • Android ${android.os.Build.VERSION.RELEASE}")
        InfoRow("Processador", "${Runtime.getRuntime().availableProcessors()} núcleos lógicos (host)")
        InfoRow("Memória", "${Runtime.getRuntime().maxMemory() / 1024 / 1024} MB heap máximo do app")
        InfoRow("Armazenamento KZ", "${fs.usedBytes() / 1024} KB usados")
        Text("CPU, RAM e armazenamento do KZ OS são leituras/simulações do app; não representam virtualização de hardware.", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun PerformanceApp(fs: KzFileSystem) {
    var pulse by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { while (true) { pulse = (pulse + 17) % 100; delay(1000) } }
    Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Monitor do sistema", style = MaterialTheme.typography.titleLarge)
        InfoRow("CPU do dispositivo", "${Runtime.getRuntime().availableProcessors()} núcleos disponíveis • amostra ${pulse}% (simulada)")
        InfoRow("Heap máximo do app", "${Runtime.getRuntime().maxMemory() / 1024 / 1024} MB")
        InfoRow("Arquivos KZ", "${fs.listItems().size} itens • ${fs.usedBytes() / 1024} KB")
        LinearProgressIndicator(progress = { pulse / 100f }, Modifier.fillMaxWidth())
        Text("Métricas didáticas do ambiente KZ; GPU e SSD virtuais ainda não são implementados.", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun BrowserApp() {
    var url by remember { mutableStateOf("https://www.google.com") }
    Column(Modifier.fillMaxSize().padding(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(url, { url = it }, Modifier.weight(1f), singleLine = true, label = { Text("Endereço web") })
            IconButton(onClick = { if (!url.startsWith("http")) url = "https://$url" }) { Icon(Icons.Default.ArrowForward, "Abrir") }
        }
        Spacer(Modifier.height(8.dp))
        Text("O navegador abre páginas online e requer conexão. O restante do KZ OS funciona localmente.", style = MaterialTheme.typography.bodySmall)
        TextButton(onClick = { url = "https://www.google.com" }) { Text("Usar pesquisa Google") }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column { Text(label, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium); Text(value, style = MaterialTheme.typography.bodyMedium) }
}
