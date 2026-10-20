import os
import sys
from pathlib import Path

# Ensure ml-service root and virtualenv site-packages are first in sys.path
BASE_DIR = Path(__file__).resolve().parent
SITE_PACKAGES = BASE_DIR / "venv" / "Lib" / "site-packages"

if str(BASE_DIR) not in sys.path:
    sys.path.insert(0, str(BASE_DIR))
if str(SITE_PACKAGES) not in sys.path:
    sys.path.insert(0, str(SITE_PACKAGES))

import pytest

if __name__ == "__main__":
    test_dir = str(BASE_DIR / "tests")
    exit_code = pytest.main([test_dir, "-v", "--tb=short"])
    sys.exit(exit_code)
