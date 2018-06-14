import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.TokenSources;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class RelateRecommend {

    final int MAX_RECOMMEND_NUM = 8;
    final double RELATE_THRESHOLD = 0.05;

    Set<String> handledWordSet =new HashSet<>();
    HashMap<Integer, ArrayList<Integer>> relate = new HashMap<>();


    ArrayList<String> find(String word, int num) {
        if (!Utils.s2i.containsKey(word)) {
            return new ArrayList<>();
        }
        int wordId = Utils.s2i.get(word);
        if(!relate.containsKey(wordId)) return new ArrayList<String>();
        ArrayList<Integer> relateIdList = relate.get(wordId);
        ArrayList<String> relateList = new ArrayList<String>();
        for (int i = 0; i < Math.min(num, relateIdList.size()); ++i) {
            relateList.add(Utils.i2s.get(relateIdList.get(i)));
        }
        return relateList;
    }

    void initAndSave(Searcher search, IndexReader reader, IndexSearcher searcher, String filePath) {
        ArrayList<String> lines = new ArrayList<String>();
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath)));

            TermsEnum termEnum = MultiFields.getTerms(reader, "content").iterator(null);
            int cnt = 0;
            while(termEnum.next() != null) {
                cnt = cnt + 1;
                if(cnt % 1000 == 0)  {
                    System.out.println(cnt);
                }

                DocsEnum docEnum = MultiFields.getTermDocsEnum(reader, MultiFields.getLiveDocs(reader), "content", termEnum.term());
                int doc;
                while((doc = docEnum.nextDoc())!= DocsEnum.NO_MORE_DOCS ){//对于每一个词
//					System.out.println(termEnum.term().utf8ToString() + ", " + docEnum.freq() + ", " + doc);
                    String word = termEnum.term().utf8ToString();
                    if(handledWordSet.contains(word)) {
//						System.out.println("in set.");
                        continue;
                    }
                    if(!Utils.s2i.containsKey(word)) continue;
                    handledWordSet.add(word);
                    TopDocs results=search.searchQuery(word, 5);
                    if (results == null) {
//						System.out.println("no result!");
                        continue;
                    }
                    ScoreDoc[] hits = results.scoreDocs;//查询并找到相关结果
                    if (hits == null) continue;
                    HashMap<String, Double> frequency = new HashMap<String, Double>();
                    for (int i = 0; i < hits.length; i++) {//对每一个结果
                        Document document = searcher.doc(hits[i].doc);

                        Analyzer analyzer=new IKAnalyzer(true);
                        StringReader stringReader=new StringReader(document.get("content"));
                        TokenStream ts=analyzer.tokenStream("content", stringReader);//内容进行分词

                        while(ts.incrementToken()) {//对分词后对每一个词
                            String wordB =ts.getAttribute(CharTermAttribute.class).toString();
                            double wordBFreq = 0.0;
                            if(frequency.containsKey(wordB)) wordBFreq = frequency.get(wordB);//计算它出现对频率并加入到hashmap里去
                            frequency.put(wordB, wordBFreq + 1);
                        }
                        stringReader.close();
                    }
                    if(frequency.size() < 1) continue;
                    for (Entry<String, Double> wordEntry : frequency.entrySet()) {//对于hashmap里的每一个关键词
                        String wordB = wordEntry.getKey();
                        double wordBFreq = 0.0;
                        if(Utils.s2i.containsKey(wordB)) {//tf:查询词 和待推荐关键词 同时出 现在⼀篇⽂档中的次数,除以 关键词出现在多少个文档之中的数目
                            wordBFreq = frequency.get(wordB) / Math.sqrt(2.0 * Utils.df.get(Utils.s2i.get(wordB)));//使用tf*idf模型思想计算出得分
                        }
                        if(wordB.equals("µÄ") || wordB.equals("and") || wordB.equals("of")) wordBFreq = 0.0;
                        frequency.put(wordB, wordBFreq);
                    }
                    List<Entry<String,Double>> list = new ArrayList<Entry<String,Double>>(frequency.entrySet());
                    Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {//排序
                        public int compare(Map.Entry<String, Double> o1,
                                           Map.Entry<String, Double> o2) {
                            Double d1 = o1.getValue();
                            Double d2 = o2.getValue();
                            return d2.compareTo(d1);
                        }
                    });

                    int recommendCount = 0;
                    String line = word + " " + Math.min(MAX_RECOMMEND_NUM, list.size()) + " ";
                    for(Entry<String,Double> wordEntry : list) {//把每一个词的8个相关推荐写入relate.txt文档之中
                        if(++recommendCount > MAX_RECOMMEND_NUM) break;
                        line += (wordEntry.getKey() + " ");
                    }

                    writer.write(line);
                    writer.write("\n");
                }
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void load(String filePath) {
        System.out.println("In Load(" + filePath + ")");
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
            for (String line = br.readLine(); line != null; line = br.readLine())
            {
//				System.out.println(line);
                String[] lineArray = line.split(" ");
                String word = lineArray[0];
                if(!Utils.s2i.containsKey(word)) continue;
                int wordId = Utils.s2i.get(word);
//				System.out.println(word + ": " + lineArray[2]);
                relate.put(wordId, new ArrayList<>());
                for (int i = 2; i < lineArray.length; ++i) {
                    String word2 = lineArray[i];
                    if(Utils.s2i.containsKey(word2)) {
                        int word2Id = Utils.s2i.get(word2);
                        relate.get(wordId).add(word2Id);
                    }
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
