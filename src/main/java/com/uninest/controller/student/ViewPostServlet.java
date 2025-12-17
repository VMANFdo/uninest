package com.uninest.controller.student;

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
import java.util.List;
import java.util.Optional;

/**
 * Servlet for viewing a single post with its comments.
 * GET: View post details and comments
 * POST: Add a new comment
 */
@WebServlet(name = "viewPost", urlPatterns = "/student/posts/view")
public class ViewPostServlet extends HttpServlet {
    private final CommunityPostDAO postDAO = new CommunityPostDAO();
    private final PostCommentDAO commentDAO = new PostCommentDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("authUser");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        String postIdStr = req.getParameter("id");
        if (postIdStr == null) {
            resp.sendRedirect(req.getContextPath() + "/student/posts");
            return;
        }

        try {
            int postId = Integer.parseInt(postIdStr);
            Optional<CommunityPost> postOpt = postDAO.findById(postId, user.getId());
            
            if (postOpt.isEmpty()) {
                resp.sendRedirect(req.getContextPath() + "/student/posts");
                return;
            }

            CommunityPost post = postOpt.get();
            List<PostComment> comments = commentDAO.findByPost(postId);

            req.setAttribute("post", post);
            req.setAttribute("comments", comments);
            req.getRequestDispatcher("/WEB-INF/views/student/view-post.jsp").forward(req, resp);
            
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/student/posts");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("authUser");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/auth/login");
            return;
        }

        String postIdStr = req.getParameter("postId");
        String content = req.getParameter("content");

        if (postIdStr == null || content == null || content.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/student/posts");
            return;
        }

        try {
            int postId = Integer.parseInt(postIdStr);
            
            PostComment comment = new PostComment();
            comment.setPostId(postId);
            comment.setUserId(user.getId());
            comment.setContent(content.trim());
            comment.setStatus("active");
            
            commentDAO.create(comment);
            
            resp.sendRedirect(req.getContextPath() + "/student/posts/view?id=" + postId);
            
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/student/posts");
        }
    }
}
