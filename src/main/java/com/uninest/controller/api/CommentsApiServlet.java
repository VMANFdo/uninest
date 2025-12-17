package com.uninest.controller.api;

import com.uninest.model.PostComment;
import com.uninest.model.User;
import com.uninest.model.dao.PostCommentDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * API endpoint for comment operations.
 * POST: Create a new comment
 * DELETE: Delete a comment (owner only)
 */
@WebServlet(name = "commentsApi", urlPatterns = "/api/comments/*")
public class CommentsApiServlet extends HttpServlet {
    private final PostCommentDAO commentDAO = new PostCommentDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        User user = (User) req.getSession().getAttribute("authUser");
        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"success\":false,\"error\":\"Not authenticated\"}");
            return;
        }

        String postIdStr = req.getParameter("postId");
        String content = req.getParameter("content");

        if (postIdStr == null || content == null || content.trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\":false,\"error\":\"Missing postId or content\"}");
            return;
        }

        try {
            int postId = Integer.parseInt(postIdStr);
            
            PostComment comment = new PostComment();
            comment.setPostId(postId);
            comment.setUserId(user.getId());
            comment.setContent(content.trim());
            comment.setStatus("active");
            
            int commentId = commentDAO.create(comment);
            
            // Get the created comment to return user info
            var createdComment = commentDAO.findById(commentId);
            if (createdComment.isPresent()) {
                PostComment c = createdComment.get();
                out.print("{\"success\":true,\"comment\":{");
                out.print("\"commentId\":" + c.getCommentId() + ",");
                out.print("\"userId\":" + c.getUserId() + ",");
                out.print("\"userName\":\"" + escapeJson(c.getUserName()) + "\",");
                out.print("\"userInitials\":\"" + escapeJson(c.getUserInitials()) + "\",");
                out.print("\"content\":\"" + escapeJson(c.getContent()) + "\",");
                out.print("\"timeAgo\":\"Just now\"");
                out.print("}}");
            } else {
                out.print("{\"success\":true,\"commentId\":" + commentId + "}");
            }
            
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\":false,\"error\":\"Invalid postId\"}");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\":false,\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        User user = (User) req.getSession().getAttribute("authUser");
        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"success\":false,\"error\":\"Not authenticated\"}");
            return;
        }

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || !pathInfo.matches("/\\d+")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\":false,\"error\":\"Invalid comment ID\"}");
            return;
        }

        try {
            int commentId = Integer.parseInt(pathInfo.substring(1));
            
            // Check if user owns the comment
            var commentOpt = commentDAO.findById(commentId);
            if (commentOpt.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"success\":false,\"error\":\"Comment not found\"}");
                return;
            }
            
            PostComment comment = commentOpt.get();
            if (comment.getUserId() != user.getId() && !user.isAdmin() && !user.isModerator()) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"success\":false,\"error\":\"Not authorized to delete this comment\"}");
                return;
            }

            boolean deleted = commentDAO.delete(commentId);
            out.print("{\"success\":" + deleted + "}");
            
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\":false,\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r");
    }
}
