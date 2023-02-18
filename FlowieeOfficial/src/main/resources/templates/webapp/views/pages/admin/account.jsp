<!DOCTYPE html>
<html lang="en" xmlns:th="https://www.thymeleaf.org">

<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Tài khoản hệ thống</title>
  <div th:replace="header :: stylesheets">
    <!--Nhúng các file css, icon,...-->
  </div>
</head>

<body class="hold-transition sidebar-mini layout-fixed">
  <div class="wrapper">
    <!-- Navbar (header) -->
    <div th:replace="header :: header"></div>
    <!-- /.navbar (header)-->

    <!-- Main Sidebar Container -->
    <div th:replace="sidebar :: sidebar"></div>

    <!-- Content Wrapper. Contains page content -->
    <div class="content-wrapper mt-3">
      <!-- Main content -->
      <section class="content">
        <div class="container-fluid">
          <!-- Small boxes (Stat box) -->
          <div class="row">
            <div class="col-12">
              <div class="card">
                <div class="card-header">
                  <h3 class="card-title"><strong>TÀI KHOẢN HỆ THỐNG</strong></h3>
                </div>
                <!-- /.card-header -->
                <div class="card-body">
                  <table id="example1" class="table table-bordered table-striped">
                    <thead>
                      <tr>
                        <th>ID</th>
                        <th>Họ tên</th>
                        <th>Giới tính</th>
                        <th>Thông tin liên hệ</th>
                        <th>Vai trò</th>
                        <th>Trạng thái</th>
                        <th>Thao tác</th>
                      </tr>
                    </thead>
                    <tbody>
                      <th:block th:each="list : ${listAccount}">
                        <tr>
                          <td th:text="${list.ID}"></td>
                          <td>
                            <th:block th:text="${list.username}">
                            </th:block> <br>
                            <th:block th:text="${list.name}">
                            </th:block>
                          </td>
                          <td th:text="${list.gender}"></td>
                          <td>
                            <th:block th:text="${list.phone}">
                            </th:block> <br>
                            <th:block th:text="${list.email}">
                            </th:block>
                          </td>
                          <td th:text="${list.isAdmin}"></td>
                          <td th:text="${list.status}"></td>
                          <td>update, delete</td>
                        </tr>
                      </th:block>
                    </tbody>
                    <tfoot>
                      <tr>
                        <th>ID</th>
                        <th>Họ tên</th>
                        <th>Giới tính</th>
                        <th>Thông tin liên hệ</th>
                        <th>Vai trò</th>
                        <th>Trạng thái</th>
                        <th>Thao tác</th>
                      </tr>
                    </tfoot>
                  </table>
                </div>
                <!-- /.card-body -->
              </div>
            </div>
          </div>
      </section>
      <!-- /.content -->
    </div>
    <!-- /.content-wrapper -->


    <!-- Control Sidebar -->
    <aside class="control-sidebar control-sidebar-dark">
      <!-- Control sidebar content goes here -->
    </aside>

    <div th:replace="header :: scripts">
      <!-- Nhúng các file JavaScript vào -->
    </div>
  </div>

</body>

</html>
