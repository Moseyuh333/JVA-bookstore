<%@ page isErrorPage="true" contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>Error</title>
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
  <style>pre{white-space:pre-wrap}</style>
  
</head>
<body>
  <div class="container my-5">
    <h3 class="text-danger">Application Error</h3>
    <p>Message: <%= exception != null ? exception.getMessage() : request.getAttribute("javax.servlet.error.message") %></p>
    <pre>
    <%
      if (exception != null) {
          exception.printStackTrace(new java.io.PrintWriter(out));
      }
    %>
    </pre>
  </div>
</body>
</html>
