package hu.nexio.hud.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import hu.nexio.hud.HudSettings
import hu.nexio.hud.OverlayController
import hu.nexio.hud.cheats.CheatRegistry
import hu.nexio.hud.cheats.CheatTab
import hu.nexio.hud.ui.components.ModButton
import hu.nexio.hud.ui.components.ModColors
import hu.nexio.hud.ui.components.OutlineText
import hu.nexio.hud.ui.components.RgbText
import hu.nexio.hud.ui.theme.NexioColors
import kotlin.math.roundToInt

private const val BRAWL_STARS_PACKAGE = "com.supercell.brawlstars"
private const val SLOTS_PER_PAGE = 12

/**
 * NEXIO MOD menü: fejléc (játékindítás, beállítások, infó, bezárás), 12 helyes rács,
 * alul Previous / Next Page, oldalszámláló és HUD kapcsoló. Az oldalak között lapozni lehet.
 */
@Composable
fun MainScreen() {
    val ctx = LocalContext.current
    val activity = ctx as ComponentActivity

    var canDraw by remember { mutableStateOf(OverlayController.canDraw(ctx)) }
    DisposableEffect(activity) {
        val obs = LifecycleEventObserver { _, e -> if (e == Lifecycle.Event.ON_RESUME) canDraw = OverlayController.canDraw(ctx) }
        activity.lifecycle.addObserver(obs)
        onDispose { activity.lifecycle.removeObserver(obs) }
    }

    val cheats = CheatRegistry.tabs
    val pageCount = maxOf(1, (cheats.size + SLOTS_PER_PAGE - 1) / SLOTS_PER_PAGE)
    var pageRaw by rememberSaveable { mutableIntStateOf(0) }
    val page = pageRaw.coerceIn(0, pageCount - 1)

    var showSettings by remember { mutableStateOf(false) }
    var showInfo by remember { mutableStateOf(false) }
    var details by remember { mutableStateOf<CheatTab?>(null) }

    Box(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(ModColors.BgTop, ModColors.BgBottom))),
        contentAlignment = Alignment.Center,
    ) {
        BoxWithConstraints(Modifier.widthIn(max = 560.dp).fillMaxWidth().padding(12.dp)) {
            val cols = if (maxWidth < 500.dp) 3 else 4
            val rows = SLOTS_PER_PAGE / cols
            val frame = RoundedCornerShape(10.dp)

            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(frame)
                    .border(3.dp, ModColors.Frame, frame)
                    .background(ModColors.Body)
                    .verticalScroll(rememberScrollState()),
            ) {
                // ---------- Fejléc ----------
                Row(
                    Modifier.fillMaxWidth().background(ModColors.Header).padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    ModButton(Modifier.size(width = 78.dp, height = 46.dp), onClick = { launchGame(ctx) }) {
                        OutlineText("▶ BRAWL\nSTARS", 10.sp)
                    }
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        OutlineText("NEXIO MOD", 22.sp, rgb = true)
                    }
                    ModButton(Modifier.size(42.dp), onClick = { showSettings = true }) { OutlineText("⚙", 18.sp) }
                    ModButton(Modifier.size(42.dp), onClick = { showInfo = true }) { OutlineText("i", 18.sp) }
                    ModButton(Modifier.size(width = 50.dp, height = 42.dp), face = ModColors.Red, edge = ModColors.RedEdge, onClick = { activity.finish() }) {
                        OutlineText("✕", 18.sp)
                    }
                }
                Box(Modifier.fillMaxWidth().height(4.dp).background(ModColors.Frame))

                Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // ---------- Engedély-figyelmeztetés ----------
                    if (!canDraw) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(NexioColors.Warn.copy(alpha = 0.9f))
                                .clickable { openOverlaySettings(ctx) }
                                .padding(10.dp),
                            contentAlignment = Alignment.Center,
                        ) { OutlineText("A HUD-hoz engedély kell – koppints ide", 12.sp) }
                    }

                    // ---------- Rács ----------
                    for (r in 0 until rows) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (c in 0 until cols) {
                                CheatCell(cheats.getOrNull(page * SLOTS_PER_PAGE + r * cols + c)) { details = it }
                            }
                        }
                    }

                    // ---------- Lábléc ----------
                    Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        ModButton(Modifier.weight(1f).height(52.dp), enabled = page > 0, onClick = { pageRaw = page - 1 }) {
                            OutlineText("Previous\nPage", 12.sp)
                        }
                        Column(Modifier.weight(1.3f), horizontalAlignment = Alignment.CenterHorizontally) {
                            OutlineText("Page ${page + 1}/$pageCount", 13.sp)
                            Spacer(Modifier.height(4.dp))
                            ModButton(
                                Modifier.fillMaxWidth().height(46.dp),
                                face = if (HudSettings.enabled) ModColors.OnFace else ModColors.Face,
                                edge = if (HudSettings.enabled) ModColors.OnEdge else ModColors.FaceEdge,
                                onClick = { toggleHud(ctx, canDraw) },
                            ) { OutlineText(if (HudSettings.enabled) "HUD: ON" else "HUD: OFF", 12.sp) }
                        }
                        ModButton(
                            Modifier.weight(1f).height(52.dp),
                            face = ModColors.Green, edge = ModColors.GreenEdge,
                            enabled = page < pageCount - 1,
                            onClick = { pageRaw = page + 1 },
                        ) { OutlineText("Next\nPage", 12.sp) }
                    }
                }
            }
        }
    }

    if (showSettings) SettingsDialog(canDraw = canDraw, onDismiss = { showSettings = false })
    if (showInfo) InfoDialog(onDismiss = { showInfo = false })
    details?.let { d ->
        AlertDialog(
            onDismissRequest = { details = null },
            containerColor = NexioColors.Background,
            text = { Column(Modifier.verticalScroll(rememberScrollState())) { d.Page() } },
            confirmButton = { TextButton(onClick = { details = null }) { Text("Bezár") } },
        )
    }
}

