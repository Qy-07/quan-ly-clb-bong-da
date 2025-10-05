@echo off
setlocal
REM Xây dựng và chạy dự án với cài đặt package/classpath đúng

REM Thiết lập thư mục nguồn và đầu ra
set "SRC=src"
set "OUT=out"

REM Tạo thư mục đầu ra nếu chưa tồn tại
if not exist "%OUT%" mkdir "%OUT%"

echo === Đang biên dịch (UTF-8) ===
javac -encoding UTF-8 -d "%OUT%" -sourcepath "%SRC%" -cp "%SRC%" %SRC%\qlclb\ui\GUIMain.java
if errorlevel 1 (
    echo Biên dịch thất bại.
    exit /b 1
)

echo === Đang chạy ===
java -cp "%OUT%" qlclb.ui.GUIMain

endlocal
