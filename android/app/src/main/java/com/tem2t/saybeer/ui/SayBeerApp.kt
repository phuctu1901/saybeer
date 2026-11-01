package com.tem2t.saybeer.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.tem2t.saybeer.data.*
import com.tem2t.saybeer.platform.*
import kotlinx.coroutines.delay
import java.text.DateFormat
import java.util.Date

fun timeLabel(millis: Long): String = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(millis))
fun dateLabel(millis: Long): String = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(millis))

@Composable fun SayBeerApp(repo: SessionRepository) {
    val state by repo.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    var route by rememberSaveable { mutableStateOf("home") }
    var pickerReturn by rememberSaveable { mutableStateOf("start") }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var enabled by remember { mutableStateOf(AppCatalog.accessibilityEnabled(context)) }
    var exact by remember { mutableStateOf(SessionAlarms.exactAllowed(context)) }
    var disclosure by rememberSaveable { mutableStateOf(false) }
    val screenStates = rememberSaveableStateHolder()
    var previousRoute by remember { mutableStateOf(route) }
    LaunchedEffect(route) {
        // Keep drafts only while choosing apps. Unlock flows always require a fresh commitment.
        if (previousRoute != route && !(route == "apps" && previousRoute in listOf("start", "schedule"))) {
            screenStates.removeState(previousRoute)
        }
        previousRoute = route
    }
    val notification = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(owner) {
        owner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (true) {
                now = System.currentTimeMillis(); repo.tick(now)
                enabled = AppCatalog.accessibilityEnabled(context)
                exact = SessionAlarms.exactAllowed(context)
                delay(500)
            }
        }
    }
    LaunchedEffect(state.session) {
        if (state.session == null && route in listOf("work", "test", "emergency")) route = "home"
    }
    BackHandler(route != "home") { route = if (route == "apps") pickerReturn else "home" }
    Box(Modifier.fillMaxSize().background(PageGradient).safeDrawingPadding(), contentAlignment = Alignment.TopCenter) {
        Box(Modifier.widthIn(max = 600.dp).fillMaxSize().padding(horizontal = 24.dp, vertical = 12.dp)
            .then(if (disclosure) Modifier.blur(7.dp) else Modifier)) {
            if (!state.onboarded) {
                Onboarding { repo.update { it.copy(onboarded = true) }; disclosure = true }
            } else screenStates.SaveableStateProvider(route) { when (route) {
                "home" -> Home(state, now, enabled, navigate = { route = it }, permission = { disclosure = true })
                "start" -> StartScreen(repo, state, enabled, exact, back = { route = "home" }, picker = { pickerReturn = "start"; route = "apps" }, permission = { disclosure = true },
                    exactPermission = { if (Build.VERSION.SDK_INT >= 31) context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}"))) },
                    started = { if (Build.VERSION.SDK_INT >= 33) notification.launch(Manifest.permission.POST_NOTIFICATIONS); route = "home" })
                "apps" -> AppPicker(repo, state, back = { route = pickerReturn })
                "work" -> WorkScreen(repo, state, now) { route = "home" }
                "test" -> TestScreen(repo, state, now) { route = "home" }
                "emergency" -> EmergencyScreen(repo) { route = "home" }
                "schedule" -> ScheduleScreen(repo, state, enabled, exact, { route = "home" }, { pickerReturn = "schedule"; route = "apps" }, { disclosure = true },
                    { if (Build.VERSION.SDK_INT >= 31) context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}"))) })
                "stats" -> StatsScreen(state) { route = "home" }
                "privacy" -> PrivacyScreen { route = "home" }
            } }
        }
    }
    if (disclosure) PermissionDisclosure(
        dismiss = { disclosure = false },
        accept = { disclosure = false; AppCatalog.openAccessibility(context) })
}

@Composable private fun PermissionDisclosure(dismiss: () -> Unit, accept: () -> Unit) {
    Dialog(onDismissRequest = dismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.widthIn(max = 430.dp).padding(16.dp), shape = RoundedCornerShape(30.dp),
            color = Color(0xFF181512), border = androidx.compose.foundation.BorderStroke(1.dp, Amber.copy(alpha = .25f))) {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Box(Modifier.size(36.dp, 4.dp).background(Color(0xFF4C4338), RoundedCornerShape(8.dp)))
                HeroIcon(Icons.Rounded.GppGood, size = 56.dp)
                Text("Cho phép SayBeer khóa app?", fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Text("SayBeer dùng dịch vụ Trợ năng để nhận tên app đang mở và phủ màn chắn lên các app bạn chọn khi phiên đang chạy.",
                    color = Color(0xFFC8BFB3), fontSize = 14.sp, lineHeight = 22.sp, textAlign = TextAlign.Center)
                GlassCard {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Rounded.CheckCircle, null, tint = Green, modifier = Modifier.size(20.dp))
                        Text("Không đọc nội dung màn hình, tin nhắn, mật khẩu hay thao tác gõ. Không gửi dữ liệu ra ngoài thiết bị.",
                            fontSize = 12.sp, lineHeight = 20.sp)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.Info, null, tint = Amber, modifier = Modifier.size(16.dp))
                    Note("Bật tại Cài đặt → Trợ năng → Ứng dụng đã cài đặt. Có thể tắt bất cứ lúc nào.")
                }
                PrimaryButton("Đồng ý · Mở Cài đặt", icon = Icons.AutoMirrored.Rounded.ArrowForward, onClick = accept)
                TextButton(onClick = dismiss, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Để sau", color = Muted) }
            }
        }
    }
}

