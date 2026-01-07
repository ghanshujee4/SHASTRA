package com.library.sdl.payment;

// import com.library.sdl.PaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, Long> {

    List<PaymentRecord> findByUserId(Long userId);

    // @Transactional
    @Modifying
    @Query("DELETE FROM PaymentRecord p WHERE p.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    // Optional<PaymentRecord> findTopByUserIdAndPaidTrueOrderByPaymentDateDesc(Long userId);
    Optional<PaymentRecord>
    findTopByUserIdAndIsPaidFalseOrderByDueDateDesc(Long userId);

}

