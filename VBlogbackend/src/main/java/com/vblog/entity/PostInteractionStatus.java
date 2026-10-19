package com.vblog.entity;

import lombok.Data;

@Data
public class PostInteractionStatus {
    private Long postId;
    private Long likeCount;
    private Long favoriteCount;
    private Boolean liked;
    private Boolean favorited;
}
