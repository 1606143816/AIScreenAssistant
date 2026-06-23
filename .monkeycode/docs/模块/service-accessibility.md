# service/accessibility

Android AccessibilityService 实现，是本系统的核心服务。同时承担 UI 树读取和自动化操作执行两个职责。

## 结构

```
service/accessibility/
├── AIAccessibilityService.kt       # AccessibilityService 实现
└── (配置)
    └── res/xml/accessibility_service_config.xml  # 服务配置
```

## 关键文件

| 文件 | 目的 |
|------|------|
| `AIAccessibilityService.kt` | 实现 onAccessibilityEvent、readUITree、performAction | 

## 权限声明

在 `AndroidManifest.xml` 中声明：
```xml
<service
    android:name=".service.accessibility.AIAccessibilityService"
    android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE">
    <intent-filter>
        <action android:name="android.accessibilityservice.AccessibilityService" />
    </intent-filter>
    <meta-data
        android:name="android.accessibilityservice"
        android:resource="@xml/accessibility_service_config" />
</service>
```

## 监听事件类型

- `typeWindowStateChanged` - 窗口切换
- `typeWindowContentChanged` - 内容变化
- `typeViewClicked` - 元素点击
- `typeViewFocused` - 焦点变化
- `typeViewScrolled` - 滚动
- `typeViewTextChanged` - 文本变化

## 依赖

**本模块依赖**:
- `core/model/` - Action 和 SerializedUITree 数据模型
- `core/serializer/` - UITreeSerializer

**依赖本模块的**:
- `core/domain/ReadUITreeUseCase`
- `core/domain/ExecuteActionUseCase`
