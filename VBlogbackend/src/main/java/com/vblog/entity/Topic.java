package com.vblog.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * Topic Entity
 * 
 * @author V-Blog Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Topic extends BaseEntity {

    /**
     * Topic ID (primary key)
     */
    private Long topicId;

    /**
     * Topic name
     */
    private String name;

    /**
     * Topic heat value
     */
    private BigDecimal heat;
}

