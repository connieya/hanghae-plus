package com.example.hanghaeplus.component.order;

import com.example.hanghaeplus.controller.order.request.ProductRequestForOrder;
import com.example.hanghaeplus.repository.common.TimeProvider;
import com.example.hanghaeplus.repository.order.Order;
import com.example.hanghaeplus.repository.user.User;
import com.example.hanghaeplus.repository.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderAppender {

    private final OrderRepository orderRepository;

    @Transactional
    public Order append(User user, List<ProductRequestForOrder> products) {
        Order order = Order.create(user, products);
        return orderRepository.save(order);
    }

}
