# core/serializer

UI 树序列化工具，将 Android AccessibilityNodeInfo 树转换为扁平化的 SerializedUITree 数据结构。

## 结构

```
core/serializer/
└── UITreeSerializer.kt      # 树遍历和元素提取
```

## 关键文件

| 文件 | 目的 |
|------|------|
| `UITreeSerializer.kt` | 从 AccessibilityNodeInfo 根节点递归遍历，提取每个可见元素的关键属性，过滤装饰性节点，输出 SerializedUITree |

## 依赖

**本模块依赖**:
- `core/model/SerializedUITree.kt` - 输出数据结构
- Android `AccessibilityNodeInfo` API

**依赖本模块的**:
- `core/domain/ReadUITreeUseCase` - 使用序列化器转换原始数据
