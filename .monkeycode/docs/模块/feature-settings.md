# feature/settings

设置界面模块，包含 LLM 配置、操作模式偏好等功能。

## 结构

```
feature/settings/
├── SettingsScreen.kt         # Compose 设置界面
└── SettingsViewModel.kt      # 设置状态管理
```

## 功能

- LLM API 地址、Key、模型名称配置
- 连接验证按钮
- 操作模式切换（建议模式 / 自主执行模式）

## 依赖

**本模块依赖**:
- `core/domain/` (ValidateLLMConfigUseCase)
- `core/data/` (SettingsRepository)

**依赖本模块的**:
- MainActivity (Navigation)
