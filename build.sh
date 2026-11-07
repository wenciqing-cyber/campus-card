#!/usr/bin/env bash
# 编译脚本：自动寻找 JDK 17+ 与 Maven，其余参数原样交给 mvn
#   联网：bash build.sh clean package
#   离线：bash build.sh -o -q compile
set -e
cd "$(dirname "$0")/backend"

# JDK 必须 17 以上。JAVA_HOME 指向 JDK8 时直接跳过，继续往下找一个能用的
javac_major() {
    "$1/bin/javac" -version 2>&1 | sed -E 's/^javac //; s/^1\.([0-9]+).*/\1/; s/^([0-9]+)[._].*/\1/'
}
usable_jdk() {
    [ -x "$1/bin/javac" ] || return 1
    [ "$(javac_major "$1")" -ge 17 ] 2>/dev/null
}

if ! usable_jdk "$JAVA_HOME"; then
    for c in "$HOME/.jdks"/* "$HOME/Library/Java/JavaVirtualMachines"/* \
             "/Library/Java/JavaVirtualMachines"/* "/usr/lib/jvm"/* \
             "/d/APP/Work/IDEA/IntelliJ IDEA 2026.2.3/jbr"; do
        if usable_jdk "$c"; then
            export JAVA_HOME="$c"
            break
        fi
    done
fi
if ! usable_jdk "$JAVA_HOME"; then
    echo "没有找到 JDK 17 以上的版本，请设置 JAVA_HOME 或安装 JDK 17+" >&2
    exit 1
fi
export PATH="$JAVA_HOME/bin:$PATH"

# Maven：优先 PATH，其次 IDEA 内置的那份
if [ -z "$MVN" ]; then
    if command -v mvn >/dev/null 2>&1; then
        MVN=mvn
    elif [ -x "/d/APP/Work/IDEA/IntelliJ IDEA 2026.2.3/plugins/maven-plugin/lib/maven3/bin/mvn" ]; then
        MVN="/d/APP/Work/IDEA/IntelliJ IDEA 2026.2.3/plugins/maven-plugin/lib/maven3/bin/mvn"
    fi
fi
if [ -z "$MVN" ]; then
    echo "没有找到 mvn，请先安装 Maven，或用 MVN=/path/to/mvn 指定" >&2
    exit 1
fi

# 上一轮的产物有时是只读的，Maven 复制资源会失败
chmod -R u+w target 2>/dev/null || true

exec "$MVN" "$@"
