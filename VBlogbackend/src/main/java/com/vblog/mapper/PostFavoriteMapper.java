package com.vblog.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.vblog.entity.Post;
import java.util.List;

@Mapper
public interface PostFavoriteMapper {
    int add(@Param("postId") Long postId, @Param("userId") Long userId);

    int remove(@Param("postId") Long postId, @Param("userId") Long userId);

    boolean exists(@Param("postId") Long postId, @Param("userId") Long userId);

    long countByPostId(@Param("postId") Long postId);

    long countByUserId(@Param("userId") Long userId);

    List<Post> selectUserFavorites(@Param("userId") Long userId,
                                   @Param("offset") long offset, @Param("size") int size);
}
