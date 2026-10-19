package com.vblog.service.impl;

import com.vblog.mapper.PostLikeMapper;
import com.vblog.service.PostService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class PostLikeServiceImplTest {
  @Test
  void countsOnlyLikesOfRequestedPost() {
    PostLikeMapper mapper = mock(PostLikeMapper.class);
    when(mapper.countByPostId(12L)).thenReturn(0L);
    when(mapper.countByPostId(11L)).thenReturn(1L);
    PostLikeServiceImpl service = new PostLikeServiceImpl(mapper);

    assertEquals(0L, service.getLikeCount(12L));
    assertEquals(1L, service.getLikeCount(11L));
    verify(mapper, never()).selectByMap(anyMap());
  }

  @Test
  void cannotLikeNonexistentPost() {
    PostLikeMapper mapper = mock(PostLikeMapper.class);
    PostService postService = mock(PostService.class);
    PostLikeServiceImpl service = new PostLikeServiceImpl(mapper);
    service.setPostService(postService);

    assertThrows(IllegalArgumentException.class, () -> service.likePost(1L, 999L));
    verify(mapper, never()).insert(any());
  }
}
