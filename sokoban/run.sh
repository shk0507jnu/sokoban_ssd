#!/usr/bin/env bash
cd "$(dirname "$0")"
[ -d out ] || ./build.sh
java -Dfile.encoding=UTF-8 -cp out sokoban.Main