@Composable private fun Onboarding(finish: () -> Unit) {
    var page by rememberSaveable { mutableIntStateOf(0) }
    val icons = listOf(Icons.Rounded.SportsBar, Icons.Rounded.Lock, Icons.Rounded.Shield)
    val titles = listOf("SayBeer", "Say có chừng.\nNhắn có lúc.", "Riêng tư trên máy bạn")
    val descriptions = listOf("Hãy say bia. Xin đừng “say” bậy.\n\nKhóa app nhắn tin trước khi đi, mở lại đúng giờ hẹn.",
        "Chọn app cần khóa và hẹn giờ mở.\n\nCần nhắn việc? Mở tạm 15 phút, tối đa 3 lần. Mở sớm cần qua bài kiểm tra; khẩn cấp thì giữ nút 5 giây.",
        "Không mạng. Không tài khoản. Không đọc tin nhắn.\n\nSayBeer chỉ cần biết tên app đang mở để chặn đúng app bạn chọn qua dịch vụ Trợ năng của Android.")
    BoxWithConstraints(Modifier.fillMaxSize()) {
    val availableHeight = maxHeight
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).heightIn(min = availableHeight), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(64.dp))
        HeroIcon(icons[page], color = if (page == 2) Green else Amber)
        Spacer(Modifier.height(28.dp))
        Text(titles[page], fontSize = 32.sp, lineHeight = 40.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(20.dp))
        Text(descriptions[page], color = Muted, textAlign = TextAlign.Center, lineHeight = 26.sp)
        Spacer(Modifier.weight(1f))
        Row(Modifier.padding(32.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) { Box(Modifier.size(if (page == it) 10.dp else 8.dp).background(if (page == it) Amber else Panel, androidx.compose.foundation.shape.CircleShape)) }
        }
        PrimaryButton(if (page < 2) "Tiếp tục" else "Thiết lập quyền & bắt đầu") { if (page < 2) page++ else finish() }
        if (page > 0) TextButton(onClick = { page-- }) { Text("Quay lại") } else Spacer(Modifier.height(48.dp))
    }
    }
}

