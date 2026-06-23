# LLMConfig

LLMConfig 定义连接云端大语言模型的配置参数，用户可在设置界面中配置。

## 什么是 LLMConfig？

LLMConfig 存储连接 LLM 服务所需的所有参数，包括 API 端点地址、认证密钥和模型参数。配置持久化在 DataStore 中，并可随时修改和验证。

## 代码位置

| 方面 | 位置 |
|------|------|
| 模型 | `core/model/LLMConfig.kt` |
| 存储 | `core/data/SettingsDataStore.kt` |
| 验证 | `core/domain/ValidateLLMConfigUseCase.kt` |
| UI | `feature/settings/SettingsScreen.kt` |

## 结构

```kotlin
data class LLMConfig(
    val baseUrl: String,        // API 地址 (如 https://api.openai.com)
    val apiKey: String,         // API 密钥
    val modelName: String,      // 模型名称 (如 gpt-4o, qwen-vl-max)
    val maxTokens: Int = 4096,  // 最大生成 token 数
    val temperature: Float = 0.7f  // 生成温度 (0-2)
)
```

### 配置项说明

| 字段 | 默认值 | 说明 |
|------|--------|------|
| `baseUrl` | - | LLM API 的基础地址，兼容 OpenAI Chat Completions 格式 |
| `apiKey` | - | API 认证密钥，存储时不显示明文 |
| `modelName` | - | 使用的模型名称 |
| `maxTokens` | 4096 | 单次响应最大 token 数 |
| `temperature` | 0.7 | 生成多样性，越低越稳定 |

## 验证流程

1. 用户在设置界面配置参数后点击「验证连接」
2. `ValidateLLMConfigUseCase` 使用配置发送测试请求
3. 成功：显示"连接成功，模型 [name] 可用"
4. 失败：显示具体错误原因（网络不通/认证失败/模型不存在）

## 安全注意事项

- apiKey 存储在 DataStore 中，不在 UI 明文展示
- apiKey 在输入框中为密码输入类型
- 不将 apiKey 写入日志或分析报告
