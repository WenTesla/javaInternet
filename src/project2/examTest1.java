package project2;

import java.io.*;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        Hashtable<String, String> outputData = new Hashtable<>();
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
            examTest1.readFlag=false;
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
    private Hashtable<String, String> outputData;

    public analysisData(List<String> inputData, Hashtable<String, String> outputData) {
        this.inputData = inputData;
        this.outputData = outputData;
    }


    @Override
    public void run() {
        while (true) {
            String line = null;
            synchronized (inputData) {
                //如果数据为空，则等待
                while (inputData.isEmpty()) {
                    if (examTest1.readFlag){
                    try {
                        inputData.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    }
                    else {
                        System.out.println("数据更新完毕");
                        return;
                    }
                }
                //取出数据并移除
                line = inputData.remove(0);
            }
            /**
             * 对航班数据进行分析
             */
            String flid;
            Matcher matcher = Pattern.compile("(?<=flid=).*?(?=,)").matcher(line);
            if (matcher.find()) {
                flid = matcher.group();
                synchronized (outputData) {
                    //判断是否需要更新数据
                    if (outputData.containsKey(flid)) {
                        //添加或覆写数据

                    } else {
                        //添加新的数据,value可以为其他数据类型
                        outputData.put(flid, line);
                        //唤醒单个线程
                        outputData.notify();
                    }
                }

            }
//            System.out.println("读入数据:"+line);
        }
    }
}

/**
 * 数据输出线程
 */
class outputData extends Thread {
    private Hashtable<String, String> outputData;

    public outputData(Hashtable<String, String> outputData) {
        this.outputData = outputData;
    }

    @Override
    public void run() {
        while (true) {
            System.out.println("更新输出!!--------------------");
            synchronized (outputData) {
                while (outputData.isEmpty()) {
                    try {
                        outputData.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                }
                Iterator<String> iterator = outputData.keySet().iterator();
                while (iterator.hasNext()) {
                    String fild = iterator.next();
                    System.out.println("更新输出后：" + fild + ":" + outputData.get(fild));
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
