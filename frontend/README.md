# Lingyi Frontend Workspace

## Apps
- `apps/web-c`: C端用户前台
- `apps/web-b`: B端管理后台

## Quick Start
```bash
cd frontend
npm install
npm run dev:c
npm run dev:b
```

## One-Click (from project root, PowerShell)
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\dev-up.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\dev-status.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\dev-down.ps1
```

## Env
默认请求网关 `http://localhost:18080`，可在各应用 `.env.development` 覆盖：
- `VITE_API_BASE_URL`
