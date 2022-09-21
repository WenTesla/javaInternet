package project1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class exam1 {
    /**
     * 初始化机场与三字码
     */
    private HashMap<String, String> airport;

    /**
     * 初始化hashMap存储数据,第一个String为航班号
     */
    private static HashMap<String, String[]> information = new HashMap<>();


    /**
     *
     */
    static String flid = null;  // 即数据文件中每行数据的flid
    static String flightNumber = null; // 航班号，如CA1402
    static String sfawOne = null; // 共享航班公司1
    static String sfnoOne = null; // 共享航班号1
    static String sfawTwo = null; // 共享航班公司2
    static String sfnoTwo = null; // 共享航班号2
    static String sfawThree = null; // 共享航班公司3
    static String sfnoThree = null; // 共享航班号3
    static String aimStation = null; // 目的站
    static String transferStation = null; // 中转站
    static String destinationStation = null; //终点站
    static String planToFlyTime = null; // 预计起飞时间
    static String changeToFlyTime = null; // 变更起飞时间
    static String actualFlyTime = null;//实际起飞时间
    static String gate = null; // 登机口
    static String state = null; // 登机状态

    static Matcher matcher = null;
    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddhhmmss");

    public static void main(String[] args) {
        String filePath = "fdsdata2.txt";
        //创建文件对象
        File file = new File(filePath);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                analysis(tempString);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
//        String[] values = information.get(flid);
//        for (int n = 1; n <= 6; n += 2) {
//            if (values[n] == null) {
//                values[n + 1] = "  ";
//            }
//        }
//        System.out.println("主航班号： " + values[0] + "\t" + "共享航班号1：" + values[1] + values[2] +
//                "\t" + "共享航班号2：" + values[3] + values[4] + "\t" + "共享航班号3：" + values[5] + values[6] +
//                "\t" + "中转站: " + values[7] + "\t" + "目的站: " + values[8] + "\t" + "预计起飞时间： " +
//                values[9] + "\t" + "变更起飞时间： " + values[10] + "\t" + "登机口： " + values[11] + "\t" +
//                "航班状态： " + values[12] );
        //输出


    }

    //对每一行的数据进行处理
    private static void analysis(String tempString) throws IOException {
        /**
         * 每次调用此方法将所有参数置为空以初始化
         */
        String line;
        flid = null;  // 即数据文件中每行数据的flid
        flightNumber = null; // 航班号，如CA1402
        sfawOne = null; // 共享航班公司1
        sfnoOne = null; // 共享航班号1
        sfawTwo = null; // 共享航班公司2
        sfnoTwo = null; // 共享航班号2
        sfawThree = null; // 共享航班公司3
        sfnoThree = null; // 共享航班号3
        aimStation = null; // 目的站
        transferStation = null; // 中转站
        destinationStation = null;
        planToFlyTime = null; // 预计起飞时间
        changeToFlyTime = null; // 变更起飞时间
        actualFlyTime = null;//实际起飞时间
        gate = null; // 登机口
        state = null; // 登机状态
        /**
         * 赋值
         */
        line = tempString;
        // 用于将机场代码转为机场名称
        Properties airports = new Properties();
        airports.load(new FileReader("机场代码2.txt"));
        /**
         * 如果符合需要的条件
         */
        if (line.contains("DFME_AIRL") || line.contains("DFME_BORE") || line.contains("DFME_CANE")
                || line.contains("DFME_DLYE") || line.contains("DFME_FPTT") || line.contains("DFME_FETT")
                || line.contains("DFME_GTLS") || line.contains("DFME_POKE") || line.contains("SFLG")) {
            /**
             * 求时间
             */

            /**
             * 求航班唯一标识和航班号
             */
            //匹配[xxxxxx]
            Pattern pattern1 = Pattern.compile("\\[.*\\]");
            Matcher matcher1 = pattern1.matcher(line);
            if (matcher1.find()) {

//                System.out.println(matcher1.group());
                String[] flight = matcher1.group().split("=");
                flid = flight[1].substring(0, flight[1].lastIndexOf(','));
//                System.out.println("flid:"+flid);
                String ffid = flight[2];
                flightNumber = Stream.of(ffid.split("-")).limit(2).collect(Collectors.joining());
//                System.out.println("flightNumber:"+flightNumber);


            }
            /**
             * 求共享航班号
             */
//            if (line.contains("SFLG")) {
//                Pattern pattern2 = Pattern.compile("SFLT \\[.*\\]");
//                Matcher matcher2 = pattern2.matcher(line);
//                if (matcher2.find()) {
//                    String shareNo = matcher2.group();
//                    shareNo = shareNo.substring(6, shareNo.length() - 4);
//                    //sfaw=ZH, sfno=1402], SFLT [sfaw=KY, sfno=1402], SFLT [sfaw=SC, sfno=1402
////                    System.out.println(shareNo);
//                    String[] sharNos = shareNo.split("\\], SFLT \\[");
////                    System.out.println(sharNos);
//                    if (sharNos.length==1){
//
//                    }
//                    else if (sharNos.length == 2){
//
//                    }
//                    else if (sharNos.length == 3){
//
//                    }
//
////                System.out.println("共享航班号1: " + sfawOne  + "\t"+
////                        "共享航班号2: " + sfawTwo + sfnoTwo + "\t"+  "共享航班号3: " + sfawThree + sfnoThree + "\t"  );
//            }
            /**
             * 求起始地与中转战
             */
            if (line.contains("apcd")) {
                matcher = Pattern.compile("ARPT\\[.*\\]").matcher(line);
                if (matcher.find()) {
                    String stationTemp = matcher.group();
//                    System.out.println(stationTemp);
                    //ARPT[arno=1, apcd=TSN, fptt=20180924123000, fett=null, frtt=null, fplt=null, felt=null, frlt=null, apat=2403], ARPT[arno=2, apcd=HSN, fptt=20180924152000, fett=null, frtt=null, fplt=20180924143500, felt=null, frlt=null, apat=2403], ARPT[arno=3, apcd=FOC, fptt=null, fett=null, frtt=null, fplt=20180924164000, felt=null, frlt=null, apat=2403], ]

                    stationTemp = stationTemp.substring(5, stationTemp.length() - 4);
                    String[] stationTempStrings = stationTemp.split("\\], ARPT\\[");
                    //arno=1, apcd=TSN, fptt=20180923080000, fett=null, frtt=null, fplt=null, felt=null, frlt=null, apat=2403
                    //arno=2, apcd=PVG, fptt=null, fett=null, frtt=null, fplt=20180923093500, felt=null, frlt=null, apat=2403
                    //含有目的站和终点站
                    if (stationTempStrings.length == 2) {
                        String destination = stationTempStrings[1].substring(13, 16);
                        destinationStation = airports.getProperty(destination);
//                        System.out.println(destinationStation);
                    }
                    //含有目的站中转站和终点站
                    if (stationTempStrings.length == 3) {
                        String transfer = stationTempStrings[1].substring(13, 16);
                        transferStation = airports.getProperty(transfer);
                        String destination = stationTempStrings[2].substring(13, 16);
                        destinationStation = airports.getProperty(destination);
                    }
                }
            }
            /**
             * 求登机门
             */
            if (line.contains("GATE")) {
                Pattern pattern3 = Pattern.compile("GATE\\[.*?\\]");
                Matcher matcher3 = pattern3.matcher(line);
                if (matcher3.find()) {
                    String gatee = matcher3.group();
                    gatee = gatee.substring(5, gatee.length() - 1);
                    String[] strings = gatee.split(", ");
                    gate = strings[2].substring(5);
                }
            }
            /**
             * 求起飞落地时间
             * <FPTT>	计划起飞时间
             * <FETT>	预计起飞时间
             * <FRTT>   实际起飞时间
             */
            // 求预计起飞时间和变更时间

            if (line.contains("fptt") || line.contains("fett")) {//一个航班同时fett和fptt，就用fett
                Pattern pattern4 = Pattern.compile("ARPT\\[.*?\\]");
                Matcher matcher4 = pattern4.matcher(line);
//                matcher = Pattern.compile("ARPT\\[.*?\\]").matcher(line);
                if (matcher4.find()) {
                    String timeFly = matcher4.group();
//                    System.out.println(timeFly);
//                    ARPT[arno=1, apcd=TSN, fptt=20180923001000, fett=null, frtt=null, fplt=null, felt=null, frlt=null, apat=2403]
                    String[] strings = timeFly.split(", ");
                    //选择planToFlyTime
                    String planFly = strings[2].substring(5);
                    //20180923080000
                    if (!"null".equals(planFly)) {
                        StringBuilder builder1 = new StringBuilder(planFly.substring(8, planFly.length() - 2));
                        builder1.insert(2, ":");
                        planToFlyTime = builder1.toString();
//                    System.out.println("预计"+planToFlyTime);
//                   strings[4]
                    }
                    //选择
                    String realFly = strings[4].substring(5);
                    if (!"null".equals(realFly)) {
                        StringBuilder builder2 = new StringBuilder(realFly.substring(8, planFly.length() - 2));
                        builder2.insert(2, ":");
                        actualFlyTime = builder2.toString();
//                        System.out.println("实际"+actualFlyTime);
                    }

                }
            }
            if (!line.contains("fptt") && line.contains("fett")) {//一个航班只有fett就用fett
                Pattern pattern5 = Pattern.compile("\\[.*\\]");
                Matcher matcher5 = pattern5.matcher(line);
                if (matcher5.find()) {
//                    System.out.println(matcher5.group());
//                    [flid=1418075, ffid=GS-7892-20180925-A, fett=null, felt=20180925012500]
                    Pattern pattern6 = Pattern.compile("(?<=fett=).*?(?=,)");
                    Matcher matcher6 = pattern6.matcher(line);
                    if (matcher6.find()) {
                        String predictToFly = matcher6.group();
                        if (!"null".equals(predictToFly)) {
                            StringBuilder builder3 = new StringBuilder(predictToFly.substring(8, predictToFly.length() - 2));
                            builder3.insert(2, ":");
                            planToFlyTime = builder3.toString();

                        }
//                        System.out.println(planToFlyTime);
                    }
                }
            }

            /**
             * 求航班的状态
             */
            if (line.contains("BORE"))
                state = "开始登机";
            if (line.contains("POKE"))
                state = "结束";
            if (line.contains("DLYE"))
                state = "航班延误";
            if (line.contains("CANE"))
                state = "航班取消";


            // 归并分析结果
            if (information.get(flid) == null) {
                information.put(flid, new String[]{flightNumber, sfawOne, sfnoOne, sfawTwo, sfnoTwo, sfawThree, sfnoThree,
                        transferStation, destinationStation, planToFlyTime, actualFlyTime, gate, state});
            } else {
                String[] values = information.get(flid);
                String[] items = {flightNumber, sfawOne, sfnoOne, sfawTwo, sfnoTwo, sfawThree, sfnoThree,
                        transferStation, destinationStation, planToFlyTime, actualFlyTime, gate, state};
                for (int i = 0; i < 13; i++) {
                    values[i] = items[i] != null ? items[i] : values[i];
                }
                information.put(flid, values);
            }
            // 输出分析结果
            String[] values = information.get(flid);
//            for (int n = 1; n <= 6; n += 2) {
//                if (values[n] == null) {
//                    values[n + 1] = "  ";
//                }
//            }
            System.out.println("航班号： " + values[0] + "\t" + "经停站: "+values[7]+"\t"+"目的站: " + values[8] + "\t" + "预计起飞时间： " +
                    values[9] + "\t" + "实际起飞时间： " + values[10] + "\t" + "登机口： " + values[11] + "\t" +
                    "航班状态： " + values[12]);
        }
    }

}
