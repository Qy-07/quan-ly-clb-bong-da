@echo off
setlocal
REM Build and run the project with proper package/classpath settings
cd /d "%~dp0"
set "SRC=src"
set "OUT=out"
if not exist "%OUT%" mkdir "%OUT%"

echo === Compiling (UTF-8) ===
javac -encoding UTF-8 -d "%OUT%" -sourcepath "%SRC%" "%SRC%\qlclb\Main.java" "%SRC%\qlclb\auth\AuthService.java" "%SRC%\qlclb\auth\User.java" "%SRC%\qlclb\model\CauThu.java" "%SRC%\qlclb\model\HauVe.java" "%SRC%\qlclb\model\ThuMon.java" "%SRC%\qlclb\model\TienDao.java" "%SRC%\qlclb\model\TienVe.java" "%SRC%\qlclb\model\TinhTrang.java" "%SRC%\qlclb\service\QuanLyCauThu.java" "%SRC%\qlclb\service\QuanLyDoiBong.java" "%SRC%\qlclb\ui\GUIMain.java" "%SRC%\qlclb\ui\Main.java"
if errorlevel 1 (
  echo Compile failed.
  exit /b 1
)

echo === Running ===
java -cp "%OUT%" qlclb.ui.GUIMain
endlocal