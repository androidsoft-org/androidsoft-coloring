#!/bin/bash

for i in `find /home/user/Android/Sdk/platforms/android-29/data/res/ | grep $1.png`; do
   mkdir -p src/main/res/`basename \`dirname $i\``
   cp $i src/main/res/`basename \`dirname $i\``/$1.png
done
