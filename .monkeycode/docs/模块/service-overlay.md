# service/overlay

悬浮球覆盖窗口服务，在所有应用上方显示可拖拽的操作入口。

## 结构

```
service/overlay/
└── OverlayService.kt        # 悬浮球 WindowManager 实现
```

## 关键文件

| 文件 | 目的 |
|------|------|
| `OverlayService.kt` | 通过 WindowManager 添加系统级悬浮 View，处理拖拽、点击、长按事件，管理通知栏入口 |

## 权限

- `SYSTEM_ALERT_WINDOW` - 悬浮窗权限，需用户在系统设置中手动授予
- `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_SPECIAL_USE` - 前台服务

## 交互事件

| 事件 | 行为 |
|------|------|
| 点击悬浮球 | 触发 UI 树读取，打开 ConversationScreen |
| 长按悬浮球 | 启动语音输入模式 |
| 拖拽悬浮球 | 改变悬浮球在屏幕边缘的位置 |
| 折叠/关闭 | 最小化至通知栏，支持一键恢复 |

## 依赖

**本模块依赖**:
- `core/domain/` - 通过 EventBus 发送事件给 UseCase
- Android WindowManager API

**依赖本模块的**:
- 直接由用户交互触发，无代码级依赖
