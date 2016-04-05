package usrhandle;

import com.sun.org.apache.bcel.internal.generic.NEW;
import org.json.JSONException;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;
import tools.Constants;
import tools.RedisClient;
import tools.Rule;
import urlhandle.TitleKeyWords;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by tian on 15-12-17.
 */
/*
1请求日期Datetime发起请求的时间,格式：20151217101232
2响应时间Datetime返回响应的时间,格式：20151217101232
3源IPString发起请求的客户端IP
4源端口String发起请求的客户端端口
5目的IPString服务器端IP
6目的端口String服务器端端口
7响应IPString返回响应到客户端的IP
8响应端口String返回响应到客户端端口
9客户端IPString接受响应的客户端IP
10客户端端口String接受响应的客户端端口
11请求方法String请求的方法类型，如GET
12URLString请求的URL
13User-AgentString用户的访问设备类型
14ReferURLString发起请求的上级URL
15CookieString发送请求时携带的Cookie集合，以Key=value形式
16域名String返回响应的
17URIString响应的URI18ContentString页面的HTML内容
 */
public class UserDataInformation {
    private String reqTime;
    private String reqHour;
    private String respTime;
    private String reqIP;
    private String reqPort;
    private String destinationIP;
    private String destinationPost;
    private String respIP;
    private String respPort;
    private String acceptRespIP;
    private String acceptRespPost;
    private String reqMethod;
    private String reqURL = "url";
    private String userEquipment;
    private String reqSupURL;
    private String reqCookie;
    private String domainName;
    private String htmlContent;
    private String schoolMessage;
    private String referURL;
    private List<String> keyWords;
    public boolean isNormalMessage;
    private Type type;
    private TitleKeyWords titleKeyWords;
    private Set<String> containsSensitiveWordsList;
    private String normalCategory;
    private String sensitiveCategory;
    private String uid;
    private String uname;
    private String mac;
    private String categoryId;
    private List<String> sensitiveId = new ArrayList<>();
    private boolean isRightTime;
    //    private String learnCategoryName = "";
    private String calculate_word_distance_url;
    private String respBody = "";

    public List<String> getSensitiveId() {
        return sensitiveId;
    }

    private void sensitiveId() {
        sensitiveId = new ArrayList<>();
        for (String sensitiveWord : containsSensitiveWordsList) {
            String id = SensitiveWord.sensitiveWordCorrespondId.get(sensitiveWord);
            sensitiveId.add(id);
        }
    }

    public UserDataInformation(String userDataInformation, String calculate_word_distance_url) throws IOException, ParseException {
        this.calculate_word_distance_url = calculate_word_distance_url;
        String[] splits = userDataInformation.trim().split(Constants.DECOLLATOR_FOR_HDFS_MESSAGE);
        isRightTime = true;
        normalCategory = Constants.NOISE;
        isNormalMessage = true;
        // added by dx
        //initUserInformationWithResp(splits);
        initUserInformation(splits);
        if (isRightTime() && isNormalMessage) {
            initKeyWords();
            containsSensitiveWordsList = titleKeyWords.getContainsSensitiveWordsList();
            sensitiveCategory = titleKeyWords.getSensitiveCategory();
//            System.out.println(titleKeyWords.getClaenHtml());
        }
    }

    public UserDataInformation(String userDataInformation) throws IOException, ParseException {
        String[] splits = userDataInformation.trim().split(Constants.DECOLLATOR_FOR_HDFS_MESSAGE);
        System.out.println(splits.length);
        isRightTime = true;
        normalCategory = Constants.NOISE;
        isNormalMessage = true;
        initUserInformation(splits);
        if (isRightTime()) {
            initKeyWords();
            containsSensitiveWordsList = titleKeyWords.getContainsSensitiveWordsList();
            sensitiveCategory = titleKeyWords.getSensitiveCategory();
        }
    }

    private void isNormalMessage(String url){
        if(url.indexOf("http://s.cpro.baidu.com/") == 0 ||url.indexOf("http://pos.baidu.com/") == 0 ||url.indexOf(" http://entry.baidu.com/") == 0 ||url.indexOf("http://googleads.g.doubleclick.net/") == 0
                ||url.indexOf(" http://eclick.baidu.com/") == 0 ||url.indexOf("http://cpro.baidustatic.com/") == 0 ||url.indexOf("http://cpro.baidu.com/") == 0){
            isNormalMessage = false;
        }

    }

