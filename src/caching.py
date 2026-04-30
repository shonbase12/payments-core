import redis
import logging

# Configure logging
logging.basicConfig(level=logging.ERROR, format='%(asctime)s - %(levelname)s - %(message)s')

# Connect to Redis
try:
    cache = redis.StrictRedis(host='localhost', port=6379, db=0)
except redis.ConnectionError as e:
    logging.error(f"Failed to connect to Redis: {e}")
    cache = None

# Failure Modes of get_transaction function:
# 
# 1. Redis Connection Failure at Initialization:
#    - When the module loads, it attempts to establish a connection to Redis.
#    - If the connection fails (e.g., Redis server down, network issues), a redis.ConnectionError is caught.
#    - The cache variable is set to None, signaling Redis is unavailable.
#    - Impact: All calls to get_transaction will bypass Redis caching and directly fetch from the database, potentially increasing database load and latency.
# 
# 2. Redis Get Operation Failure:
#    - During the retrieval of a cached transaction, cache.get may raise a redis.RedisError due to transient Redis issues or command failures.
#    - The exception is caught and logged, and the function continues to fetch the transaction from the database.
#    - Impact: Cache misses or degraded cache performance, leading to increased database queries.
# 
# 3. Redis Set Operation Failure:
#    - After fetching from the database, the function attempts to cache the transaction with cache.set.
#    - If a redis.RedisError occurs (e.g., Redis out of memory, network issues), the error is caught and logged.
#    - Impact: The transaction will not be cached, reducing cache hit rates and potentially affecting performance on subsequent calls.
# 
# 4. Cache Unavailability Fallback:
#    - If Redis is entirely unavailable (cache is None), the function always falls back to fetching transactions from the database.
#    - There is no retry mechanism or circuit breaker to restore Redis usage once it becomes available again within the same runtime.
#    - Impact: Persistent Redis downtime causes sustained higher database load until the service restarts or the connection is re-established externally.
# 
# Summary:
# The function is resilient to Redis failures by gracefully degrading to direct database access, ensuring availability of transaction data. However, prolonged Redis issues can degrade overall system performance due to increased database load and lack of automatic Redis connection recovery within the function.

def get_transaction(user_id):
    if not cache:
        # Redis not available, fallback to DB fetch
        logging.error(f"Redis cache unavailable, fetching transaction {user_id} directly from DB.")
        return fetch_transaction_from_db(user_id)

    try:
        # Check if the transaction is in cache
        cached_transaction = cache.get(f"transaction:{user_id}")
        if cached_transaction:
            return cached_transaction  # Return cached transaction
    except redis.RedisError as e:
        logging.error(f"Redis error during get operation for transaction {user_id}: {e}")

    # Fetch from the database if not cached or Redis failed
    transaction = fetch_transaction_from_db(user_id)

    try:
        cache.set(f"transaction:{user_id}", transaction)  # Cache the transaction
    except redis.RedisError as e:
        logging.error(f"Redis error during set operation for transaction {user_id}: {e}")

    return transaction
