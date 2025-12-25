package com.vblog.service;

import com.vblog.entity.Comment;

import java.util.List;

/**
 * Comment Service Interface
 * 
 * @author V-Blog Team
 */
public interface CommentService {

    /**
     * Add a comment
     * 
     * @param userId User ID
     * @param postId Post ID
     * @param content Comment content
     * @param parentId Parent comment ID (null for top-level comment)
     * @return Created comment
     */
    Comment addComment(Long userId, Long postId, String content, Long parentId);

    /**
     * Get comments by post ID (tree structure)
     * 
     * @param postId Post ID
     * @return List of comments (top-level comments with replies)
     */
    List<CommentTree> getCommentsByPostId(Long postId);

    /**
     * Get comment by ID
     * 
     * @param commentId Comment ID
     * @return Comment entity
     */
    Comment getCommentById(Long commentId);
    
    /**
     * Delete a comment
     * 
     * @param commentId Comment ID
     * @param userId User ID (to check ownership)
     * @return true if deleted, false otherwise
     */
    boolean deleteComment(Long commentId, Long userId);
    
    /**
     * Update a comment
     * 
     * @param commentId Comment ID
     * @param userId User ID (to check ownership)
     * @param content New comment content
     * @return Updated comment
     */
    Comment updateComment(Long commentId, Long userId, String content);
    
    /**
     * Like a comment
     * 
     * @param commentId Comment ID
     * @param userId User ID
     * @return true if liked, false if already liked
     */
    boolean likeComment(Long commentId, Long userId);
    
    /**
     * Unlike a comment
     * 
     * @param commentId Comment ID
     * @param userId User ID
     * @return true if unliked, false if not liked
     */
    boolean unlikeComment(Long commentId, Long userId);
    
    /**
     * Check if user has liked the comment
     * 
     * @param commentId Comment ID
     * @param userId User ID
     * @return true if liked, false otherwise
     */
    boolean isCommentLiked(Long commentId, Long userId);
    
    /**
     * Get comment like count
     * 
     * @param commentId Comment ID
     * @return Like count
     */
    Long getCommentLikeCount(Long commentId);

    /**
     * Comment tree structure
     */
    class CommentTree {
        private Comment comment;
        private List<CommentTree> replies;

        public CommentTree(Comment comment) {
            this.comment = comment;
            this.replies = new java.util.ArrayList<>();
        }

        public Comment getComment() {
            return comment;
        }

        public List<CommentTree> getReplies() {
            return replies;
        }
    }
}


