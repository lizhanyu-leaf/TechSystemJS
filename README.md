## 简介

> TechSystemJS 是一个基于 KubeJS 的扩展模组，允许你在非 reload 时机加载并执行配方调整相关的 JavaScript 代码，有效避免 /reload 带来的性能开销。

---

## 详细介绍

### TechSystemJS — 让配方加载不再受限于重载

在 KubeJS 的原生机制中，许多配方调整代码需要在游戏重载（/reload）或重新进入世界时才会生效。对于整合包中的特殊机制（如配方锁定、分段解锁等），这意味着频繁的等待与反复操作，极大地拖慢了开发与测试节奏。

**TechSystemJS** 正是为了解决这一痛点而生。

#### 核心功能

- **灵活的加载时机**：支持在非 reload 时机触发配方 JS 代码执行，彻底避免 /reload 带来的巨大开销，让配方调整即时生效。

- **兼容良好**：得益于本模组的底层机制，无需担心其他模组的配方变更会绕过配方锁或造成冲突，确保整套逻辑稳定可控。

- **轻量高效**：专注单一职责，不引入多余依赖，对游戏性能几乎零影响。

- **迁移简单**：本模组提供的事件与 KubeJS 原生 `ServerEvents.recipes` 事件所传递的 `event` 对象完全一致，迁移成本几乎为零，现有代码无需大幅改动即可接入。

#### 适用场景

- 整合包中采用分段式配方解锁、阶段性 progression 等特殊机制。
- 需要根据玩家进度、游戏状态等条件动态调整配方的场景。
- 开发过程中希望快速迭代配方逻辑，无需反复执行 /reload。

#### 技术说明

本模组不替代 KubeJS 原有的加载机制，而是作为补充，提供更灵活的代码执行入口。它既可以作为开发阶段的效率工具，也可作为动态配方系统的底层支撑，服务于整合包的深度定制需求。

---

## Introduction

> TechSystemJS is a KubeJS-based extension mod that allows you to load and execute recipe-adjusting JavaScript code outside of reload events, effectively avoiding the performance cost of /reload.

---

## Detailed Description

### TechSystemJS — Free Recipe Loading from Reloads

In KubeJS's native mechanism, many recipe adjustment scripts only take effect after a game reload (/reload) or a world re-entry. For modpack-specific mechanics such as recipe locking and staged unlocking, this often means frequent waiting and repetitive operations — significantly slowing down development and testing.

**TechSystemJS** is built to solve exactly this problem.

#### Core Features

- **Flexible Loading Timing** — Execute recipe JS code outside of reload events, eliminating the heavy overhead of /reload and making recipe adjustments take effect immediately.

- **Great Compatibility** — Thanks to the mod's underlying design, there is no need to worry about other mods' recipe changes bypassing recipe locks or causing conflicts, ensuring the entire logic remains stable and controllable.

- **Lightweight & Efficient** — Focuses on a single responsibility with no unnecessary dependencies, and has virtually zero impact on game performance.

- **Easy Migration** — The event provided by this mod passes an `event` object that is fully consistent with KubeJS's native `ServerEvents.recipes` event. Migration costs are nearly zero — existing code can be integrated with minimal changes.

#### Use Cases

- Modpacks with staged recipe unlocking, phased progression, and other special mechanics.
- Scenarios where recipes need to be dynamically adjusted based on player progress or game state.
- Development scenarios where you want to quickly iterate on recipe logic without repeatedly running /reload.

#### Technical Note

This mod does not replace KubeJS's existing loading mechanism, but rather serves as a complementary extension that provides a more flexible execution entry point. It can be used both as a development efficiency tool and as the underlying foundation for dynamic recipe systems in deeply customized modpacks.