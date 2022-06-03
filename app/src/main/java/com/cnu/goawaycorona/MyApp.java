package com.cnu.goawaycorona;

import android.app.Application;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Environment;

import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

public class MyApp extends Application {

    private boolean isSatified = true;
    private final long[] arrElapsedTime = new long[10];   // 케이스 종료 시 업데이트 구현 해야함
    long startTime = 0;                             // 케이스 시작 시 업데이트 구현 해야함

    String currentFileName = "";
    String dirPath = "";

    Vector<Modification>[] listModification = null; // 위치 수정 시 업데이트 구현 해야 함.
    private int countTotalError = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        /* 외부저장소 - 공용 영역 */
        // 최상위 경로
        dirPath = Environment.getExternalStorageDirectory() + "/GoAwayCorona/";

        for(int i=0; i<10; i++) {
            listModification[i] = new Vector<Modification>();
        }

        // get Total Error
        InputStreamReader is = new InputStreamReader(getResources().openRawResource(R.raw.data));
        BufferedReader reader = new BufferedReader(is);
        CSVReader read = new CSVReader(reader);
        String[] record = null;
        try {
            while ((record = read.readNext()) != null){
                // skip first field information
                if( record[0].contains("case")) continue;
                ItemData item = new ItemData(record);
                // see items
                if( item.isStatus()==false) { // 이상 값이면
                    countTotalError++;
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public int getCountMissInputPosition()
    {
        // cvs 파일에서 status가 0인것만 리스트로 가져온다.
        Vector<ItemData> list = new Vector<ItemData>();

        InputStreamReader is = new InputStreamReader(getResources().openRawResource(R.raw.data));
        BufferedReader reader = new BufferedReader(is);
        CSVReader read = new CSVReader(reader);
        String[] record = null;
        try {
            int sequence_num = 0;
            int case_num = 1;
            while ((record = read.readNext()) != null){
                // skip first field information
                if( record[0].contains("case")) continue;
                ItemData item = new ItemData(record);

                // 각 케이스 내에서 순서를 sequence_num에 저장시켜준다.
                if( case_num == item.getCase_num()) {
                    item.sequence_num = sequence_num;
                    sequence_num++;
                }
                else {
                    case_num = item.getCase_num();
                    sequence_num = 0;
                }

                // see items
                if( item.isStatus()==false) { // 이상 값이면
                    list.add(item);
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

        int countCorrect = 0;
        // Preference에서 수정된 데이터가 있는지 확인한다.
        for(ItemData item : list) {
            // 만약 수정 데이터의 주소가 원본데이터의 주소와 같다면 맞은개수를 1증가시킨다.
            String value = loadPreference(item.getCase_num() + "," + item.sequence_num);
            if (value != null && item.getAns().equals(value)) countCorrect++;
        }

        // 잘못입력한 개수 = 총에러수 - 맞은개수
        int countMiss = countTotalError - countCorrect;

        return countMiss;
    }

    private double getDistance(double x1, double y1, double x2, double y2){
        return Math.sqrt( Math.pow( (x1-x2),2) - Math.pow((y1-y2),2) );
    }

    private double getDistance(Point p1, Point p2){
        return Math.sqrt( Math.pow( (p1.x-p2.x),2) - Math.pow((p1.y-p2.y),2) );
    }

    public double getMetric_1_1_1(Vector<Modification> list) {
        //    1 Metric 1-1-1 : 동선 수정 후 감소된 오차(m)
        //    유클리드 거리로 계산 sqrt((x_1-x_2)^2-(y_1-y_2)^2)
        //    avg[ (정답 위치, 오류 위치) - (정답 위치, 수정한 위치) ]
        return getAverageDistanceError(list);
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

    public String getMetric_1_1_2(int countModifiedError, int countTotalError) {
        //            2 Metric 1-1-2 : 수정한 오류 위치 비율(%)
        //    수정한 오류위치 개수 / 전체 오류위치 개수
        //	100 예상
        return getStringPercentage(countModifiedError, countTotalError);
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

    public boolean getMetric_1_2_2() {
        //3. Metric 1-2-2 : 동선 수정 후 정확도 만족 여부(Boolean)
        //    별도 result page 제공


        return isSatified;
    }

    public  String getMetric_2_1_1(int countMissInputPosition, int countTotalErrorPosition) {
        //4. Metric 2-1-1 : 잘못된 동선 입력 비율(%)
        //
        //    정답이 아닌 위치 입력 개수(또는 미입력) / 전체 오류위치 개수
        //	0 예상
        return getStringPercentage(countMissInputPosition,countTotalErrorPosition);
    }

    public double getMetric_2_1_2(Vector<Modification> list) {
        //5. Metric 2-1-2 : 잘못 수정한 위치로 발생한 오차(m)
        //
        //    유클리드 거리로 계산 sqrt((x_1-x_2)^2-(y_1-y_2)^2)
        //            (정답 위치, 오류 위치) - (정답 위치, 잘못 수정한 위치)
        return getDistance(p1,p2);
    }

    public String getMetric_2_2_1() {
        //            6. Metric 2-2-1 : 평균 완료 시간(sec)
        //
        //    Case별 시작시간(05 entry 또는 Case이동)부터 종료시간(Case이동 또는 05 exit)

        double averageCompleteTime = getAverageCompleteTime();

        return getStringSecond(averageCompleteTime);
    }

    private double getAverageCompleteTime() {

        int size = arrElapsedTime.length;
        long sum = 0;

        for(Long time : arrElapsedTime) {
            sum+=time;
        }

        double average = (double)sum/(double)size;

        return average;
    }

    public String getMetric_2_2_2(int countAutoSelection, int countTotalErrorPoint) {
        //            7. Metric 2-2-2 : 자동 선택 비율(%)
        //    Case별 자동수정 선택 개수 / 전체 오류위치 개수
        return getStringPercentage(countAutoSelection, countTotalErrorPoint);
    }


    public void writeFile(String fileTitle, String content) {
        File file = new File(dirPath, fileTitle);

        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter writer = new FileWriter(file, false);
            writer.write(content+"\n");
            writer.close();
        } catch (IOException e) {

        }
    }

    public String readFile(String fileTitle) {
        File file = new File(dirPath, fileTitle);
        String result = "";

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                result += line;
            }

            System.out.println( "불러온 내용 : " + result);

            reader.close();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }

        return result;
    }

    public void createNewLog() {
        Date date = new Date();
        SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy.MM.dd_HH_mm_ss");
        currentFileName = simpleFormat.format(date)+".txt";
    }

    public void setStartTime() {
        startTime = System.currentTimeMillis();
    }

    public long getElapsedTimeSec() {
        return (System.currentTimeMillis() - startTime)/1000;
    }

    public void setTime(int page, long value) {
        arrElapsedTime[page-1] = value;
    }

    public long getTime(int page) {
        return arrElapsedTime[page-1];
    }

    public void reset() {
        isSatified = false;
        for(int i=0; i<arrElapsedTime.length; i++)
           arrElapsedTime[i] = 0;

        currentFileName = "";
        startTime = 0;

        for(int i=0; i<10; i++) {
            listModification[i].clear();
        }
    }

    public String save() {
        createNewLog();

        String content = "";


        for(int i=0; i<10; i++) {
            // Metric 1~7까지 저장하기
            Vector<Modification> list = listModification[i];
            int countModification = list.size();

            double metric_1_1_1 = getMetric_1_1_1(list);
            String metric_1_1_2 = getMetric_1_1_2(countModification, countTotalError);
            boolean metric_1_2_2 = getMetric_1_2_2();
            String metric_2_1_1 = getMetric_2_1_1(getCountMissInputPosition(), countTotalError);
            double metric_2_1_2 = getMetric_2_1_2(list);
            String metric_2_2_1 = getMetric_2_2_1();
            String metric_2_2_2 = getMetric_2_2_2();
        }




        writeFile(currentFileName, content);

        String filePath = dirPath + currentFileName;
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
}
