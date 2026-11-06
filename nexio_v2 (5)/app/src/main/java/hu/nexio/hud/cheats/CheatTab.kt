package hu.nexio.hud.cheats

import androidx.compose.runtime.Composable
import hu.nexio.hud.cheats.ranked.RankedAssistantTab

/**
 * Egy hely a mod menü rácsában. Új eszköz hozzáadása: implementáld, majd vedd fel a CheatRegistry.tabs listába.
 * Page() = részletes nézet (párbeszédablak), Hud() = kompakt változat a lebegő panelen.
 */
interface CheatTab {
    val id: String
    val title: String

    /** Be van-e töltve / aktív (a gomb zölden jelzi). */
    val isOn: Boolean

    /** Töltés közben 0..1, különben null. */
    val progress: Float?

    /** Koppintás a rács gombján. openDetails megnyitja a Page() nézetet. */
    fun onTap(openDetails: () -> Unit)

    @Composable
    fun Page()

    @Composable
    fun Hud()
}

object CheatRegistry {
    /** Egyelőre csak a Ranked Assistant; a többi hely üres marad. */
    val tabs: List<CheatTab> = listOf(RankedAssistantTab)
    var selected: Int = 0
}
