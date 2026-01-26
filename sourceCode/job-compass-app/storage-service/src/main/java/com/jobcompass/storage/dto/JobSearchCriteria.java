package com.jobcompass.storage.dto;

import com.jobcompass.common.model.Source;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Search criteria for job queries.
 * 
 * @author Palrajjayaraj
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobSearchCriteria {

    private Source source;
    private String location;
    private String companyName;
    private Set<String> skills;
    private Integer maxJobAgeDays;
}
