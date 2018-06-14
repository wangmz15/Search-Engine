<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
  request.setCharacterEncoding("utf-8");
  System.out.println(request.getCharacterEncoding());
  response.setCharacterEncoding("utf-8");
  System.out.println(response.getCharacterEncoding());
  String path = request.getContextPath();
  String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
  System.out.println(path);
  System.out.println(basePath);
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
  <title>Sixer</title>
  <!-- bootstrap -->
  <link href="servlet/bootstrap/css/bootstrap.css" rel="stylesheet" />
  <link href="servlet/bootstrap/css/bootstrap-responsive.css" rel="stylesheet" />
  <link href="servlet/search.css" rel="stylesheet" />

  <!-- global font styles -->
  <style type="text/css">
    body,a,p,input,button{font-family:Arial,Verdana,"Microsoft YaHei",Georgia,Sans-serif}
    body{
      background-size: cover;
    }
  </style>
</head>
<body>
<center>
  <div style="height:30px;margin-top:110px" >
  </div>
  <div>
    <h1><img src="servlet/logo0.png" width="270" height="99"></h1>
    <form id="searchForm" name="form1" method="get" action="servlet/Server" class="form-search">
      <div id="inputDiv">
        <input name="query" type="text" size="50" id="appendedInputButton"/>
      </div>
      <div id="buttonDiv">
        <input id="buttonInput" class = "btn btn-success" type="submit" name="Submit" value="Search"/>
      </div>
    </form>
  </div>
  <div style=" margin-top:230px ">Copyright © 2017 - 2018 SIXER. All Rights Reserved</div>
</center>
</body>
</html>
