import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.Weight;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.TextFragment;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.spell.Dictionary;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.search.suggest.fst.BytesRefSorter;
import org.apache.lucene.search.suggest.fst.ExternalRefSorter;
import org.apache.lucene.search.suggest.fst.FSTCompletion;
import org.apache.lucene.search.suggest.fst.FSTCompletionBuilder;
import org.apache.lucene.search.suggest.fst.InMemorySorter;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.wltea.analyzer.lucene.IKAnalyzer;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;


public class Searcher {
    // TODO:hard code
    final static String RELATE_RECOMMEND_FILE = "/Users/wangmz15/Desktop/Searcher/relate.txt";//离线保存每⼀关键词最相关的8个词项。

    private IndexReader reader;
    private IndexSearcher searcher;
    private Analyzer analyzer;
    private SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter("<em>", "</em>");
    private Highlighter highlighter = null;
    private float avgLength=1.0f;

    RelateRecommend RelateRecommend = new RelateRecommend();
    FSTCompletion fstCompletion = null;


    @SuppressWarnings("deprecation")
    public Searcher(String indexdir){//用index来初始化Searcher
        analyzer = new IKAnalyzer();
        System.out.println("Initialzing");
        try{
            reader = IndexReader.open(FSDirectory.open(new File(indexdir)));
            searcher = new IndexSearcher(reader);
            searcher.setSimilarity(new BM25Similarity());

            FSTCompletionBuilder fstb = new FSTCompletionBuilder();
            TermsEnum termEnum = MultiFields.getTerms(reader, "content").iterator(null);
            int cnt = 0;
            while(termEnum.next() != null)//建立好索引之后，将所有的词，将content域中的每个token（即⽂本经过分词后得到的词汇）取出加入FSTCompletionBuilder，并将每个查询词的重要性置为其在各个⽂档中的出现频度。然后交由FSTCompletion模块进⾏处理。
            {
                cnt = cnt + 1;
                if(cnt % 5000 == 0) {
                    System.out.println(cnt);
                    //break;
                }

                DocsEnum docEnum = MultiFields.getTermDocsEnum(reader, MultiFields.getLiveDocs(reader), "content", termEnum.term());
                int doc;//是对输⼊的词汇构造有限状态⾃动机
                while((doc = docEnum.nextDoc())!= DocsEnum.NO_MORE_DOCS ){//每找到⼀个终态就把当前路径对应的词汇添加进备选词汇列表中

                    Utils.add( termEnum.term().utf8ToString(), docEnum.freq(), doc);

                    int weight = termEnum.docFreq();
                    weight = FSTWeightConvert(weight);
                    fstb.add(termEnum.term(), weight);
                }
            }
            System.out.println("Handle terms finish!\n");
            System.out.println("loading recommend...");
            RelateRecommend.load(RELATE_RECOMMEND_FILE);
            System.out.println("load recommend finish.\n");

            System.out.println("buliding completion...");
            fstCompletion = fstb.build();
            System.out.println("bulid completion finish.\n");

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public String getImageUrl(String page){
        return null;
    }

    public ArrayList<String> getCompletion(String querystring){

        System.out.print("AutoComplete: [");
        ArrayList<String> rst = new ArrayList<String>();
        if(fstCompletion == null)
            System.out.println("??????????????????????");
        List<FSTCompletion.Completion> c = fstCompletion.lookup(querystring, 6);
        for(FSTCompletion.Completion a: c) {
            System.out.print(a.utf8.utf8ToString()+ ", ");
            rst.add(a.utf8.utf8ToString());
        }
        System.out.print("]\n");
        return rst;
    }


    public List<String> getRelateRecommend(String querystring){
        List<String> result = RelateRecommend.find(querystring, 8);
        System.out.println("RelateRecommend: "+ result);
        return result;
    }

    public ArrayList<String> getCorrection(String querystring)
    {
        ArrayList<String> fake = new ArrayList<String>();
        fake.add("completion");
        fake.add("completion2");
        return fake;
    }


    public TopDocs searchQuery(String queryString, int maxnum){
        try {
            String [] fields = new String[] {"title","keyword","content","link"};//每个域有⼀个域的权重 。
            Map<String, Float> boosts = new HashMap<String, Float>();
            boosts.put("title", 1.0f);//超参数需要进行调整
            boosts.put("keyword", 1.0f);
            boosts.put("content", 5.0f);
            boosts.put("link", 100.0f);
            QueryParser parser = new MultiFieldQueryParser(Version.LUCENE_40,fields, analyzer, boosts);
            Query query = parser.parse(queryString);
            // highlighter will be used as text filter in function(getDecoratedTitle and Description)
            highlighter = new Highlighter(htmlFormatter, new QueryScorer(query));
            TopDocs results = searcher.search(query, maxnum);

            return results;
        } catch (Exception e) {
//			e.printStackTrace();
        }
        return null;
    }

    public Document getDoc(int docID){
        try{
            return searcher.doc(docID);
        }catch(IOException e){
            e.printStackTrace();
        }
        return null;
    }


    public String getDecoratedTitle(int docID) {
        // See if the title match the query
        TokenStream tokenStream = null;
        try {
            tokenStream = TokenSources.getAnyTokenStream(searcher.getIndexReader(), docID, "title", analyzer);
            String title_matched = highlighter.getBestFragments(tokenStream, searcher.doc(docID).get("title"), 1, "...");
            if(title_matched.length() > 0){
                return title_matched;
            } else {
                return searcher.doc(docID).get("title");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getDecoratedDescription(int docID) {
        try{
            // Try to get matched text from Field content and Field Link, if impossible, just use content
            TokenStream tokenStream = TokenSources.getAnyTokenStream(searcher.getIndexReader(), docID, "content", analyzer);
            String content = highlighter.getBestFragments(tokenStream, searcher.doc(docID).get("content"), 1, "...");

            TokenStream tokenStream2 = TokenSources.getAnyTokenStream(searcher.getIndexReader(), docID, "link", analyzer);
            String link = highlighter.getBestFragments(tokenStream2, searcher.doc(docID).get("link"), 1, "...");

            if(content.length()>0)
                return content;
            else if(link.length()>0)
                return link;
            else if(content.length()+link.length()==0)
                return searcher.doc(docID).get("content").substring(0, Math.min(searcher.doc(docID).get("content").length(), 100));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static int FSTWeightConvert(int weight) {
        int[] convertors = {400, 200, 100, 50, 40, 30, 20, 10};
        int convertedWeight = 9;
        for (int convertor : convertors) {
            if(weight > convertor) break;
            -- convertedWeight;
        }
        return convertedWeight;
    }
}
