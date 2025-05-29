package easy4j.module.jpa.page;

import org.springframework.data.domain.Sort;

/**
 * SortTools
 *
 * @author bokun.li
 * @date 2025-05
 */
public class SortTools {
	public static Sort basicSort() {
        return basicSort("desc", "id");
    }

    public static Sort basicSort(String orderType, String orderField) {
        return Sort.by(Sort.Direction.fromString(orderType), orderField);
    }

    public static Sort basicSort(SortDto... dtos) {
        Sort result = Sort.unsorted();
        for (SortDto dto : dtos) {
            result = result.and(Sort.by(Sort.Direction.fromString(dto.getOrderType()), dto.getOrderField()));
        }
        return result;
    }
}