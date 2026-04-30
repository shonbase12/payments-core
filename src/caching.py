import redis
import logging
import threading
import time

# Configure logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

class RedisCache:
    def __init__(self, host='localhost', port=6379, db=0, reconnect_interval=5):
        self.host = host
        self.port = port
        self.db = db
        self.reconnect_interval = reconnect_interval
        self.cache = None
        self.lock = threading.Lock()
        self._connect()
        # Start background thread to monitor and reconnect if needed
        self.monitor_thread = threading.Thread(target=self._monitor_connection, daemon=True)
        self.monitor_thread.start()

    def _connect(self):
        try:
            self.cache = redis.StrictRedis(host=self.host, port=self.port, db=self.db, socket_connect_timeout=5, socket_timeout=5, retry_on_timeout=True)
            # Test connection
            self.cache.ping()
            logging.info("Connected to Redis successfully.")
        except redis.ConnectionError as e:
            logging.error(f"Failed to connect to Redis: {e}")
            self.cache = None

    def _monitor_connection(self):
        while True:
            if self.cache is None:
                logging.info("Attempting to reconnect to Redis...")
                with self.lock:
                    self._connect()
            else:
                try:
                    self.cache.ping()
                except redis.ConnectionError:
                    logging.warning("Lost connection to Redis, setting cache to None.")
                    self.cache = None
            time.sleep(self.reconnect_interval)

    def get(self, key):
        if not self.cache:
            raise redis.ConnectionError("Redis cache is unavailable.")
        return self.cache.get(key)

    def set(self, key, value):
        if not self.cache:
            raise redis.ConnectionError("Redis cache is unavailable.")
        self.cache.set(key, value)

redis_cache = RedisCache()

# Updated get_transaction function with improved Redis resilience

def get_transaction(user_id):
    try:
        cached_transaction = redis_cache.get(f"transaction:{user_id}")
        if cached_transaction:
            return cached_transaction
    except redis.ConnectionError as e:
        logging.error(f"Redis connection error during get for transaction {user_id}: {e}")
    except redis.RedisError as e:
        logging.error(f"Redis error during get operation for transaction {user_id}: {e}")

    # Fallback to DB fetch
    transaction = fetch_transaction_from_db(user_id)

    try:
        redis_cache.set(f"transaction:{user_id}", transaction)
    except redis.ConnectionError as e:
        logging.error(f"Redis connection error during set for transaction {user_id}: {e}")
    except redis.RedisError as e:
        logging.error(f"Redis error during set operation for transaction {user_id}: {e}")

    return transaction
