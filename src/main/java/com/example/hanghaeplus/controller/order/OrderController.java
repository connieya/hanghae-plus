package com.example.hanghaeplus.controller.order;

import com.example.hanghaeplus.controller.order.request.OrderPostRequest;
import com.example.hanghaeplus.common.result.ResultCode;
import com.example.hanghaeplus.common.result.ResultResponse;
import com.example.hanghaeplus.service.order.OrderService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;


    @ApiOperation("주문 API")
    @PostMapping
    public ResponseEntity<ResultResponse> createOrder(@RequestBody OrderPostRequest request) {
        return ResponseEntity.ok(ResultResponse.of(ResultCode.ORDER_POST_SUCCESS, orderService.create(request)));
    }

}
