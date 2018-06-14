import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
public class Utils {

    static HashMap<Integer, ArrayList<Integer>> tf = new HashMap<Integer, ArrayList<Integer>>();
    static HashMap<Integer, Integer> df = new HashMap<Integer, Integer>();
    static HashMap<String, Integer> s2i = new HashMap<String, Integer>();
    static HashMap<Integer, String> i2s = new HashMap<Integer, String>();
    static int icnt = 0;
    static ArrayList<String> getFilePathList(String path, String type) {
        File[] fileList = (new File(path)).listFiles();
        ArrayList<String> filePathList = new ArrayList<String>();
        for (File file : fileList) {
            if (file.isDirectory()) {
                ArrayList<String> sonFilePathList = getFilePathList(file.getAbsolutePath(), type);
                filePathList.addAll(sonFilePathList);
            } else if (file.isFile() && getFileType(file.getName()).toLowerCase().equals(type)) {
                filePathList.add(file.getAbsolutePath());
            }
//			if(filePathList.size() > 10) return filePathList;
        }
        return filePathList;
    }
    // Useless
    static ArrayList<String> getSpecificFilePathList(ArrayList<String> filePathList, String type) {
        ArrayList<String> specificFilePathList = new ArrayList<String>();
        for (String filePath : filePathList) {
            File file = new File(filePath);
            String fileName = file.getName();
            String fileType = getFileType(fileName);
            if (fileType.toLowerCase().equals(type)) {
                specificFilePathList.add(filePath);
            }
        }
        return specificFilePathList;
    }

    static String getFileType(String fileName) {
        int pos = fileName.lastIndexOf('.');
        return fileName.substring(pos + 1);
    }

    static void add(String word, int freq, int docID) {
        for (int i = 0; i < word.length(); ++i) {
            char c = word.charAt(i);
            if (c >= '0' && c <= '9') return;
        }
        int wordID;
        if (s2i.containsKey(word)) {
            wordID = s2i.get(word);
        } else {
            s2i.put(word, icnt);
            i2s.put(icnt, word);
            df.put(icnt, 0);
            wordID = icnt++;
        }
        df.put(wordID, df.get(wordID) + 1);

    }
}
