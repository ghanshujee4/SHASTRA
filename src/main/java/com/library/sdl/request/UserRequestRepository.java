package com.library.sdl.request;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserRequestRepository extends JpaRepository<UserRequest, Long> {
    List<UserRequest> findByUserId(Long userId);
    List<UserRequest> findByStatus(String status);
}
