package spring.ecommerce.dto;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PageResponseDto<T> {
    private List<T> content;
    private int totalPages;
    private long totalElements;
    private int pageSize;
    private int currentPage;

    public PageResponseDto(List<T> content, int totalPages, long totalElements, int pageSize, int currentPage) {
        this.content = content;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.pageSize = pageSize;
        this.currentPage = currentPage;
    }

}

