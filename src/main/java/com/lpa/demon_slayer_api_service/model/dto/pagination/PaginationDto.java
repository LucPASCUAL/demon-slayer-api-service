package com.lpa.demon_slayer_api_service.model.dto.pagination;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
  *
  * @param currentPage  is the current page number
  * @param totalPages   is the total number of pages
  */
@JsonIgnoreProperties(ignoreUnknown = true) //Jackson will ignore any additional fields present in the JSON
public record PaginationDto(
        int currentPage,
        int totalPages
) {
    public boolean hasNext() {
        return (currentPage + 1) <= totalPages;
    }

    public int getNextPageNumber() {
        if (!hasNext())
            throw new IllegalStateException("No next page available");
        return currentPage + 1;
    }
}
