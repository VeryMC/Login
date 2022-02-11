package fr.verymc.login.redis;

public class RedisCredentials {
    private final String address;
    private final String password;
    private final int port;
    private final String clientName;

    public RedisCredentials(String address, String password, int port, String clientName) {
        this.address = address;
        this.password = password;
        this.port = port;
        this.clientName = clientName;
    }

    public RedisCredentials(String address, String password, int port) {
        this(address, password, port, "Login");
    }

    public RedisCredentials(String address, String password, String clientName) {
        this(address, password, 6379, clientName);
    }

    public RedisCredentials(String address, String password) {
        this(address, password, 6379, "Login");
    }

    public RedisCredentials() {
        this(System.getenv("REDIS_HOST"),
                System.getenv("REDIS_PASSWORD"),
                6379,
                "jlogin");
    }

    public String getAddress() {
        return address;
    }

    public String getPassword() {
        return password;
    }

    public int getPort() {
        return port;
    }

    public String getClientName() {
        return clientName;
    }
}
