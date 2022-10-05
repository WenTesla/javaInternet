//package project1;
//
//import java.io.File;
//import java.io.FileReader;
//import java.io.StringReader;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.HashMap;
//import java.util.Properties;
//import java.util.Scanner;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
//
//
//public class exma1 {
//    public static void main(String[] args) throws Exception {
//        //使用指定的模式创建格式化程序。2022:9:10:11:11:50
//        DateTimeFormatter fulldtm = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
//        //使用指定的模式创建格式化程序.比如,18:18
//        DateTimeFormatter hourAndMinuterdtm = DateTimeFormatter.ofPattern("HH:mm");
//        // 用于存放分析好的航班信息
//        HashMap<String, String[]> flights = new HashMap<>(); // flid->{航班号、目的站等数据}
//        //初始化matcher
//        Matcher matcher = null;
//        // 用于将机场代码转为机场名称
//        Properties airports = new Properties();
//        airports.load(new FileReader("机场代码.txt"));
//
//        try (Scanner scanner = new Scanner(new File("fdsdata.txt"))) {
//            String line = null;
//            while (scanner.hasNextLine()) {
//                line = scanner.nextLine();
//                if (line.contains("DFME_AIRL") || line.contains("DFME_BORE") || line.contains("DFME_CANE")
//                        || line.contains("DFME_DLYE") || line.contains("DFME_FPTT") || line.contains("DFME_FETT")
//                        || line.contains("DFME_GTLS") || line.contains("DFME_POKE") || line.contains("SFLG")) {
//                    String flid = null; // 即数据文件中每行数据的flid
//                    String flightNo = null; // 航班号，如CA1402
//                    String sfawOne = null; // 共享航班公司
//                    String sfnoOne = null; // 共享航班号
//                    String sfawTwo = null;
//                    String sfnoTwo = null;
//                    String sfawThree = null;
//                    String sfnoThree = null;
//                    String aimStation = null; // 目的站
//                    String transferStation = null; // 中转站
//                    String planTime = null; // 预计起飞时间
//                    String changeTime = null; // 变更起飞时间
//                    String gate = null; // 登机口
//                    String state =null; // 登机状态
//
//
//                    // 求当前时间
//                    LocalDateTime now = LocalDateTime.parse(line.substring(5, 19), fulldtm);
//                    // System.out.print("当前时间：" + now.format(hourAndMinuterdtm) + "\t");
//                    //匹配[xxxxxx]
//                    matcher = Pattern.compile("\\[.*\\]").matcher(line);
//                    if (matcher.find()) {
//                        String flightInfo = matcher.group();
//                        flightInfo = flightInfo.substring(1, flightInfo.length() - 1).replaceAll(",\s", "\r\n");
//                        // System.out.println(flightInfo + "\n");
//                        Properties flightInfoProperties = new Properties();
//                        flightInfoProperties.load(new StringReader(flightInfo));
//                        // 求flid
//                        flid = flightInfoProperties.getProperty("flid");
//                        // System.out.println(flid);
//
//                        // 求主航班号
//                        String ffid = (String) flightInfoProperties.getProperty("ffid");
//                        flightNo = Stream.of(ffid.split("-")).limit(2).collect(Collectors.joining());
//                        // System.out.print("主航班号: " + flightNo + "\t");
//
//                        // 求共享航班号
//                        if (line.contains("SFLG")) {
//                            matcher = Pattern.compile("SFLT \\[.*\\]").matcher(line);
//                            if (matcher.find()) {
//                                String shareNo = matcher.group();
//                                shareNo = shareNo.substring(6, shareNo.length() - 4).replaceAll("\\], SFLT \\[", "\r\n").replaceAll(",\s", "\r\n");
//                                String[] sflgStrings = shareNo.split("\\], SFLT \\[");
//                                Properties sflgProperties = new Properties();
//                                if (sflgStrings.length == 1) {
//                                    sflgProperties.load(new StringReader(sflgStrings[0]));
//                                    sfawOne = sflgProperties.getProperty("sfaw");
//                                    sfnoOne = sflgProperties.getProperty("sfno");
//                                }
//                                if (sflgStrings.length == 2) {
//                                    sflgProperties.load(new StringReader(sflgStrings[0]));
//                                    sfawOne = sflgProperties.getProperty("sfaw");
//                                    sfnoOne = sflgProperties.getProperty("sfno");
//                                    sflgProperties.load(new StringReader(sflgStrings[1]));
//                                    sfawTwo = sflgProperties.getProperty("sfaw");
//                                    sfnoTwo = sflgProperties.getProperty("sfno");
//                                }
//                                if (sflgStrings.length == 3) {
//                                    sflgProperties.load(new StringReader(sflgStrings[0]));
//                                    sfawOne = sflgProperties.getProperty("sfaw");
//                                    sfnoOne = sflgProperties.getProperty("sfno");
//                                    sflgProperties.load(new StringReader(sflgStrings[1]));
//                                    sfawTwo = sflgProperties.getProperty("sfaw");
//                                    sfnoTwo = sflgProperties.getProperty("sfno");
//                                    sflgProperties.load(new StringReader(sflgStrings[2]));
//                                    sfawThree = sflgProperties.getProperty("sfaw");
//                                    sfnoThree = sflgProperties.getProperty("sfno");
//                                }
//
//                            }
//                        }
//                        //System.out.println("共享航班号1: " + sfawOne  + "\t"+
//                        //"共享航班号2: " + sfawTwo + sfnoTwo + "\t"+  "共享航班号3: " + sfawThree + sfnoThree + "\t"  );
//
//                        // 求中转站和目的站
//                        if (line.contains("apcd")) {
//                            matcher = Pattern.compile("ARPT\\[.*\\]").matcher(line);
//                            if (matcher.find()) {
//                                String stationTemp = matcher.group();
//                                stationTemp = stationTemp.substring(5, stationTemp.length() - 4);
//                                String[] stationTempStrings = stationTemp.split("\\], ARPT\\[");
//                                Properties stationProperties = new Properties();
//                                if (stationTempStrings.length == 2) {
//                                    stationTemp = stationTempStrings[1].replaceAll(",\s", "\r\n");
//                                    stationProperties.load(new StringReader(stationTemp));
//                                    String apcdTemp = stationProperties.getProperty("apcd");
//                                    aimStation = airports.getProperty(apcdTemp);
//                                    // System.out.print("目的站：" + destStation);
//                                }
//                                if (stationTempStrings.length == 3) {
//                                    stationTemp = stationTempStrings[1].replaceAll(",\s", "\r\n");
//                                    stationProperties.load(new StringReader(stationTemp));
//                                    String apcdTemp = stationProperties.getProperty("apcd");
//                                    transferStation = airports.getProperty(apcdTemp);
//                                    // System.out.print("中转站：" + transitStation);
//                                    stationTemp = stationTempStrings[2].replaceAll(",\s", "\r\n");
//                                    stationProperties.load(new StringReader(stationTemp));
//                                    apcdTemp = stationProperties.getProperty("apcd");
//                                    aimStation = airports.getProperty(apcdTemp);
//                                    // System.out.print("目的站：" + destStation);
//                                }
//                            }
//                        }
//
//
//                        // 求预计起飞时间和变更时间
//                        if (line.contains("fptt") && line.contains("fett")) {//一个航班同时fett和fptt，就用fett
//                            matcher = Pattern.compile("ARPT\\[.*?\\]").matcher(line);
//                            if (matcher.find()) {
//                                String timeFly = matcher.group();
//                                timeFly = timeFly.substring(5, timeFly.length() - 1).replaceAll(",\s", "\r\n");
//                                Properties planTimeProperties = new Properties();
//                                planTimeProperties.load(new StringReader(timeFly));
//                                if (!planTimeProperties.getProperty("fett").equals("null")) {
//                                    planTime = LocalDateTime.parse(planTimeProperties.getProperty("fett"), fulldtm)
//                                            .format(hourAndMinuterdtm);
//                                } else {
//                                    planTime = LocalDateTime.parse(planTimeProperties.getProperty("fptt"), fulldtm)
//                                            .format(hourAndMinuterdtm);
//                                }
//                                // System.out.print("预计起飞时间：" + planTime);
//                            }
//                        }
//                        if (line.contains("fptt") && !line.contains("fett")) {//一个航班只有fptt就用fptt
//                            matcher = Pattern.compile("\\[.*\\]").matcher(line);
//                            if (matcher.find()) {
//                                String timeFly = matcher.group();
//                                timeFly = timeFly.substring(1, timeFly.length() - 1).replaceAll(",\s", "\r\n");
//                                Properties planTimeProperties = new Properties();
//                                planTimeProperties.load(new StringReader(timeFly));
//                                if (!planTimeProperties.getProperty("fptt").equals("null")) {
//                                    changeTime = LocalDateTime.parse(planTimeProperties.getProperty("fptt"), fulldtm)
//                                            .format(hourAndMinuterdtm);
//                                }
//                                // System.out.print("变更预计起飞时间：" + changeTime);
//                            }
//                        }
//                        if (!line.contains("fptt") && line.contains("fett")) {//一个航班只有fett就用fett
//                            matcher = Pattern.compile("\\[.*\\]").matcher(line);
//                            if (matcher.find()) {
//                                String timeFly = matcher.group();
//                                timeFly = timeFly.substring(1, timeFly.length() - 1).replaceAll(",\s", "\r\n");
//                                Properties planTimeProperties = new Properties();
//                                planTimeProperties.load(new StringReader(timeFly));
//                                if (!planTimeProperties.getProperty("fett").equals("null")) {
//                                    changeTime = LocalDateTime.parse(planTimeProperties.getProperty("fett"), fulldtm)
//                                            .format(hourAndMinuterdtm);
//                                }
//                                // System.out.print("变更预计起飞时间：" + changeTime);
//                            }
//                        }
//
//                        // 求登机门
//                        if (line.contains("GATE")) {
//                            matcher = Pattern.compile("GATE\\[.*?\\]").matcher(line);
//                            if (matcher.find()) {
//                                String gatee = matcher.group();
//                                gatee = gatee.substring(5, gatee.length() - 1).replaceAll(",\s", "\r\n");
//                                Properties gateProperties = new Properties();
//                                gateProperties.load(new StringReader(gatee));
//                                gate = gateProperties.getProperty("code");
//                                // System.out.print("登机口：" + gate);
//                            }
//                        }
//                        // 求航班状态
//                        if (line.contains("DFME_BORE"))
//                            state = "开始登机";
//                        if (line.contains("DFME_CANE"))
//                            state = "航班取消";
//                        if (line.contains("DFME_DLYE"))
//                            state = "航班延误";
//                        if (line.contains("DFME_POKE"))
//                            state = "结束登机";
//                        // System.out.println("航班状态：" + state);
//
//                        // 归并分析结果
//                        if (flights.get(flid) == null) {
//                            flights.put(flid, new String[] { flightNo,sfawOne,sfnoOne,sfawTwo,sfnoTwo,sfawThree,sfnoThree,
//                                    transferStation,aimStation,planTime,changeTime,gate,state });
//                        } else {
//                            String[] values = flights.get(flid);
//                            String[] items = { flightNo,sfawOne,sfnoOne,sfawTwo,sfnoTwo,sfawThree,sfnoThree,
//                                    aimStation,transferStation,planTime,changeTime,gate,state };
//                            for (int i = 0; i < 13; i++) {
//                                values[i] = items[i] != null ? items[i] : values[i];
//                            }
//                            flights.put(flid, values);
//                        }
//                        // 输出分析结果
//                        String[] values = flights.get(flid);
//                        for (int n = 1; n <= 6; n += 2) {
//                            if (values[n] == null) {
//                                values[n + 1] = "  ";
//                            }
//                        }
//                        System.out.println("主航班号： " + values[0] + "\t" + "共享航班号1：" + values[1] + values[2] +
//                                "\t" + "共享航班号2：" + values[3] + values[4] + "\t" + "共享航班号3：" + values[5] + values[6] +
//                                "\t" + "中转站: " + values[7] + "\t" + "目的站: " + values[8] + "\t" + "预计起飞时间： " +
//                                values[9] + "\t" + "变更起飞时间： " + values[10] + "\t" + "登机口： " + values[11] + "\t" +
//                                "航班状态： " + values[12] );
//                    }
//                }
//            }
//        }
//    }
//
//}
