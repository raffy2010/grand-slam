file_dir=$( dirname "${BASH_SOURCE[0]}")

lein clean

RUBYOPT="-r $file_dir/sync_stdout.rb" foreman start