    // added by dx
    private static String replaceIpWithUser(String ip, long timestamp) throws Exception
    {
        String value = RedisClient.get(ip + "_resp");
        JSONObject jsonObj = new JSONObject(value);
        Iterator iterator = jsonObj.sortedKeys();
        long last_op_time = 0;
        while (iterator.hasNext()) {
            long op_time = Long.valueOf((String) iterator.next());
            if (op_time > timestamp) {
                return jsonObj.getString(String.valueOf(last_op_time));
            } else {
                last_op_time = op_time;
            }
        }
        return "0";
    }

    private void initUserInformation(String[] splits) throws ParseException {
        isRightTime = true;
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
        Date dt = df.parse(splits[0].substring(0, 8));
        reqTime = df2.format(dt);
        if (!splits[0].substring(0, 8).equals(Constants.TIME)) {
            return;
        }
//        reqHour = splits[0].substring(8, 10);
//        reqIP = splits[1];
//        reqPort = splits[2];
//        respIP = splits[3];
//        respPort = splits[4];
//        reqMethod = splits[6];
//        reqURL = splits[5];
//        isNormalMessage(reqURL);
//        int length = splits.length;
//        if (length > 10)
//            uid = splits[10];
//       if (length > 11)
//            uname = splits[11];
//        if (length > 12)
//            mac = splits[12];
//        if (length <= 10) {
//            uid = "0";
//            uname = "0";
//            mac = "0";
//        }
        uid = splits[5];
        uname = splits[6];
        respBody = splits[8];
        if (uid.equals("")) {
            uid = "0";
            uname = "0";
        }
        // 时间    目的IP    目的端口    源IP    源端口    userid    username    usermac    响应body
        // url\thtml
    }

    private void initKeyWords() throws IOException {
//        titleKeyWords = new TitleKeyWords(new URL(reqURL), Constants.KEY_NUM);
//        System.out.println(respBody);
        // added by dx
        titleKeyWords = new TitleKeyWords(respBody, Constants.KEY_NUM);
        titleKeyWords.init();
        keyWords = titleKeyWords.getTopNumKey();
    }

    public void sensitiveClassify() {
        if (normalCategory == Constants.NOISE)
            if (containsSensitiveWordsList.size() >= Constants.SENSITIVE_WORD_NUM) {
                sensitiveId();
                this.type = Type.sensitive;
            }
    }

    public void calculateAndGenerateNormalCategory(Map<String, Integer> keyWordsType, Map<String, Double> wordsScore) {
        int max = 1;
        for (Map.Entry<String, Integer> entry : keyWordsType.entrySet()) {
            if (entry.getValue() > max) {
                normalCategory = entry.getKey();
                max = entry.getValue();
            }
        }
        if (max == 1) {
            double score = 0;
            for (Map.Entry<String, Double> entry : wordsScore.entrySet()) {
                if (entry.getValue() > score) {
                    normalCategory = entry.getKey();
                    score = entry.getValue();
                }
            }
        }
        keyWordsType.clear();
    }

    private void calculateAndGenerateKeyWordCategory(int i, JSONObject categoryJsonObject, Map<String, Integer> keyWordsType, Map<String, Double> wordsScore) throws IOException, InterruptedException {
        for (String keyWord : keyWords) {
            double maxScore = 0;
            Iterator iterate = categoryJsonObject.keys();
            while (iterate.hasNext()) {
                String tempCategory = (String) iterate.next();
//                if (!tempCategory.contains(Constants.LEARNING)) {
//                    double score = similarScore(keyWord, tempCategory.split(Constants.CATEGORY_ID_SPLIT)[0]);
//                    maxScore = calculateMaxScore(i, score, tempCategory, maxScore);
//                } else {
//                    learnCategoryName = tempCategory;
//                }
//                System.out.println(tempCategory.split(Constants.CATEGORY_ID_SPLIT)[0]);
                double score = similarScore(keyWord, tempCategory.split(Constants.CATEGORY_ID_SPLIT)[0]);
                maxScore = calculateMaxScore(i, score, tempCategory, maxScore);
            }
            if (!normalCategory.equals(Constants.NOISE)) {
                if (keyWordsType.containsKey(normalCategory))
                    keyWordsType.put(normalCategory, keyWordsType.get(normalCategory) + 1);
                else
                    keyWordsType.put(normalCategory, 1);
                wordsScore.put(normalCategory, maxScore);
                normalCategory = Constants.NOISE;
            }
        }
    }

