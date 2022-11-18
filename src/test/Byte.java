package test;

import java.io.*;
import java.util.ArrayList;

public class Byte {

    public static void main(String[] args) throws IOException {
        File file = new File("fdsdata.txt");
        String line;
        int count = 0;
        ArrayList<java.lang.Byte> byteArrayList = new ArrayList<>();
//        byte[] bytes = new byte[7];
//        String temp="abcdefg";
//        for (int i = 0; i < temp.getBytes().length; i++) {
//            System.out.println(temp.getBytes()[i]);
////            byte[i]=temp.getBytes()[i];
//        }
//        System.out.println(temp.getBytes().length);
//        for (int i = 0; i < temp.getBytes().length; i++) {
//            byteArrayList.add(temp.getBytes()[i]);
//        }
//        System.out.println(byteArrayList.size());

//

        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        while ((line = bufferedReader.readLine()) != null) {
            for (int i = 0; i < line.getBytes().length; i++) {
                byteArrayList.add(line.getBytes()[i]);
            }
        }
        System.out.println(byteArrayList.size());
    }
}
