-- KEYS[1]: The user's bucket key (e.g., "rate_limit:192.168.1.5")
-- ARGV[1]: Maximum bucket capacity (e.g., 10 tokens)
-- ARGV[2]: Refill rate per second (e.g., 2 tokens)
-- ARGV[3]: Current Unix timestamp from the Java server

local key = KEYS[1]
local capacity = tonumber(ARGV[1])
local refill_rate_per_sec = tonumber(ARGV[2])
local now = tonumber(ARGV[3])
local requested = 1

-- Fetch current tokens and last refresh time from Redis
local bucket = redis.call('HMGET', key, 'tokens', 'last_refreshed')
local tokens = tonumber(bucket[1])
local last_refreshed = tonumber(bucket[2])

-- Initialize bucket if it doesn't exist
if tokens == nil then
    tokens = capacity
    last_refreshed = now
else
    -- Calculate how many tokens to add based on time passed
    local time_passed = math.max(0, now - last_refreshed)
    local tokens_to_add = math.floor(time_passed * refill_rate_per_sec)

    if tokens_to_add > 0 then
        -- Add tokens, but don't exceed the max capacity
        tokens = math.min(capacity, tokens + tokens_to_add)
        last_refreshed = now
    end
end

-- Check if the user has enough tokens to make the API request
if tokens >= requested then
    tokens = tokens - requested
    redis.call('HMSET', key, 'tokens', tokens, 'last_refreshed', last_refreshed)
    -- Set a TTL so inactive users don't clutter Redis memory forever (e.g., 60 seconds)
    redis.call('EXPIRE', key, 60)
    return 1 -- True (Request Allowed)
else
    redis.call('HMSET', key, 'tokens', tokens, 'last_refreshed', last_refreshed)
    redis.call('EXPIRE', key, 60)
    return 0 -- False (429 Too Many Requests)
end