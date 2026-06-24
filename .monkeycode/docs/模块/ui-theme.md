# UI Theme 模块

应用的 Material3 主题配置。

## 文件

| 文件 | 职责 |
|------|------|
| `Theme.kt` | `AIScreenAssistantTheme` composable，定义 Light/Dark 配色方案，状态栏颜色同步 |

## 配色方案

以紫色系 (`#6200EE`) 为主色：

| 颜色角色 | Light | Dark |
|---------|-------|------|
| Primary | `#6200EE` | `#D0BCFF` |
| PrimaryContainer | `#E8DEF8` | `#4F378B` |
| Secondary | `#625B71` | `#CCC2DC` |
| Tertiary | `#7D5260` | `#EFB8C8` |
| Error | `#B3261E` | `#F2B8B5` |
| Background | `#FFFBFE` | `#1C1B1F` |

## 功能

- 自动跟随系统 Dark Mode 设置 (`isSystemInDarkTheme()`)
- 通过 `SideEffect` 同步状态栏颜色与 Primary 色
- `WindowCompat.getInsetsController` 控制状态栏文字明暗
