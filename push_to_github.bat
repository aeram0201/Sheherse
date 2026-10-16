@echo off
echo ========================================================
echo         SheherSe - Push Project to GitHub
echo ========================================================
echo.
echo Target Repository: https://github.com/aeram0201/Sheherse.git
echo.

git remote remove origin 2>nul
git remote add origin https://github.com/aeram0201/Sheherse.git
git branch -M main

echo Pushing code to GitHub...
echo (If a browser or login prompt appears, sign in with aerammmm@gmail.com / aeram0201)
echo.

git push -u origin main

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================================
    echo   SUCCESS! SheherSe is now live on GitHub!
    echo   https://github.com/aeram0201/Sheherse
    echo ========================================================
) else (
    echo.
    echo ========================================================
    echo   Push failed or cancelled.
    echo   Make sure you are logged into GitHub with aeram0201.
    echo ========================================================
)
echo.
pause
