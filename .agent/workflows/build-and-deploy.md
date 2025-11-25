---
description: 如何构建和部署 Operit APP
---

# Operit 构建和部署指南

## 📋 前置条件

### 本地开发环境
- JDK 17
- Android SDK (API 26-34)
- Android Studio (推荐)
- Git

### CI/CD 环境
- GitHub Repository Secrets 配置:
  - `GITHUB_CLIENT_ID`: GitHub OAuth App Client ID
  - `GITHUB_CLIENT_SECRET`: GitHub OAuth App Client Secret

---

## 🚀 本地构建流程

### 1. 克隆项目并初始化子模块
```bash
git clone https://github.com/AAswordman/Operit.git
cd Operit
git submodule update --init --recursive
```

### 2. 配置本地环境
```bash
# 复制配置模板
cp local.properties.example local.properties

# 编辑 local.properties，填入你的凭证
# GITHUB_CLIENT_ID=你的ID
# GITHUB_CLIENT_SECRET=你的SECRET
```

### 3. 下载额外依赖
从 [Google Drive](https://drive.google.com/drive/folders/1g-Q_i7cf6Ua4KX9ZM6V282EEZvTVVfF7) 下载依赖库，放置到 `app/libs/` 目录。

### 4. 构建 APK

#### Debug 版本
```bash
chmod +x gradlew
./gradlew assembleDebug

# 输出位置: app/build/outputs/apk/debug/app-debug.apk
```

#### Release 版本
```bash
./gradlew assembleRelease

# 输出位置: app/build/outputs/apk/release/app-release.apk
```

#### Nightly 版本
```bash
./gradlew assembleNightly

# 输出位置: app/build/outputs/apk/nightly/app-nightly.apk
```

### 5. 安装到设备
```bash
# 安装 Debug 版本
./gradlew installDebug

# 或使用 adb
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## ⚙️ CI/CD 自动构建

### Debug 构建（新增）

**触发方式：**
1. 推送到 `main`, `dev`, 或 `me` 分支
2. 创建 Pull Request
3. 手动触发（在 GitHub Actions 页面）

**产物获取：**
- 进入 GitHub Actions 页面
- 找到对应的 workflow run
- 在 Artifacts 区域下载 `operit-debug-apk`

**手动触发步骤：**
// turbo
1. 打开仓库的 Actions 页面
2. 选择 "Build Debug APK" workflow
3. 点击 "Run workflow" 按钮
4. 选择分支，点击绿色的 "Run workflow"

### Release 构建（已存在）

**触发方式：**
1. 推送 tag（格式：`v*`，例如 `v1.6.3`）
2. 手动触发

**发布流程：**
```bash
# 创建并推送 tag
git tag v1.6.3
git push origin v1.6.3

# CI 会自动:
# 1. 构建 Release APK
# 2. 创建 GitHub Release
# 3. 上传 APK 到 Release
```

---

## 🔍 特殊配置说明

### 1. 签名配置
项目使用 `app/release.keystore` 进行签名：
- Store Password: `android`
- Key Alias: `androiddebugkey`
- Key Password: `android`

**注意：** 生产环境应使用更安全的密钥！

### 2. ABI 支持
项目当前只构建 `arm64-v8a` 架构（见 `app/build.gradle.kts` line 50）

如需支持更多架构，修改：
```kotlin
ndk {
    abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a"))
}
```

### 3. 外部依赖
CI 会自动下载以下内容：
- **Sherpa NCNN**: 语音识别库和模型
  - 库文件: `app/src/main/jniLibs/arm64-v8a/`
  - 模型文件: `app/src/main/assets/models/`

### 4. 构建变体
- **debug**: 开发调试版本
- **release**: 正式发布版本（启用签名）
- **nightly**: 每夜构建版本（与 release 类似）

---

## 🐛 常见问题

### Q: 构建失败 - libsudo.so stripping error
**A:** CI 已配置跳过 libsudo.so 的符号剥离。如本地遇到此问题，在 `terminal/build.gradle.kts` 添加：
```kotlin
android {
    packaging {
        jniLibs {
            keepDebugSymbols.add("**/libsudo.so")
        }
    }
}
```

### Q: GitHub OAuth 配置问题
**A:** 
1. 访问 https://github.com/settings/developers
2. 创建新的 OAuth App
3. Callback URL 设置为：`operit://github-oauth-callback`
4. 将 Client ID 和 Secret 填入 `local.properties`

### Q: 依赖下载失败
**A:** 
1. 检查网络连接
2. 确保 Google Drive 链接可访问
3. 确保 `app/libs/` 目录存在

### Q: 子模块未初始化
**A:**
```bash
git submodule update --init --recursive
```

---

## 📦 发布清单

发布新版本前检查：
- [ ] 更新 `versionCode` (app/build.gradle.kts line 38)
- [ ] 更新 `versionName` (app/build.gradle.kts line 39)
- [ ] 更新 `README.md` 版本历程
- [ ] 测试所有核心功能
- [ ] 创建 tag 并推送
- [ ] 验证 CI/CD 构建成功
- [ ] 检查 GitHub Release 内容

---

## 🔗 相关链接

- [项目主页](https://github.com/AAswordman/Operit)
- [用户指南](https://aaswordman.github.io/OperitWeb)
- [开发文档](docs/CONTRIBUTING.md)
- [依赖下载](https://drive.google.com/drive/folders/1g-Q_i7cf6Ua4KX9ZM6V282EEZvTVVfF7)
