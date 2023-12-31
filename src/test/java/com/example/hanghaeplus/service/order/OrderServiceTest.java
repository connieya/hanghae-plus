package com.example.hanghaeplus.service.order;

import com.example.hanghaeplus.controller.order.request.OrderPostRequest;
import com.example.hanghaeplus.controller.order.response.OrderPostResponse;
import com.example.hanghaeplus.controller.order.request.ProductRequestForOrder;
import com.example.hanghaeplus.repository.product.Product;
import com.example.hanghaeplus.repository.user.User;
import com.example.hanghaeplus.repository.product.ProductRepository;
import com.example.hanghaeplus.repository.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Slf4j
public class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;


    @DisplayName("주문 한 상품 수량 만큼 재고를 차감한다.")
    @Test
    void deductQuantity() {
        // given
        User user = User.create("건희", 1000000L);
        User savedUser = userRepository.save(user);

        Product product1 = Product.create("양파", 1000L, 5L);
        Product product2 = Product.create("감자", 2000L, 15L);
        Product product3 = Product.create("당금", 3000L, 20L);


        productRepository.saveAll(List.of(product1, product2, product3));


        ProductRequestForOrder request1 = ProductRequestForOrder.of(product1.getId(), 5L, product1.getPrice());
        ProductRequestForOrder request2 = ProductRequestForOrder.of(product2.getId(), 8L, product2.getPrice());
        ProductRequestForOrder request3 = ProductRequestForOrder.of(product3.getId(), 15L, product3.getPrice());


        List<ProductRequestForOrder> requests = List.of(request1, request2, request3);


        OrderPostRequest orderPostRequest = OrderPostRequest.builder()
                .userId(savedUser.getId())
                .products(requests)
                .build();

        // when
        orderService.create(orderPostRequest);

        List<Product> products = productRepository.findAllById(List.of(product1.getId(), product2.getId(), product3.getId()));
        Product findProduct1 = products.get(0);
        Product findProduct2 = products.get(1);
        Product findProduct3 = products.get(2);
        //then
        assertThat(findProduct1.getQuantity()).isZero();
        assertThat(findProduct2.getQuantity()).isEqualTo(7L);
        assertThat(findProduct3.getQuantity()).isEqualTo(5L);
    }

    @DisplayName("주문 한 상품의 총 가격을 구한다.")
    @Test
    void createOrder() {
        User user = User.create("건희", 50000L);
        User savedUser = userRepository.save(user);

        Product product1 = Product.create("양파", 1000L, 5L);
        Product product2 = Product.create("감자", 2000L, 1L);
        Product product3 = Product.create("당금", 3000L, 5L);

        List<Product> products = List.of(product1, product2, product3);

        productRepository.saveAll(products);


        ProductRequestForOrder request1 = ProductRequestForOrder.of(product1.getId(), 1L, product1.getPrice());
        ProductRequestForOrder request2 = ProductRequestForOrder.of(product2.getId(), 1L, product2.getPrice());
        ProductRequestForOrder request3 = ProductRequestForOrder.of(product3.getId(), 2L, product3.getPrice());

        List<ProductRequestForOrder> requests = List.of(request1, request2, request3);

        // given
        OrderPostRequest orderPostRequest = OrderPostRequest.builder()
                .userId(savedUser.getId())
                .products(requests)
                .build();

        // when
        OrderPostResponse order = orderService.create(orderPostRequest);


        //then
        assertThat(order.getTotalPrice()).isEqualTo(9000L);
    }


    @DisplayName("주문 한 상품 가격 만큼 잔액을 차감 한다.")
    @Test
    void deductPoint() {
        User user = User.create("건희", 50000L);
        User savedUser = userRepository.save(user);

        Product product1 = Product.create("양파", 1000L, 5L);
        Product product2 = Product.create("감자", 2000L, 1L);
        Product product3 = Product.create("당금", 3000L, 5L);

        List<Product> products = List.of(product1, product2, product3);

        productRepository.saveAll(products);


        ProductRequestForOrder request1 = ProductRequestForOrder.of(product1.getId(), 1L, product1.getPrice());
        ProductRequestForOrder request2 = ProductRequestForOrder.of(product2.getId(), 1L, product2.getPrice());
        ProductRequestForOrder request3 = ProductRequestForOrder.of(product3.getId(), 2L, product3.getPrice());

        List<ProductRequestForOrder> requests = List.of(request1, request2, request3);

        // given
        OrderPostRequest orderPostRequest = OrderPostRequest.builder()
                .userId(savedUser.getId())
                .products(requests)
                .build();

        // when
        orderService.create(orderPostRequest);

        User findUser = userRepository.findById(savedUser.getId()).get();
        Long totalPrice = product1.getPrice() * request1.getQuantity() + product2.getPrice() * request2.getQuantity() + product3.getPrice() * request3.getQuantity();

        //then
        assertThat(findUser.getCurrentPoint()).isEqualTo(50000L - totalPrice);
    }

    @DisplayName("동시에 상품을 주문 하여도 주문한 수량 만큼 재고를 차감한다.")
    @Test
    void deductQuantityWithConcurrency() {
        // given
        User user1 = User.create("건희", 100000000L);
        User user2 = User.create("거니", 100000000L);
        User savedUser1 = userRepository.save(user1);
        User savedUser2 = userRepository.save(user2);

        Product product1 = Product.create("양파", 1000L, 30L);
        Product product2 = Product.create("감자", 2000L, 30L);
        Product product3 = Product.create("당근", 3000L, 30L);


        productRepository.saveAll(List.of(product1, product2, product3));


        ProductRequestForOrder request1 = ProductRequestForOrder.of(product1.getId(), 5L, product1.getPrice());
        ProductRequestForOrder request2 = ProductRequestForOrder.of(product2.getId(), 10L, product2.getPrice());
        ProductRequestForOrder request3 = ProductRequestForOrder.of(product3.getId(), 5L, product3.getPrice());


        ProductRequestForOrder request4 = ProductRequestForOrder.of(product1.getId(), 3L, product3.getPrice());
        ProductRequestForOrder request5 = ProductRequestForOrder.of(product2.getId(), 5L, product3.getPrice());
        ProductRequestForOrder request6 = ProductRequestForOrder.of(product3.getId(), 5L, product3.getPrice());


        List<ProductRequestForOrder> requests1 = List.of(request1, request2, request3);
        List<ProductRequestForOrder> requests2 = List.of(request4, request5, request6);


        OrderPostRequest orderPostRequest1 = OrderPostRequest.builder()
                .userId(savedUser1.getId())
                .products(requests1)
                .build();

        OrderPostRequest orderPostRequest2 = OrderPostRequest.builder()
                .userId(savedUser2.getId())
                .products(requests2)
                .build();


        // when
        CompletableFuture.allOf(
                CompletableFuture.runAsync(() -> orderService.create(orderPostRequest1)),
                CompletableFuture.runAsync(() -> orderService.create(orderPostRequest2))
        ).join();

        List<Product> products = productRepository.findAllById(List.of(product1.getId(), product2.getId(), product3.getId()));
        Product findProduct1 = products.get(0);
        Product findProduct2 = products.get(1);
        Product findProduct3 = products.get(2);
        //then
        assertThat(findProduct1.getQuantity()).isEqualTo(30L - 5L - 3L);
        assertThat(findProduct2.getQuantity()).isEqualTo(30L - 10L - 5L);
        assertThat(findProduct3.getQuantity()).isEqualTo(30L - 5L - 5L);
    }


    @DisplayName("동시에 상품을 주문 하여도 주문한 수량 만큼 재고를 차감한다.")
    @Test
    void deductQuantityWithConcurrency2() {
        // given
        User user1 = User.create("건희", 100000000L);
        User user2 = User.create("거니", 100000000L);
        User savedUser1 = userRepository.save(user1);
        User savedUser2 = userRepository.save(user2);

        Product product1 = Product.create("양파", 1000L, 30L);
        Product product2 = Product.create("감자", 2000L, 30L);
        Product product3 = Product.create("당근", 3000L, 30L);


        productRepository.saveAll(List.of(product1, product2, product3));


        ProductRequestForOrder request1 = ProductRequestForOrder.of(product1.getId(), 5L, product1.getPrice());
        ProductRequestForOrder request2 = ProductRequestForOrder.of(product2.getId(), 10L, product2.getPrice());
        ProductRequestForOrder request3 = ProductRequestForOrder.of(product3.getId(), 5L, product3.getPrice());


        ProductRequestForOrder request4 = ProductRequestForOrder.of(product1.getId(), 3L, product3.getPrice());
        ProductRequestForOrder request5 = ProductRequestForOrder.of(product2.getId(), 5L, product3.getPrice());
        ProductRequestForOrder request6 = ProductRequestForOrder.of(product3.getId(), 5L, product3.getPrice());


        List<ProductRequestForOrder> requests1 = List.of(request1, request2, request3);
        List<ProductRequestForOrder> requests2 = List.of(request4, request5, request6);


        OrderPostRequest orderPostRequest1 = OrderPostRequest.builder()
                .userId(savedUser1.getId())
                .products(requests1)
                .build();

        OrderPostRequest orderPostRequest2 = OrderPostRequest.builder()
                .userId(savedUser2.getId())
                .products(requests2)
                .build();


        // when
        CompletableFuture.allOf(
                CompletableFuture.runAsync(() -> orderService.create(orderPostRequest1)),
                CompletableFuture.runAsync(() -> orderService.create(orderPostRequest2))
        ).join();

        List<Product> products = productRepository.findAllById(List.of(product1.getId(), product2.getId(), product3.getId()));
        Product findProduct1 = products.get(0);
        Product findProduct2 = products.get(1);
        Product findProduct3 = products.get(2);
        //then
        assertThat(findProduct1.getQuantity()).isEqualTo(30L - 5L - 3L);
        assertThat(findProduct2.getQuantity()).isEqualTo(30L - 10L - 5L);
        assertThat(findProduct3.getQuantity()).isEqualTo(30L - 5L - 5L);
    }


    // 한 사용자가 다른 상품들을 주문 했을 때 테스트
    @DisplayName("동시에 상품을 주문 하여도 주문한 상품 횟수 만큼 잔액을 차감한다.")
    @Test
    void deductPointWithConcurrency() {
        // given
        User user = User.create("건희", 50000L);
        User savedUser = userRepository.save(user);

        Product productOnion = Product.create("양파", 1000L, 30L);
        Product productPotato = Product.create("감자", 2000L, 30L);
        Product productCarrot = Product.create("당근", 3000L, 30L);
        Product productMushroom = Product.create("버섯", 5000L, 30L);


        productRepository.saveAll(List.of(productOnion, productPotato, productCarrot, productMushroom));


        ProductRequestForOrder request1_1 = ProductRequestForOrder.of(productOnion.getId(), 2L, productOnion.getPrice());
        ProductRequestForOrder request1_2 = ProductRequestForOrder.of(productPotato.getId(), 2L, productPotato.getPrice());


        ProductRequestForOrder request2_1 = ProductRequestForOrder.of(productCarrot.getId(), 2L, productCarrot.getPrice());
        ProductRequestForOrder request2_2 = ProductRequestForOrder.of(productMushroom.getId(), 2L, productMushroom.getPrice());


        List<ProductRequestForOrder> requests1 = List.of(request1_1, request1_2);
        List<ProductRequestForOrder> requests2 = List.of(request2_1, request2_2);


        OrderPostRequest orderPostRequest1 = OrderPostRequest.builder()
                .userId(savedUser.getId())
                .products(requests1)
                .build();

        OrderPostRequest orderPostRequest2 = OrderPostRequest.builder()
                .userId(savedUser.getId())
                .products(requests2)
                .build();

        // when

        CompletableFuture.allOf(
                CompletableFuture.runAsync(() -> orderService.create(orderPostRequest1)),
                CompletableFuture.runAsync(() -> orderService.create(orderPostRequest2))
        ).join();


        User findUser = userRepository.findById(savedUser.getId()).get();

        //then                현재 잔액 5000L  - (양파 2개 , 감자 2개)  / ( 당근 2개 , 버섯 2개)
        assertThat(findUser.getCurrentPoint()).isEqualTo(50000L-6000L-16000L);
    }
}
