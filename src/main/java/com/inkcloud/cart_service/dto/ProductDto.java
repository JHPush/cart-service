package com.inkcloud.cart_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class ProductDto {

    private Long id;

    private String name;

    private String status;

    private String image;

    private int price;

    private String author;

    private String publisher;

}
