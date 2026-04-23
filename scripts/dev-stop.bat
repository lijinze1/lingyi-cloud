@echo off

echo Stopping windows by title...
taskkill /FI "WINDOWTITLE eq lingyi-user*" /F >nul 2>nul
taskkill /FI "WINDOWTITLE eq lingyi-gateway*" /F >nul 2>nul
taskkill /FI "WINDOWTITLE eq lingyi-web-c*" /F >nul 2>nul
taskkill /FI "WINDOWTITLE eq lingyi-web-b*" /F >nul 2>nul

echo Stopping processes on ports 18080/18081/5173/5174...
for %%p in (18080 18081 5173 5174) do (
  for /f "tokens=5" %%a in ('netstat -ano ^| findstr :%%p') do (
    taskkill /PID %%a /F >nul 2>nul
  )
)

echo Done.
