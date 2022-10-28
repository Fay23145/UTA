package com.example.utapair;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.GridLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class GameActivity extends AppCompatActivity implements View.OnClickListener {

    private int numberOfElements;

    private MemoryButton[] button; // ปุ่มที่จะเอาขึ้นไปวางไว้บน GridLayout

    private int[] buttonGraphicLocation; // ตำแหน่ง Graphic ของ Button แต่ละตัวที่จะนำไป drawable
    private int[] buttonGraphic; // เก็บภาพจาก drawable มาใส่ array ไว้

    private MemoryButton selectedButton1;
    private MemoryButton selectedButton2;

    private int numRows;
    private int numColumns;

    private Boolean isBusy = false;

    @Override
    protected void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_game_easy2); // หน้า ตอนเล่นเกม

        GridLayout gridLayout = (GridLayout) findViewById(R.id.GridLayout_easy); // ต้องไปเขียนหน้า layout ที่จะให้ grid อยู่ก็คือหน้าเล่นเกม (in-game)

        numColumns = gridLayout.getColumnCount(); // อย่าลืม ไป define หน้าเดียวกันกับบรรทัดที่ 28
        numRows = gridLayout.getRowCount();

        gridLayout.setUseDefaultMargins(true); // ตรงนี้ถ้าอยาก specifile เองก็อาจจะต้องเขียนโค้ด อันนี้ใช่ default
        numberOfElements = numRows * numColumns;

        button = new MemoryButton[numberOfElements];

        buttonGraphic = new int[numberOfElements/2];

        // generate รูปที่ต้องใช้ตามขนาดจะทำแบบไหน ? แบบนี้ fix ไว้ (2 x 3)/2 = 3 unique picture
        buttonGraphic[0] = R.drawable.bttn_1;
        buttonGraphic[1] = R.drawable.bttn_2;
        buttonGraphic[2] = R.drawable.bttn_3;



        // สร้าง array สำหรับเก็บรูปของตำแหน่งนั้นว่าเป็นอะไร
        buttonGraphicLocation = new int[numberOfElements];

        shuffleButtonGraphics();


        for(int r = 0; r < numRows; r++) {

            for(int c = 0; c < numColumns; c++){
                MemoryButton tempButton = new MemoryButton(this, r, c, buttonGraphic[ buttonGraphicLocation[r * numColumns+c] ]);
                tempButton.setId(View.generateViewId()); // สร้าง id ไว้ตอน match
                tempButton.setOnClickListener(this);
                button[r * numColumns+c] = tempButton;
                gridLayout.addView(tempButton);
            }

        }



    }
    public void shuffleButtonGraphics(){
        Random rand = new Random();
        for (int i = 0 ; i < numberOfElements ; i++){
            buttonGraphicLocation[i] = i % (numberOfElements/2); // mod by unique value เพราะมันมีภาพต่างกัน 3 ภาพ 6 คู่
        }
        for(int i = 0; i < numberOfElements ; i++){
            int temp = buttonGraphicLocation[i];
            int randindex = rand.nextInt(6); // 0-5 random int number

            // swap number of buttonGraphic
            buttonGraphicLocation[i] = buttonGraphicLocation[randindex];
            buttonGraphicLocation[randindex] = temp;

        }
    }

    public boolean checkAllMatched(){
        boolean checkAllMatched = true;
        for(int r = 0; r < numRows ; r++){
            for(int c = 0 ; c < numColumns ; c++){
                if( button[r * numColumns + c].isMatched == false){
                    checkAllMatched = false;
                }


            }
        }
        return checkAllMatched;
    }

    @Override
    public void onClick(View v) {
        if(isBusy){ // มีการทำงานอยู่ไหม
            return;
        }
        MemoryButton button = (MemoryButton) v;
        if(button.isMatched) {
            return;
        }
        if(selectedButton1 == null){ // ถ้ามันถูกกดแล้วไม่ busy และ ยังไม่ Mactch ให้มาเช็คว่ามีค่าใน bttn 1 หรือยัง
            selectedButton1 = button;
            selectedButton1.filpped();
            return;
        }
        if(selectedButton1.getId() == button.getId()){
            return; // กดอันเดิมซ้ำๆมันก็ไม่ทำอะไร จนกว่าจะไปกดอันที่สอง
        }
        // วนเช็คว่า

        if(selectedButton1.getFrontDrawableId() == button.getFrontDrawableId()){ // matched
            button.filpped();

            selectedButton1.setMatched(true); // บอกว่ามันถูกจับคู่แล้ว
            button.setMatched(true);

            selectedButton1.setEnabled(false); // ปิดปุ่มไม่ให้สามารถกดได้อีกต่อไป
            button.setEnabled(false);

            selectedButton1 = null; // ให้มันไปชี้ null เพื่อรอรับค่าใหม่

        }
        else { // not matched
            selectedButton2 = button;

            selectedButton2.filpped();
            isBusy = true;

            final Handler handler = new Handler();

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    selectedButton1.filpped();
                    selectedButton2.filpped();
                    selectedButton1 = null;
                    selectedButton2 = null;
                    isBusy = false;  // delay จบแล้ว
                }
            }, 500);
        }
        // วนเช็คว่าทุก button == true แล้ว
        if(checkAllMatched() == true){
            Intent intent = new Intent(this, ScoreboardActivity.class);
            startActivity(intent);
        }
    }
}