import redis

# Connect to Redis
cache = redis.StrictRedis(host='localhost', port=6379, db=0)

def get_transaction(user_id):
    # Check if the transaction is in cache
    cached_transaction = cache.get(f"transaction:{user_id}")
    if cached_transaction:
        return cached_transaction  # Return cached transaction

    # Fetch from the database if not cached
    transaction = fetch_transaction_from_db(user_id)
    cache.set(f"transaction:{user_id}", transaction)  # Cache the transaction
    return transaction
