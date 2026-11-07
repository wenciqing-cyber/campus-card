#!/usr/bin/env bash
# 本机离线构建脚本（网络不通，全部依赖取自 ~/.m2）
set -e
export JAVA_HOME="/d/APP/Work/IDEA/IntelliJ IDEA 2026.2.3/jbr"
export PATH="$JAVA_HOME/bin:$PATH"
MVN="/d/APP/Work/IDEA/IntelliJ IDEA 2026.2.3/plugins/maven-plugin/lib/maven3/bin/mvn"
cd "$(dirname "$0")/backend"
# 沙箱有时把上一轮产物写成只读，先解锁再编译
chmod -R u+w target 2>/dev/null || true
exec bash "$MVN" -o "$@"
