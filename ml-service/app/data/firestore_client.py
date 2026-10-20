import logging
import os
from typing import Optional, Any
from app.config import settings

logger = logging.getLogger("sheherse.firestore")

_firebase_app = None
_firestore_db = None

def initialize_firebase():
    global _firebase_app, _firestore_db
    if _firebase_app is not None:
        return _firestore_db

    cred_path = settings.firebase_credentials_path
    if not cred_path or not os.path.exists(cred_path):
        logger.info("Firebase credentials path not provided or not found on disk. Operating in local seed/fallback mode.")
        return None

    try:
        import firebase_admin
        from firebase_admin import credentials, firestore

        # Check if already initialized by another module
        if not firebase_admin._apps:
            cred = credentials.Certificate(cred_path)
            _firebase_app = firebase_admin.initialize_app(cred)
            logger.info("Firebase Admin SDK successfully initialized.")
        else:
            _firebase_app = firebase_admin.get_app()

        _firestore_db = firestore.client()
        return _firestore_db
    except Exception as e:
        logger.warning(f"Failed to initialize Firebase Admin SDK: {e}. Falling back to local data source.")
        return None

def get_firestore_db() -> Optional[Any]:
    global _firestore_db
    if _firestore_db is None:
        _firestore_db = initialize_firebase()
    return _firestore_db

def verify_firebase_token(id_token: str) -> Optional[dict]:
    """
    Verifies a Firebase ID token sent in the Authorization header.
    Returns decoded token dictionary on success, or None on failure.
    """
    if not id_token:
        return None

    try:
        import firebase_admin
        from firebase_admin import auth

        if not firebase_admin._apps:
            initialize_firebase()

        decoded_token = auth.verify_id_token(id_token)
        return decoded_token
    except Exception as e:
        logger.warning(f"Firebase token verification failed: {e}")
        return None
