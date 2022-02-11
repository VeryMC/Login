package fr.verymc.login.redis;

import redis.clients.jedis.JedisPool;

public class RedisAccess {
    public static RedisAccess INSTANCE;
    private JedisPool pool;

    public RedisAccess(RedisCredentials credentials) {
        INSTANCE = this;
        this.pool = new JedisPool(credentials.getAddress(), credentials.getPort(), credentials.getClientName(), credentials.getPassword());
    }

    public static void init() {
        new RedisAccess(new RedisCredentials());
    }

    public JedisPool getPool() {
        return this.pool;
    }
}
