package com.uninest.model;

import java.sql.Timestamp;

/**
 * Model class representing a like on a community post.
 * Maps to the `post_likes` table.
 */
public class PostLike {
    private int likeId;
    private int postId;
    private int userId;
    private Timestamp createdAt;
    private String userName;

    public PostLike() {}

    public PostLike(int postId, int userId) {
        this.postId = postId;
        this.userId = userId;
    }

    public int getLikeId() { return likeId; }
    public void setLikeId(int likeId) { this.likeId = likeId; }

    public int getPostId() { return postId; }
    public void setPostId(int postId) { this.postId = postId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
}
