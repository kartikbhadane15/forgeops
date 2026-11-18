package com.forgeops.common.dto;

import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.List;

public class PagedResponse<T> {

    private boolean success;
    private List<T> data;
    private PaginationMetadata pagination;
    private Instant timestamp;

    public PagedResponse() {
        this.timestamp = Instant.now();
    }

    public PagedResponse(List<T> data, Page<?> page) {
        this.success = true;
        this.data = data;
        this.pagination = new PaginationMetadata(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
        this.timestamp = Instant.now();
    }

    public static <T> PagedResponse<T> of(List<T> data, Page<?> page) {
        return new PagedResponse<>(data, page);
    }

    public boolean isSuccess() {
        return success;
    }

    public List<T> getData() {
        return data;
    }

    public PaginationMetadata getPagination() {
        return pagination;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public static class PaginationMetadata {
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;

        public PaginationMetadata() {}

        public PaginationMetadata(int page, int size, long totalElements, int totalPages, boolean first, boolean last) {
            this.page = page;
            this.size = size;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.first = first;
            this.last = last;
        }

        public int getPage() {
            return page;
        }

        public int getSize() {
            return size;
        }

        public long getTotalElements() {
            return totalElements;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public boolean isFirst() {
            return first;
        }

        public boolean isLast() {
            return last;
        }
    }
}
