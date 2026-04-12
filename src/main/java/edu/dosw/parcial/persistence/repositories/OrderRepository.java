package edu.dosw.parcial.persistence.repositories;

import edu.dosw.parcial.core.models.OrderStatus;
import edu.dosw.parcial.persistence.entities.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    Optional<OrderEntity> findByUser_IdAndStatusIn(Long userId, List<OrderStatus> statuses);

    List<OrderEntity> findByUser_Id(Long userId);
}