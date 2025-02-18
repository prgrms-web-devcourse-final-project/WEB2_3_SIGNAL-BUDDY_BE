package org.programmers.signalbuddyfinal.global.dto;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class PageResponse<T> {

    private final long totalElements;
    private final long totalPages;
    private final long currentPageNumber;
    private final long pageSize;
    private final boolean hasNext;
    private final boolean hasPrevious;
    private final List<T> searchResults;

    public PageResponse(Page<T> page) {
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.currentPageNumber = page.getNumber();
        this.pageSize = page.getSize();
        this.hasNext = page.hasNext();
        this.hasPrevious = page.hasPrevious();
        this.searchResults = page.getContent();
    }
}
