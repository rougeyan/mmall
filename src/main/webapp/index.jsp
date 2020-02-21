<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<html>
<body>
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
