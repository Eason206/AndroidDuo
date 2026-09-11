# Android Duo

实验性 Android 折叠 / 倾斜屏幕动画原型
An experimental Android fold / tilt screen animation prototype.

---

## 简介 · About

Android Duo 是一个用于探索 Android 折叠屏、设备倾斜以及玻璃屏幕视觉效果的实验性项目。
Android Duo is an experimental Android project for exploring foldable screens, device tilt, and glass-like visual effects.

项目尝试通过透视投影、固定边缘几何以及空间模糊，让屏幕在动画过程中表现得更加接近真实的物理玻璃表面，而不是简单地进行图片裁切或缩放。
The project uses perspective-based reprojection, fixed-edge geometry, and spatial blur to make the screen behave more like a physical glass surface instead of a simple bitmap crop or scale animation.

当前版本主要用于验证核心动画与渲染系统，UI仍处于原型阶段。
The current version mainly focuses on validating the core animation and rendering system, while the UI is still in an early prototype stage.

当前最新版本为 0.1.0。
The current version is 0.1.0.

---

## 功能 · Features

固定边缘折叠 / 倾斜动画 · Fixed-edge fold / tilt animation

透视屏幕重新投影 · Perspective-based screen reprojection

完整源图像参与投影，不使用简单裁切 · Full source image projection without simple cropping

基于屏幕距离的空间模糊 · Distance-based spatial blur

基于距离的画面明暗变化 · Distance-based screen dimming

折叠屏设备动画支持 · Foldable device animation support

普通直板设备倾斜模拟 · Slab device tilt simulation

设备姿态 / 传感器驱动动画 · Sensor / device-orientation driven animation

自定义内屏与外屏图片 · Custom inner and outer screen images

Android系统图片选择器 · Android system image picker

---

## 工作原理 · How It Works

Android Duo并不是简单地对Bitmap进行`scale`或`crop`。

Android Duo does not simply scale or crop a bitmap.

渲染器将完整源图像视为一个固定的屏幕平面，并模拟一个可以发生旋转和位移的玻璃表面。

The renderer treats the complete source image as a fixed screen plane and simulates a glass surface that can rotate and move.

基本过程可以理解为：

The basic process can be understood as:

```text
输出像素 · Output Pixel
        ↓
旋转后的玻璃表面 · Rotated Glass Surface
        ↓
观察点 / 摄像机射线 · Eye / Camera Ray
        ↓
固定屏幕平面 · Fixed Screen Plane
        ↓
源图像 · Source Image
```

这样可以让屏幕的一侧保持固定，而另一侧随着折叠或倾斜逐渐发生空间形变。

This allows one edge of the screen to remain fixed while the opposite side progressively deforms during folding or tilting.

---

## 固定边缘 · Fixed Edge

Android Duo的一个核心设计是固定屏幕的一侧。

One of the core ideas of Android Duo is keeping one edge of the screen fixed.

例如在倾斜过程中：

For example, during a tilt:

```text
左侧固定 · Left edge fixed

┃────────────────╲
┃                 ╲
┃                  ╲
┃                   ╲
```

或者：

```text
右侧固定 · Right edge fixed

╱────────────────┃
╱                 ┃
╱                 ┃
╱                 ┃
```

固定边缘不会随着动画整体移动。

The fixed edge remains at the same screen position throughout the transition.

因此动画不会简单地表现为整张图片缩放、平移或裁切。

This prevents the transition from looking like a simple global scale, translation, or crop animation.

---

## 空间模糊 · Spatial Blur

随着模拟玻璃表面逐渐远离固定屏幕平面，画面会产生越来越明显的空间模糊。

As the simulated glass surface moves farther away from the fixed screen plane, the projected image becomes progressively more blurred.

```text
距离较近 · Smaller distance
        ↓
画面较清晰 · Sharper image
        ↓
距离增加 · Larger distance
        ↓
画面更加模糊 · Stronger blur
```

模糊直接作用于投影后的画面，而不是简单叠加一层半透明颜色。

The blur is applied spatially to the projected image rather than using a simple translucent overlay.

---

## 设备模式 · Device Modes

### 折叠屏设备 · Foldable Devices

对于折叠屏设备，Android Duo可以使用设备提供的姿态或折叠角度信息驱动动画。

For foldable devices, Android Duo can use available device posture or fold-angle information to drive the animation.

外屏和内屏使用不同的固定边缘以及运动方向。

The outer and inner screens use different fixed edges and movement directions.

### 直板设备 · Slab Devices

对于普通直板手机，Android Duo可以使用设备姿态 / 旋转传感器模拟类似的玻璃倾斜效果。

For conventional slab phones, Android Duo can use device orientation / rotation sensors to simulate a similar glass-tilt effect.

---

## 界面 · Screenshots

当前版本主要用于动画和渲染测试，UI仍属于早期原型。

The current version is primarily intended for animation and rendering testing, and the UI is still an early prototype.

未来版本将加入更加完整的壁纸设置以及现代化UI。

