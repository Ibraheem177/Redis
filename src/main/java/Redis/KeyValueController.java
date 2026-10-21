package Redis;

import org.springframework.http.HttpStatus;
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

    public KeyValueController(RedisClient redis) {
        this.redis = redis;
    }

    @PutMapping("/{key}")
    public KeyValue put(@PathVariable String key, @RequestBody String value) {
        redis.set(key, value);
        return new KeyValue(key, value);
    }

    @GetMapping("/{key}")
    public KeyValue get(@PathVariable String key) {
        String value = redis.get(key);
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Key not found");
        }
        return new KeyValue(key, value);
    }

    @DeleteMapping("/{key}")
    public void delete(@PathVariable String key) {
        redis.del(key);
    }

    public record KeyValue(String key, String value) { }
}