    private Boolean ruleClassifyLayerOne() {
//        for (Map.Entry<String, Set> entry : Rule.RULE_FOR_LAYER_ONE.entrySet()) {
//            Set<String> set = entry.getValue();
//            for (String url : set) {
//                if (reqURL.contains(url)) {
//                    normalCategory = entry.getKey();
//                    String id = Category.superCategoryId.get(normalCategory);
//                    normalCategory = normalCategory + Constants.CATEGORY_ID_SPLIT + id;
//                    return true;
//                }
//            }
//        }
        return false;
    }

    private Boolean ruleClassifyLayerTwo() {
//        for (Map.Entry<String, Set> entry : Rule.RULE_FOR_LAYER_TWO.entrySet()) {
//            Set<String> set = entry.getValue();
//            for (String url : set) {
//                if (reqURL.contains(url)) {
//                    normalCategory = entry.getKey();
//                    return true;
//                }
//            }
//        }
        return false;
    }

    public void commonClassify() throws IOException, JSONException, InterruptedException {
        if (this.type != Type.sensitive) {
            Map<String, Integer> keyWordsType = new HashMap<>();
            JSONObject categoryJsonObject = Category.category;
            int layer = 0;
            if (ruleClassifyLayerTwo())
                return;
            if (ruleClassifyLayerOne()) {
                categoryJsonObject = categoryJsonObject.getJSONObject(normalCategory);
                layer = 1;
            }
            for (int i = layer; i < Constants.LAYER; ) {
                Map<String, Double> wordsScore = new HashMap<>();
                calculateAndGenerateKeyWordCategory(i, categoryJsonObject, keyWordsType, wordsScore);
                calculateAndGenerateNormalCategory(keyWordsType, wordsScore);
                if (++i < Constants.LAYER && !normalCategory.equals(Constants.NOISE)) {
                    categoryJsonObject = categoryJsonObject.getJSONObject(normalCategory);
                }
//                } else if (i < Constants.LAYER && normalCategory.equals(Constants.NOISE)) {
//                    categoryJsonObject = categoryJsonObject.getJSONObject(learnCategoryName);
//                }
            }
        }
    }

    private double calculateMaxScore(int i, double score, String tempCategory, double maxScore) {
        if (i == 0) {
            if (score >= maxScore && score > Constants.SIMILAR_SCORE_LINE) {
                normalCategory = tempCategory;
                return score;
            }
            return maxScore;
        }
        if (i == 1) {
            if (score >= maxScore && score > Constants.SIMILAR_SCORE_LINE_TWO) {
                normalCategory = tempCategory;
                return score;
            }
            return maxScore;
        }
        return maxScore;
    }

    private double similarScore(String word, String type) throws IOException, InterruptedException {
        double score;
        //calculate_word_distance_url = "http://192.168.1.106:8080/zz_nlp/wordsDistance?word1=%s&word2=%s";
        URL url = new URL(String.format(calculate_word_distance_url, word, type));
        URLConnection urlcon = url.openConnection();
        InputStream is = urlcon.getInputStream();
        BufferedReader buffer = new BufferedReader(new InputStreamReader(is));
        String s = buffer.readLine();
        score = Double.valueOf(s);
        buffer.close();
        return score;
    }

    public boolean isRightTime() {
        return isRightTime;
    }

    public int returnType = 0;

