package com.tem2t.saybeer.ui

import android.app.TimePickerDialog
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.tem2t.saybeer.data.*
import com.tem2t.saybeer.platform.AppCatalog
import com.tem2t.saybeer.platform.InstalledApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId

@Composable fun StartScreen(repo: SessionRepository, state: BeerState, enabled: Boolean, exact: Boolean, back: () -> Unit, picker: () -> Unit,
                            permission: () -> Unit, exactPermission: () -> Unit, started: () -> Unit) {
    var mode by rememberSaveable { mutableIntStateOf(0) }
    var customMinutes by rememberSaveable { mutableStateOf("360") }
    val end = when(mode) {
        0 -> {
            val now = java.time.ZonedDateTime.now()
            val six = LocalDate.now().atTime(6, 0).atZone(ZoneId.systemDefault())
            (if (six > now) six else six.plusDays(1)).toInstant().toEpochMilli()
        }
        1 -> System.currentTimeMillis() + 240 * MINUTE
        2 -> System.currentTimeMillis() + 480 * MINUTE
        else -> System.currentTimeMillis() + (customMinutes.toLongOrNull()?.coerceIn(0, 1440) ?: 0) * MINUTE
    }
    Screen("Chế độ nhậu", back, footer = {
        PrimaryButton("Kích hoạt chế độ nhậu", state.selected.isNotEmpty() && enabled && (mode != 3 || (customMinutes.toIntOrNull() ?: 0) in 30..1440), Icons.Rounded.Lock) {
            repo.start(end)
            if (repo.current.session != null) started()
        }
    }) {
        SelectionSection("App cần khóa", state, picker)
        Section("Khóa đến") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("6h sáng", "+4 giờ", "+8 giờ").forEachIndexed { index, text ->
                    ChoiceTile(text, mode == index, Modifier.weight(1f)) { mode = index }
                }
            }
            OutlinedButton(onClick = { mode = 3 }, shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Rounded.Schedule, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Tùy chọn thời lượng", color = if (mode == 3) Gold else Muted)
            }
            if (mode == 3) OutlinedTextField(customMinutes, { customMinutes = it.filter(Char::isDigit).take(4) }, label = { Text("Số phút (30–1440)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), singleLine = true)
            HorizontalDivider(color = CardBorder)
            Text("●  Mở khóa lúc ${dateLabel(end)}", color = Gold, fontSize = 12.sp, lineHeight = 18.sp)
        }
        Section("Bài kiểm tra mở sớm") {
            ToggleRow("Kèm tính nhẩm", state.math) { value -> repo.update { it.copy(math = value) } }
            Note("Gõ đúng câu líu lưỡi luôn là bắt buộc. Sai 3 lần phải chờ 5 phút trước khi thử lại.")
        }
        PermissionNotice(enabled, exact, permission, exactPermission)
        Note("Android không hỗ trợ chặn gỡ cài đặt bằng app thông thường. Hãy giữ Trợ năng hoạt động để SayBeer bảo vệ phiên của bạn.")
        Note("Trong lúc khóa vẫn mở tạm được 15 phút để nhắn việc, tối đa 3 lần. Phiên và bộ đếm được giữ khi đóng app.")
        Spacer(Modifier.height(12.dp))
    }
}

@Composable private fun SelectionSection(label: String, state: BeerState, picker: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(label.uppercase(), color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        GlassCard(Modifier.clickable(onClick = picker)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(42.dp).background(Amber.copy(alpha = .12f), RoundedCornerShape(12.dp))
                    .border(1.dp, Amber.copy(alpha = .2f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Apps, null, tint = Gold)
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Chọn app cần khóa", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Note(if (state.selected.isEmpty()) "Chọn ứng dụng bạn muốn giữ khoảng cách" else "${state.selected.size} ứng dụng đang chọn")
                }
                Icon(Icons.Rounded.ChevronRight, null, tint = Amber)
            }
        }
    }
}

