<%@ page language="java"  contentType="text/html; charset=UTF-8" %>

<html>
<body>

<h1>tomcat 1 !</h1>
<h1>tomcat 1 !</h1>
<h1>tomcat 1 !</h1>
<h2>Hello World!</h2>

<h3>springmvc上传文件</h3>
<form name="form1" action="/manage/product/upload.do" method="post" enctype="multipart/form-data">
    <input type="file" name="upload_file"/>
    <input type="submit" name="springmvc上传文件"/>
</form>


<h3>富文本上传文件</h3>
<form name="form1" action="/manage/product/richtext_img_upload.do" method="post" enctype="multipart/form-data">
    <input type="file" name="upload_file"/>
    <input type="submit" name="富文本上传文件"/>
</form>



</body>
</html>
