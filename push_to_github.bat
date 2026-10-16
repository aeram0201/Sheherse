@echo off
echo ========================================================
echo         SheherSe - Push Project to GitHub
echo ========================================================
echo.
set /p REPO_URL="Enter your GitHub Repository URL (e.g. https://github.com/<username>/Sheherse.git): "

if "%REPO_URL%"=="" (
    echo Error: No repository URL entered. Exiting.
    pause
    exit /b 1
)

echo.
echo Setting remote origin to: %REPO_URL%
git remote remove origin 2>nul
git remote add origin %REPO_URL%
git branch -M main

echo.
echo Pushing code to GitHub...
git push -u origin main

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================================
    echo   SUCCESS! SheherSe has been pushed to GitHub!
    echo ========================================================
) else (
    echo.
    echo PUSH FAILED. Please ensure you have permission and are logged into GitHub.
)
echo.
pause
