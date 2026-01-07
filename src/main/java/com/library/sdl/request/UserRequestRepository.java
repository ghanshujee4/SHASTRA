package com.library.sdl.request;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRequestRepository extends JpaRepository<UserRequest, Long> {
    List<UserRequest> findByUserId(Long userId);
    List<UserRequest> findByStatus(String status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM UserRequest ur WHERE ur.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

}
