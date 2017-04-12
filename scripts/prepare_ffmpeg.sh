ffmpeg_url="https://evermeet.cx/pub/ffmpeg/"
ffprobe_url="https://evermeet.cx/pub/ffprobe/"

test7z=$(which 7z)

file_dir=$( dirname "${BASH_SOURCE[0]}")

root_dir="$file_dir/.."

cd $root_dir

rm -rf "bin"
mkdir "bin"

if [$test7z = ""]; then
  brew install p7zip
fi

ffmpeg_latest=$(curl $ffmpeg_url | grep -o -E "ffmpeg-[0-9]{1,2}.[0-9]{1,2}(.[0-9]{1,2})*.7z" | uniq | head -n 1)
ffprobe_latest=$(curl $ffprobe_url | grep -o -E "ffprobe-[0-9]{1,2}.[0-9]{1,2}(.[0-9]{1,2})*.7z" | uniq | head -n 1)

wget -O "bin/$ffmpeg_latest" "$ffmpeg_url$ffmpeg_latest"
wget -O "bin/$ffprobe_latest" "$ffprobe_url$ffprobe_latest"

7z x "bin/$ffmpeg_latest" -obin
7z x "bin/$ffprobe_latest" -obin

rm "bin/$ffmpeg_latest"
rm "bin/$ffprobe_latest"
