package Redis;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import redis.clients.jedis.RedisClient;

@RestController
@RequestMapping("/api/keys")
public class KeyValueController {

    private final RedisClient redis;
    private final JdbcTemplate jdbcTemplate;

    public KeyValueController(RedisClient redis, JdbcTemplate jdbcTemplate) {
        this.redis = redis;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PutMapping("/{key}")
    public KeyValue put(@PathVariable String key, @RequestBody String value) {
        jdbcTemplate.update("""
                INSERT INTO key_values (key, value) VALUES (?, ?)
                ON CONFLICT (key) DO UPDATE SET value = EXCLUDED.value
                """, key, value);
        redis.set(key, value);
        return new KeyValue(key, value);
    }

    @GetMapping("/{key}")
    public KeyValue get(@PathVariable String key) {
        String value = redis.get(key);
        if (value == null) {
            var results = jdbcTemplate.query(
                    "SELECT value FROM key_values WHERE key = ?",
                    (resultSet, rowNumber) -> resultSet.getString("value"), key);
            if (results.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Key not found");
            }
            value = results.getFirst();
            redis.set(key, value);
        }
        return new KeyValue(key, value);
    }

    @DeleteMapping("/{key}")
    public void delete(@PathVariable String key) {
        jdbcTemplate.update("DELETE FROM key_values WHERE key = ?", key);
        redis.del(key);
    }

    public record KeyValue(String key, String value) { }
}
