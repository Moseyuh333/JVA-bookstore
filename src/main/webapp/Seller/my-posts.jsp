<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.sql.*,java.util.*,com.myapp.model.*,com.myapp.utils.*" %>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Bài đăng của tôi</title>
</head>
<body>
  <h2>📰 Danh sách bài đăng của bạn</h2>
  <a href="${pageContext.request.contextPath}/seller/create-post.jsp">+ Đăng bài mới</a>
  <hr>

  <%
    User user = (User) session.getAttribute("user");
    if (user != null) {
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT * FROM seller_posts WHERE seller_id = ? ORDER BY created_at DESC";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, user.getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
  %>
    <div style="border:1px solid #ccc; padding:10px; margin:10px 0;">
      <h3><%= rs.getString("title") %></h3>
      <p><%= rs.getString("content") %></p>
      <% if (rs.getString("image_url") != null && !rs.getString("image_url").isEmpty()) { %>
          <img src="<%= rs.getString("image_url") %>" style="max-width:200px;">
      <% } %>
      <p><small>Đăng ngày: <%= rs.getTimestamp("created_at") %></small></p>
    </div>
  <%
            }
        } catch (Exception e) {
            out.print("<p>Lỗi: " + e.getMessage() + "</p>");
        }
    } else {
        out.print("<p>Vui lòng đăng nhập trước.</p>");
    }
  %>
</body>
</html>
