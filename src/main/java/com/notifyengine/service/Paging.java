package com.notifyengine.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public final class Paging {
    private Paging() {}

    public static PageRequest of(int page, int size) {
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by(Sort.Direction.DESC, "id"));
    }
}
