package com.example.hanghaeplus.repository.coupon;

public enum CouponState {
    UNUSED("사용 전"),
    USED("사용 완료"),
    EXPIRED("기간 만료"),
    DELETED("삭제");

    private final String state;


    CouponState(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }
}
