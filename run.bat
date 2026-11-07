@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion
cd /d %~dp0backend

REM 数据库密码通过环境变量注入，配置文件里不写明文
if "%DB_PASSWORD%"=="" (
  echo [x] 还没有设置数据库密码，先在命令行执行：
  echo     set DB_PASSWORD=你的MySQL密码
  echo     然后再运行 run.bat
  pause
  exit /b 1
)

REM Spring Boot 3 需要 JDK 17 以上，挨个候选试，取第一个版本够的
set "JAVA_EXE="
set "CAND=%JAVA_HOME%\bin\java.exe" & call :try_java
set "CAND=%ProgramFiles%\Java\jdk-17\bin\java.exe" & call :try_java
set "CAND=%ProgramFiles%\Java\jdk-21\bin\java.exe" & call :try_java
set "CAND=%ProgramFiles%\Eclipse Adoptium\jdk-17\bin\java.exe" & call :try_java
set "CAND=D:\APP\Work\IDEA\IntelliJ IDEA 2026.2.3\jbr\bin\java.exe" & call :try_java

if "%JAVA_EXE%"=="" (
  echo [x] 没有找到 JDK 17 以上的 java，请安装 JDK 17+ 或设置 JAVA_HOME
  pause
  exit /b 1
)

REM 打过包就直接跑 jar，没打包就用编译产物加 lib 里的依赖
if exist "target\campus-card-1.0.0.jar" (
  echo [*] 启动 campus-card-1.0.0.jar ... http://localhost:8080
  "%JAVA_EXE%" -Dfile.encoding=UTF-8 -jar "target\campus-card-1.0.0.jar"
  pause
  exit /b 0
)

if not exist target\classes (
  echo [x] 还没编译，先执行 bash build.sh -o -q compile
  pause
  exit /b 1
)

echo [*] 启动 Spring Boot ... http://localhost:8080
"%JAVA_EXE%" -Dfile.encoding=UTF-8 -cp "target\classes;lib\*" com.lwb.campuscard.CampusCardApplication
pause
exit /b 0


:try_java
if defined JAVA_EXE goto :eof
if not exist "%CAND%" goto :eof
for /f "tokens=3" %%v in ('"%CAND%" -version 2^>^&1 ^| findstr /i "version"') do set "JVER=%%~v"
if not defined JVER goto :eof
for /f "tokens=1,2 delims=." %%a in ("!JVER!") do (
  if "%%a"=="1" (set "JMAJOR=%%b") else (set "JMAJOR=%%a")
)
if not defined JMAJOR goto :eof
if !JMAJOR! GEQ 17 set "JAVA_EXE=%CAND%"
set "JVER="
set "JMAJOR="
goto :eof
