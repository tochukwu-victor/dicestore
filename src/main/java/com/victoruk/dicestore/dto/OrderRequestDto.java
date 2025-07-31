package com.victoruk.dicestore.dto;

public record OrderRequestDto (
                               String paymentId, String paymentStatus){



}

//
//public record OrderRequestDto (BigDecimal totoalPrice,
//                               String paymentId, String paymentStatus, List<OrderItemDto> items){
//
//
//
//}