package com.victoruk.dicestore.order.repository;

import com.victoruk.dicestore.order.entity.Order;
import com.victoruk.dicestore.order.entity.OrderStatus;
import com.victoruk.dicestore.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserOrderByCreatedAt(User user);

    List<Order> findByUserOrderByCreatedAtDesc(User user);

    List<Order> findByOrderStatus(OrderStatus orderStatus);

    @Modifying
    @Query("UPDATE Order o SET o.orderStatus = :orderStatus, o.updatedBy = :updatedBy WHERE o.orderId = :orderId")
    int updateOrderStatus(@Param("orderId") Long orderId,
                          @Param("orderStatus") OrderStatus orderStatus,
                          @Param("updatedBy") String updatedBy);
}