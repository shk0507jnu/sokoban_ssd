#!/usr/bin/env bash
# 테스트 컴파일 + 실행 (외부 라이브러리 없이 자체 러너 사용)
set -e
cd "$(dirname "$0")"
SEP=":"; case "$(uname)" in MINGW*|MSYS*|CYGWIN*) SEP=";";; esac
./build.sh
rm -rf out-test && mkdir -p out-test
javac -encoding UTF-8 -cp out -d out-test $(find test -name '*.java')
java -Dfile.encoding=UTF-8 -cp "out${SEP}out-test" sokoban.AllTests
