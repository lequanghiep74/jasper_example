## Spring Data Redis

### 1. Relevant Articles:
- [Introduction to Redis](https://redis.io/)
- [Introduction to Spring Data Redis](https://docs.spring.io/spring-data/data-redis/docs/current/reference/html/)
- [Redis Tutorial](http://openmymind.net/2011/11/8/Redis-Zero-To-Master-In-30-Minutes-Part-1/)

### 2. Installing Redis
Follow [this](https://redis.io/topics/quickstart) to install Redis.

### 3. Maven Dependencies
```python
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-context</artifactId>
        <version>4.3.7.RELEASE</version>
    </dependency>

    <dependency>
        <groupId>org.springframework.data</groupId>
        <artifactId>spring-data-redis</artifactId>
        <version>1.8.1.RELEASE</version>
    </dependency>

    <dependency>
        <groupId>redis.clients</groupId>
        <artifactId>jedis</artifactId>
        <version>2.9.0</version>
        <type>jar</type>
    </dependency>
    
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-core</artifactId>
        <version>4.3.7.RELEASE</version>
        <exclusions>
            <exclusion>
                <artifactId>commons-logging</artifactId>
                <groupId>commons-logging</groupId>
            </exclusion>
        </exclusions>
    </dependency>

    <dependency>
        <groupId>org.springframework.data</groupId>
        <artifactId>spring-data-jpa</artifactId>
        <version>1.11.4.RELEASE</version>
    </dependency>
```

### 4. The Redis Configuration
#### 4.1 Java Configuration
Let’s start with the configuration bean definitions:

```java
@Bean
JedisConnectionFactory jedisConnectionFactory()
{
    JedisConnectionFactory connectionFactory = new JedisConnectionFactory();
    connectionFactory.setHostName(redisHost);
    connectionFactory.setPort(redisPort);
    return connectionFactory;
}

@Bean
public RedisTemplate<String, Object> redisTemplate()
{
    final RedisTemplate<String, Object> template = new RedisTemplate<String, Object>();
    template.setConnectionFactory(jedisConnectionFactory());
    template.setKeySerializer(new StringRedisSerializer());
    return template;
}
``` 

Use `setHostName` and `setPort` in `JedisConnectionFactory` to set hostname and port of redis server (Default is `127.0.0.1:6379`).

Use `setKeySerializer` with `StringRedisSerializer` to remove redundant character in `key` store in redis.

#### 4.2 Enable Time To Live (TTL)

Create class extend `KeyspaceConfiguration` and override method `getKeyspaceSettings` to enable TTL for all data.

```java
public class MyKeyspaceConfiguration extends KeyspaceConfiguration
{

    @Override
    public boolean hasSettingsFor(Class<?> type)
    {
        return true;
    }

    @Override
    public KeyspaceSettings getKeyspaceSettings(Class<?> type)
    {
        KeyspaceSettings keyspaceSettings = new KeyspaceSettings(type, "my-keyspace");
        keyspaceSettings.setTimeToLive(3600L);
        return keyspaceSettings;
    }
}
```

Add annotation `@EnableRedisRepositories` to use `KeyspaceConfiguration` you defined above.

```java
@EnableRedisRepositories(keyspaceConfiguration = MyKeyspaceConfiguration.class)
```

### 5. Redis Entity
To access domain entities stored in a Redis you can leverage repository support that eases implementing those quite significantly.

Sample Bus Entity:
```java
@RedisHash("Bus")
public class Bus implements Serializable
{
    @Id
    private String id;
    @Indexed
    private String plateNo;
    @GeoIndexed
    private Point location;
    private int numPassenger;
    private Date createdDate;
}
```

- `@RedisHash` to define prefixes used to create the actual key for the Redis Hash. Example is `bus`.
- `@Id` as well as those named `id` are considered as the identifier properties.
- `@Indexed` and `@GeoIndexed` are used to enable lookup operations based on native Redis structures. (Read [this](https://redis.io/topics/indexes) for more information)

### 6. Redis Repository
Spring data JPA is support for Redis. You can use query method but it only support some simple keyword inside method names.

|Keyword|Sample|Redis snippet|
|---|---|---|
|And|findByLastnameAndFirstname|SINTER …:firstname:rand …:lastname:al’thor|
|Or|findByLastnameOrFirstname|SUNION …:firstname:rand …:lastname:al’thor|
|Is,Equals|findByFirstname, findByFirstnameIs,findByFirstnameEquals|SINTER …:firstname:rand|
|Top,First|findFirst10ByFirstname, findTop5ByFirstname||

Sample:
```java
@Repository
public interface BusRepository
        extends JpaRepository<Bus, String>
{
    List<Bus> findByLocationNear(Point point, Distance distance);
}
```

Using derived query methods might not always be sufficient to model the queries to execute. `RedisCallback` and `RedisTemplate` offers more control over the actual matching of index structures or even custom added ones.

Sample finder using `RedisCallback` to find all `Bus's id` have `createdDate` between `fromTime` and `toTime`
```java
redisTemplate.executePipelined(new RedisCallback<Object>()
{
    @Override
    public Object doInRedis(final RedisConnection redisConnection)
        throws DataAccessException
    {
        return redisConnection.zRangeByScore("Bus:createdDate".getBytes(), fromTime, toTime);
    }
});
```

Or using direct `RedisTemplate`:
```java
redisTemplate.opsForZSet().rangeByScore("Bus:createdDate", fromDate, toDate);
```

### 7. Sample
- [View sample](https://github.com/lequanghiep74/spring_redis_sample)
