package com.tem2t.saybeer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tem2t.saybeer.data.BeerState
import com.tem2t.saybeer.platform.AchievementShare
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable fun StatsScreen(state: BeerState, back: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var sharing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    Screen("Thống kê", back) {
        if (state.history.isEmpty()) {
            Spacer(Modifier.height(64.dp))
            HeroIcon(Icons.Rounded.BarChart)
            Text("Chưa có phiên nào kết thúc.", fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Note("Nhậu một trận là có số liệu ngay. Thành tích và lịch sử chỉ lưu trên thiết bị của bạn.")
        } else {
            Text("Mỗi lần giữ lời,\nmột sáng nhẹ đầu.", fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, lineHeight = 36.sp)
            if (state.streak >= 2) Text("🔥 Chuỗi ${state.streak} phiên an toàn liên tiếp", color = Amber)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile("${state.survived}", "Phiên an toàn", Modifier.weight(1f))
                StatTile("%.1f giờ".format(state.hours), "Tổng giờ khóa", Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile("${state.history.count { it.reason == "test" }}", "Mở bằng kiểm tra", Modifier.weight(1f))
                StatTile("${state.history.count { it.reason == "emergency" }}", "Khẩn cấp", Modifier.weight(1f))
            }
            PrimaryButton(if (sharing) "Đang tạo ảnh…" else "Khoe thành tích với hội nhậu", !sharing) {
                sharing = true; error = null
                scope.launch {
                    try {
                        val uri = withContext(Dispatchers.IO) { AchievementShare.create(context, state) }
                        AchievementShare.share(context, uri)
                    } catch (_: Exception) { error = "Chưa thể chia sẻ ảnh. Vui lòng thử lại." }
                    finally { sharing = false }
                }
            }
            error?.let { Note(it, Red) }
            Section("Các phiên gần đây") {
                state.history.takeLast(30).asReversed().forEachIndexed { index, record ->
                    if (index > 0) HorizontalDivider(color = Muted.copy(alpha = .15f))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(dateLabel(record.start), fontWeight = FontWeight.Medium)
                        val minutes = record.duration / 60_000
                        Note("${minutes / 60} giờ ${minutes % 60} phút · ${record.windows} lần nhắn việc")
                        val color = if (record.survived) Green else if (record.reason == "test") Amber else Red
                        Text(if (record.survived) "Trọn phiên" else if (record.reason == "test") "Mở sớm" else "Khẩn cấp", color = color,
                            style = MaterialTheme.typography.labelMedium, modifier = Modifier.background(color.copy(alpha = .12f), RoundedCornerShape(20.dp)).padding(horizontal = 12.dp, vertical = 5.dp))
                    }
                }
            }
        }
    }
}

@Composable private fun StatTile(value: String, label: String, modifier: Modifier) {
    Column(modifier.background(CardGradient, RoundedCornerShape(18.dp)).border(1.dp, CardBorder, RoundedCornerShape(18.dp)).padding(vertical = 24.dp, horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(value, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Amber)
        Text(label, style = MaterialTheme.typography.bodySmall, color = Muted)
    }
}
