package com.gymlog.ui.settings

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gymlog.ui.GymLogViewModel
import com.gymlog.ui.common.AppScaffold
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun SettingsScreen(viewModel: GymLogViewModel) {
    val profile by viewModel.profile.collectAsState()
    val message by viewModel.settingsMessage.collectAsState()
    val context = LocalContext.current
    var pendingBackupJson by remember { mutableStateOf<String?>(null) }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var showTextImport by remember { mutableStateOf(false) }
    var importText by remember { mutableStateOf("") }
    val appVersionLabel = remember {
        runCatching {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
            "${packageInfo.versionName} ($versionCode)"
        }.getOrDefault("알 수 없음")
    }

    val backupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        val json = pendingBackupJson ?: return@rememberLauncherForActivityResult
        pendingBackupJson = null
        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(json) }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            if (json != null) viewModel.importBackupJson(json)
        }
    }

    LaunchedEffect(profile) {
        profile?.let {
            height = if (it.heightCm > 0.0) it.heightCm.toString() else ""
            weight = if (it.weightKg > 0.0) it.weightKg.toString() else ""
            gender = it.gender
            age = if (it.age > 0) it.age.toString() else ""
        }
    }

    AppScaffold("설정") {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("프로필", color = Color.White, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = height,
                        onValueChange = { height = it },
                        label = { Text("키(cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text("몸무게(kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = gender,
                        onValueChange = { gender = it },
                        label = { Text("성별") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = age,
                        onValueChange = { age = it },
                        label = { Text("나이") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(
                        onClick = {
                            viewModel.saveProfile(
                                height.toDoubleOrNull() ?: 0.0,
                                weight.toDoubleOrNull() ?: 0.0,
                                gender,
                                age.toIntOrNull() ?: 0,
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                    ) {
                        Text("프로필 저장", color = Color.White)
                    }
                }
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("앱 버전", color = Color.White, fontWeight = FontWeight.Bold)
                    Text(appVersionLabel, color = Color.Gray)
                }
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("운동 기록", color = Color.White, fontWeight = FontWeight.Bold)
                    Button(
                        onClick = { importLauncher.launch(arrayOf("application/json", "text/*")) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("불러오기")
                    }
                    Button(
                        onClick = { showTextImport = true },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("텍스트로 추가")
                    }
                    Button(
                        onClick = {
                            viewModel.exportBackupJson { json ->
                                pendingBackupJson = json
                                val name = "gymlog-backup-${SimpleDateFormat("yyyyMMdd-HHmm", Locale.getDefault()).format(Date())}.json"
                                backupLauncher.launch(name)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("운동 기록 백업하기")
                    }
                }
            }
        }
    }

    if (showTextImport) {
        AlertDialog(
            onDismissRequest = { showTextImport = false },
            title = { Text("운동 기록 텍스트 추가") },
            text = {
                OutlinedTextField(
                    value = importText,
                    onValueChange = { importText = it },
                    minLines = 10,
                    label = { Text("운동 기록 붙여넣기") },
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.importWorkoutText(importText)
                        showTextImport = false
                        importText = ""
                    },
                ) {
                    Text("추가")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTextImport = false }) { Text("취소") }
            },
        )
    }

    message?.let {
        AlertDialog(
            onDismissRequest = viewModel::clearSettingsMessage,
            title = { Text("알림") },
            text = { Text(it) },
            confirmButton = {
                TextButton(onClick = viewModel::clearSettingsMessage) { Text("확인") }
            },
        )
    }
}