/** Egy rácshely: üres (halvány) vagy egy eszköz gombja. */
@Composable
private fun RowScope.CheatCell(cheat: CheatTab?, onDetails: (CheatTab) -> Unit) {
    if (cheat == null) {
        Box(
            Modifier
                .weight(1f)
                .height(62.dp)
                .padding(bottom = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.18f))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp)),
        )
        return
    }
    val on = cheat.isOn
    val p = cheat.progress
    ModButton(
        modifier = Modifier.weight(1f).height(62.dp),
        face = if (on) ModColors.OnFace else ModColors.Face,
        edge = if (on) ModColors.OnEdge else ModColors.FaceEdge,
        progress = p,
        onClick = { cheat.onTap { onDetails(cheat) } },
    ) {
        val label = cheat.title + when {
            p != null -> "\n${(p * 100).toInt()}%"
            on -> "\nON"
            else -> ""
        }
        OutlineText(label, 11.sp, rgb = true, modifier = Modifier.padding(horizontal = 4.dp))
    }
}

@Composable
private fun SettingsDialog(canDraw: Boolean, onDismiss: () -> Unit) {
    val ctx = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NexioColors.Background,
        title = { RgbText("BEÁLLÍTÁSOK", fontSize = 22.sp) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    if (canDraw) "Megjelenítés más alkalmazások fölött: engedélyezve ✓" else "Megjelenítés más alkalmazások fölött: kikapcsolva",
                    color = if (canDraw) NexioColors.Ok else NexioColors.Warn, fontSize = 12.sp,
                )
                if (!canDraw) TextButton(onClick = { openOverlaySettings(ctx) }) { Text("Engedély megadása") }
                Text("Átlátszóság: ${(HudSettings.opacity * 100).roundToInt()}%", color = NexioColors.TextSecondary, fontSize = 13.sp)
                Slider(value = HudSettings.opacity, onValueChange = { HudSettings.setOpacity(ctx, it) }, valueRange = 0.3f..1f)
                Text("Méret: ${(HudSettings.scale * 100).roundToInt()}%", color = NexioColors.TextSecondary, fontSize = 13.sp)
                Slider(value = HudSettings.scale, onValueChange = { HudSettings.setScale(ctx, it) }, valueRange = 0.6f..1.6f)
                Text("A lebegő HUD-ot a fejlécében lévő ⠿ fogóval húzhatod, a ▾ gombbal összecsukhatod.", color = NexioColors.TextSecondary, fontSize = 11.sp)
                TextButton(onClick = {
                    HudSettings.resetPosition(ctx)
                    if (HudSettings.enabled) { OverlayController.stop(ctx); OverlayController.start(ctx) }
                }) { Text("Pozíció visszaállítása") }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Bezár") } },
    )
}

@Composable
private fun InfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NexioColors.Background,
        title = { RgbText("NEXIO MOD  v2.1", fontSize = 20.sp) },
        text = {
            Text(
                "Külön alkalmazás: nem módosítja a Brawl Starst, nem olvas a memóriájából és nem automatizál semmit. " +
                    "A Ranked Assistant valódi adatokból dolgozik, és csak ellenőrzött forrásból ad ajánlást.",
                color = NexioColors.TextPrimary, fontSize = 13.sp,
            )
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Bezár") } },
    )
}

private fun toggleHud(ctx: Context, canDraw: Boolean) {
    if (HudSettings.enabled) {
        HudSettings.setEnabled(ctx, false)
        OverlayController.stop(ctx)
    } else if (!canDraw) {
        openOverlaySettings(ctx)
    } else {
        HudSettings.setEnabled(ctx, true)
        OverlayController.start(ctx)
    }
}

private fun launchGame(ctx: Context) {
    val i = ctx.packageManager.getLaunchIntentForPackage(BRAWL_STARS_PACKAGE)
    if (i != null) ctx.startActivity(i)
    else Toast.makeText(ctx, "A Brawl Stars nincs telepítve.", Toast.LENGTH_LONG).show()
}

private fun openOverlaySettings(ctx: Context) {
    ctx.startActivity(
        Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${ctx.packageName}"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
    )
}
