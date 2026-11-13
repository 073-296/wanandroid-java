# 玩Android（WanAndroid 客户端）

一个基于 [wanandroid 开放 API](https://www.wanandroid.com/blog/show/2) 的 Android 客户端练习项目，用来练手 **Java + 传统 View 体系 + MVC** 这套老技术栈：Activity/Fragment 生命周期、RecyclerView 分页、SQLite 本地存储、Material 3 主题与深色模式。

不追求新框架（没有 Compose、没有 Kotlin、没有 Hilt/Retrofit），刻意保留手写 OkHttp + Gson + 原生 SQLite 的方式，把每个环节都摊开写清楚。

> 界面显示名为「玩Android」，包名/工程名仍是 `com.example.myproject` / `MyProject` —— 三者互相独立，改显示名不影响应用身份。

## 功能

- **登录 / 注册**：账号密码本地保存，自动登录；支持切换账号、注销账号（连带清空该账号的收藏）
- **首页文章流**
  - 顶部轮播图（ViewPager2 无限轮播 + 圆点指示器）
  - 文章列表分页加载（滑到底自动加载下一页）
  - 下拉刷新：重新拉取第 0 页并整体替换列表
- **收藏**
  - 首页条目一键收藏 / 取消收藏
  - 收藏列表按分组查看、自由排序（收藏时间升序 / 降序）
  - 分组管理：新建分组、删除分组、把收藏移动到指定分组
- **文章正文**：内嵌 WebView 打开原文链接
- **主题**：Material 3，浅色 / 深色自动跟随系统；工具栏、图标、卡片全部适配

## 技术栈

| 类别 | 选型 |
|---|---|
| 语言 / 构建 | Java 11、AGP 8.9.1、Gradle Kotlin DSL（Version Catalog 管理依赖） |
| 最低 / 目标版本 | minSdk 21、targetSdk 35 |
| UI | 传统 View + ViewBinding、Material 3（`Theme.Material3.DayNight.NoActionBar`）、ConstraintLayout |
| 列表 | RecyclerView、ViewPager2、SwipeRefreshLayout |
| 网络 | OkHttp + Gson（手写回调，无 Retrofit） |
| 图片 | Glide |
| 本地存储 | SQLite（`SQLiteOpenHelper` 单例，手写建表与增删改查） |

## 项目结构

分包规则：**数据层按职责分层，界面层按页面聚合**。

```
com.example.myproject
├── data/                         数据层
│   ├── local/                    MyDatabaseHelper（SQLite 建表与单例管理）
│   ├── model/
│   │   ├── api/                  接口 DTO：Article、ArticleList、BannerImage、ImageMessage
│   │   └── db/                   数据库实体：User、CollectionItem、CollectionGroup
│   └── repository/               MyRepository（对上层屏蔽 SQLite 细节）
├── ui/                           界面层（一个包 = 一个页面）
│   ├── login/                    MainActivity（登录 / 注册）
│   ├── home/                     HomeActivity、HomeFragment、MyAdapter、ImageSliderAdapter
│   ├── favourite/                FavouriteFragment、FavouriteAdapter、ShowGroupNameAdapter
│   ├── account/                  AccountFragment、ChangeAccountActivity
│   ├── group/                    ManageGroupActivity、ManageGroupAdapter
│   └── web/                      WebViewActivity
└── util/                         无状态工具：HttpUtil、ToastUtil
```

约定：

- Activity / Fragment / Adapter **跟着使用它的页面走**，不复用就不上提到公共包；
- 接口 DTO 与数据库实体分开，避免「服务端改个字段就牵动数据库版本」；
- 新增页面时建 `ui/<功能>/`，只有被两个以上页面复用的东西才考虑 `ui/common/`。

## 构建与运行

### 环境

- JDK 21（用 Android Studio 自带的 jbr 即可）
- Android SDK Platform 35、Build-Tools 35

### 命令

```bash
# Debug 包
./gradlew assembleDebug

# Release 包（产出 app/build/outputs/apk/release/app-release.apk）
./gradlew assembleRelease
```

Windows 下如果命令行找不到 JDK，可以显式指定：

```bash
JAVA_HOME="D:/Android/Android Studio/jbr" ./gradlew assembleDebug
```

### 关于 Release 签名

签名信息从项目根目录的 `keystore.properties` 读取：

```properties
storeFile=release.jks
storePassword=******
keyAlias=wanandroid
keyPassword=******
```

`keystore.properties` 与 `*.jks` 都在 `.gitignore` 中，**不会进仓库**。文件不存在时（例如刚 clone 下来），Release 构建会自动回退到 debug 签名，保证 `assembleRelease` 依然能产出可安装的 APK。

> 自己重新发版时请换一套新密钥，并把 keystore 与口令备份好——签名变了，老版本就无法覆盖升级。

## 接口说明

全部数据来自 wanandroid 开放 API，客户端不做任何后端改写：

- 文章列表：`GET /article/list/{page}/json`（首页轮播取 `data.banners`，文章取 `data.datas`）
- 作者字段：`author` 为空字符串时展示 `shareUser`（分享者），两者都为空才显示「佚名」
- 文章正文：直接用 WebView 打开 `link`

## 已知取舍

- **搜索功能已移除**：早期用 `String.matches()` 拼正则做关键字匹配，遇到 `(`、`*` 等元字符直接崩，功能已下线，待重写。
- 收藏表里存的是「收藏那一刻的展示作者」，历史记录若存了空串，仍会显示「佚名」。
- 密码为本地演示用途，**明文存于本地 SQLite**，仅作练习，切勿用于真实账号。
- 未开启代码混淆（`isMinifyEnabled = false`）；将来开启需给 DTO 补 `@SerializedName` 或 keep 规则，否则 Gson 反射会失效。

## 版本

- **v1.0**：首个发布版本，包含登录注册、首页文章流、收藏与分组管理、深色模式适配。

APK 见仓库的 [Releases](../../releases) 页面。

## 说明

个人学习项目，与 wanandroid 官方无关，接口与文章内容版权归原作者所有。
