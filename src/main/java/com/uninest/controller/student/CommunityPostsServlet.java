package com.uninest.controller.student;

import com.uninest.model.CommunityPost;
import com.uninest.model.User;
import com.uninest.model.dao.CommunityPostDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Servlet for displaying community posts/highlights.
 * GET: Display posts list for the user's community
 */
@WebServlet(name = "communityPosts", urlPatterns = "/student/posts")
public class CommunityPostsServlet extends HttpServlet {
    private final CommunityPostDAO postDAO = new CommunityPostDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("authUser");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }
        
        if (user.getCommunityId() == null) {
            resp.sendRedirect(req.getContextPath() + "/student/join-community");
            return;
        }

        // Get posts for the user's community
        List<CommunityPost> posts = postDAO.findByCommunity(user.getCommunityId(), user.getId());
        req.setAttribute("posts", posts);
        
        req.getRequestDispatcher("/WEB-INF/views/student/posts.jsp").forward(req, resp);
    }
}
