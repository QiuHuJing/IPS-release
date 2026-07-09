<div align="center">

# 🏃 无尽跑酷 Infinite Parkour
### Folia 1.21.x 适配 & 汉化重构版

<strong>
原项目: <a href="https://github.com/Efnilite/Walk-in-the-Park">Walk-in-the-Park</a> |
原作者: <a href="https://github.com/Efnilite">Efnilite</a> |
Folia适配 & 汉化: 叶小光
</strong>

<br>

**✨ 基于原版 Infinite Parkour v5.3.1 的 Folia 适配重构版本 ✨**

[![Version](https://img.shields.io/badge/version-5.3.2--Folia-blue)](https://github.com)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.x-green)](https://papermc.io)
[![Folia](https://img.shields.io/badge/Folia-supported-brightgreen)](https://github.com/PaperMC/Folia)
[![Java](https://img.shields.io/badge/Java-21-orange)](https://adoptium.net)
[![License](https://img.shields.io/badge/license-MIT-lightgrey)](LICENSE)

</div>

---

## 📖 简介

**无尽跑酷**（Infinite Parkour）是一个**无限自动生成跑酷**的 Minecraft 服务端插件。玩家加入后进入无限延伸的跑酷世界，每次跳跃都是独特的——方块随机生成、结构随机出现、分数实时计算。

### 🔄 与原版的区别

| 项目 | 原版 (v5.3.1) | 本重构版 (v5.3.2-Folia) |
|------|:--:|:--:|
| 服务端 | Spigot/Paper 1.20.x | **Folia/Luminol 1.21.8+** |
| API | spigot-api 1.20.4 | paper-api 1.21.4 |
| Java | 17 | 21 |
| 默认语言 | English | **简体中文** + 多语言 |
| 调度器 | Bukkit.getScheduler() | **GlobalRegionScheduler + AsyncScheduler** |
| 世界创建 | Bukkit.createWorld() | **已有世界回退 + NBT 反射** |
| 方块操作 | 全局线程定时器 | **PlayerMoveEvent 区域线程** |
| 配置文件 | 英文注释 | **全中文注释** |
| 命令行 | 英文 | **中英双语** |
| 配置管理 | 手动编辑 YAML | **`/ip settings` GUI + 命令行** |
| PAPI 变量 | 基础 10+ 变量 | **30+ 丰富变量** |
| 全息投影 | HolographicDisplays v3 | **HD v3 + DecentHolograms + FancyHolograms** |
| 记分板 | FastBoard v2.1.2 | **1.21 兼容存根** |
| 粒子系统 | vilib 原版 | **1.21 兼容替换** |

---

## ✨ 特性

- 🎲 **无限自动生成** — 每次跳跃随机生成距离、高度、方块类型
- 🏗 **结构跳跃** — 内置 35+ 种跑酷结构（schematic）
- 🎨 **16+ 方块风格** — 红石、海洋、末地、彩虹……一键切换
- 🏆 **排行榜** — 分数/时间/难度三维排序，支持 MySQL 跨服
- 👥 **多人同场** — 大厅系统、旁观模式
- 💬 **聊天分区** — 大厅/玩家/公开三种模式
- 🎁 **奖励** — 分数/间隔/一次性奖励，支持 Vault 经济
- 🖥 **全息排行榜** — HD / DecentHolograms / FancyHolograms
- 📊 **PAPI** — 30+ 内置变量
- 🌍 **多语言** — 简体中文（默认）/ English / 日本語 / Français / Nederlands
- 🎮 **GUI 配置** — `/ip settings gui` 图形化管理所有配置
- 🖥 **控制台配置** — 命令行增删改查配置，即时生效

---

## 📥 安装

### 前置要求
- **服务端**: Paper 1.21.x 或 Folia/Luminol 1.21.8+
- **Java**: 21+
- **可选**: PlaceholderAPI, Vault, HolographicDisplays / DecentHolograms / FancyHolograms, Multiverse-Core

### 安装步骤
1. 下载 `IP-5.3.2-Folia.jar`
2. 放入 `plugins/` 文件夹
3. 重启服务器
4. （Folia 用户）插件自动回退到已有世界运行
5. 进入服务器，`/parkour` 打开菜单开始！

---

## 🎮 指令

### 玩家指令
| 指令 | 说明 |
|------|------|
| `/parkour` / `/跑酷` | 打开主菜单 |
| `/parkour join` | 加入跑酷 |
| `/parkour leave` | 退出跑酷 |
| `/parkour play` | 模式选择菜单 |
| `/parkour leaderboard` | 排行榜菜单 |

### 管理员指令
| 指令 | 说明 |
|------|------|
| `/ip settings` | 配置管理帮助 |
| `/ip settings gui` | 🆕 GUI 配置管理器 |
| `/ip settings list <cfg>` | 列出配置内容 |
| `/ip settings get <cfg:path>` | 获取配置值 |
| `/ip settings set <cfg:path> <val>` | 修改配置（自动保存+重载） |
| `/ip settings reload` | 重载所有配置 |
| `/ip reload` | 重载配置 |
| `/ip reset <everyone/player>` | 重置记录 |
| `/ip forcejoin <everyone/nearest/player>` | 强制加入 |
| `/ip forceleave <everyone/player>` | 强制退出 |

---

## 📊 PAPI 变量

> 前缀: `%witp_<变量>%`

### 全局（无需玩家）
| 变量 | 返回值 |
|------|--------|
| `version` | 插件版本 |
| `top_player` | 🆕 榜首玩家 |
| `top_score` | 🆕 榜首分数 |
| `playing` | 🆕 在线人数 |
| `total_players` | 🆕 总玩家数 |

### 玩家通用
| 变量 | 返回值 |
|------|--------|
| `rank` | 排名 |
| `high_score` | 最高分 |
| `is_playing` | 🆕 是否游戏中 |
| `locale` | 🆕 语言 |

### 游戏中
| 变量 | 返回值 |
|------|--------|
| `score` | 当前分数 |
| `total_score` | 🆕 总分 |
| `time` | 计时 |
| `style` | 方块风格 |
| `difficulty_string` | 🆕 难度文字 |
| `particles_enabled` | 🆕 粒子 |
| `sound_enabled` | 🆕 音效 |
| `session_players` | 🆕 同场人数 |
| `score_until_N` | 距间隔奖励 |

### 模式特定
| 格式 | 示例 |
|------|------|
| `<mode>_rank_N_name` | 🆕 `default_rank_1_name` |
| `<mode>_rank_N_score` | 🆕 `default_rank_3_score` |
| `<mode>_player_rank` | 🆕 `default_player_rank` |

---

## ⚙️ 配置

所有配置文件已全面汉化，默认语言为简体中文。

```
config.yml          — 主配置
generation.yml      — 生成设置
rewards-v2.yml      — 奖励配置
locales/zh_cn.yml   — 简体中文语言
```

### 全息排行榜配置示例：
```yaml
holograms:
  decentholograms:
    enabled: true
    leaderboards:
      - location: "world,0,100,0"
        mode: default
        lines: 10
```

---

## 🔧 技术细节

### Folia 兼容层
| 系统 | 原实现 | 适配方案 |
|------|--------|---------|
| 任务调度 | `Bukkit.getScheduler()` | `GlobalRegionScheduler` + `AsyncScheduler` |
| 方块操作 | 全局定时器 | `PlayerMoveEvent` 区域线程 |
| 世界创建 | `Bukkit.createWorld()` | 已有世界回退 + NBT |
| 玩家传送 | `player.teleport()` | `PaperLib.teleportAsync()` |
| 粒子系统 | vilib 原版 | 1.21 兼容替换 |
| 记分板 | FastBoard v2.1.2 | 1.21 兼容存根 |

### 构建
```bash
mvn clean package -DskipTests
```
输出: `target/IP-5.3.2-Folia.jar`

---

## 🙏 致谢

- **原作者**: [Efnilite](https://github.com/Efnilite) — 创造了优秀的 Infinite Parkour
- **原版仓库**: [Walk-in-the-Park](https://github.com/Efnilite/Walk-in-the-Park)
- **Folia 适配 & 汉化重构**: 叶小光

### 使用的开源库
- [vilib](https://github.com/Efnilite/vilib) — 工具库
- [PaperLib](https://github.com/PaperMC/PaperLib) — 异步传送
- [FastBoard](https://github.com/MrMicky-FR/FastBoard) — 记分板 API
- [PlaceholderAPI](https://github.com/PlaceholderAPI/PlaceholderAPI)
- [HolographicDisplays](https://github.com/filoghost/HolographicDisplays)
- [DecentHolograms](https://github.com/DecentSoftware-eu/DecentHolograms)
- [FancyHolograms](https://github.com/FancyMcPlugins/FancyHolograms)

---

## 📄 许可证

本项目基于原版 [Walk-in-the-Park](https://github.com/Efnilite/Walk-in-the-Park) 的 MIT 许可证进行重构。

```
MIT License — Copyright (c) 2020-2024 Efnilite
Folia adaptation & Chinese localization — 叶小光
```
