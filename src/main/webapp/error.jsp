<%@ page isErrorPage="true" contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/page" prefix="page" %>
<page:title>Error</page:title>
<div class="container my-5">
  <h3 class="text-danger">Application Error</h3>
  <p>Message: <%= exception != null ? exception.getMessage() : "Unknown" %></p>
  <pre>
  <%
    if (exception != null) {
        exception.printStackTrace(new java.io.PrintWriter(out));
    }
  %>
  </pre>
</div>
