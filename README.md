<h1 align="center">
  <img width="256" src="https://raw.githubusercontent.com/raffy2010/grand-slam/master/resources/assets/logo/logo.png" alt="GrandSlam">
  <br />
  Grand-Slam
</h1>
<p align="center">a video editor based on FFmpeg, Electron, ClojureScript and Material design.</p>

## Features

- [x] Video format conversion
- [ ] Custom video conversion
- [ ] Audio conversion
- [ ] Subtitle support
- [ ] Multiple system support
- [ ] Auto update

## Download

just download from [releases](https://github.com/raffy2010/grand-slam/releases/latest)

## Development

### Prerequisite

install prebuilt Electron binary
```shell
npm install electron-prebuilt -g
```

install electron-packager
```shell
npm install electron-packager -g
```

install foreman
```shell
gem install foreman
```

start the dev flow
```shell
./scripts/foreman_start.sh
```

open the app
```shell
electron .
```

### Release

build frontend stuff
```shell
lein cljsbuild once frontend-release
```

build electron stuff
```shell
lein cljsbuild once electron-release
```

download the latest ffmpeg bundle
```shell
sh scripts/prepare_ffmpeg.sh
```

package the app
```shell
# just support mac os currently, but windows and linux versions are on the way
# use mirror to handle the bad network in China, damn it!
electron-packager . GrandSlam \
                  --download.mirror=https://npm.taobao.org/mirrors/electron/ \
                  --platform=darwin \
                  --arch=x64 \
                  --electron-version=x.x.x \
                  --logo=resources/assets/logo/logo.icns \
                  --overwrite \
                  --ignore="(electron_src|ui_src|dev_src|src|target|figwheel_server\.log|Procfile|electron-release|ui-release-out)"
```
