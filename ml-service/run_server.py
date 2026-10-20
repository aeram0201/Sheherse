import os
import sys
from pathlib import Path

# Add ml-service root and virtualenv site-packages to sys.path
BASE_DIR = Path(__file__).resolve().parent
SITE_PACKAGES = BASE_DIR / "venv" / "Lib" / "site-packages"

if str(BASE_DIR) not in sys.path:
    sys.path.insert(0, str(BASE_DIR))
if str(SITE_PACKAGES) not in sys.path:
    sys.path.insert(0, str(SITE_PACKAGES))

import uvicorn
from app.config import settings

if __name__ == "__main__":
    print(f"Starting {settings.service_name} on {settings.host}:{settings.port}...")
    print(f"API Docs available at: http://localhost:{settings.port}/docs")
    uvicorn.run(
        "app.main:app",
        host=settings.host,
        port=settings.port,
        reload=settings.debug
    )
