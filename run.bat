@echo off
chcp 65001 >nul
cd /d %~dp0backend

REM 数据库密码通过环境变量注入，配置文件里不写明文
if "%DB_PASSWORD%"=="" (
  echo [x] 还没有设置数据库密码，先在命令行执行：
  echo     set DB_PASSWORD=你的MySQL密码
  echo     然后再运行 run.bat
  pause
  exit /b 1
)

if not exist target\classes (
  echo [x] 还没编译，先执行 bash build.sh -q compile
  pause
  exit /b 1
)

REM 系统默认是 JDK8，Spring Boot 3 需要 17 以上，这里优先用 IDEA 自带的 JBR
set "JAVA_EXE=java"
if exist "%ProgramFiles%\Java\jdk-17\bin\java.exe" set "JAVA_EXE=%ProgramFiles%\Java\jdk-17\bin\java.exe"
if exist "D:\APP\Work\IDEA\IntelliJ IDEA 2026.2.3\jbr\bin\java.exe" set "JAVA_EXE=D:\APP\Work\IDEA\IntelliJ IDEA 2026.2.3\jbr\bin\java.exe"

echo [*] 启动 Spring Boot ... http://localhost:8080
"%JAVA_EXE%" -Dfile.encoding=UTF-8 -cp "target\classes;lib\*" com.lwb.campuscard.CampusCardApplication
pause
