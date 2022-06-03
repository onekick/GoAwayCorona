package com.cnu.goawaycorona;

import android.app.Application;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Environment;

import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

public class MyApp extends Application {
    private static final String PREFERENCE_NAME = "MyPreferecne";
    private boolean isSatified = true;
    private final long[] arrElapsedTime = new long[10];         // 케이스 종료 시 업데이트
    long startTime = 0;                                         // 케이스 시작 시 업데이트

    String currentFileName = "";

    // cvs 파일에서 status가 0인것만 리스트로 가져온다.
    Vector<ItemData>[] listOfListError = new Vector[10];

    @Override
    public void onCreate() {
        super.onCreate();

        /* 외부저장소 - 공용 영역 */
        // 최상위 경로
//        dirPath = Environment.getExternalStorageDirectory() + "/";

        for (int i = 0; i < 10; i++) {
            listOfListError[i] = new Vector<ItemData>();
        }


        // get Total Error
        InputStreamReader is = new InputStreamReader(getResources().openRawResource(R.raw.data));
        BufferedReader reader = new BufferedReader(is);
        CSVReader read = new CSVReader(reader);
        String[] record = null;
        try {
            int sequence_num = 0;
            int case_num = 1;
            while ((record = read.readNext()) != null) {
                // skip first field information
                if (record[0].contains("case")) continue;
                ItemData item = new ItemData(record);

                // 각 케이스 내에서 순서를 sequence_num에 저장시켜준다.
                if (case_num == item.getCase_num()) {
                    item.sequence_num = sequence_num;
                    sequence_num++;
                } else {
                    case_num = item.getCase_num();
                    sequence_num = 0;
                }

                // see items
                if (item.isStatus() == false) { // 이상 값이면
                    listOfListError[case_num - 1].add(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getCountMissInputPosition(int page_num)
    {
        int countCorrect = 0;
        Vector<ItemData> list = listOfListError[page_num-1];
        // Preference에서 수정된 데이터가 있는지 확인한다.
        for(ItemData item :list)
        {
            // 만약 수정 데이터의 주소가 원본데이터의 주소와 같다면 맞은개수를 1증가시킨다.
            String value = loadPreference(item.getCase_num() + "," + item.sequence_num);
            if (value != null) {
                item.isModified = true;
                if (item.getAns().equals(value)) {
                    countCorrect++;
                    item.isCorrect = true;
                } else {
                    item.isCorrect = false;
                }
            } else {
                item.isModified = false;
            }
        }

        // 잘못입력한 개수 = 총에러수 - 맞은개수
        int countMissInputPosition =list.size() -countCorrect;
        return countMissInputPosition;
    }


    private double getDistance(double x1, double y1, double x2, double y2){
        return Math.sqrt( Math.pow( x1-x2,2) + Math.pow(y1-y2,2) );
    }

    private double getDistance(Position p1, Position p2){
        return Math.sqrt( Math.pow( (p1.x-p2.x),2) - Math.pow((p1.y-p2.y),2) );
    }

    public double getMetric_1_1_1(int case_num) {
        //    1 Metric 1-1-1 : 동선 수정 후 감소된 오차(m)
        //    유클리드 거리로 계산 sqrt((x_1-x_2)^2-(y_1-y_2)^2)
        //    avg[ (정답 위치, 오류 위치) - (정답 위치, 수정한 위치) ]

        Vector<ItemData> list = listOfListError[case_num-1];

        int size = list.size();

        double[] arrCurrentErr = new double[size];
        double[] arrModifiedErr = new double[size];
        double[] arrDiff = new double[size];

        for(int i=0; i<size; i++) {
            ItemData item = list.get(i);
            arrCurrentErr[i] = getDistance(item.ans_x, item.ans_y, item.app_x, item.app_y);

            if (item.isModified) {
                if(item.isCorrect) {
                    arrModifiedErr[i] = 0;
                }
                else{
                    arrModifiedErr[i] = item.getBasic_error();
                }
            }
            else {
                arrModifiedErr[i] = arrCurrentErr[i];
            }
        }

        for(int i=0; i<size; i++)
            arrDiff[i] = arrCurrentErr[i] - arrModifiedErr[i];

        double sum = 0;
        for(int i=0; i<size; i++)
            sum+=arrDiff[i];

        double average = sum/size;


        return average;
    }

    private double getAverageDistanceError(Vector<Modification> list) {
        double average = 0;
        double sum = 0;
        double size = list.size();

        for(Modification modification: list) {
            sum+=getDistance(modification.prev, modification.next);
        }

        average = sum/size;

        return average;
    }

    public String getMetric_1_1_2(int case_num) {
        //            2 Metric 1-1-2 : 수정한 오류 위치 비율(%)
        //    수정한 오류위치 개수 / 전체 오류위치 개수
        //	100 예상
        Vector<ItemData> list = listOfListError[case_num-1];

        int countModifiedError=0;
        for(ItemData item: list){
            if(item.isModified) countModifiedError++;
        }

        return getStringPercentage(countModifiedError, list.size());
    }

    private String getStringPercentage(double percentage) {
        return String.format("%.2f", percentage)+"%";
    }

    private String getStringSecond(double second) {
        return String.format("%.2f", second)+"Sec";
    }

    private String getStringPercentage(int value, int total) {
        double percentage = ((double) value / (double) total) * 100.0;
        return getStringPercentage(percentage);
    }

    public boolean getMetric_1_2_2(int case_num) {
        //3. Metric 1-2-2 : 동선 수정 후 정확도 만족 여부(Boolean)
        //    별도 result page 제공


        return isSatified;
    }

    public  String getMetric_2_1_1(int case_num) {
        //4. Metric 2-1-1 : 잘못된 동선 입력 비율(%)
        //
        //    정답이 아닌 위치 입력 개수(또는 미입력) / 전체 오류위치 개수
        //	0 예상
        Vector<ItemData> list = listOfListError[case_num-1];

        int countCorrect = 0;
        for(ItemData item : list) {
            if(item.isModified && item.isCorrect) countCorrect++;
        }

        int countMiss = list.size() - countCorrect;

        return getStringPercentage(countMiss,list.size());
    }

    public double getMetric_2_1_2(int case_num) {
        //5. Metric 2-1-2 : 잘못 수정한 위치로 발생한 오차(m)
        //
        //    유클리드 거리로 계산 sqrt((x_1-x_2)^2-(y_1-y_2)^2)
        //            (정답 위치, 오류 위치) - (정답 위치, 잘못 수정한 위치)

        Vector<ItemData> list = listOfListError[case_num-1];

        // 입력이 잘못된 아이템들의 기본오차의 평균
        double sum = 0;
        for(ItemData item: list) {
            if( item.isModified && !item.isCorrect) {
                sum+= item.basic_error;
            }
        }
        return sum/list.size();
    }

    public String getMetric_2_2_1(int case_num) {
        //            6. Metric 2-2-1 : 평균 완료 시간(sec)
        //
        //    Case별 시작시간(05 entry 또는 Case이동)부터 종료시간(Case이동 또는 05 exit)

        Vector<ItemData> list = listOfListError[case_num-1];


        long averageCompleteTime = arrElapsedTime[case_num-1];

        return getStringSecond(averageCompleteTime);
    }

    private double getAverageCompleteTime() {

        int size = 0;
        long sum = 0;

        for(Long time : arrElapsedTime) {
            if(time<=0) continue;
            sum+=time;
            size++;
        }

        double average = (double)sum/(double)size;

        return average;
    }

    public String getMetric_2_2_2(int case_num) {
        //            7. Metric 2-2-2 : 자동 선택 비율(%)
        //    Case별 자동수정 선택 개수 / 전체 오류위치 개수

        int countAutoSelection = 0;
        Vector<ItemData> list = listOfListError[case_num-1];
        for(ItemData item : list) {
            if(item.isModifiedAutomatically) countAutoSelection++;
        }

        return getStringPercentage(countAutoSelection, list.size());
    }


    public void writeFile(String fileTitle, String content) {

        try {
//            File file = new File(dirPath, fileTitle);
//            if (!file.exists()) {
//                file.createNewFile();
//            }
//            FileWriter writer = new FileWriter(file, false);
//            writer.write(content+"\n");
//            writer.close();
            FileOutputStream fos = openFileOutput(fileTitle,MODE_PRIVATE);
            DataOutputStream dos = new DataOutputStream(fos);
//데이터를 쓴다.
            dos.writeBytes(content);
            dos.flush();
            dos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    public String readFile(String fileTitle) {
//        File file = new File(dirPath, fileTitle);
//        String result = "";
//
//        try {
//            BufferedReader reader = new BufferedReader(new FileReader(file));
//            String line;
//            while ((line = reader.readLine()) != null) {
//                result += line;
//            }
//
//            System.out.println( "불러온 내용 : " + result);
//
//            reader.close();
//        } catch (FileNotFoundException e1) {
//            e1.printStackTrace();
//        } catch (IOException e2) {
//            e2.printStackTrace();
//        }
//
//        return result;
//    }

    public void createNewLog() {
        Date date = new Date();
        SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy.MM.dd_HH_mm_ss");
        currentFileName = simpleFormat.format(date)+".txt";
    }

    public void setStartTime() {
        startTime = System.currentTimeMillis();
    }

    private long getElapsedTimeSec() {
        return (System.currentTimeMillis() - startTime)/1000;
    }

    private void setTime(int page, long value) {
        arrElapsedTime[page-1] = value;
    }

    public long getTime(int page) {
        return arrElapsedTime[page-1];
    }

    public void reset() {
        isSatified = true;
        for(int i=0; i<arrElapsedTime.length; i++)
           arrElapsedTime[i] = 0;

        currentFileName = "";
        startTime = 0;

        listOfListError = new Vector[10];
        for (int i = 0; i < 10; i++) {
            listOfListError[i] = new Vector<ItemData>();
        }

        InputStreamReader is = new InputStreamReader(getResources().openRawResource(R.raw.data));
        BufferedReader reader = new BufferedReader(is);
        CSVReader read = new CSVReader(reader);
        String[] record = null;
        try {
            int sequence_num = 0;
            int case_num = 1;
            while ((record = read.readNext()) != null) {
                // skip first field information
                if (record[0].contains("case")) continue;
                ItemData item = new ItemData(record);

                // 각 케이스 내에서 순서를 sequence_num에 저장시켜준다.
                if (case_num == item.getCase_num()) {
                    item.sequence_num = sequence_num;
                    sequence_num++;
                } else {
                    case_num = item.getCase_num();
                    sequence_num = 0;
                }

                // see items
                if (item.isStatus() == false) { // 이상 값이면
                    listOfListError[case_num - 1].add(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String save() {
        createNewLog();

        writeFile(currentFileName, getResult());

        String filePath = currentFileName;
        return filePath;
    }


    public void setSatisfied(boolean b) {
        isSatified = b;
    }



    private void clearPreference() {
        SharedPreferences preferences =
                getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }
    private String loadPreference(String key) {
        SharedPreferences preferences =
                getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        return preferences.getString(key, null);
    }

    private void savePreference(String key, String value) {
        SharedPreferences preferences =
                getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor= preferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public void setStopTime(int page_num) {
        long elapsedTime = getElapsedTimeSec();

        setTime(page_num, elapsedTime);
    }

    public void setModification(int case_num, int sequence_num, boolean isModifiedAutomatically, String address) {
        Vector<ItemData> list = listOfListError[case_num-1];

        for(ItemData item : list) {
            if( item.sequence_num == sequence_num){
                item.isModifiedAutomatically = isModifiedAutomatically;
                item.isModified = true;

                if(item.getAns().equals(address))
                    item.isCorrect = true;
                else item.isCorrect = false;
            }
        }
    }

    public String getResult() {
        String content = "";


        for(int i=0; i<10; i++) {
            // Metric 1~7까지 생성하기
            int case_num = i+1;

            double metric_1_1_1 = getMetric_1_1_1(case_num);    // 동선 수정 후 감소된 오차(m)
            String metric_1_1_2 = getMetric_1_1_2(case_num);          // 수정한 오류 위치 비율(%)
            boolean metric_1_2_2 = getMetric_1_2_2(case_num);       // 동선 수정 후 정확도 만족 여부(Boolean)
            String metric_2_1_1 = getMetric_2_1_1(case_num);     // 잘못된 동선 입력 비율(%)
            double metric_2_1_2 = getMetric_2_1_2(case_num);    // 잘못 수정한 위치로 발생한 오차(m)
            String metric_2_2_1 = getMetric_2_2_1(case_num);        // 평균 완료 시간(sec)
            String metric_2_2_2 = getMetric_2_2_2(case_num);        // 자동 선택 비율(%)

            if(i>0) content += "\n";
            content += String.valueOf(metric_1_1_1);
            content += "," + metric_1_1_2;
            content += "," + metric_1_2_2;
            content += "," + metric_2_1_1;
            content += "," + metric_2_1_2;
            content += "," + metric_2_2_1;
            content += "," + metric_2_2_2;
        }

        return content;
    }
}
