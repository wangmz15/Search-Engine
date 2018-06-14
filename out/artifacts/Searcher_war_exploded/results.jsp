<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
request.setCharacterEncoding("utf-8");
response.setCharacterEncoding("utf-8");
String [] autocomplete = (String[]) request.getAttribute("autocomplete");
//String path = request.getContextPath();
//String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
//String imagePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+"/";
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title> Search </title>
    <!-- bootstrap -->
    <link href="bootstrap/css/bootstrap.css" rel="stylesheet" />
    <link href="result.css" rel="stylesheet" />
    <link href="bootstrap/css/bootstrap-responsive.css" rel="stylesheet" />
    <link href="bootstrap/css/bootstrap-responsive.css" rel="stylesheet" />
    <link href="bootstrap/js/bootstrap.min.js" rel="stylesheet" />
    <script src="jquery-1.11.3.min.js"></script>
    <script src="bootstrap/js/bootstrap-typeahead.js"></script>
	
    <!-- global font styles -->
    <style type="text/css">
        body,a,p,input,button{font-family:Arial,Verdana,"Microsoft YaHei",Georgia,Sans-serif}
        body{
      background-size: cover;
     }
    </style>
    
</head>

<body>

<script>
	$(document).ready(function($) {
	   // Workaround for bug in mouse item selection
	   $.fn.typeahead.Constructor.prototype.blur = function() {
	      var that = this;
	      setTimeout(function () { that.hide() }, 250);
	   };
	 
	   $('#appendedInputButton').typeahead({
	      source: function(query, process) {
	      $.ajax({
                url: 'Server',
                type: 'post',
                data: {word: query},
                dataType: 'json',
	            success: function(json) {
	                process(json);
	            }
            });     
	      },
	      showHintOnFocus: true,
	      fitToElement: true,
	      autoSelect: true,
	   });
	})
</script>

<%
	String currentQuery=(String) request.getAttribute("currentQuery");
	int currentPage=(Integer) request.getAttribute("currentPage");
%>

<div class = "container" id="searchAndResult">

<div class = "row-fluid" id="searchHead">

<div id="logo"><a href="/Searcher/search.jsp"><img src="logo.png"  style="height:44px; width:120px; "></a></div>
<div id="search">
  <form id="searchForm" name="form1" method="get" action="Server" >
    <div id="inputDiv">
      <input autocomplete="off" data-provide="typeahead" data-items="4" name="query" value="<%=currentQuery%>" id="appendedInputButton" type="text" size="70"  data-items="4" />
    </div>
    <div id="buttonDiv">
    	<input id="buttonInput" type="submit" name="Submit" value="Search"  class = "btn btn-success" />
    </div>
  </form>
</div>
</div>

<div class = "row-fluid" id="resultBody">
	<div class = "span8">
  	<div id = "resultList">
  	<% 
		String [] suggestions = (String[]) request.getAttribute("suggestions");
	  	String[] paths=(String[]) request.getAttribute("paths");
	  	String[] titles = (String[] )request.getAttribute("titles");
	  	String[] descriptions = (String[]) request.getAttribute("descriptions");
	  	if(paths!=null && paths.length> 0) {
	  		for(int i=0;i<paths.length;i++){
	  		%>
	  		<div class="resultItem">
		  		<h3 class="titleHead"><a class="titleHref" href= "<%="http://"+paths[i].substring(paths[i].indexOf("news.tsinghua"))%>" target=" <%=i%>"><%= titles[i] %>
		  		</a></h3>
		  		<div class="urlDiv"><cite class="urlCite">http://news.tsinghua.edu.cn/publish/</cite></div>
		  		<span class="descriptions"><%=descriptions[i]%>
		  		</span>
			</div>
  		<%
  		}; 
  		%>  
	  	<%}else{ %>
	  		<div>no such result</div><%
	  	}; 
  	%>
  	</div>
  	
  	<div class = "pagination" id="allPage">
  	<ul id="pageList">
		<%if(currentPage>1){ %>
			<li><a href="Server?query=<%=currentQuery%>&page=<%=currentPage-1%>">上一页</a></li>
		<%}; %>
		<%for (int i=Math.max(1,currentPage-5);i<currentPage;i++){%>
			<li><a href="Server?query=<%=currentQuery%>&page=<%=i%>"><%=i%></a></li>
		<%}; %>
		    <li class="disabled"><a href = ""><%=currentPage%></a></li>
		<%for (int i=currentPage+1;i<=currentPage+5;i++){ %>
			<li><a href="Server?query=<%=currentQuery%>&page=<%=i%>"><%=i%></a></li>
		<%}; %>
		    <li><a href="Server?query=<%=currentQuery%>&page=<%=currentPage+1%>">下一页</a></li>
	</ul>
	</div>
	</div>
	<div class="span4">
	<table class="table">
	<%if(suggestions!=null && suggestions.length>0){
	  		%><tr><td> <h4 class = "text-success">相关词汇：</h4><h5 > <%
		  	for(int i=0; i < suggestions.length;i++){ %>
		  		<a  href="/Searcher/servlet/Server?query=<%= suggestions[i] %>&Submit=Search">
		  		<%= suggestions[i] %> 
		  		</a>
	  			<br/>
		  	<% }
	  		%></h5></td></tr> <%
	  	}%>
	  </table>
	</div>
  </div>
</div>

</body>