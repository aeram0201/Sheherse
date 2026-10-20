@echo off
set PYTHONPATH=%~dp0;%~dp0venv\Lib\site-packages
where python >nul 2>nul
if %errorlevel% equ 0 (
    python run_tests.py
) else (
    "C:\Program Files\KiCad\10.0\bin\python.exe" run_tests.py
)
pause
