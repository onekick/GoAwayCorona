package com.cnu.goawaycorona;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

public class PathView extends View {
    Point size = new Point(0,0);
    Paint paint = new Paint();
    Paint paintBG = new Paint();
    Paint paintPt = new Paint();
    Paint paintEdge = new Paint();


    private final ArrayList<ItemData> arrayList = new ArrayList<>();

    public PathView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        paint.setColor(Color.argb(255,192,192,192));
        paint.setStrokeWidth((float) 4.0);

        paintBG.setColor(Color.argb(255,255,255,255));

        paintPt.setColor(Color.argb(255,255,0,0));
        paintPt.setStrokeWidth((float) 6.0);

        paintEdge.setColor(Color.argb(255,255,0,0));
        paintEdge.setStrokeWidth((float) 2.0);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        size.x = w;
        size.y = h;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // clear bg
        canvas.drawRect(0,0,size.x,size.y,paintBG);

        drawGrid(canvas, 10,10);

        float prevX = -100000;
        float prevY = -100000;
        for(ItemData item : arrayList) {
            float x = getRelativeX(item.app_x);
            float y = getRelativeY(item.app_y); // y축은 좌표계 반대방향 증가
            canvas.drawCircle(x,y,6,paintPt);

            if(prevX > -90000 && prevY > -90000) {
                canvas.drawLine(prevX,prevY,x,y,paintEdge);
            }
            prevX = x;
            prevY = y;

        }
    }

    private float getRelativeX(double app_position) {

        double base = size.x/2.0;
        double max = size.x/2.0;
        double percentage = app_position/100.0;
        double relativePosition = base + percentage*max;

        return (float)relativePosition;
    }

    private float getRelativeY(double app_position) {

        double base = size.y/2.0;
        double max = size.y/2.0;
        double percentage = -1*app_position/100.0;    // y축은 반대방향 증가
        double relativePosition = base + percentage*max;

        return (float)relativePosition;
    }

    private void drawGrid(Canvas canvas, int x, int y) {

        float dx = size.x/(float)10.0;
        float dy = size.y/(float)10.0;

        for(int i=0; i<=x; i++){
            canvas.drawLine(i*dx,0,i*dx,size.y,paint);
        }

        for(int i=0; i<=y; i++){
            canvas.drawLine(0,dy*i,size.x,dy*i,paint);
        }

    }

    public void add(ItemData item) {
        arrayList.add(item);
    }

    public void clear() {
        arrayList.clear();
    }

    public double getAccuracy(MyPathActivity activity, int pageNumber) {

        double totalError = 0;
        // 현재 페이지의 모든 인덱스에 대해 저장된 주소를 가져온다.
        for(int i=0; i<arrayList.size(); i++) {
            // 저장된게 있으면 업데이트 시키고 상태를 바꾼다.
            ItemData itemData = (ItemData) arrayList.get(i);
            double error = itemData.getBasic_error();
            if(!itemData.isStatus()) {
                String key = pageNumber+","+i;
                String address = activity.loadPreference(key);
                if( address!=null) {
                    error = 0;
                }
            }
            totalError+=error;
        }

        // 상단 정확도 갱신하기
        // 평균에러 = 에러총합/갯수
        double std_err = totalError/arrayList.size();
        // 정확도 계산법 = 100% - 평균 에러
        double accuracy = 100.0 - std_err;

        return accuracy;
    }


}
