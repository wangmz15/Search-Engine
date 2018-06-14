import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import com.google.gson.Gson;

import java.util.*;

import java.math.*;
import java.net.*;
import java.io.*;


public class Server extends HttpServlet{
    // TODO:hard code
    public static final String INDEX_ABSOLUTE_PATH = "/Users/wangmz15/Desktop/Searcher";
    static final String PAGE_ROOT_DIR = "/Users/wangmz15/Downloads/heritrix/jobs/news/mirror/news.tsinghua.edu.cn";

    public static final int PAGE_RESULT=10;
    private Searcher search;
    public Server(){
        super();
        search=new Searcher(new String(INDEX_ABSOLUTE_PATH+"/index"));//初始化Searcher
    }

    public ScoreDoc[] showList(ScoreDoc[] results,int page){
        if(results==null || results.length<(page-1)*PAGE_RESULT){
            return null;
        }
        int start=Math.max((page-1)*PAGE_RESULT, 0);
        int docnum=Math.min(results.length-start,PAGE_RESULT);
        ScoreDoc[] ret=new ScoreDoc[docnum];
        for(int i=0;i<docnum;i++){
            ret[i]=results[start+i];
        }
        return ret;
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=utf-8");
        request.setCharacterEncoding("utf-8");

        String queryString=request.getParameter("query");
        String pageString=request.getParameter("page");
        if(queryString.replace(" ", "").length() == 0) {
            System.out.println("null query");
            response.sendRedirect("/index.jsp");
            return;
        }
        int page=1;
        if(pageString!=null){
            page=Integer.parseInt(pageString);
        }
        if(queryString==null){
            System.out.println("null query");
        }else{
            TopDocs results=search.searchQuery(queryString, 100);
            String[] paths = null;
            String[] titles = null;
            String[] descriptions = null;
            String[] imgURL = null;


            //System.out.println(autocomplete.toString()+" "+corrections.toString()+" "+suggestions.toString());
            if (results != null) {
                ScoreDoc[] hits = showList(results.scoreDocs, page);
                if (hits != null) {
                    paths = new String[hits.length];
                    titles = new String[hits.length];
                    descriptions = new String[hits.length];
                    imgURL = new String[hits.length];
                    for (int i = 0; i < hits.length && i < PAGE_RESULT; i++) {
                        //Document doc = search.getDoc(hits[i].doc);
                        paths[i] = "news.tsinghua.edu.cn" + search.getDoc(hits[i].doc).get("path").replace(PAGE_ROOT_DIR, "");
                        titles[i] = search.getDecoratedTitle(hits[i].doc) ;
                        descriptions[i] = search.getDecoratedDescription(hits[i].doc);
                    }
                } else {
                    System.out.println("page null");
                }
            }else{
                System.out.println("result null");
            }
            ArrayList<String> a1 = search.getCompletion(queryString);
            ArrayList<String> b1 = search.getCorrection(queryString);
            List<String> c1 = search.getRelateRecommend(queryString);

            request.setAttribute("currentQuery",queryString);
            request.setAttribute("currentPage", page);
            request.setAttribute("paths", paths);
            request.setAttribute("titles", titles);
            request.setAttribute("descriptions", descriptions);

            String [] autocomplete = (String[])a1.toArray(new String[a1.size()]);
            String [] corrections = (String[])b1.toArray(new String[b1.size()]);
            String [] suggestions = (String[])c1.toArray(new String[c1.size()]);

            request.setAttribute("autocomplete", autocomplete);
            request.setAttribute("corrections", null);
            request.setAttribute("suggestions", suggestions);

            request.getRequestDispatcher("/results.jsp").forward(request,response);
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        boolean ajax = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
        if (ajax) {
//			System.out.println(request.toString());
            String word = request.getParameter("word");
            System.out.println(word);

            List<String> completeList = search.getCompletion(word);
            String json = new Gson().toJson(completeList);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json);
        }
        else {
            System.out.print("NOT AJAX\n");
        }
    }
}
