# Grand-Slam

a video editor based on FFmpeg, Electron, ClojureScript and Material design.

### Development


#### Prerequisite

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
foreman start
```

open the app
```shell
electron .
```

#### Release

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
                  --version=x.x.x \
                  --overwrite \
                  --ignore="(electron_src|ui_src|dev_src|src|target|figwheel_server\.log|Procfile|electron-release|ui-release-out)"
```
