package at.moritzmusel.gwent.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

import at.moritzmusel.gwent.R;
import at.moritzmusel.gwent.model.Card;

public class RedrawActivity extends AppCompatActivity {
    private static List<Card> sPlayerCards;
    private List<Card> mPlayerCards;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_window_redraw);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mPlayerCards = sPlayerCards; //b.getSerializable("cards");

       List<List<Card>> redrawCards = halveList(mPlayerCards);
        GameViewActivity.setCards(findViewById(R.id.redrawUserCards1), redrawCards.get(0), getApplicationContext(), this, new RedrawDragListener());
        GameViewActivity.setCards(findViewById(R.id.redrawUserCards2), redrawCards.get(1), getApplicationContext(), this, new RedrawDragListener());
    }


    public static void showRedraw(GameViewActivity gameView, List<Card> playerCards) {
        new CountDownTimer(1500, 1500) {
            @Override
            public void onTick(long l) {
            }
            @Override
            public void onFinish() {
//                Intent intent = new Intent(gameView, RedrawActivity.class);
//                Bundle b = new Bundle();
//                b.putSerializable("cards", playerCards); //todo: checken wie man list übergeben kann
//                intent.putExtras(b);
//                gameView.startActivity(intent);

                sPlayerCards = playerCards; //workaround
                gameView.startActivity(new Intent(gameView, RedrawActivity.class));

            }
        }.start();
    }

    public void onClickCloseRedraw(View view) {
        mPlayerCards.set(0, new Card(6, 10, false, true));

        finish();
    }

    public List<List<Card>> halveList(List<Card> list) {
        int marker = list.size()/2;
        List<Card> firstHalf = new ArrayList<Card>();
        List<Card> secondHalf = new ArrayList<Card>();


        for (int i = 0; i < list.size() ; i++) {
            if (i<marker){
                firstHalf.add(list.get(i));
            } else {
                secondHalf.add(list.get(i));
            }
        }
        List<List<Card>> result = new ArrayList<List<Card>>();
        result.add(firstHalf);
        result.add(secondHalf);

        return result;
    }
}
