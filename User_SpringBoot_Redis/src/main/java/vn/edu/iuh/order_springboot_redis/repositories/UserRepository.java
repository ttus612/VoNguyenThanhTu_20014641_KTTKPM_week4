package vn.edu.iuh.order_springboot_redis.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.order_springboot_redis.models.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

}
