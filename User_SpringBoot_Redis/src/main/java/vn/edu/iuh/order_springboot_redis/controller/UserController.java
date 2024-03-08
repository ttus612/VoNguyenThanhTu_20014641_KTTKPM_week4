package vn.edu.iuh.order_springboot_redis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.Jedis;
import vn.edu.iuh.order_springboot_redis.models.User;
import vn.edu.iuh.order_springboot_redis.repositories.UserRepository;

import java.util.List;


@RestController
@RequestMapping("/api/v1")
public class UserController {
    @Autowired
    private UserRepository userRepository;
    private Jedis jedis = new Jedis();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/users/{userId}")
    public User findUserByID(@PathVariable(value = "userId") long userId) {
        String key = String.valueOf(userId);

        // Kiểm tra xem user có trong cache không
        if (jedis.exists(key)) {
            User userInCache = new User();
            userInCache.setId(userId);
            System.out.println("fetching from cache >>>>>>>>>>" + userId);
            String userName = jedis.get(key);
            userInCache.setName(userName);
            return userInCache;
        } else {
            // Nếu user không có trong cache, lấy từ cơ sở dữ liệu và lưu vào cache
            System.out.println("fetching from database >>>>>>>>>>" + userId);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User_id " + userId + " not found"));

            // Lưu user name vào cache
            jedis.set(key, user.getName());
            System.out.println("saved in cache");
            return user;
        }
    }

    @PutMapping("/users/{userId}")
//    @CachePut(value = "users", key = "#userId")
    public User updateUser(@PathVariable(value = "userId") long userId, @RequestBody User user){
        User userUpdate = userRepository.findById(userId).orElseThrow(()-> new RuntimeException("User not found"));
        userUpdate.setName(user.getName());
        jedis.set(String.valueOf(user.getId()), user.getName());
        System.out.println("saved in cache");
        return userRepository.save(userUpdate);
    }

    @DeleteMapping("/users/{userId}")
//    @CacheEvict(value = "users")
    public void deleteUser(@PathVariable(value = "userId") long userId){
        User user = userRepository.findById(userId).orElseThrow(()-> new RuntimeException("User not found"));
        userRepository.delete(user);
        jedis.del(String.valueOf(user.getId()));
        System.out.println("delete in cache");
        System.out.println("Delete complete!");
    }

}