@Composable private fun ChoiceTile(label: String, selected: Boolean, modifier: Modifier = Modifier, click: () -> Unit) {
    Box(modifier.heightIn(min = 48.dp).background(if (selected) ActionGradient else CardGradient, RoundedCornerShape(12.dp))
        .border(1.dp, if (selected) Amber else CardBorder, RoundedCornerShape(12.dp))
        .clip(RoundedCornerShape(12.dp)).selectable(selected, onClick = click, role = Role.Button).padding(horizontal = 8.dp, vertical = 12.dp), contentAlignment = Alignment.Center) {
        Text(label, fontSize = 13.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, color = if (selected) Ink else Muted)
    }
}

@Composable private fun PermissionNotice(enabled: Boolean, exact: Boolean, permission: () -> Unit, exactPermission: () -> Unit) {
    if (!enabled || !exact) Section("Quyền hoạt động") {
        if (!enabled) { Note("Cần bật Trợ năng để khóa các app đã chọn."); SecondaryButton("Bật Trợ năng", onClick = permission) }
        if (!exact) { Note("Cấp quyền báo thức để lịch nền bắt đầu đúng giờ. Nếu chưa cấp, Android có thể trì hoãn lịch."); SecondaryButton("Cho phép báo thức chính xác", onClick = exactPermission) }
    }
}

@Composable fun AppPicker(repo: SessionRepository, state: BeerState, back: () -> Unit) {
    val context = LocalContext.current
    var apps by remember { mutableStateOf<List<InstalledApp>?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    var selected by rememberSaveable { mutableStateOf<List<String>>(ArrayList(state.selected)) }
    val owner = LocalLifecycleOwner.current
    LaunchedEffect(owner) {
        owner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            apps = withContext(Dispatchers.IO) { AppCatalog.load(context) }
        }
    }
    fun save() { repo.update { if (it.session == null) it.copy(selected = selected.toSet()) else it }; back() }
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = back) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Hủy") }
            Text("App cần khóa", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            TextButton(onClick = { save() }) { Text("Lưu (${selected.size})") }
        }
        OutlinedTextField(query, { query = it }, placeholder = { Text("Tìm tên ứng dụng") }, leadingIcon = { Icon(Icons.Rounded.Search, null) },
            modifier = Modifier.fillMaxWidth(), singleLine = true)
        Note("Điện thoại mặc định, Cài đặt và màn hình chính luôn được giữ mở. App nhắn tin có chức năng gọi như Zalo vẫn có thể chọn khóa.")
        val list = apps
        when {
            list == null -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            list.isEmpty() -> Text("Chưa tìm thấy ứng dụng có thể chọn trên thiết bị này.")
            else -> {
                val visible = list.filter { it.label.contains(query, true) || it.packageName.contains(query, true) }
                if (visible.isEmpty()) Note("Không có ứng dụng phù hợp. Kiểm tra app đã được cài trong cùng hồ sơ Android với SayBeer; app trong Hồ sơ công việc hoặc Không gian riêng có thể không xuất hiện ở đây.")
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    items(visible, key = { it.packageName }) { app ->
                        val checked = app.packageName in selected
                        Row(Modifier.fillMaxWidth().clickable { selected = ArrayList(selected.toSet().let { if (checked) it - app.packageName else it + app.packageName }) }.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            AppIcon(app.packageName)
                            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                                Text(app.label, fontWeight = FontWeight.Medium)
                                Text(app.packageName, style = MaterialTheme.typography.labelSmall, color = Muted, maxLines = 1)
                            }
                            Checkbox(checked, null)
                        }
                    }
                }
                val missing = selected.toSet() - list.map { it.packageName }.toSet()
                if (missing.isNotEmpty()) TextButton(onClick = { selected = ArrayList(selected - missing) }) { Text("Bỏ ${missing.size} app không còn cài đặt") }
            }
        }
        PrimaryButton("Lưu lựa chọn (${selected.size})", onClick = { save() })
    }
}

@Composable private fun AppIcon(pkg: String) {
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(null, pkg) {
        value = withContext(Dispatchers.IO) { runCatching {
            val drawable: Drawable = context.packageManager.getApplicationIcon(pkg)
            Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888).also { drawable.setBounds(0, 0, 96, 96); drawable.draw(Canvas(it)) }
        }.getOrNull() }
    }
    bitmap?.let { Image(it.asImageBitmap(), null, Modifier.size(42.dp)) }
        ?: Icon(Icons.Rounded.Apps, null, Modifier.size(42.dp), tint = Amber)
}

