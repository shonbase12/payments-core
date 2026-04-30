import redis
import logging
import threading
import time

# Configure logging
logging.basicConfig(level=logging.ERROR, format='%(asctime)s - %(levelname)s - %(message)s')

class RedisCache:
    def __init__(self, host='localhost', port=6379, db=0, max_retries=5, retry_delay=5):
        self.host = host
        self.port = port
        self.db = db
        self.max_retries = max_retries
        self.retry_delay = retry_delay
        self.cache = None
        self.lock = threading.Lock()
        self.circuit_open = False
        self.failure_count = 0
        self.failure_threshold = 3
        self._connect_with_retry()

        # Start a background thread to monitor Redis health and reconnect
        self.monitor_thread = threading.Thread(target=self._monitor_redis, daemon=True)
        self.monitor_thread.start()

    def _connect_with_retry(self):
        retries = 0
        while retries < self.max_retries:
            try:
                self.cache = redis.StrictRedis(host=self.host, port=self.port, db=self.db)
                # Test connection
                self.cache.ping()
                self.circuit_open = False
                self.failure_count = 0
                logging.error(f"Connected to Redis at {self.host}:{self.port}")
                return
            except redis.ConnectionError as e:
                retries += 1
                logging.error(f"Failed to connect to Redis (attempt {retries}): {e}")
                time.sleep(self.retry_delay)
        self.cache = None
        self.circuit_open = True

    def _monitor_redis(self):
        while True:
            if self.circuit_open:
                try:
                    temp_cache = redis.StrictRedis(host=self.host, port=self.port, db=self.db)
                    temp_cache.ping()
                    with self.lock:
                        self.cache = temp_cache
                        self.circuit_open = False
                        self.failure_count = 0
                    logging.error("Redis connection restored, circuit closed.")
                except redis.ConnectionError:
                    pass
            time.sleep(10)  # Check every 10 seconds

    def get(self, key):
        if self.circuit_open or not self.cache:
            raise redis.ConnectionError("Redis circuit is open or cache is not available.")
        try:
            return self.cache.get(key)
        except redis.RedisError as e:
            self._record_failure()
            raise e

    def set(self, key, value):
        if self.circuit_open or not self.cache:
            raise redis.ConnectionError("Redis circuit is open or cache is not available.")
        try:
            return self.cache.set(key, value)
        except redis.RedisError as e:
            self._record_failure()
            raise e

    def _record_failure(self):
        with self.lock:
            self.failure_count += 1
            if self.failure_count >= self.failure_threshold:
                self.circuit_open = True
                logging.error("Redis circuit opened due to repeated failures.")


redis_cache = RedisCache()


def get_transaction(user_id):
    try:
        cached_transaction = redis_cache.get(f"transaction:{user_id}")
        if cached_transaction:
            return cached_transaction
    except (redis.ConnectionError, redis.RedisError) as e:
        logging.error(f"Redis error during get operation for transaction {user_id}: {e}")

    transaction = fetch_transaction_from_db(user_id)

    try:
        redis_cache.set(f"transaction:{user_id}", transaction)
    except (redis.ConnectionError, redis.RedisError) as e:
        logging.error(f"Redis error during set operation for transaction {user_id}: {e}")

    return transaction

# Placeholder function for database fetch
# Replace with actual DB access implementation

def fetch_transaction_from_db(user_id):
    # Simulate DB fetch
    return f"transaction_data_for_{user_id}"
