package com.tem2t.saybeer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tem2t.saybeer.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val COMMITMENT = "chỉ nhắn việc, không tâm sự"
private val Sentences = listOf("buổi trưa ăn bưởi chua đỡ say hơn hẳn", "nồi đồng nấu ốc nồi đất nấu ếch",
    "con lươn nó luồn qua lườn con lươn", "lúa nếp là lúa nếp làng lúa lên lớp lớp", "khi đi cưa ngọn khi về cũng cưa ngọn")

@Composable fun WorkScreen(repo: SessionRepository, state: BeerState, now: Long, back: () -> Unit) {
    var typed by rememberSaveable { mutableStateOf("") }
    var matchedAt by rememberSaveable { mutableLongStateOf(0) }
    val matches = normalize(typed, true) == normalize(COMMITMENT, true)
    val left = if (matchedAt > 0) ((matchedAt + 15_000 - now + 999) / 1000).coerceIn(0, 15) else 15
    val session = state.session
    Screen("Cửa sổ nhắn việc", back, action = { StatusPill("SayBeer") }, footer = {
        PrimaryButton(if (!matches) "Gõ lời cam kết để tiếp tục" else if (left > 0) "Hít thở đã… ${left}s" else "Mở cửa sổ nhắn việc",
            matches && left == 0L && session != null && session.windowsUsed < MAX_WINDOWS && session.windowEnd <= now) {
            repo.openWindow(); back()
        }
        Text("●  Còn ${MAX_WINDOWS - (session?.windowsUsed ?: 0)} lần mở trong cuộc nhậu này.", color = Muted,
            fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }) {
        Column(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)) {
            HeroIcon(Icons.Rounded.Work)
            Text("Mở 15 phút để nhắn việc", fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text("Gõ đúng câu dưới đây để cam kết:", color = Muted, fontSize = 14.sp, textAlign = TextAlign.Center)
        }
        Section("Lời hứa với chính mình") {
            Text("“$COMMITMENT”", color = Amber, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(typed, { value ->
                typed = value
                if (normalize(value, true) == normalize(COMMITMENT, true)) {
                    if (matchedAt == 0L) matchedAt = System.currentTimeMillis()
                } else matchedAt = 0
            }, modifier = Modifier.fillMaxWidth(), label = { Text("Gõ lại câu trên…") }, keyboardOptions = KeyboardOptions(autoCorrectEnabled = false))
        }
        Note("Hít thở 15 giây sau khi gõ đúng. Cửa sổ sẽ tự đóng khi hết giờ, hoặc khi phiên kết thúc.")
    }
}

@Composable fun TestScreen(repo: SessionRepository, state: BeerState, now: Long, back: () -> Unit) {
    var sentence by rememberSaveable { mutableStateOf(Sentences.random()) }
    var a by rememberSaveable { mutableIntStateOf((23..89).random()) }
    var b by rememberSaveable { mutableIntStateOf((23..89).random()) }
    var typed by rememberSaveable { mutableStateOf("") }
    var answer by rememberSaveable { mutableStateOf("") }
    var deadline by rememberSaveable { mutableLongStateOf(System.currentTimeMillis() + 90_000) }
    var failed by rememberSaveable { mutableStateOf(false) }
    val session = state.session ?: return
    val locked = session.lockoutUntil > now
    val left = ((deadline - now + 999) / 1000).coerceIn(0, 90)
    fun reset() {
        sentence = Sentences.random(); a = (23..89).random(); b = (23..89).random()
        typed = ""; answer = ""; deadline = System.currentTimeMillis() + 90_000
    }
    var wasLocked by rememberSaveable { mutableStateOf(locked) }
    LaunchedEffect(locked) { if (wasLocked && !locked) reset(); wasLocked = locked }
    LaunchedEffect(left, locked) {
        if (!locked && left == 0L) { repo.fail(); failed = true; reset() }
    }
    Screen("Kiểm tra tỉnh táo", back, footer = {
        if (!locked) PrimaryButton("Mở khóa", left > 0 && typed.isNotBlank() && (!session.math || answer.isNotBlank()), Icons.Rounded.LockOpen) {
            val current = repo.current.session
            val realNow = System.currentTimeMillis()
            if (current != null && current.lockoutUntil <= realNow && realNow < deadline && normalize(typed) == normalize(sentence) && (!current.math || answer.toIntOrNull() == a + b)) {
                repo.finish("test"); back()
            } else { repo.fail(); failed = true; reset() }
        }
        SecondaryButton("Thôi, để khóa tiếp", onClick = back)
        if (!locked) Text("Bạn có 90 giây mỗi lượt. Sai 3 lần phải chờ 5 phút.\nĐây chỉ là bước tự cam kết, không đo nồng độ cồn hay khả năng lái xe.",
            color = Muted, fontSize = 11.sp, lineHeight = 18.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }) {
        if (locked) {
            HeroIcon(Icons.Rounded.Bedtime)
            Text("Sai 3 lần rồi", fontWeight = FontWeight.Bold, fontSize = 26.sp)
            Text("Nghỉ ${remaining(session.lockoutUntil, now)} nữa rồi thử lại.", color = Amber, fontSize = 23.sp)
            Note("Hoặc cứ để khóa đến giờ hẹn cho lành. Mở khẩn cấp vẫn luôn khả dụng.")
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LinearProgressIndicator(progress = { left / 90f }, modifier = Modifier.weight(1f), color = if (left <= 15) Red else Amber)
                Text("${left}s", color = Amber)
            }
            Section("Gõ chính xác câu sau · đúng cả dấu") {
                Text("“$sentence”", color = Amber, fontWeight = FontWeight.SemiBold, fontSize = 19.sp)
                OutlinedTextField(typed, { typed = it }, label = { Text("Gõ lại câu trên…") }, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(autoCorrectEnabled = false))
            }
            if (session.math) Section("Tính nhẩm") {
                Text("$a + $b = ?", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(answer, { answer = it.filter(Char::isDigit).take(4) }, label = { Text("Kết quả") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
            if (failed) Note("Chưa đúng hoặc đã hết giờ. Thử câu mới nhé.", Red)
        }
    }
}

@Composable fun EmergencyScreen(repo: SessionRepository, back: () -> Unit) {
    var progress by remember { mutableFloatStateOf(0f) }
    var accessibleCountdown by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    fun finish() { repo.finish("emergency"); back() }
    // TalkBack cannot sustain a raw pointer hold; offer an explicit, cancellable 5-second countdown.
    LaunchedEffect(accessibleCountdown) {
        if (accessibleCountdown) {
            repeat(100) { delay(50); progress = (it + 1) / 100f }
            finish()
        }
    }
    Screen("Khẩn cấp", back) {
        HeroIcon(Icons.Rounded.Sos, color = Red)
        Text("Cần mở ngay?", fontWeight = FontWeight.Bold, fontSize = 28.sp)
        Text("Giữ nút 5 giây để gỡ toàn bộ khóa và kết thúc phiên.", color = Muted)
        Note("Cuộc gọi và ứng dụng Điện thoại vẫn mở bình thường, không cần kết thúc phiên.")
        val latestFinish by rememberUpdatedState(::finish)
        Box(Modifier.fillMaxWidth().height(72.dp).background(Red.copy(alpha = .22f), RoundedCornerShape(18.dp))
            .semantics {
                contentDescription = "Giữ 5 giây để mở khóa khẩn cấp"
                onLongClick("Bắt đầu đếm ngược 5 giây") { accessibleCountdown = true; true }
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    val job = scope.launch {
                        repeat(100) { delay(50); progress = (it + 1) / 100f }
                        latestFinish()
                    }
                    try { waitForUpOrCancellation() } finally { job.cancel(); progress = 0f }
                }
            }, contentAlignment = Alignment.Center) {
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxSize(), color = Red.copy(alpha = .5f), trackColor = androidx.compose.ui.graphics.Color.Transparent,
                drawStopIndicator = {})
            Text(if (progress == 0f) "Giữ 5 giây để mở khóa" else "Giữ tiếp… ${kotlin.math.ceil(5 * (1 - progress)).toInt()}s",
                fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
        Note("Nhả tay là tính lại từ đầu.")
        if (accessibleCountdown) SecondaryButton("Hủy mở khẩn cấp") { accessibleCountdown = false; progress = 0f }
        SecondaryButton("Thôi, không khẩn", onClick = back)
    }
}
