package com.uninest.controller.api;

import com.uninest.model.CommunityPost;
import com.uninest.model.PostComment;
import com.uninest.model.User;
import com.uninest.model.dao.CommunityPostDAO;
import com.uninest.model.dao.PostCommentDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * API endpoint for community posts operations.
 * GET: Fetch posts (for AJAX loading)
 * DELETE: Delete a post (owner only)
 */
@WebServlet(name = "postsApi", urlPatterns = "/api/posts/*")
public class PostsApiServlet extends HttpServlet {
    private final CommunityPostDAO postDAO = new CommunityPostDAO();
    private final PostCommentDAO commentDAO = new PostCommentDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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
        
        // GET /api/posts/latest - Get latest posts
        if (pathInfo != null && pathInfo.equals("/latest")) {
            int limit = 10;
            try {
                String limitStr = req.getParameter("limit");
                if (limitStr != null) limit = Integer.parseInt(limitStr);
            } catch (NumberFormatException ignored) {}

            List<CommunityPost> posts = postDAO.findLatest(limit, user.getId());
            out.print("{\"success\":true,\"posts\":[");
            for (int i = 0; i < posts.size(); i++) {
                CommunityPost p = posts.get(i);
                out.print("{");
                out.print("\"postId\":" + p.getPostId() + ",");
                out.print("\"userId\":" + p.getUserId() + ",");
                out.print("\"userName\":\"" + escapeJson(p.getUserName()) + "\",");
                out.print("\"userInitials\":\"" + escapeJson(p.getUserInitials()) + "\",");
                out.print("\"content\":\"" + escapeJson(p.getContent()) + "\",");
                out.print("\"timeAgo\":\"" + escapeJson(p.getTimeAgo()) + "\",");
                out.print("\"likeCount\":" + p.getLikeCount() + ",");
                out.print("\"commentCount\":" + p.getCommentCount() + ",");
                out.print("\"liked\":" + p.isLikedByCurrentUser());
                out.print("}");
                if (i < posts.size() - 1) out.print(",");
            }
            out.print("]}");
            return;
        }

        // GET /api/posts/{id}/comments - Get comments for a post
        if (pathInfo != null && pathInfo.matches("/\\d+/comments")) {
            try {
                int postId = Integer.parseInt(pathInfo.split("/")[1]);
                List<PostComment> comments = commentDAO.findByPost(postId);
                out.print("{\"success\":true,\"comments\":[");
                for (int i = 0; i < comments.size(); i++) {
                    PostComment c = comments.get(i);
                    out.print("{");
                    out.print("\"commentId\":" + c.getCommentId() + ",");
                    out.print("\"userId\":" + c.getUserId() + ",");
                    out.print("\"userName\":\"" + escapeJson(c.getUserName()) + "\",");
                    out.print("\"userInitials\":\"" + escapeJson(c.getUserInitials()) + "\",");
                    out.print("\"content\":\"" + escapeJson(c.getContent()) + "\",");
                    out.print("\"timeAgo\":\"" + escapeJson(c.getTimeAgo()) + "\"");
                    out.print("}");
                    if (i < comments.size() - 1) out.print(",");
                }
                out.print("]}");
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\":false,\"error\":\"Invalid post ID\"}");
            }
            return;
        }

        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        out.print("{\"success\":false,\"error\":\"Invalid endpoint\"}");
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
            out.print("{\"success\":false,\"error\":\"Invalid post ID\"}");
            return;
        }

        try {
            int postId = Integer.parseInt(pathInfo.substring(1));
            
            // Check if user owns the post
            var postOpt = postDAO.findById(postId, user.getId());
            if (postOpt.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"success\":false,\"error\":\"Post not found\"}");
                return;
            }
            
            CommunityPost post = postOpt.get();
            if (post.getUserId() != user.getId() && !user.isAdmin() && !user.isModerator()) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"success\":false,\"error\":\"Not authorized to delete this post\"}");
                return;
            }

            boolean deleted = postDAO.delete(postId);
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
