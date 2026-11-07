#!/usr/bin/env bash
# Linux / macOS 启动脚本，与 run.bat 等价
set -e
cd "$(dirname "$0")/backend"

if [ -z "$DB_PASSWORD" ]; then
    echo "还没有设置数据库密码，先执行： export DB_PASSWORD=你的MySQL密码" >&2
    exit 1
fi

# 只认 17 以上的 java：系统 PATH 里若是 JDK8，Spring Boot 3 报“找不到主类”
java_major() {
    "$1" -version 2>&1 | awk -F'"' '/version/ {print $2; exit}' | awk -F. '{ if ($1 == 1) print $2; else print $1 }'
}

JAVA_EXE=""
for c in "$JAVA_HOME/bin/java" "$HOME/.jdks"/*/bin/java \
         "$HOME/Library/Java/JavaVirtualMachines"/*/Contents/Home/bin/java \
         "/Library/Java/JavaVirtualMachines"/*/Contents/Home/bin/java \
         "/usr/lib/jvm"/*/bin/java \
         "/d/APP/Work/IDEA/IntelliJ IDEA 2026.2.3/jbr/bin/java"; do
    if [ -x "$c" ] && [ "$(java_major "$c")" -ge 17 ] 2>/dev/null; then
        JAVA_EXE="$c"
        break
    fi
done
if [ -z "$JAVA_EXE" ]; then
    echo "没有找到 JDK 17 以上的 java，请设置 JAVA_HOME" >&2
    exit 1
fi

if [ -f target/campus-card-1.0.0.jar ]; then
    echo "[*] 启动 campus-card-1.0.0.jar ... http://localhost:8080"
    exec "$JAVA_EXE" -Dfile.encoding=UTF-8 -jar target/campus-card-1.0.0.jar
fi

if [ ! -d target/classes ]; then
    echo "还没编译，先执行 bash build.sh -o -q compile" >&2
    exit 1
fi

# Git Bash 里 classpath 也要用 Windows 的分号，并且别让 MSYS 改写参数
case "$(uname -s)" in
    MINGW*|MSYS*|CYGWIN*)
        export MSYS2_ARG_CONV_EXCL='*'
        CP="target/classes;lib/*" ;;
    *)
        CP="target/classes:lib/*" ;;
esac

echo "[*] 启动 Spring Boot ... http://localhost:8080"
exec "$JAVA_EXE" -Dfile.encoding=UTF-8 -cp "$CP" com.lwb.campuscard.CampusCardApplication
