package fit.se.be_phone_store.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * ChartsDataResponse - Response DTO for charts data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChartsDataResponse {

    @JsonProperty("chart_type")
    private String chartType;

    @JsonProperty("period")
    private String period;

    @JsonProperty("labels")
    private List<String> labels;

    @JsonProperty("datasets")
    private List<Dataset> datasets;

    @JsonProperty("summary")
    private SummaryInfo summary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Dataset {
        @JsonProperty("label")
        private String label;

        @JsonProperty("data")
        private List<Number> data;

        @JsonProperty("backgroundColor")
        private String backgroundColor;

        @JsonProperty("borderColor")
        private String borderColor;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryInfo {
        @JsonProperty("total")
        private BigDecimal total;

        @JsonProperty("average")
        private BigDecimal average;

        @JsonProperty("peak_day")
        private String peakDay;

        @JsonProperty("peak_value")
        private BigDecimal peakValue;
    }
}

