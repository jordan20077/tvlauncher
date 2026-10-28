package com.example.tvlauncher

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppItem(val pkg: String, val label: String, val intent: Intent, val image: Bitmap)

val FAVORITES = listOf(
    "com.netflix.ninja",
    "com.amazon.amazonvideo.livingroom",
    "com.amazon.firetv.youtube",
    "com.google.android.youtube.tv",
    "com.disney.disneyplus",
    "com.wbd.stream",
    "com.apple.atve.amazon.appletv",
    "com.spotify.tv.android",
    "com.plexapp.android",
    "org.videolan.vlc"
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pm = packageManager
        val main = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        val all = pm.queryIntentActivities(main, 0)
            .filter { it.activityInfo.packageName != packageName }
            .map { ri ->
                val p = ri.activityInfo.packageName
                val d = ri.activityInfo.loadBanner(pm) ?: ri.loadIcon(pm)
                AppItem(
                    p, ri.loadLabel(pm).toString(),
                    Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
                        .setClassName(p, ri.activityInfo.name).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    d.toBitmap()
                )
            }
        val feat = FAVORITES.mapNotNull { f -> all.firstOrNull { it.pkg == f } }
        val rest = all.filter { it !in feat }.sortedBy { it.label.lowercase() }
        setContent { Home(feat, rest) { startActivity(it.intent) } }
    }
}

@Composable
fun Home(feat: List<AppItem>, rest: List<AppItem>, open: (AppItem) -> Unit) {
    val bg = Brush.verticalGradient(listOf(Color(0xFF1A0033), Color(0xFF05010A)))
    Column(Modifier.fillMaxSize().background(bg).padding(horizontal = 48.dp, vertical = 32.dp)) {
        Clock()
        Spacer(Modifier.height(16.dp))
        if (feat.isNotEmpty()) { Section("Favorites"); AppRow(feat, 260.dp, open) }
        Section("All apps"); AppRow(rest, 190.dp, open)
    }
}

@Composable
fun Clock() {
    val fmt = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val day = remember { SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()) }
    val now by produceState(Date()) { while (true) { value = Date(); delay(15_000) } }
    Label(fmt.format(now), 64.sp, FontWeight.Bold, 1f)
    Label(day.format(now), 20.sp, FontWeight.Normal, 0.6f)
}

@Composable
fun Section(t: String) = Label(t, 22.sp, FontWeight.SemiBold, 0.85f, Modifier.padding(top = 20.dp))

@Composable
fun Label(s: String, size: TextUnit, w: FontWeight, a: Float, m: Modifier = Modifier) =
    androidx.compose.foundation.text.BasicText(
        s, m, androidx.compose.ui.text.TextStyle(Color.White.copy(alpha = a), size, w)
    )

@Composable
fun AppRow(list: List<AppItem>, width: Dp, open: (AppItem) -> Unit) =
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 22.dp)
    ) { items(list, key = { it.pkg }) { Tile(it, width, open) } }

@Composable
fun Tile(item: AppItem, width: Dp, open: (AppItem) -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val s by animateFloatAsState(if (focused) 1.14f else 1f, label = "scale")
    val shape = RoundedCornerShape(20.dp)
    Column(Modifier.width(width), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.fillMaxWidth().aspectRatio(16f / 9f).scale(s)
                .border(if (focused) 3.dp else 0.dp, Color.White, shape)
                .clip(shape).background(Color(0xFF241638))
                .onFocusChanged { focused = it.isFocused }
                .clickable { open(item) },
            contentAlignment = Alignment.Center
        ) {
            Image(item.image.asImageBitmap(), item.label, Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
        }
        Spacer(Modifier.height(12.dp))
        Label(item.label, 16.sp, FontWeight.Medium, if (focused) 1f else 0.5f)
    }
}
