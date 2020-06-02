<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<html>
<script src="https://cdn.bootcss.com/jquery/3.4.1/jquery.min.js"></script>
<body>
<h1>Welcome to nginx!</h1>
<p>If you see this page, the nginx web server is successfully installed and
    working. Further configuration is required.</p>

<p>For online documentation and support please refer to
    <a href="http://nginx.org/">nginx.org</a>.<br>
    Commercial support is available at
    <a href="http://nginx.com/">nginx.com</a>.</p>

<p><em>Thank you for using nginx.</em></p>

<h2>Hello World!</h2>
<h2>tomcat_1</h2>
<h2>tomcat_1</h2>
<h2>tomcat_1</h2>
<%--springmvc上传标签--%>
<p>上传图片</p>
<form name="form1" action="/manage/product/upload.do" method="post" enctype="multipart/form-data">
    <input type="file" name="upload_file"/>
    <input type="submit" value="图片上传文件">
</form>

<p>富文本上传文件</p>
<form name="form1" action="/manage/product/richtext_img_upload.do" method="post" enctype="multipart/form-data">
    <input type="file" name="upload_file"/>
    <input type="submit" value="富文本上传文件">
</form>
</body>
</html>
