package com.library.sdl;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailAndPassword(String email, String password);


    @Query("SELECT s.seat FROM User s WHERE s.isRegistered = 'Y' " +
            "AND (s.shift LIKE CONCAT(:shift, ',%') " +
            "OR s.shift LIKE CONCAT('%,', :shift, ',%') " +
            "OR s.shift LIKE CONCAT('%,', :shift) " +
            "OR s.shift = :shift)")
    List<String> findRegisteredSeatsByShift(@Param("shift") String shift);



    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.email = :email")
    boolean existsByEmail(String email);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.mobile = :mobile")
    boolean existsByMobile(long mobile);

    Optional<User> findByEmail(String email);
}

