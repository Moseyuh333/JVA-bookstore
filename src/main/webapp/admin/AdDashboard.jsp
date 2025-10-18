<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>


<%@ include file="/WEB-INF/includes/admin/header.jsp" %>

    <div id="wrapper">
    <!-- Include Sidebar -->
    <%@ include file="/WEB-INF/includes/admin/AdSideBar.jsp" %>

    <!-- ====== CONTENT ====== -->
    <div id="content-wrapper" class="p-4" style="margin-left:260px;">

        <h2 class="font-weight-bold mb-4 text-dark">
            <i class="fas fa-chart-line text-warning mr-2"></i> Dashboard tổng quan
        </h2>

        <!-- ====== STAT CARDS ====== -->
        <div class="row">

            <div class="col-md-3 mb-4">
                <div class="card shadow-sm border-left-warning">
                    <div class="card-body d-flex justify-content-between align-items-center">
                        <div>
                            <h6 class="text-muted text-uppercase mb-1">Người dùng</h6>
                            <h4 class="font-weight-bold text-dark" id="userCount">1,254</h4>
                        </div>
                        <i class="fas fa-users fa-2x text-warning"></i>
                    </div>
                </div>
            </div>

            <div class="col-md-3 mb-4">
                <div class="card shadow-sm border-left-primary">
                    <div class="card-body d-flex justify-content-between align-items-center">
                        <div>
                            <h6 class="text-muted text-uppercase mb-1">Sản phẩm</h6>
                            <h4 class="font-weight-bold text-dark" id="productCount">3,640</h4>
                        </div>
                        <i class="fas fa-book fa-2x text-primary"></i>
                    </div>
                </div>
            </div>

            <div class="col-md-3 mb-4">
                <div class="card shadow-sm border-left-success">
                    <div class="card-body d-flex justify-content-between align-items-center">
                        <div>
                            <h6 class="text-muted text-uppercase mb-1">Đơn hàng</h6>
                            <h4 class="font-weight-bold text-dark" id="orderCount">879</h4>
                        </div>
                        <i class="fas fa-shopping-cart fa-2x text-success"></i>
                    </div>
                </div>
            </div>

            <div class="col-md-3 mb-4">
                <div class="card shadow-sm border-left-danger">
                    <div class="card-body d-flex justify-content-between align-items-center">
                        <div>
                            <h6 class="text-muted text-uppercase mb-1">Doanh thu (VNĐ)</h6>
                            <h4 class="font-weight-bold text-dark" id="revenueCount">356,000,000</h4>
                        </div>
                        <i class="fas fa-dollar-sign fa-2x text-danger"></i>
                    </div>
                </div>
            </div>
        </div>

        <!-- ====== CHART ====== -->
        <div class="card shadow-sm mb-4">
            <div class="card-header bg-gradient-warning text-white">
                <h6 class="m-0 font-weight-bold"><i class="fas fa-chart-bar mr-2"></i>Doanh thu 6 tháng gần nhất</h6>
            </div>
            <div class="card-body">
                <canvas id="revenueChart" height="120"></canvas>
            </div>
        </div>

        <!-- ====== TOP SELLERS ====== -->
        <div class="card shadow-sm mb-5">
            <div class="card-header bg-gradient-warning text-white">
                <h6 class="m-0 font-weight-bold"><i class="fas fa-store mr-2"></i>Top cửa hàng bán chạy</h6>
            </div>
            <div class="card-body">
                <div class="table-responsive">
                    <table class="table table-bordered mb-0">
                        <thead class="thead-light">
                            <tr>
                                <th>#</th>
                                <th>Tên cửa hàng</th>
                                <th>Tổng đơn hàng</th>
                                <th>Doanh thu</th>
                                <th>Chiết khấu</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td>1</td>
                                <td>BookHaven</td>
                                <td>254</td>
                                <td>92,000,000₫</td>
                                <td>10%</td>
                            </tr>
                            <tr>
                                <td>2</td>
                                <td>MangaWorld</td>
                                <td>187</td>
                                <td>68,500,000₫</td>
                                <td>15%</td>
                            </tr>
                            <tr>
                                <td>3</td>
                                <td>LightNovelVN</td>
                                <td>143</td>
                                <td>54,000,000₫</td>
                                <td>12%</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

    </div>
    <!-- END CONTENT -->

    <%@ include file="/WEB-INF/includes/admin/footer.jsp" %>

    <script src="${pageContext.request.contextPath}/assets/js/admin/AdDashboard.js"></script>
</body>
</html>
