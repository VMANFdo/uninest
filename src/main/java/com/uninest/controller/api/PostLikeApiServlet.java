package com.uninest.controller.api;

import com.uninest.model.User;
import com.uninest.model.dao.PostLikeDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * API endpoint for toggling post likes.
 * POST: Toggle like on a post (like if not liked, unlike if liked)
 * Returns JSON with new like status and count
 */
@WebServlet(name = "postLikeApi", urlPatterns = "/api/posts/like")
public class PostLikeApiServlet extends HttpServlet {
    private final PostLikeDAO likeDAO = new PostLikeDAO();

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
        if (postIdStr == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\":false,\"error\":\"Missing postId\"}");
            return;
        }

        try {
            int postId = Integer.parseInt(postIdStr);
            
            // Toggle the like
            boolean isNowLiked = likeDAO.toggleLike(postId, user.getId());
            int newCount = likeDAO.countByPost(postId);

            out.print("{\"success\":true,\"liked\":" + isNowLiked + ",\"likeCount\":" + newCount + "}");
            
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\":false,\"error\":\"Invalid postId\"}");
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
