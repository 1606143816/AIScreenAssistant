# 用户指令记忆

本文件记录了用户指令、偏好和项目知识，用于在未来的交互中提供参考。

## 条目

### 环境无 Android 构建工具链
- Date: 2026-06-24
- Context: Agent 在开发 AI 屏幕助手 Android 项目时发现
- Category: 环境配置
- Instructions:
  - 当前环境无 Java、Gradle、Android SDK (`ANDROID_HOME` 未设置)
  - 项目代码为纯文件编辑，无法编译或运行测试
  - gradle-wrapper.jar 缺失，需在有 Gradle 的环境中运行 `gradle wrapper` 生成
  - 项目已配置 GitHub Actions CI：推送后自动构建 APK + 运行测试

### 项目无远程 Git 仓库
- Date: 2026-06-24
- Context: Agent 在执行 git push 时发现
- Category: 运维部署
- Instructions:
  - 本地 Git 仓库无 remote 配置，无法 push
  - 所有提交仅保存在本地，需手动配置 remote 后推送
