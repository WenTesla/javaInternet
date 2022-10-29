package project2;

import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 使用非线程安全的类
 */
public class examTest1 {
    //通过volatile的类型来监听数据是否接受完毕
    public volatile static boolean readFlag = true;


    public static void main(String[] args) {
        //定义接受/读取到的原始航班数据
        LinkedList<String> inputData = new LinkedList<>();
        //定义处理完成的集合变量
        Hashtable<String, String[]> outputData = new Hashtable<>();
        //定义读取的文件
        File file = new File("fdsdata.txt");
        //数据读取
        new readData(file, inputData).start();
        //分析航班数据
        new analysisData(inputData, outputData).start();
        //循环输出线程代码
        new outputData(outputData).start();

    }


}

/**
 * 数据读入进程
 */
class readData extends Thread {
    private File input;
    private LinkedList<String> inputData;

    public readData(File input, LinkedList<String> inputData) {
        this.input = input;
        this.inputData = inputData;
    }


    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(input)));) {
            String line;
            while ((line = in.readLine()) != null) {
                //加锁，申请同步访问权
                synchronized (inputData) {
                    //添加到链表，实现先进先出，最末尾添加
                    inputData.add(inputData.size(), line);
                    //唤醒单个线程
                    inputData.notify();
                }
                //休眠
                Thread.sleep(50);
            }
            examTest1.readFlag = false;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

/**
 * 数据分析线程
 */

class analysisData extends Thread {
    private List<String> inputData;
    private Hashtable<String, String[]> outputData;

    /**
     * 定义各种数据
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

    /**
     * 格式化类DateTimeFormatter,用于格式化时间
     * fullTimeFormatting:         年-月-日-时-分-秒
     * hourAndMinuterFormatting：  时-分
     */

    static DateTimeFormatter fullTimeFormatting = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    static DateTimeFormatter hourAndMinuterFormatting = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * 引入properties类，用于将机场代码转为机场名称
     */
    static Properties airports = new Properties();
//    static InputStream inputStream = analysisData.class.getClassLoader().getResourceAsStream("airport.txt");
    static InputStream inputStream;

    /**
     * 定义静态块只需要初始化一次airports对象
     */

    static {
        try {
            inputStream = new FileInputStream("airport.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
//            airports.load(new FileReader("airport.txt"));
            airports.load(new InputStreamReader(inputStream,"UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param inputData
     * @param outputData
     */
    public analysisData(List<String> inputData, Hashtable<String, String[]> outputData) {
        this.inputData = inputData;
        this.outputData = outputData;
    }


    @Override
    public void run() {
        while (true) {
            String line;
            synchronized (inputData) {
                //如果数据为空，则等待
                while (inputData.isEmpty()) {
                    //如果还数据未接受完毕
                    if (examTest1.readFlag) {
                        try {
                            inputData.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
//                        System.out.println("数据更新完毕");
                        return;
                    }
                }
                //取出数据并移除
                line = inputData.remove(0);
            }
            /**
             * 对航班数据进行分析(如果有需要的信息)
             */
            if (line.contains("DFME_AIRL") || line.contains("DFME_BORE") || line.contains("DFME_CANE")
                    || line.contains("DFME_DLYE") || line.contains("DFME_FPTT") || line.contains("DFME_FETT")
                    || line.contains("DFME_GTLS") || line.contains("DFME_POKE") || line.contains("SFLG")) {
                analysisEveryLine(line);
            }
//            System.out.println("读入数据:" + line);
        }
    }

    /**
     * 专门分析每一行的数据,并添加到数据outData中
     *
     * @param line 每一行的数据
     */
    private void analysisEveryLine(String line) {
        /**
         * 每次分析一行数据初始化置空
         */
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
         * 求航班唯一标识(fild)和航班号flightNumber
         * 航向分析以判断是否为出港航班
         */
        //匹配[xxxxxx]
        Matcher matcher = Pattern.compile("\\[.*\\]").matcher(line);
        if (matcher.find()) {
//            System.out.println(matcher.group());
            String[] flight = matcher.group().split("=");
            flid = flight[1].substring(0, flight[1].lastIndexOf(','));
            String ffid = flight[2];
//            System.out.println(ffid);
            flightNumber = Stream.of(ffid.split("-")).limit(2).collect(Collectors.joining());
//            System.out.println("flid:"+flid+" flightNumber:"+flightNumber);
//            if (flightNumber.length()>=7){
//                System.out.println(flightNumber);
//            }
//            System.out.println(ffid.substring(ffid.lastIndexOf(',') - 1, ffid.lastIndexOf(',')));
            //如果是进港航班，直接返回
            if ("A".equals(ffid.substring(ffid.lastIndexOf(',') - 1, ffid.lastIndexOf(',')))) {
//                System.out.println(ffid);
                return;
            }
        }
        /**
         * 求共享航班号
         */
        if (line.contains("SFLG")) {
            matcher = Pattern.compile("SFLT \\[.*\\]").matcher(line);
            if (matcher.find()) {
                String shareNo = matcher.group();
                shareNo = shareNo.substring(6, shareNo.length() - 4);
                //sfaw=ZH, sfno=1402], SFLT [sfaw=KY, sfno=1402], SFLT [sfaw=SC, sfno=1402
//                System.out.println(shareNo);
                String[] shareNos = shareNo.split("\\], SFLT \\[");
//                    System.out.println(sharNos);
                if (shareNos.length == 1) {
                    sfawOne = shareNos[0].substring(5, 7);
//                    System.out.println(sfawOne);
                    sfnoOne = shareNos[0].substring(14, 18);
//                    System.out.println(sfnoOne);
                } else if (shareNos.length == 2) {
                    sfawOne = shareNos[0].substring(5, 7);
                    sfnoOne = shareNos[0].substring(14, 18);
                    sfawTwo = shareNos[1].substring(5, 7);
                    sfnoTwo = shareNos[1].substring(14, 18);
                } else if (shareNos.length == 3) {
                    sfawOne = shareNos[0].substring(5, 7);
                    sfnoOne = shareNos[0].substring(14, 18);
                    sfawTwo = shareNos[1].substring(5, 7);
                    sfnoTwo = shareNos[1].substring(14, 18);
                    sfawThree = shareNos[2].substring(5, 7);
                    sfnoThree = shareNos[2].substring(14, 18);
                }

//                System.out.println("共享航班号1: " + sfawOne  + "\t"+
//                        "共享航班号2: " + sfawTwo + sfnoTwo + "\t"+  "共享航班号3: " + sfawThree + sfnoThree + "\t"  );
            }
        }
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
//                System.out.println("中转站:"+transferStation+" 终点站："+destinationStation);
            }
        }
        /**
         * 求登机门
         */
        if (line.contains("GATE")) {
            matcher = Pattern.compile("GATE\\[.*?\\]").matcher(line);
            if (matcher.find()) {
                String gatee = matcher.group();
                gatee = gatee.substring(5, gatee.length() - 1);
                String[] strings = gatee.split(", ");
                gate = strings[2].substring(5);
            }
//            System.out.println("gate:" + gate);
        }
        /**
         * 求起飞落地时间
         * <FPTT>	计划起飞时间
         * <FETT>	预计起飞时间
         * <FRTT>   实际起飞时间
         * 后续将简化
         */
        if (line.contains("fptt") || line.contains("fett")) {//一个航班同时fett和fptt，就用fett
            matcher = Pattern.compile("ARPT\\[.*?\\]").matcher(line);

//                matcher = Pattern.compile("ARPT\\[.*?\\]").matcher(line);
            if (matcher.find()) {
                String timeFly = matcher.group();
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
            matcher = Pattern.compile("\\[.*\\]").matcher(line);

            if (matcher.find()) {
//                    System.out.println(matcher5.group());
//                    [flid=1418075, ffid=GS-7892-20180925-A, fett=null, felt=20180925012500]
                Pattern pattern2 = Pattern.compile("(?<=fett=).*?(?=,)");
                Matcher matcher6 = pattern2.matcher(line);
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
//        System.out.println("state:"+state);
        /**
         * 归并分析结果
         */

        synchronized (outputData) {
            if (outputData.get(flid) == null) {
                outputData.put(flid, new String[]{flightNumber, sfawOne, sfnoOne, sfawTwo, sfnoTwo, sfawThree, sfnoThree,
                        transferStation, destinationStation, planToFlyTime, actualFlyTime, gate, state});
            } else {
                String[] values = outputData.get(flid);
                String[] items = {flightNumber, sfawOne, sfnoOne, sfawTwo, sfnoTwo, sfawThree, sfnoThree,
                        transferStation, destinationStation, planToFlyTime, actualFlyTime, gate, state};
                for (int i = 0; i < 13; i++) {
                    values[i] = items[i] != null ? items[i] : values[i];
                }
                outputData.put(flid, values);
                outputData.notify();
            }
        }


    }

}

/**
 * 数据输出线程
 */
class outputData extends Thread {
    private Hashtable<String, String[]> outputData;

    public outputData(Hashtable<String, String[]> outputData) {
        this.outputData = outputData;
    }

    @Override
    public void run() {
        while (true) {
            /**
             * 如果下面语句注释，则程序一直输出同一条数据  ？？？？？
             */
            System.out.println("----------------更新输出!!--------------------");
            synchronized (outputData) {
                while (outputData.isEmpty()) {
                    try {
                        outputData.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                }
                /**
                 * 开始输出
                 */
//                System.out.println("----开始输出-----");
                Iterator<String> iterator = outputData.keySet().iterator();
                int count = 0;
                while (iterator.hasNext()) {
                    String fild = iterator.next();
                    String[] values = outputData.get(fild);
//                    for (int i = 0; i < values.length; i++) {
//                        values[i]=String.format("%-10s",values[i]);
//                    }
                    //共享航班陈对出现，如果为空航班号也为空
                    for (int n = 1; n <= 6; n += 2) {
                        if (values[n] == null) {
//                            values[n]=" ";
                            values[n + 1] = " ";
                        }
                    }
                    /**
                     * 需要优化来控制格式相同
                     */
//                    for (int i = 0; i < values.length; i++) {
//                        if (values[i]!=" ")
//                        values[i]=String.format("%-10s",values[i]);
//                    }
                    values[0]=String.format("%-10s",values[0]);
//                    System.out.println("主航班号： " + values[0] + "\t" + "共享航班号1：" + values[1] + values[2]
//                            + "\t" + "共享航班号2：" + values[3] + values[4] + "\t" + "共享航班号3：" + values[5]
//                            + values[6] + "\t" + "中转站: " + values[7] + "\t" + "目的站: " + values[8] + "\t"
//                            + "预计起飞时间： " + values[9] + "\t" + "实际起飞时间： " + values[10] + "\t" + "登机口： "
//                            + values[11] + "\t" + "航班状态： " + values[12]);
                    System.out.println("主航班号： " + values[0] +   "共享航班号1：" + values[1] + values[2]
                            + "共享航班号2：" + values[3] + values[4] +"共享航班号3：" + values[5]
                            + values[6] + " 中转站: " + values[7] +  " 目的站: " + values[8]
                            + " 预计起飞时间： " + values[9] +  " 实际起飞时间： " + values[10] +  " 登机口： "
                            + values[11] +  " 航班状态： " + values[12]);

                    //System.out.println(fild + ":" + Arrays.toString(strings));
                    if ((++count) % 10 == 0)
                        System.out.println("---------------------------------------------------------------------------------");
                }
                //设置睡眠，防止输出过快
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
