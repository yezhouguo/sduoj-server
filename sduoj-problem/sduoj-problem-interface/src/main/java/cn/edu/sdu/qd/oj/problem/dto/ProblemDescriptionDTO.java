package cn.edu.sdu.qd.oj.problem.dto;

import cn.edu.sdu.qd.oj.common.entity.BaseDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProblemDescriptionDTO extends BaseDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Map<String, String> features;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long problemId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long userId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer voteNum;

    private String markdownDescription;

    private String htmlDescription;

    private String htmlInput;

    private String htmlOutput;

    private String htmlSampleInput;

    private String htmlSampleOutout;

    private String htmlHint;

    // -------------------------------

    @NotNull
    @NotBlank
    private String problemCode;
}
