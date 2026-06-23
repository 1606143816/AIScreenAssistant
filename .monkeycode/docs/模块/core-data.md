# core/data

数据持久化层，负责 Room 数据库管理、Repository 实现、LLM API 网络请求和 Settings DataStore。

## 结构

```
core/data/
├── local/
│   ├── AppDatabase.kt           # Room 数据库类
│   ├── entity/                  # Room Entity 定义
│   │   ├── ConversationEntity.kt
│   │   ├── MessageEntity.kt
│   │   └── UITreeRecordEntity.kt
│   └── dao/                     # Room DAO 接口
│       ├── ConversationDao.kt
│       └── MessageDao.kt
├── repository/
│   ├── LLMRepository.kt         # LLM API 调用封装
│   ├── ConversationRepository.kt # 对话 CRUD
│   └── SettingsRepository.kt    # 配置读写
├── network/
│   ├── LLMApiService.kt         # OkHttp LLM 客户端
│   └── NetworkMonitor.kt        # 网络状态监听
└── datastore/
    └── SettingsDataStore.kt      # DataStore 键值存储
```

## 关键文件

| 文件 | 目的 |
|------|------|
| `AppDatabase.kt` | Room 数据库入口，声明所有 Entity 和 DAO |
| `LLMApiService.kt` | 封装 OkHttp 调用，构造 JSON 请求，解析 AI 响应 |
| `LLMRepository.kt` | 组合 LLMApiService + NetworkMonitor，处理重试和队列 |
| `ConversationRepository.kt` | 封装 Room DAO，对外暴露 Flow |
| `SettingsDataStore.kt` | 使用 Preferences DataStore 持久化 LLMConfig 和 OperationMode |

## 依赖

**本模块依赖**:
- `core/model/` - 数据模型定义
- `core/domain/` (仅 SettingsRepository 的 OperationMode)

**依赖本模块的**:
- `core/domain/` - UseCase 通过 Repository 访问数据和 API
