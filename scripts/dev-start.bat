@echo off
setlocal

set ROOT=%~dp0..
cd /d "%ROOT%"

if /I not "%1"=="--skip-build" (
  echo [1/2] Compile backend once...
  call mvn.cmd -DskipTests -pl lingyi-common/lingyi-common-core,lingyi-common/lingyi-common-web,lingyi-service/lingyi-user,lingyi-gateway/lingyi-gateway-server compile
  if errorlevel 1 (
    echo Compile failed.
    exit /b 1
  )
)

echo [2/2] Start 2 backend + 2 frontend...

start "lingyi-user" cmd /k "cd /d %ROOT% && set NACOS_ADDR=127.0.0.1:8848 && set NACOS_USERNAME=nacos && set NACOS_PASSWORD=nacos && set SPRING_PROFILES_ACTIVE=dev && set MYSQL_HOST=127.0.0.1 && set MYSQL_PORT=3306 && set MYSQL_DB=lingyi && set MYSQL_USERNAME=root && set MYSQL_PASSWORD=123456 && set JWT_SECRET=lingyi-dev-secret-please-change && mvn.cmd -pl lingyi-service/lingyi-user -DskipTests spring-boot:run"

start "lingyi-gateway" cmd /k "cd /d %ROOT% && set NACOS_ADDR=127.0.0.1:8848 && set NACOS_USERNAME=nacos && set NACOS_PASSWORD=nacos && set SPRING_PROFILES_ACTIVE=dev && set JWT_SECRET=lingyi-dev-secret-please-change && mvn.cmd -pl lingyi-gateway/lingyi-gateway-server -DskipTests spring-boot:run"

start "lingyi-web-c" cmd /k "cd /d %ROOT%\frontend && npm.cmd run dev:c"
start "lingyi-web-b" cmd /k "cd /d %ROOT%\frontend && npm.cmd run dev:b"

echo.
echo Started:
echo   user    -> http://localhost:18081
echo   gateway -> http://localhost:18080
echo   web-c   -> http://localhost:5173
echo   web-b   -> http://localhost:5174
echo.
echo First run:
echo   cd /d "%ROOT%\frontend" ^&^& npm.cmd install
echo.
echo Skip compile:
echo   scripts\dev-start.bat --skip-build
