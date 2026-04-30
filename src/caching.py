import redis

# Connect to Redis
try:
    cache = redis.StrictRedis(host='localhost', port=6379, db=0)
except redis.ConnectionError as e:
    print(f"Failed to connect to Redis: {e}")
    cache = None

def get_transaction(user_id):
    if not cache:
        # Redis not available, fallback to DB fetch
        return fetch_transaction_from_db(user_id)

    try:
        # Check if the transaction is in cache
        cached_transaction = cache.get(f"transaction:{user_id}")
        if cached_transaction:
            return cached_transaction  # Return cached transaction
    except redis.RedisError as e:
        print(f"Redis error during get: {e}")

    # Fetch from the database if not cached or Redis failed
    transaction = fetch_transaction_from_db(user_id)

    try:
        cache.set(f"transaction:{user_id}", transaction)  # Cache the transaction
    except redis.RedisError as e:
        print(f"Redis error during set: {e}")

    return transaction
