package ua.utilix.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ua.utilix.model.User;

import java.util.List;

//public interface UserRepository {
//    List<User> findNewUsers();
//
//    User findByChatId(long id);
//
//    User findBySigfoxId(String id);
//
//    List<User> findAll();
//
//    int count();
//
//    void save(User user);
//
//    void saveAll(List<User> users);
//}



public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.notified = false " +
            "AND u.sigfoxId IS NOT NULL")
    List<User> findNewUsers();

    User findById(long id);

    User[] findByChatId(long chatId);

    //User findByChatIdAndSigfoxId(long id, String sigfoxId);

    User findBySigfoxId(String sigfoxId);

    //User findBySigfoxName(long id, String sigfoxName);
}
