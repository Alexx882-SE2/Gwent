package at.moritzmusel.gwent;

import android.content.ClipData;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class GameViewActivity extends AppCompatActivity {

    private ArrayList<ImageView> imageViewList;
    private LinearLayout ll, llOpponent;
    private String msg;
    private android.widget.LinearLayout.LayoutParams layoutParams;
    private Button buttonOpponentCards;
    private PopupWindow popupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_view);

        buttonOpponentCards = (Button) findViewById(R.id.buttonOpponentCards);
        buttonOpponentCards.setOnTouchListener(new OnSwipeTouchListener(this, findViewById(R.id.buttonOpponentCards)) {
            @Override
            void onSwipeTop() {
                buttonOpponentCards.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_keyboard_arrow_down_24));
                popupWindow.dismiss();
                super.onSwipeTop();
            }

            @Override
            void onSwipeBottom() {
                buttonOpponentCards.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_keyboard_arrow_up_24));
                onButtonShowPopupWindowClick(getWindow().getDecorView().getRootView());
                super.onSwipeBottom();
            }
        });

        ll = findViewById(R.id.linearLayoutMainCardsDeck);

        imageViewList = new ArrayList<>();
        int size = 9;

        //FrameLayout fl = new FrameLayout(ll.getContext());
        //fl.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));

        for (int i = 0; i < size; i++) {
            //ImageView im = new ImageView(fl.getContext());
            ImageView im = new ImageView(this);
            if (i == 0) {
                im.setPadding(50, 50, 5, 50);
            } else if (i == size - 1) {
                im.setPadding(5, 50, 50, 50);
            } else {
                im.setPadding(5, 50, 5, 50);
            }

            im.setId(i + 1);
            im.setOnDragListener(new View.OnDragListener() {
                @Override
                public boolean onDrag(View v, DragEvent event) {
                    switch (event.getAction()) {
                        case DragEvent.ACTION_DRAG_STARTED:
                            layoutParams = (LinearLayout.LayoutParams) v.getLayoutParams();
                            Log.d(msg, "Action is DragEvent.ACTION_DRAG_STARTED");
                            break;
                        case DragEvent.ACTION_DRAG_ENTERED:
                            Log.d(msg, "Action is DragEvent.ACTION_DRAG_ENTERED");
                            int x_cord = (int) event.getX();
                            int y_cord = (int) event.getY();
                            break;
                        case DragEvent.ACTION_DRAG_EXITED:
                            Log.d(msg, "Action is DragEvent.ACTION_DRAG_EXITED");
                            x_cord = (int) event.getX();
                            y_cord = (int) event.getY();
                            layoutParams.leftMargin = x_cord;
                            layoutParams.topMargin = y_cord;
                            v.setLayoutParams(layoutParams);
                            break;
                        case DragEvent.ACTION_DRAG_LOCATION:
                            Log.d(msg, "Action is DragEvent.ACTION_DRAG_LOCATION");
                            x_cord = (int) event.getX();
                            y_cord = (int) event.getY();
                            break;
                        case DragEvent.ACTION_DRAG_ENDED:
                            Log.d(msg, "Action is DragEvent.ACTION_DRAG_ENDED");
                            break;
                        case DragEvent.ACTION_DROP:
                            Log.d(msg, "ACTION_DROP event");
                            break;
                        default:
                            break;
                    }
                    return true;
                }
            });

            im.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        ClipData data = ClipData.newPlainText("", "");
                        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(im);

                        im.startDrag(data, shadowBuilder, im, 0);
                        im.setVisibility(View.INVISIBLE);
                        return true;
                    } else {
                        return false;
                    }
                }
            });

            setImageFromAsset(im);
            imageViewList.add(im);
            //fl.addView(im);

            /*TextView tv = new TextView(fl.getContext());
            tv.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.tv_points));
            tv.setText("3");
            tv.setTextSize(5);
            tv.setGravity(Gravity.LEFT);
            tv.setTextColor(Color.parseColor("#FFFFFF"));
            FrameLayout.LayoutParams lp1 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.LEFT);
            tv.setLayoutParams(lp1);
            fl.addView(tv);*/

            //ll.removeAllViews();
            ll.addView(im);
        }
    }

    /**
     * Card is currently randomly chosen.
     *
     * @param image
     */
    public void setImageFromAsset(ImageView image) {

        AssetManager manager = getAssets();
        try {
            String[] file = manager.list("");
            int fileSize = file.length;
            int i = ThreadLocalRandom.current().nextInt(0, fileSize + 1);
            try {
                InputStream imgStream;
                imgStream = manager.open(file[i]);
                Drawable d = Drawable.createFromStream(imgStream, null);
                Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
                Drawable dr = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 150, 200, true));
                image.setImageDrawable(dr);

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setImageFromAssetForOpponent(ImageView image) {
        Bitmap bitmap = ((BitmapDrawable) getDrawable(R.drawable.card_deck_back_opponent_right)).getBitmap();
        Drawable dr = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 50, 70, true));
        image.setImageDrawable(dr);
    }

    /**
     * Pop-op windows of cards from opponent.
     * At the moment the number of cards is randomly chosen.
     *
     * @param view
     */
    public void onButtonShowPopupWindowClick(View view) {
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_window_opponent, null);
        llOpponent = popupView.findViewById(R.id.linearLayoutMainCardsDeckOpponent);

        int size = ThreadLocalRandom.current().nextInt(1, 6);
        System.out.println(size);

        if (size == 1) {
            ImageView im = new ImageView(view.getContext());
            im.setPadding(50, 50, 50, 50);
            setImageFromAssetForOpponent(im);
            llOpponent.addView(im);
        } else {
            for (int i = 0; i < size; i++) {
                ImageView im = new ImageView(view.getContext());
                if (i == 0) {
                    im.setPadding(50, 50, 12, 50);
                } else if (i == size - 1) {
                    im.setPadding(12, 50, 50, 50);
                } else {
                    im.setPadding(12, 50, 12, 50);
                }
                setImageFromAssetForOpponent(im);
                llOpponent.addView(im);
            }
        }

        // important: before getting the size of pop-up we should assign default measurements for the view
        popupView.measure(0, 0);

        // create the popup window
        popupWindow = new PopupWindow(popupView, popupView.getMeasuredWidth(), popupView.getMeasuredHeight(), false);

        // show the popup window
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
    }


}
