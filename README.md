# 汇率兑换 · CurrencyM3

一款遵循 Google Material 3 设计规范的 Android 汇率换算应用，使用 Jetpack Compose 构建。

## 特性

**汇率精度**

汇率数据以欧洲央行（ECB）每日参考汇率为基准。ECB 发布的是 EUR 基准数据，应用自行完成到 USD 基准的换算，因此保留了完整精度：USD→CNY 得到 `6.719070`，而非第三方预计算 USD 基准时四舍五入后的 `6.7191`。这也是与 Google、XE 显示值一致的原因。

ECB 覆盖 30 种主要货币；其余 136 种由 open.er-api.com 补齐。每个货币对都标注数据来源——`ECB 基准` 或 `综合报价`——交叉汇率取两条腿中较弱的一方。

所有汇率固定显示 6 位小数。小于 `0.0001` 的汇率（如 JPY→KWD）改用有效数字表示，避免显示为 `0.000002`。

**界面**

- Material 3 完整色彩系统，浅色/深色双色盘按 M3 色调规范配对
- 深色模式三档切换：跟随系统 / 浅色 / 深色，选择持久化
- Android 12+ 动态取色（Material You），可关闭
- 自绘数字键盘，避免 IME 导致的窗口重排；长按退格清空
- 双向输入：点击任一侧即可编辑，切换时携带已换算值

**货币选择器**

- 166 种货币，与 API 返回集合完全对齐
- 按七个地区分组，粘滞分组头
- 搜索支持货币代码、中文名、英文名、货币符号
- 相关度排序：精确代码匹配 > 代码前缀 > 名称前缀 > 子串
- 星标常用货币，置顶显示

**数据策略**

纯联网。不内置离线汇率表——硬编码快照会静默提供过期数据，比提示用户联网更糟。首次启动无网络时显示重试界面；已有缓存时先展示缓存并在后台校验。

## 构建

需要 JDK 17 与 Android SDK 36。

```bash
# local.properties 中指定 SDK 路径
echo "sdk.dir=/path/to/android-sdk" > local.properties

./gradlew assembleRelease
```

Release 构建启用 R8 代码压缩与资源压缩，产物约 1.6 MB。

签名配置引用 `keystore/dev.jks`，该目录已被 `.gitignore` 排除。首次构建 release 前需自行生成：

```bash
keytool -genkeypair -keystore keystore/dev.jks -storetype JKS \
  -keyalg RSA -keysize 2048 -validity 10950 -alias dev \
  -storepass <your-password> -keypass <your-password> \
  -dname "CN=CurrencyM3, O=Dev, C=CN"
```

并在 `app/build.gradle.kts` 的 `signingConfigs` 中填入对应口令，或改为从环境变量读取。

## 测试

```bash
./gradlew testDebugUnitTest
```

29 项单元测试，覆盖 ECB 基准换算算术、交叉汇率一致性、6 位小数格式化边界、按币种小数位处理、货币元数据完整性与 JSON 解析容错。

## 技术栈

| | |
|---|---|
| 语言 | Kotlin 2.2.21 |
| UI | Jetpack Compose，Material 3 |
| 构建 | AGP 8.13.2，Gradle 8.14.5 |
| 网络 | OkHttp + kotlinx.serialization |
| 持久化 | DataStore Preferences |
| minSdk / targetSdk | 24 / 36 |

## 数据来源

- [European Central Bank](https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml) — 每日参考汇率
- [open.er-api.com](https://www.exchangerate-api.com/docs/free) — 长尾货币

汇率为中间市场汇率，仅供参考，不含银行点差，不构成交易依据。

## 许可

MIT
