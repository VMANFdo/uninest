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

/**
 * Servlet for creating new community posts.
 * GET: Show create post form
 * POST: Create new post
 */
@WebServlet(name = "createPost", urlPatterns = "/student/posts/create")
public class CreatePostServlet extends HttpServlet {
    private final CommunityPostDAO postDAO = new CommunityPostDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("authUser");
        if (user == null || user.getCommunityId() == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }
        req.getRequestDispatcher("/WEB-INF/views/student/create-post.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("authUser");
        if (user == null || user.getCommunityId() == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        String content = req.getParameter("content");
        if (content == null || content.trim().isEmpty()) {
            req.setAttribute("error", "Post content cannot be empty");
            req.getRequestDispatcher("/WEB-INF/views/student/create-post.jsp").forward(req, resp);
            return;
        }

        CommunityPost post = new CommunityPost();
        post.setUserId(user.getId());
        post.setCommunityId(user.getCommunityId());
        post.setContent(content.trim());
        post.setStatus("active");

        postDAO.create(post);
        
        resp.sendRedirect(req.getContextPath() + "/student/posts");
    }
}
