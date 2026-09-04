# Material Flux · 汇率兑换

一款遵循 Google Material 3 设计规范的 Android 汇率换算应用，使用 Jetpack Compose 构建。

当前版本 1.2.0（versionCode 3）。

## 特性

**汇率精度**

汇率数据以欧洲央行（ECB）每日参考汇率为基准。ECB 发布的是 EUR 基准数据，应用自行完成到 USD 基准的换算，因此保留了完整精度：USD→CNY 得到 `6.719070`，而非第三方预计算 USD 基准时四舍五入后的 `6.7191`。这也是与 Google、XE 显示值一致的原因。

四类数据源按可靠性合并，ECB 优先级最高：

| 来源 | 覆盖 | 徽章 |
|---|---|---|
| 欧洲央行每日参考汇率 | 30 种主要法币 | `ECB 基准` |
| open.er-api.com | 166 种法币（补长尾） | `综合报价` |
| api.gold-api.com | 4 种贵金属现货 | `贵金属现货` |
| Coinbase 公开行情 | 20 种数字资产 | `数字资产` |

交叉汇率取两条腿中较弱的一方——CNY→TWD 会诚实标为综合报价，而非 ECB 基准。

所有汇率固定显示 6 位小数。小于 `0.0001` 的汇率（如 JPY→KWD）改用有效数字表示，避免显示为 `0.000002`。

**货币搜索与选择**

- 190 种货币：166 法币 + 4 贵金属（XAU/XAG/XPT/XPD）+ 20 数字资产（BTC/ETH/USDT 等）
- 顶部固定搜索框，实时显示匹配数量
- 搜索覆盖货币代码、中文名、英文名、货币符号
- 相关度排序：精确代码匹配 > 代码前缀 > 中文名前缀 > 英文名前缀 > 子串
- 九个分区筛选（亚洲/欧洲/美洲/中东/非洲/大洋洲/贵金属/数字货币/其他），搜索时自动隐藏
- 粘滞分区标题带该区数量，星标常用货币置顶

**界面**

- Material 3 完整色彩系统，浅色/深色双色盘按 M3 色调规范配对
- 深色模式三档切换：跟随系统 / 浅色 / 深色，选择持久化
- Android 12+ 动态取色（Material You），可关闭
- 自绘数字键盘，避免 IME 导致的窗口重排；长按退格清空
- 双向输入：点击任一侧即可编辑，切换时携带已换算值

**数据策略**

纯联网。不内置离线汇率表——硬编码快照会静默提供过期数据，比提示用户联网更糟。首次启动无网络时显示重试界面；已有缓存时先展示缓存并在后台校验。贵金属与数字资产为附加源，其失败不影响法币换算。

## 构建

需要 JDK 17 与 Android SDK 36。

```bash
# local.properties 中指定 SDK 路径
echo "sdk.dir=/path/to/android-sdk" > local.properties

./gradlew assembleRelease
```

Release 构建启用 R8 代码压缩与资源压缩，产物约 1.6 MB。

### 签名

签名口令不写入版本库。在项目根目录创建 `keystore.properties`（已被 `.gitignore` 排除）：

```properties
storeFile=keystore/release.jks
storeType=PKCS12
storePassword=<your-password>
keyAlias=<your-alias>
keyPassword=<your-password>
```

或改用环境变量 `CURRENCY_STORE_FILE` / `CURRENCY_STORE_TYPE` / `CURRENCY_STORE_PASSWORD` / `CURRENCY_KEY_ALIAS` / `CURRENCY_KEY_PASSWORD`。

生成 keystore：

```bash
keytool -genkeypair -keystore keystore/release.jks -storetype PKCS12 \
  -keyalg RSA -keysize 4096 -validity 36500 -alias materialflux \
  -storepass <your-password> -keypass <your-password> \
  -dname "CN=Material Flux, O=<your-org>, C=CN"
```

Release 使用 APK Signature Scheme v2 + v3（v1 仅 API 24 以下需要，而 minSdk 即为 24）。

未提供任何凭证时，release 构建会回退到 debug 签名并输出警告，以保证新克隆的仓库仍可构建——此类产物不可用于分发。

## CI / 发布

两条 GitHub Actions 工作流：

**`.github/workflows/build.yml`** — 推送到 `main`、提交 PR 或手动触发时运行单元测试并构建 debug/release APK，产物上传为 workflow artifact。

**`.github/workflows/release.yml`** — 推送 `v*` 标签或手动触发时发布到 Releases 页面。APK 按版本号与构建日期命名（`MaterialFlux-1.2.0-20260904.apk`），发布说明包含 versionCode、构建时间、提交哈希与 SHA-256 校验和。标签与 `app/build.gradle.kts` 中的 `versionName` 不一致时构建失败，避免产物与发布说明不符。

发布流程：

```bash
# 1. 在 app/build.gradle.kts 中递增 versionCode 与 versionName
# 2. 提交并打标签
git tag v1.2.0 && git push origin main --tags
```

或在 Actions 页面手动运行 Release 工作流，标签从 `build.gradle.kts` 自动推导。

### 配置签名密钥

在仓库 Settings → Secrets and variables → Actions 中添加：

| Secret | 内容 |
|---|---|
| `KEYSTORE_BASE64` | keystore 文件的 base64 编码 |
| `KEYSTORE_PASSWORD` | keystore 口令 |
| `KEY_ALIAS` | 密钥别名 |
| `KEY_PASSWORD` | 密钥口令 |

生成 base64：

```bash
base64 -w0 keystore/release.jks          # Linux
certutil -encodehex -f release.jks out.txt 0x40000002   # Windows
```

未配置这些 secret 时，CI 仍会成功构建，但产物使用 debug 签名并在日志中告警。工作流结束时无条件清理 keystore 与凭证文件。

## 测试

```bash
./gradlew testDebugUnitTest
```

32 项单元测试，覆盖 ECB 基准换算算术、四源合并的 provenance 降级顺序、贵金属与数字资产的交叉汇率、6 位小数格式化边界、按币种小数位处理、货币元数据完整性与 JSON 解析容错。

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

- [European Central Bank](https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml) — 每日参考汇率基准
- [open.er-api.com](https://www.exchangerate-api.com/docs/free) — 长尾法币
- [api.gold-api.com](https://api.gold-api.com) — 贵金属现货价
- [Coinbase](https://api.coinbase.com/v2/exchange-rates) — 数字资产行情

汇率为中间市场汇率，仅供参考，不含银行点差，不构成交易依据。贵金属按金衡盎司计价。数字资产价格波动剧烈，显示值仅为查询时刻的快照。

## 许可

[GPL-3.0-or-later](LICENSE)

选择 GPL 而非 MIT 是有意的：任何人可以自由使用、修改和分发本项目，但基于它的衍生作品同样必须开源。这样能防止有人把它闭源打包、加上广告后上架应用商店。

代码主要由 AI 生成，相关坦白见 [DISCLAIMER.md](DISCLAIMER.md)。该文档仅为态度陈述，不具法律效力。
