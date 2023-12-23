package com.example.hanghaeplus.component.order;

import com.example.hanghaeplus.orm.entity.Order;
import com.example.hanghaeplus.orm.entity.Product;
import com.example.hanghaeplus.orm.entity.User;
import com.example.hanghaeplus.orm.repository.OrderRepository;
import com.example.hanghaeplus.orm.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderAppender {

    private final OrderRepository orderRepository;

    public Order append(User user, List<Product> products) {
        Order order = Order.create(user, products);
        return orderRepository.save(order);

    }
}
