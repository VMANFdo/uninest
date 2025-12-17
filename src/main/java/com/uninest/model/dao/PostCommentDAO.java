package com.uninest.model.dao;

import com.uninest.model.PostComment;
import com.uninest.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class PostCommentDAO {

    private PostComment map(ResultSet rs) throws SQLException {
        PostComment comment = new PostComment();
        comment.setCommentId(rs.getInt("comment_id"));
        comment.setPostId(rs.getInt("post_id"));
        comment.setUserId(rs.getInt("user_id"));
        comment.setContent(rs.getString("content"));
        comment.setCreatedAt(rs.getTimestamp("created_at"));
        comment.setUpdatedAt(rs.getTimestamp("updated_at"));
        comment.setStatus(rs.getString("status"));
        try { comment.setUserName(rs.getString("user_name")); } catch (SQLException ignored) {}
        return comment;
    }

    public int create(PostComment comment) {
        String sql = "INSERT INTO post_comments (post_id, user_id, content, status) VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, comment.getPostId());
            ps.setInt(2, comment.getUserId());
            ps.setString(3, comment.getContent());
            ps.setString(4, comment.getStatus() != null ? comment.getStatus() : "active");
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    comment.setCommentId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating comment", e);
        }
        return 0;
    }

    public Optional<PostComment> findById(int commentId) {
        String sql = "SELECT c.*, u.name AS user_name FROM post_comments c " +
                     "JOIN users u ON c.user_id = u.id WHERE c.comment_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, commentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching comment", e);
        }
        return Optional.empty();
    }

    public List<PostComment> findByPost(int postId) {
        String sql = "SELECT c.*, u.name AS user_name FROM post_comments c " +
                     "JOIN users u ON c.user_id = u.id " +
                     "WHERE c.post_id = ? AND c.status = 'active' ORDER BY c.created_at ASC";
        List<PostComment> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, postId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error listing comments", e);
        }
        return list;
    }

    public List<PostComment> findByPostLatest(int postId, int limit) {
        String sql = "SELECT c.*, u.name AS user_name FROM post_comments c " +
                     "JOIN users u ON c.user_id = u.id " +
                     "WHERE c.post_id = ? AND c.status = 'active' ORDER BY c.created_at DESC LIMIT ?";
        List<PostComment> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, postId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error listing latest comments", e);
        }
        Collections.reverse(list);
        return list;
    }

    public boolean update(PostComment comment) {
        String sql = "UPDATE post_comments SET content = ?, updated_at = NOW() WHERE comment_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, comment.getContent());
            ps.setInt(2, comment.getCommentId());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating comment", e);
        }
    }

    public boolean delete(int commentId) {
        String sql = "UPDATE post_comments SET status = 'deleted', updated_at = NOW() WHERE comment_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, commentId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting comment", e);
        }
    }

    public int countByPost(int postId) {
        String sql = "SELECT COUNT(*) FROM post_comments WHERE post_id = ? AND status = 'active'";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, postId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting comments", e);
        }
        return 0;
    }
}