    public String toString() {
//        System.out.println(titleKeyWords.getClaenHtml());
//        System.out.println("----------------------------------------");
//        System.out.println(uid + Constants.SEPARATOR + uname + Constants.SEPARATOR + uid + Constants.SEPARATOR + Category.categorySuperId.get(normalCategory) + Constants.SEPARATOR + Category.categoryCorrespondId.get(normalCategory) + Constants.SEPARATOR + reqTime + Constants.SEPARATOR + reqHour);
        if (normalCategory.equals(Constants.NOISE) && sensitiveId.size() >= Constants.SENSITIVE_WORD_NUM) {
            returnType = Constants.RETURN_TYPE_SENSITIVE;
            return uid + Constants.SEPARATOR + reqTime + Constants.SEPARATOR + reqURL + Constants.SEPARATOR + titleKeyWords.getTitle();
        } else if (!normalCategory.equals(Constants.NOISE) && (Category.categorySuperId.get(normalCategory) != null)) {
            returnType = Constants.RETURN_TYPE_NORMAL;
            return 0 + Constants.SEPARATOR + 0 + Constants.SEPARATOR + uid + Constants.SEPARATOR + Category.categorySuperId.get(normalCategory) + Constants.SEPARATOR + Category.categoryCorrespondId.get(normalCategory) + Constants.SEPARATOR + reqTime + Constants.SEPARATOR + reqHour;
        } else {
            returnType = Constants.RETURN_TYPE_NOISE;
            return reqURL;
        }
    }

    public int getReturnType() {
        return returnType;
    }

    public String getURLCategory() {
        return reqURL + Constants.SEPARATOR + normalCategory;
    }

    public String getURL() {
        return this.respPort + this.reqURL;
    }