@Composable private fun Home(state: BeerState, now: Long, enabled: Boolean, navigate: (String) -> Unit, permission: () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val height = maxHeight
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).heightIn(min = height), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("SAYBEER", color = Amber, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, modifier = Modifier.weight(1f))
                Box(Modifier.heightIn(min = 48.dp).clickable { navigate("privacy") }, contentAlignment = Alignment.Center) {
                    StatusPill("Riêng tư", Icons.Rounded.Shield)
                }
            }
            if (!enabled) Section("Cần thiết lập") {
                Text("Quyền khóa app chưa được bật", fontWeight = FontWeight.Bold)
                Note("Bật dịch vụ Trợ năng SayBeer để màn chắn hoạt động. Phiên đã lưu vẫn giữ nguyên.")
                SecondaryButton("Thiết lập quyền", onClick = permission)
            }
            Spacer(Modifier.weight(1f).heightIn(min = 24.dp))
            val session = state.session
            if (session == null) {
                Box(Modifier.size(112.dp).amberGlow(.3f), contentAlignment = Alignment.Center) {
                    Icon(painterResource(com.tem2t.saybeer.R.drawable.stitch_beer), null, Modifier.size(112.dp), tint = Color.Unspecified)
                }
                Text("SayBeer", fontSize = 40.sp, letterSpacing = (-1).sp, fontWeight = FontWeight.ExtraBold)
                Text(buildAnnotatedString {
                    append("Hãy say bia.\nXin đừng ")
                    withStyle(SpanStyle(color = Amber, fontWeight = FontWeight.Medium)) { append("“say”") }
                    append(" bậy.")
                }, textAlign = TextAlign.Center, color = Muted, fontSize = 19.sp, lineHeight = 30.sp)
                if (state.history.isNotEmpty()) TextButton(onClick = { navigate("stats") }) {
                    Icon(Icons.Rounded.LocalFireDepartment, null, tint = Amber)
                    Text(" ${state.survived} phiên an toàn · %.0f giờ đã khóa".format(state.hours))
                }
                if (state.schedule.enabled) Note("Tự khóa ${daySummary(state.schedule.days)} lúc ${minuteLabel(state.schedule.startMinute)}")
            } else {
                val window = session.windowEnd > now
                HeroIcon(if (window) Icons.Rounded.Work else Icons.Rounded.Lock)
                Text(if (window) "Cửa sổ nhắn việc đang mở" else "Đang khóa ${state.selected.size} app", fontWeight = FontWeight.Bold, fontSize = 23.sp, textAlign = TextAlign.Center)
                Text(remaining(if (window) session.windowEnd else session.end, now), color = Gold, fontSize = 46.sp, fontWeight = FontWeight.Bold,
                    style = LocalTextStyle.current.copy(shadow = Shadow(Amber.copy(alpha = .4f), blurRadius = 30f)),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                Text(if (window) "Nhắn việc xong thì thôi nhé. Không tâm sự." else "Tự mở khóa lúc ${timeLabel(session.end)}", color = Muted, textAlign = TextAlign.Center)
                Text("Thông báo và cuộc gọi vẫn hoạt động. SayBeer chỉ chặn mở các app bạn đã chọn.", color = Muted, fontSize = 13.sp, lineHeight = 22.sp, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.weight(1f).heightIn(min = 24.dp))
            if (session == null) {
                PrimaryButton("Bắt đầu chế độ nhậu", enabled, Icons.Rounded.LocalBar) { navigate("start") }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    HomeAction("Hẹn lịch", Icons.Rounded.CalendarMonth, Modifier.weight(1f)) { navigate("schedule") }
                    HomeAction("Thống kê", Icons.Rounded.BarChart, Modifier.weight(1f)) { navigate("stats") }
                }
            } else {
                if (session.windowEnd <= now) SecondaryButton("Cần nhắn việc · 15 phút (còn ${MAX_WINDOWS - session.windowsUsed} lần)", session.windowsUsed < MAX_WINDOWS) { navigate("work") }
                SecondaryButton("Tỉnh rồi — mở khóa hẳn") { navigate("test") }
                TextButton(onClick = { navigate("emergency") }) { Text("SOS  Khẩn cấp", color = Red) }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable private fun HomeAction(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, click: () -> Unit) {
    OutlinedButton(onClick = click, modifier = modifier.heightIn(min = 64.dp).background(CardGradient, RoundedCornerShape(14.dp)),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder), shape = RoundedCornerShape(14.dp), contentPadding = PaddingValues(10.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE0D5C7))) {
        Box(Modifier.size(32.dp).background(Amber.copy(alpha = .1f), RoundedCornerShape(9.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, null, Modifier.size(18.dp), tint = Amber)
        }
        Spacer(Modifier.width(9.dp))
        Text(label, fontSize = 13.sp)
    }
}

@Composable private fun PrivacyScreen(back: () -> Unit) {
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    Screen("Quyền & riêng tư", back) {
        HeroIcon(Icons.Rounded.Shield, color = Green)
        SecondaryButton("Chính sách quyền riêng tư đầy đủ") {
            uriHandler.openUri("https://saybeer.1labs.space/android-privacy.html")
        }
        Section("Dữ liệu của bạn") {
            Text("Mọi thứ nằm trên thiết bị", fontWeight = FontWeight.Bold)
            Note("SayBeer không có tài khoản, quảng cáo, phân tích hành vi hay quyền Internet. Danh sách app, phiên khóa và tối đa 200 phiên lịch sử được lưu riêng trên máy, không sao lưu đám mây.")
        }
        Section("Trợ năng") {
            Note("Dịch vụ chỉ nhận tên gói ứng dụng khi cửa sổ thay đổi để hiển thị màn chắn. Không đọc cây nội dung màn hình, tin nhắn, mật khẩu hoặc phím gõ. Không tự thao tác trong app khác.")
        }
        Section("Giới hạn Android") {
            Note("SayBeer là công cụ tự cam kết. Bạn vẫn có thể tắt Trợ năng, buộc dừng hoặc gỡ app trong Cài đặt. Android không cho app thông thường cấm gỡ cài đặt. Không chặn ứng dụng Điện thoại, Cài đặt và màn hình chính.")
            Note("Nếu chưa cấp quyền báo thức chính xác, lịch nền có thể trễ do tiết kiệm pin. Khi Trợ năng hoạt động, SayBeer kiểm tra thời gian mỗi giây. Sau khi buộc dừng app, cần mở lại để phục hồi lịch.")
        }
        Note("Không dùng bài kiểm tra này để đánh giá khả năng lái xe hoặc nồng độ cồn. Đã uống thì đừng lái xe.")
    }
}
