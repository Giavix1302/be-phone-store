package fit.se.be_phone_store.dto.response.color;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ColorStatisticsResponse DTO for color statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColorStatisticsResponse {

    private Overview overview;
    private List<ColorUsage> mostUsedColors;
    private List<ColorResponse> unusedColors;
    private ColorTrends colorTrends;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Overview {
        private Long totalColors;
        private Long colorsInUse;
        private Long colorsUnused;
        private Long mostPopularCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ColorUsage {
        private Long id;
        private String colorName;
        private String hexCode;
        private Long productCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ColorTrends {
        private List<String> mostPopularHexPatterns;
    }
}