    public static void main(String[] args) throws IOException, JSONException, InterruptedException, ParseException {
        String category_get_url = "http://192.168.1.106:8080/public_behavior/api/behavior.do";
        String sensitiveWord_get_url = "http://192.168.1.106:8080/public_behavior/api/sensitive.do";
        if (Category.categorySuperId == null) {
            Category category = new Category(category_get_url);
            category.init();
        }
        if (SensitiveWord.sensitiveWordCorrespondId == null) {
            SensitiveWord sensitiveWord = new SensitiveWord(sensitiveWord_get_url);
            sensitiveWord.init();
        }
        // 时间    目的IP    目的端口    源IP    源端口    userid    username    usermac    响应body
        String s = "20160324215752\t172.16.35.143\t49491\t42.56.65.167\t80\t\t\t\t<!DOCTYPE HTML><html lang=\"zh-CN\"><head><meta charset=\"gbk\"><meta name=\"author\" content=\"Tencent-TGideas\"><meta name=\"format-detection\" content=\"telephone=no\" /><!--<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=no\"/>--><meta name=\"viewport\" content=\"width=320,minimum-scale=1,maximum-scale=5,user-scalable=no\"><meta name=\"apple-mobile-web-app-capable\" content=\"yes\" /><meta name=\"apple-mobile-web-app-status-bar-style\" content=\"black\" /><title>赛事详情 - 赛事中心</title><!-- 设计：CP | 重构：smallniding | 团队博客：http://tgideas.qq.com --><script>function getUrlParam(name){var reg = new RegExp(\"(^|&)\"+ name +\"=([^&]*)(&|$)\"); var r = window.location.search.substr(1).match(reg);if (r!=null) return unescape(r[2]); return null;}var color = getUrlParam('color');if(color == 'dark'){document.write(\"<link rel='stylesheet' type='text/css' href='http://lol.qq.com/m/act/a20141225match/css/common.css'/>\");document.write(\"<link rel='stylesheet' type='text/css' href='http://lol.qq.com/m/act/a20141225match/css/common_dark.css'/>\");}else if (color == 'none') {document.write(\"<link rel='stylesheet' type='text/css' href='http://lol.qq.com/m/act/a20141225match/css/common.css'/>\");document.write(\"<link rel='stylesheet' type='text/css' href='http://lol.qq.com/m/act/a20141225match/css/common_none.css'/>\")}else{document.write(\"<link rel='stylesheet' type='text/css' href='http://lol.qq.com/m/act/a20141225match/css/common.css'/>\");}</script></head><body style=\"padding:10px;\"><div class=\"live-txt-item\"><div class=\"live-txt-author\">26:50<br>第一场</div><div class=\"live-txt-info\"><p>小龙刷新，IG先手开龙，但随后VG选择进攻，IG只能撤退，VG直接击杀小龙。随后IG展开追击，泰坦残血，千珏给大随后交闪现逃走，IG追击到，只好拆掉VG的中路一塔。</p></div></div><div class=\"live-txt-item\"><div class=\"live-txt-author\">22:10<br>第一场</div><div class=\"live-txt-info\"><p>目前双方人头比为4:0，VG经济领先4K，防御塔三座，双方各击杀一条小龙。</p></div></div><div class=\"live-txt-item\"><div class=\"live-txt-author\">21:20<br>第一场</div><div class=\"live-txt-info\"><p>VG拆掉IG的中路一塔。</p></div></div><div class=\"live-txt-item\"><div class=\"live-txt-author\">18:46<br>第一场</div><div class=\"live-txt-info\"><p class=\"cimg\" align=\"center\"><img src=\"http://files.15w.com/image/2016/0324/1458818250489.png\" alt=\"\" /></p><p>中路小龙附近做视野时，巨魔被抓到，VG四人直接围剿，随后VG拿下小龙。</p></div></div><div class=\"live-txt-item\"><div class=\"live-txt-author\">17:03<br>第一场</div><div class=\"live-txt-info\"><p class=\"cimg\" align=\"center\"><img src=\"http://files.15w.com/image/2016/0324/14588181308380.png\" alt=\"\" /></p><p>千珏到上路gank，波比大招锤飞泰坦，但千珏有大招完全不虚的直接越塔，击杀了波比。</p></div></div><div class=\"live-txt-item\"><div class=\"live-txt-author\">15:00<br>第一场</div><div class=\"live-txt-info\"><p>目前双方人头比为2:0，VG领先经济2K防御塔一座，IG领先小龙一条。</p></div></div><div class=\"live-txt-item\"><div class=\"live-txt-author\">13:00<br>第一场</div><div class=\"live-txt-info\"><p class=\"cimg\" align=\"center\"><img src=\"http://files.15w.com/image/2016/0324/14588179737356.png\" alt=\"\" /></p><p>IG的波比选择一个超级传送绕后，配合豹女想击杀下路，但布隆的360完美保护，奥巴马不但没被击杀，反手杀掉了传送的波比。</p></div></div><div class=\"live-txt-item\"><div class=\"live-txt-author\">12:07<br>第一场</div><div class=\"live-txt-info\"><p class=\"cimg\" align=\"center\"><img src=\"http://files.15w.com/image/2016/0324/1458817868104.png\" alt=\"\" /></p><p>IG率击杀第一条小龙。</p></div></div><div class=\"live-txt-item\"><div class=\"live-txt-author\">10:19<br>第一场</div><div class=\"live-txt-info\"><p class=\"cimg\" align=\"center\"><img src=\"http://files.15w.com/image/2016/0324/14588177268755.png\" alt=\"\" /></p><p>下路率先开战，巨魔被打成残血闪现跑走塔下，EZ在塔下被传送下来的泰坦套大，奥巴马追击拿下一血。</p></div></div><div class=\"live-txt-item\"><div class=\"live-txt-author\">09:30<br>第一场</div><div class=\"live-txt-info\"><p>豹女再次入侵野区，千珏无奈将蓝拿下，维克托没有拿到蓝buff。</p></div></div><div class=\"live-txt-item\"><div class=\"live-txt-author\">07:20<br>第一场</div><div class=\"live-txt-info\"><p>豹女野区被千珏看到，一波压血之后豹女回城，千珏反掉豹女的红buff。</p></div></div><div class=\"live-txt-item\"><div class=\"live-txt-author\">05:45<br>第一场</div><div class=\"live-txt-info\"><p>目前开局5分钟，双方人头比为0:0，经济平，双方零防御塔零小龙。</p></div></div><div class=\"live-txt-item\"><div class=\"live-txt-author\">03:30<br>第一场</div><div class=\"live-txt-info\"><p>千珏走到中路gank一波，逼出飞机的W但没有造成任何伤害。</p></div></div><div class=\"live-txt-item\"><div class=\"live-txt-author\">02:00<br>第一场</div><div class=\"live-txt-info\"><p>在本场比赛中，双方都没有选择换线，进入正常对线期。</p></div></div><div class=\"live-txt-item\"><div class=\"live-txt-author\">00:00<br>第一场</div><div class=\"live-txt-info\"><p class=\"cimg\" align=\"center\"><img src=\"http://files.15w.com/image/2016/0324/1458817013493.png\" alt=\"\" /></p><p>最终，VG的阵容为：上单泰坦，中单维克托，打野千珏，辅助布隆，AD奥巴马。</p><p>IG的阵容为：上单波比，中单飞机，打野豹女，辅助巨魔，AD EZ。</p></div></div><div class=\"live-txt-item\"><div class=\"live-txt-author\">00:00<br>第一场</div><div class=\"live-txt-info\"><p class=\"cimg\" align=\"center\"><img src=\"http://files.15w.com/image/2016/0324/1458816814799.png\" alt=\"\" /></p><p>在本场比赛中，VG选择BAN掉了：妖姬、船长复仇之矛。</p><p>而IG则BAN掉了：冰女、瑞兹和璐璐。</p></div></div><div class=\"live-txt-item\"><div class=\"live-txt-author\">00:00<br>第一场</div><div class=\"live-txt-info\"><p class=\"cimg\" align=\"center\"><img src=\"http://files.15w.com/image/2016/0324/14588152858582.jpg\" alt=\"\" /></p><p>观众老爷们大家好，欢迎来到掌盟直播间，本场比赛将由VG对阵IG，比赛即将开始，敬请期待！</p></div></div><div class=\"live-txt-item\"><div class=\"live-txt-author\">00:00<br>第一场</div><div class=\"live-txt-info\"><p>标清（450kbs）观看一小时 大约消耗流量：197.75M</p><p>高清（750kbs）观看一小时 大约消耗流：329.58M</p></div></div><div class=\"live-txt-item\"><div class=\"live-txt-author\"><br>第一场</div><div class=\"live-txt-info\"><p>文字直播尚未开始，敬请期待！</p></div></div><div style=\"display:none;\"><script type=\"text/javascript\">var cnzz_protocol = ((\"https:\" == document.location.protocol) ? \" https://\" : \" http://\");document.write(unescape(\"%3Cspan id='cnzz_stat_icon_1000034028'%3E%3C/span%3E%3Cscript src='\" + cnzz_protocol + \"s22.cnzz.com/z_stat.php%3Fid%3D1000034028' type='text/javascript'%3E%3C/script%3E\"));</script><script type=\"text/javascript\">var cnzz_protocol = ((\"https:\" == document.location.protocol) ? \" https://\" : \" http://\");document.write(unescape(\"%3Cspan id='cnzz_stat_icon_30070062'%3E%3C/span%3E%3Cscript src='\" + cnzz_protocol + \"w.cnzz.com/c.php%3Fid%3D30070062' type='text/javascript'%3E%3C/script%3E\"));</script></div></body></html>";
//        String s = "20160319215752\t172.16.35.143\t49491\t42.56.65.167\t80\thttp://sports.sina.com.cn/basketball/nba/2016-03-21/doc-ifxqnski7787493.shtml\tGET\tMozilla/5.0 (Linux; U; Android 4.4.4; zh-cn; ZTE G719C Build/KTU84P) AppleWebKit/533.1 (KHTML, like Gecko) Mobile Safari/533.1\t\t\t2015200406\t\tA8A668A93DD6";
        UserDataInformation userDataInformation = new UserDataInformation(s);
        userDataInformation.commonClassify();
        userDataInformation.sensitiveClassify();
        System.out.println(userDataInformation.getURLCategory());
        System.out.println(userDataInformation.getSensitiveId());
        System.out.println(userDataInformation.htmlContent);
//        File file2 = new File("/home/tian/test");
//        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file2, true));
//        while((s = bufferedReader.readLine())!=null){
//            UserDataInformation userDataInformation = null;
//            try {
//                userDataInformation = new UserDataInformation(s);
//                bufferedWriter.write(userDataInformation.getURL());
//                bufferedWriter.write("\n");
//                userDataInformation.commonClassify();
//                userDataInformation.sensitiveClassify();
//                bufferedWriter.write(userDataInformation.getURLCategory());
//                bufferedWriter.write("\n");
//            } catch (ParseException e) {
//                e.printStackTrace();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
////            System.out.print(userDataInformation.toString());
//        }
//        bufferedReader.close();
//        bufferedWriter.close();
    }
}
