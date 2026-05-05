-- KEYS[1]: The user's bucket key (e.g., "rate_limit:192.168.1.5")
-- ARGV[1]: Maximum bucket capacity (e.g., 10 tokens)
-- ARGV[2]: Refill rate per second (e.g., 2 tokens)
-- ARGV[3]: Current Unix timestamp from the Java server

local key = KEYS[1]
local capacity = tonumber(ARGV[1]) or 0
local refill_rate_per_sec = tonumber(ARGV[2]) or 0
local now = tonumber(ARGV[3]) or 0

-- Guard against missing/invalid arguments to avoid nil arithmetic
if capacity <= 0 or now <= 0 or refill_rate_per_sec < 0 then
    return 0
end

-- Initialize defaults that are guaranteed to be numbers
local tokens = capacity + 0
local last_refreshed = now + 0

-- Fetch current tokens and last refresh time from Redis
local result = redis.call('HGETALL', key)

-- Parse the hash result
if result and #result > 0 then
    -- Hash exists, parse the values
    for i = 1, #result, 2 do
        if tostring(result[i]) == 'tokens' then
            local val = tonumber(result[i + 1])
            if val then tokens = val + 0 end
        elseif tostring(result[i]) == 'last_refreshed' then
            local val = tonumber(result[i + 1])
            if val then last_refreshed = val + 0 end
        end
    end

    -- Calculate refill
    local time_passed = now - last_refreshed
    if time_passed > 0 then
        local tokens_to_add = math.floor(time_passed * refill_rate_per_sec)
        if tokens_to_add > 0 then
            tokens = math.min(capacity, tokens + tokens_to_add)
            last_refreshed = now + 0
        end
    end
end

-- Ensure tokens is a number before comparison
tokens = tokens + 0

-- Check if the user has enough tokens
if tokens >= 1 then
    tokens = tokens - 1
    redis.call('HSET', key, 'tokens', tostring(tokens), 'last_refreshed', tostring(last_refreshed))
    redis.call('EXPIRE', key, 60)
    return 1
else
    redis.call('HSET', key, 'tokens', tostring(tokens), 'last_refreshed', tostring(last_refreshed))
    redis.call('EXPIRE', key, 60)
    return 0
end