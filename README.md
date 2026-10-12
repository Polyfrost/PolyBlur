# PolyBlur

[![Modrinth downloads](https://img.shields.io/modrinth/dt/xT45UG5o?style=flat&logo=modrinth&label=Modrinth&color=1bd96a)](https://modrinth.com/mod/polyblur)
[![CurseForge downloads](https://img.shields.io/curseforge/dt/1147793?style=flat&logo=curseforge&label=CurseForge&color=f16436)](https://www.curseforge.com/minecraft/mc-mods/polyblur)

PolyBlur adds motion blur to the world. You choose one of three blur types and set how strong the blur is.

## Features

### Blur types

- Phosphor blends each frame with the frames before it, so anything that moves leaves a trail.
- Unity blurs the picture along the direction the camera moves.
- Hybrid uses Unity for the world and Phosphor for your held item. Hybrid is the default.

PolyBlur scales the Phosphor blend by frame time, so a trail lasts as long at 60 FPS as it does at 240 FPS.

### Options

- "Blur Strength" runs from 1 to 10. The default is 3.
- "Hand Blur Strength" sets the blur on your held item in Hybrid. It runs from 1 to 10 and the default is 5.
- "Phosphor Mode" changes how Phosphor blends frames. The modes are Weighted Max, Linear Mix and Alpha Decay, and Alpha Decay is the default.
- "Motion Blur Samples" sets how many samples Unity builds the blur from, between 4 and 32. The default is 16. More samples look smoother and cost more FPS.
- "Blur Hand" is on by default. Turn it off to keep your held item sharp in Phosphor and Unity.
- "Velocity Buffer" makes Unity track movement for each pixel. It is on by default.
- "Translation Parallax" makes Unity blur when you walk, not only when you turn the camera. It is on by default and needs Velocity Buffer.
- The `/polyblur` command opens the settings.

## Differences between versions

- On 1.8.9, PolyBlur has no Velocity Buffer and no Translation Parallax.
- On 1.21.1 and 1.21.4, Hybrid applies Phosphor to the whole frame.
- On 1.21.5, Hybrid applies no blur. Use Phosphor or Unity there.

## Compatibility

PolyBlur turns Velocity Buffer off while an Iris shader pack is in use.

## Supported versions

- Minecraft 1.8.9 on Ornithe.
- Minecraft 1.21.1, 1.21.4, 1.21.5, 1.21.8, 1.21.10, 1.21.11, 26.1, 26.2 and 26.3 on Fabric.

PolyBlur needs [OneConfig](https://modrinth.com/mod/oneconfig) and Fabric Language Kotlin.

## Support

- Report bugs and ask questions on the [Polyfrost Discord](https://polyfrost.org/discord).
- Check [Polyfrost Status](https://status.polyfrost.org/) if a Polyfrost service is down.

## Building

```sh
./gradlew ":26.3:build"
```

Replace `26.3` with any supported Minecraft version. The build needs JDK 21 and JDK 25 installed.
