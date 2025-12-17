package com.uninest.model.dao;

import com.uninest.model.CommunityPost;
import com.uninest.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CommunityPostDAO {

    private CommunityPost map(ResultSet rs) throws SQLException {
        CommunityPost post = new CommunityPost();
        post.setPostId(rs.getInt("post_id"));
        post.setUserId(rs.getInt("user_id"));
        post.setCommunityId(rs.getInt("community_id"));
        post.setContent(rs.getString("content"));
        post.setCreatedAt(rs.getTimestamp("created_at"));
        post.setUpdatedAt(rs.getTimestamp("updated_at"));
        post.setStatus(rs.getString("status"));
        try { post.setUserName(rs.getString("user_name")); } catch (SQLException ignored) {}
        try { post.setLikeCount(rs.getInt("like_count")); } catch (SQLException ignored) {}
        try { post.setCommentCount(rs.getInt("comment_count")); } catch (SQLException ignored) {}
        try { post.setLikedByCurrentUser(rs.getBoolean("liked_by_current_user")); } catch (SQLException ignored) {}
        return post;
    }

    public int create(CommunityPost post) {
        String sql = "INSERT INTO community_posts (user_id, community_id, content, status) VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, post.getUserId());
            ps.setInt(2, post.getCommunityId());
            ps.setString(3, post.getContent());
            ps.setString(4, post.getStatus() != null ? post.getStatus() : "active");
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    post.setPostId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating post", e);
        }
        return 0;
    }

    public Optional<CommunityPost> findById(int postId, int currentUserId) {
        String sql = "SELECT p.*, u.name AS user_name, " +
                     "(SELECT COUNT(*) FROM post_likes WHERE post_id = p.post_id) AS like_count, " +
                     "(SELECT COUNT(*) FROM post_comments WHERE post_id = p.post_id AND status = 'active') AS comment_count, " +
                     "(SELECT COUNT(*) > 0 FROM post_likes WHERE post_id = p.post_id AND user_id = ?) AS liked_by_current_user " +
                     "FROM community_posts p JOIN users u ON p.user_id = u.id WHERE p.post_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, currentUserId);
            ps.setInt(2, postId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching post", e);
        }
        return Optional.empty();
    }

    public List<CommunityPost> findByCommunity(int communityId, int currentUserId) {
        String sql = "SELECT p.*, u.name AS user_name, " +
                     "(SELECT COUNT(*) FROM post_likes WHERE post_id = p.post_id) AS like_count, " +
                     "(SELECT COUNT(*) FROM post_comments WHERE post_id = p.post_id AND status = 'active') AS comment_count, " +
                     "(SELECT COUNT(*) > 0 FROM post_likes WHERE post_id = p.post_id AND user_id = ?) AS liked_by_current_user " +
                     "FROM community_posts p JOIN users u ON p.user_id = u.id " +
                     "WHERE p.community_id = ? AND p.status = 'active' ORDER BY p.created_at DESC";
        List<CommunityPost> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, currentUserId);
            ps.setInt(2, communityId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error listing posts", e);
        }
        return list;
    }

    public List<CommunityPost> findLatest(int limit, int currentUserId) {
        String sql = "SELECT p.*, u.name AS user_name, " +
                     "(SELECT COUNT(*) FROM post_likes WHERE post_id = p.post_id) AS like_count, " +
                     "(SELECT COUNT(*) FROM post_comments WHERE post_id = p.post_id AND status = 'active') AS comment_count, " +
                     "(SELECT COUNT(*) > 0 FROM post_likes WHERE post_id = p.post_id AND user_id = ?) AS liked_by_current_user " +
                     "FROM community_posts p JOIN users u ON p.user_id = u.id " +
                     "WHERE p.status = 'active' ORDER BY p.created_at DESC LIMIT ?";
        List<CommunityPost> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, currentUserId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error listing latest posts", e);
        }
        return list;
    }

    public boolean update(CommunityPost post) {
        String sql = "UPDATE community_posts SET content = ?, updated_at = NOW() WHERE post_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, post.getContent());
            ps.setInt(2, post.getPostId());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating post", e);
        }
    }

    public boolean delete(int postId) {
        String sql = "UPDATE community_posts SET status = 'deleted', updated_at = NOW() WHERE post_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, postId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting post", e);
        }
    }

    public boolean hide(int postId) {
        String sql = "UPDATE community_posts SET status = 'hidden', updated_at = NOW() WHERE post_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, postId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Error hiding post", e);
        }
    }

    public int countByCommunity(int communityId) {
        String sql = "SELECT COUNT(*) FROM community_posts WHERE community_id = ? AND status = 'active'";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, communityId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting posts", e);
        }
        return 0;
    }
}
