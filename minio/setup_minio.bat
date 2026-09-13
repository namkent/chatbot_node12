@echo off
setlocal
cd /d "%~dp0"
echo [MinIO Setup] Setting up alias 'myminio'...
mc.exe alias set myminio http://127.0.0.1:9000 minioadmin minioadmin
if %ERRORLEVEL% NEQ 0 (
    echo [MinIO Setup] Failed to connect to MinIO. Make sure MinIO server is running on port 9000!
    exit /b %ERRORLEVEL%
)

echo [MinIO Setup] Creating bucket 'chatbot'...
mc.exe mb myminio/chatbot --ignore-existing

echo [MinIO Setup] Setting public policy for 'chatbot'...
mc.exe anonymous set public myminio/chatbot

echo [MinIO Setup] Setting 1-day ILM expiration rule for 'chatbot'...
mc.exe ilm rule add myminio/chatbot --expire-days 1

echo [MinIO Setup] Listing ILM rules for 'chatbot':
mc.exe ilm rule list myminio/chatbot

echo [MinIO Setup] Bucket 'chatbot' is ready!