Future versions will introduce a more complete wallpaper settings experience and a redesigned modern UI.

---

## 下载 · Download

### 最新版本 · Latest Release

**Android Duo v0.1.0**

**下载 APK · Download APK**

请前往 GitHub Releases 下载最新版本。

Please visit GitHub Releases to download the latest version.

需要 Android 8.0（API 26）或更高版本。
Android 8.0 (API 26) or later is required.

当前发布版本为 Android Debug 测试版本，主要用于实验和功能测试。
The current release is an Android Debug build intended primarily for experimentation and testing.

---

## 安装 · Installation

从 Releases 下载最新 APK。
Download the latest APK from Releases.

安装 APK。
Install the APK on your Android device.

打开 Android Duo。
Open Android Duo.

选择或使用默认的内屏 / 外屏图片。
Select or use the default inner / outer screen images.

点击“开启测试”开始动画测试。
Tap “Start Test” to start the animation test.

---

## 技术信息 · Technical Details

| 项目 · Item             | 信息 · Information                         |
| --------------------- | ---------------------------------------- |
| 开发语言 · Language       | Kotlin                                   |
| UI · UI Framework     | Android Views / Custom View              |
| 图形渲染 · Graphics       | Custom Renderer / AGSL                   |
| 最低版本 · Minimum SDK    | Android 8.0 (API 26)                     |
| 目标版本 · Target SDK     | Android 15 (API 35)                      |
| 编译版本 · Compile SDK    | Android 16 (API 37)                      |
| Android Gradle Plugin | 9.4.0                                    |
| Gradle                | 9.6.1                                    |
| Java                  | OpenJDK 17                               |
| Shader                | Android Runtime Shader / AGSL            |
| Animation             | Custom fold / tilt animation             |
| Sensors               | Android device rotation / motion sensors |

核心渲染器使用透视重新投影来计算屏幕表面的可见位置。

The core renderer uses perspective-based reprojection to calculate the visible position of the screen surface.

在支持的 Android 版本上使用 AGSL 进行逐像素处理，并在不支持的情况下提供备用渲染路径。

AGSL is used for per-pixel processing on supported Android versions, with a fallback rendering path for unsupported environments.

---

## 项目结构 · Project Structure

主要渲染流程：

Main rendering flow:

```text
MainActivity
    ↓
FoldAnimationView
    ↓
FoldRenderer
    ├── FoldGeometry
    ├── FoldProgressSource
    ├── ScreenSurface
    ├── ScreenContentSource
    └── Device / Sensor Sources
```

项目将动画状态、设备姿态、屏幕内容、几何计算以及渲染逻辑分离，以方便后续继续进行实验和优化。

The project separates animation state, device posture, screen content, geometry, and rendering logic to make further experimentation and optimization easier.

---

## 开源参考 · Open Source Reference

本项目的部分折叠与玻璃屏幕视觉效果设计参考了开源社区中的相关项目与研究。

Some of the fold and glass-like screen effect concepts in this project were inspired by related open-source projects and research from the community.

特别参考：

Special reference:

**DuoLikeAnimation**

https://github.com/elijah-semyonov/DuoLikeAnimation

感谢该项目在折叠动画、固定界面平面、透视重新投影以及空间模糊等方面提供的思路参考。

Special thanks to the project for providing useful ideas and references for fold animation, fixed interface planes, perspective reprojection, and spatial blur.

Android Duo为独立开发项目，与Apple、Samsung、Xiaomi、Google或任何其他设备制造商不存在官方关联。

Android Duo is an independent project and is not officially affiliated with Apple, Samsung, Xiaomi, Google, or any other device manufacturer.

---

## 更新历史 · Version History

### v0.1.0

首个公开版本 · Initial public release

基础折叠 / 倾斜动画 · Basic fold / tilt animation

固定边缘动画逻辑 · Fixed-edge animation behavior

透视屏幕重新投影 · Perspective-based screen reprojection

空间模糊 · Spatial blur

折叠屏与直板设备模式 · Foldable and slab device modes

自定义内屏 / 外屏图片 · Custom inner / outer screen images

Android系统图片选择器 · Android system image picker

早期原型UI · Early prototype UI

---

## 开源 · Open Source

本项目使用Kotlin开发，并以学习、研究、实验和个人使用为主要目的。

This project is developed with Kotlin and is primarily intended for learning, research, experimentation, and personal use.

---

## 致谢 · Credits

特别感谢以下开源项目与作者：

Special thanks to the following open-source projects and authors:

### DuoLikeAnimation

感谢`elijah-semyonov/DuoLikeAnimation`项目，为折叠、倾斜以及玻璃屏幕过渡效果提供了有价值的参考。

Special thanks to `elijah-semyonov/DuoLikeAnimation` for providing valuable references for fold, tilt, and glass-like screen transition effects.

感谢所有开源项目的作者与贡献者。

Thanks to all open-source authors and contributors who make projects like this possible.

---

# Android Duo

由 Eason206 制作
Made by Eason206
