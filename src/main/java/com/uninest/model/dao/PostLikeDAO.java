package com.uninest.model.dao;

import com.uninest.model.PostLike;
import com.uninest.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostLikeDAO {

    private PostLike map(ResultSet rs) throws SQLException {
        PostLike like = new PostLike();
        like.setLikeId(rs.getInt("like_id"));
        like.setPostId(rs.getInt("post_id"));
        like.setUserId(rs.getInt("user_id"));
        like.setCreatedAt(rs.getTimestamp("created_at"));
        try { like.setUserName(rs.getString("user_name")); } catch (SQLException ignored) {}
        return like;
    }

    public boolean like(int postId, int userId) {
        String sql = "INSERT IGNORE INTO post_likes (post_id, user_id) VALUES (?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, postId);
            ps.setInt(2, userId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Error liking post", e);
        }
    }

    public boolean unlike(int postId, int userId) {
        String sql = "DELETE FROM post_likes WHERE post_id = ? AND user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, postId);
            ps.setInt(2, userId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Error unliking post", e);
        }
    }

    public boolean toggleLike(int postId, int userId) {
        if (hasLiked(postId, userId)) {
            unlike(postId, userId);
            return false;
        } else {
            like(postId, userId);
            return true;
        }
    }

    public boolean hasLiked(int postId, int userId) {
        String sql = "SELECT 1 FROM post_likes WHERE post_id = ? AND user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, postId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking like status", e);
        }
    }

    public int countByPost(int postId) {
        String sql = "SELECT COUNT(*) FROM post_likes WHERE post_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, postId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting likes", e);
        }
        return 0;
    }

    public List<PostLike> findByPost(int postId) {
        String sql = "SELECT pl.*, u.name AS user_name FROM post_likes pl " +
                     "JOIN users u ON pl.user_id = u.id WHERE pl.post_id = ? ORDER BY pl.created_at DESC";
        List<PostLike> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, postId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error listing likes", e);
        }
        return list;
    }
}
