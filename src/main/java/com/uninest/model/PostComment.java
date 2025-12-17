package com.uninest.model;

import java.sql.Timestamp;

/**
 * Model class representing a comment on a community post.
 * Maps to the `post_comments` table.
 */
public class PostComment {
    private int commentId;
    private int postId;
    private int userId;
    private String content;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String status;
    private String userName;
    private String userInitials;

    public PostComment() {}

    public PostComment(int postId, int userId, String content) {
        this.postId = postId;
        this.userId = userId;
        this.content = content;
        this.status = "active";
    }

    public int getCommentId() { return commentId; }
    public void setCommentId(int commentId) { this.commentId = commentId; }

    public int getPostId() { return postId; }
    public void setPostId(int postId) { this.postId = postId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { 
        this.userName = userName;
        if (userName != null && !userName.isEmpty()) {
            String[] parts = userName.trim().split("\\s+");
            StringBuilder initials = new StringBuilder();
            for (String part : parts) {
                if (!part.isEmpty() && initials.length() < 2) {
                    initials.append(Character.toUpperCase(part.charAt(0)));
                }
            }
            this.userInitials = initials.toString();
        }
    }

    public String getUserInitials() { return userInitials; }
    public void setUserInitials(String userInitials) { this.userInitials = userInitials; }

    public boolean isActive() { return "active".equals(status); }

    public String getTimeAgo() {
        if (createdAt == null) return "";
        long diffMs = System.currentTimeMillis() - createdAt.getTime();
        long diffMin = diffMs / 60000;
        long diffHour = diffMin / 60;
        long diffDay = diffHour / 24;
        if (diffDay > 0) return diffDay + (diffDay == 1 ? " day ago" : " days ago");
        if (diffHour > 0) return diffHour + (diffHour == 1 ? " hour ago" : " hours ago");
        if (diffMin > 0) return diffMin + (diffMin == 1 ? " minute ago" : " minutes ago");
        return "Just now";
    }
}