val DayNames = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
fun minuteLabel(minute: Int) = "%02d:%02d".format(minute / 60, minute % 60)
fun daySummary(days: Set<Int>) = days.sorted().joinToString(", ") { DayNames[it - 1] }

@Composable fun ScheduleScreen(repo: SessionRepository, state: BeerState, enabled: Boolean, exact: Boolean, back: () -> Unit, picker: () -> Unit,
                               permission: () -> Unit, exactPermission: () -> Unit) {
    var on by rememberSaveable { mutableStateOf(state.schedule.enabled) }
    var days by rememberSaveable { mutableStateOf<List<Int>>(ArrayList(state.schedule.days)) }
    var start by rememberSaveable { mutableIntStateOf(state.schedule.startMinute) }
    var end by rememberSaveable { mutableIntStateOf((state.schedule.startMinute + state.schedule.durationMinutes) % 1440) }
    val duration = (end - start + 1440) % 1440
    val context = LocalContext.current
    Screen("Hẹn lịch", back, action = { StatusPill(if (on) "Đang bật" else "Đang tắt") }, footer = {
        PrimaryButton("Lưu lịch", !on || (days.isNotEmpty() && duration >= 15 && state.selected.isNotEmpty() && enabled), Icons.Rounded.Save) {
            repo.update { it.copy(schedule = WeeklySchedule(on, days.toSet(), start, duration), lastScheduledStart = System.currentTimeMillis()) }
            back()
        }
    }) {
        Section("Tự khóa hàng tuần") {
            ToggleRow("Tự khóa theo lịch", on) { on = it }
            Note("Tới giờ, SayBeer tự khóa các app đã chọn, kể cả khi không mở giao diện. Các phiên vẫn có mở tạm, kiểm tra và khẩn cấp.")
        }
        if (on) {
            Section("Ngày trong tuần") {
                (0..7).toList().chunked(4).forEach { group ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        group.forEach { index ->
                            val selected = if (index == 7) days.size == 7 else index + 1 in days
                            ChoiceTile(if (index == 7) "Tất cả" else DayNames[index], selected, Modifier.weight(1f).heightIn(min = 58.dp)) {
                                days = ArrayList(if (index == 7) { if (days.size == 7) emptySet() else (1..7).toSet() }
                                    else days.toSet().let { if (index + 1 in it) it - (index + 1) else it + (index + 1) })
                            }
                        }
                    }
                }
            }
            Section("Khung giờ khóa") {
                TimeRow("Bắt đầu khóa", minuteLabel(start), Icons.Rounded.Lock, Amber) {
                    TimePickerDialog(context, { _, h, m -> start = h * 60 + m }, start / 60, start % 60, true).show()
                }
                HorizontalDivider(color = CardBorder)
                TimeRow("Mở khóa", minuteLabel(end), Icons.Rounded.LockOpen, Green) {
                    TimePickerDialog(context, { _, h, m -> end = h * 60 + m }, end / 60, end % 60, true).show()
                }
                HorizontalDivider(color = CardBorder)
                Note(if (duration < 15) "Chọn thời lượng tối thiểu 15 phút" else "Thời lượng: ${duration / 60} giờ ${duration % 60} phút${if (end <= start) " · qua đêm" else ""}", color = if (duration < 15) Red else Muted)
            }
            SelectionSection("App bị khóa", state, picker)
            PermissionNotice(enabled, exact, permission, exactPermission)
        }
        Note("Lịch sử dụng múi giờ hiện tại của thiết bị. Sau khi buộc dừng SayBeer, hãy mở lại app để khôi phục hoạt động.")
    }
}

@Composable private fun TimeRow(label: String, time: String, icon: ImageVector, color: Color, click: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = click).heightIn(min = 60.dp), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(36.dp).background(color.copy(alpha = .1f), RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, null, Modifier.size(19.dp), tint = color)
        }
        Text(label, Modifier.weight(1f), fontSize = 14.sp)
        Text(time, color = color, fontSize = 23.sp, fontWeight = FontWeight.Bold, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
        Icon(Icons.Rounded.ChevronRight, null, Modifier.size(18.dp), tint = Muted)
    }
}
