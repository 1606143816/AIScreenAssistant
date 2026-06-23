package com.monkeycode.aiscreen.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.monkeycode.aiscreen.core.model.OperationMode
import com.monkeycode.aiscreen.feature.settings.SettingsUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onBaseUrlChange: (String) -> Unit,
    onApiKeyChange: (String) -> Unit,
    onModelNameChange: (String) -> Unit,
    onMaxTokensChange: (String) -> Unit,
    onTemperatureChange: (String) -> Unit,
    onOperationModeChange: (OperationMode) -> Unit,
    onSave: () -> Unit,
    onValidate: () -> Unit,
    onClearError: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "LLM 连接配置",
                style = MaterialTheme.typography.titleLarge
            )

            OutlinedTextField(
                value = uiState.baseUrl,
                onValueChange = onBaseUrlChange,
                label = { Text("API 地址") },
                placeholder = { Text("https://api.openai.com") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.apiKey,
                onValueChange = onApiKeyChange,
                label = { Text("API 密钥") },
                placeholder = { Text("sk-...") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.modelName,
                onValueChange = onModelNameChange,
                label = { Text("模型名称") },
                placeholder = { Text("gpt-4o") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = uiState.maxTokens,
                    onValueChange = onMaxTokensChange,
                    label = { Text("Max Tokens") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = uiState.temperature,
                    onValueChange = onTemperatureChange,
                    label = { Text("Temperature") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onSave,
                    enabled = !uiState.isSaving,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (uiState.isSaving) "保存中..." else "保存配置")
                }

                OutlinedButton(
                    onClick = onValidate,
                    enabled = !uiState.isValidating,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (uiState.isValidating) "验证中..." else "测试连接")
                }
            }

            uiState.validationResult?.let { result ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (result.success)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        else
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (result.success) "连接成功" else "连接失败",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = result.message,
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (result.modelName != null) {
                            Text(
                                text = "模型: ${result.modelName}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            Divider()

            Text(
                text = "操作模式",
                style = MaterialTheme.typography.titleLarge
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val modes = listOf(OperationMode.SUGGESTION, OperationMode.AUTONOMOUS)
                modes.forEach { mode ->
                    FilterChip(
                        selected = uiState.operationMode == mode,
                        onClick = { onOperationModeChange(mode) },
                        label = {
                            Text(
                                when (mode) {
                                    OperationMode.SUGGESTION -> "建议模式"
                                    OperationMode.AUTONOMOUS -> "自动模式"
                                }
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Text(
                text = when (uiState.operationMode) {
                    OperationMode.SUGGESTION -> "LLM 将分析屏幕并提供操作建议，由您决定是否执行"
                    OperationMode.AUTONOMOUS -> "LLM 将自动分析屏幕并执行操作"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Divider()

            Text(
                text = "关于",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "AI 屏幕助手 v1.0.0\n" +
                        "通过 Android 无障碍服务读取屏幕内容，\n" +
                        "连接云端 LLM 进行智能分析和自动化操作。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (uiState.error != null) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = onClearError) {
                        Text("关闭")
                    }
                }
            ) {
                Text(uiState.error)
            }
        }
    }
}
