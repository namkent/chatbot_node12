@echo off
setlocal
cd /d "%~dp0"
if not exist "data" mkdir "data"
set MINIO_ROOT_USER=minioadmin
set MINIO_ROOT_PASSWORD=minioadmin
echo [MinIO] Starting MinIO Server on port 9000 (Console: 9090)...
minio.exe server data --address :9000 --console-address :9090
