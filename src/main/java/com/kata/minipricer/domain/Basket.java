package com.kata.minipricer.domain;

import java.util.List;

public class Basket {
    private final Product pivot;
    private final List<Product> products;

    public Basket(Product pivot, List<Product> products) {
        this.pivot = pivot;
        this.products = products;
    }


}
