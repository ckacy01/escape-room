package org.technoready.escaperoom.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ManorResponse {
    private String narrative;
    private String status;
    private String inscription;
    private Integer currentStage;
}
