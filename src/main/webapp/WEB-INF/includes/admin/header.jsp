<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bookish Bliss Haven - Admin Panel</title>
        <link rel="icon" type="image/x-icon" href="/static/favicon.ico">
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
        <script src="https://cdn.tailwindcss.com"></script>
        <script src="https://unpkg.com/feather-icons"></script>
        <style>
            @import url('https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;700&family=Roboto:wght@300;400;500&display=swap');

            body {
                font-family: 'Roboto', sans-serif;
            }

            .hero-bg {
                background-image: linear-gradient(rgba(0, 0, 0, 0.5), rgba(0, 0, 0, 0.5)), url('https://static.photos/books/1200x630/42');
                background-size: cover;
                background-position: center;
            }

            .book-card:hover {
                transform: translateY(-5px);
                box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
            }

            .title-font {
                font-family: 'Playfair Display', serif;
            }

            nav a {
                color: inherit;
                text-decoration: none;
            }

            nav a:hover, nav a:focus {
                color: inherit;
                text-decoration: none;
            }
        </style>
    </head>
    <!-- Navigation -->
    <body class="bg-gray-100 text-gray-800">
    <nav class="admin-header bg-amber-800 text-white shadow-lg fixed-top">
        <div class="container mx-auto px-4 py-4">
            <div class="flex justify-between items-center">
                <a href="<%=request.getContextPath()%>/index.jsp" class="flex items-center space-x-2">
                    <i data-feather="book-open" class="w-6 h-6"></i>
                    <span class="title-font text-xl font-bold">Bookish Bliss Haven 
                        <br>
                        <span class="text-sm font-normal">Admin Panel</span>
                    </span>
                </a>
                <div class="flex items-center space-x-4">
                    <!-- User Dropdown -->
                    <div class="relative">
                        <button id="userDropdownBtn"
                            class="p-2 rounded-full hover:bg-amber-700 focus:bg-amber-700 focus:outline-none">
                            <i data-feather="user" class="w-5 h-5"></i>
                        </button>
                        <div id="userDropdown"
                            class="hidden absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg border border-gray-200 z-50">
                            <div class="py-2">
                                <a href="<%=request.getContextPath()%>/login.jsp"
                                    class="flex items-center px-4 py-2 text-gray-800 hover:bg-amber-50 hover:text-amber-800">
                                    <i data-feather="log-in" class="w-4 h-4 mr-2"></i>
                                    Đăng nhập
                                </a>
                                <a href="<%=request.getContextPath()%>/register.jsp"
                                    class="flex items-center px-4 py-2 text-gray-800 hover:bg-amber-50 hover:text-amber-800">
                                    <i data-feather="user-plus" class="w-4 h-4 mr-2"></i>
                                    Đăng ký
                                </a>
                                <hr class="my-1">
                                <a href="<%=request.getContextPath()%>/forgot-password.jsp"
                                    class="flex items-center px-4 py-2 text-gray-800 hover:bg-amber-50 hover:text-amber-800">
                                    <i data-feather="key" class="w-4 h-4 mr-2"></i>
                                    Quên mật khẩu
                                </a>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </nav>