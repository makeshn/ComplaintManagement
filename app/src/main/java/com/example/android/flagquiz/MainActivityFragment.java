package com.example.android.flagquiz;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static android.R.attr.path;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    private static final String TAG = "Flag Quiz";
    private static final int FLAGS_IN_QUIZ = 10;
    private List<String> fileNameList;
    private List<String> quizCountrieslist;
    private Set<String> regionsSet;
    private String correctAnswer;
    private int totalguess;
    private int correctanswer;
    private int guessrows;
    private SecureRandom random;
    private Handler handler;
    private Animation shakeAnimation;
    private LinearLayout quizLayout;
    private TextView questionNumberTextView;
    private ImageView quizflagImageView;
    private LinearLayout[] guessLinearLayouts;
    private TextView Answertextview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        fileNameList = new ArrayList<>();
        quizCountrieslist = new ArrayList<>();
        random = new SecureRandom();
        handler = new Handler();
        shakeAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.incorrect_shake);
        shakeAnimation.setRepeatCount(3);
        quizLayout = (LinearLayout) view.findViewById(R.id.quizLinearLayout);
        questionNumberTextView = (TextView) view.findViewById(R.id.questionNumbertextView);
        quizflagImageView = (ImageView) view.findViewById(R.id.flagImageView);
        guessLinearLayouts = new LinearLayout[4];
        guessLinearLayouts[0] = (LinearLayout) view.findViewById(R.id.rowlayout1);
        guessLinearLayouts[1] = (LinearLayout) view.findViewById(R.id.rowlayout2);
        guessLinearLayouts[2] = (LinearLayout) view.findViewById(R.id.rowlayout3);
        guessLinearLayouts[3] = (LinearLayout) view.findViewById(R.id.rowlayout4);
        Answertextview = (TextView) view.findViewById(R.id.answerTextView);
        for (LinearLayout row : guessLinearLayouts) {
            for (int col = 0; col < row.getChildCount(); col++) {
                Button button = (Button) row.getChildAt(col);
                button.setOnClickListener(guessButtonListener);
            }
        }
        questionNumberTextView.setText(getString(R.string.question, 1, FLAGS_IN_QUIZ));


        return view;
    }

    public void updateGuessRows(SharedPreferences sharedPreferences) {
        String choices = sharedPreferences.getString(MainActivity.number, null);
        guessrows = Integer.parseInt(choices) / 2;
        for (LinearLayout layout : guessLinearLayouts)
            layout.setVisibility(View.GONE);
        for (int row = 0; row < guessrows; row++)
            guessLinearLayouts[row].setVisibility(View.VISIBLE);
    }

    public void updateRegions(SharedPreferences sharedPreferences) {
        regionsSet = sharedPreferences.getStringSet(MainActivity.region, null);
    }

    public void quizreset() {
        AssetManager asset = getActivity().getAssets();
        fileNameList.clear();
        try {
            for (String region : regionsSet) {
                String[] paths = asset.list(region);
                for (String path : paths)
                    fileNameList.add(path.replace(".png", ""));
            }
        } catch (IOException e) {
            Log.e(TAG, "Error", e);
        }
        correctanswer = 0;
        totalguess = 0;
        quizCountrieslist.clear();
        int flagcount = 1;
        int flagtotal = fileNameList.size();
        while (flagcount <= FLAGS_IN_QUIZ) {
            int randomIndex = random.nextInt(flagtotal);
            String filename = fileNameList.get(randomIndex);
            if (!quizCountrieslist.contains(filename)) {
                quizCountrieslist.add(filename);
                ++flagcount;
            }
        }
        loadNextFlag();
    }

    private void loadNextFlag() {
        String nextImage = quizCountrieslist.remove(0);
        correctAnswer = nextImage;
        Answertextview.setText("");
        questionNumberTextView.setText(getString(R.string.question, correctanswer + 1, FLAGS_IN_QUIZ));
        String region = nextImage.substring(0, nextImage.indexOf('-'));
        AssetManager assets = getActivity().getAssets();
        try (InputStream stream = assets.open(region + '/' + nextImage + ".png")) {
            Drawable img = Drawable.createFromStream(stream, nextImage);
            quizflagImageView.setImageDrawable(img);

            animate(false);
        } catch (IOException e) {
            Log.e("IOEXC", "Error handling image");
        }
        Collections.shuffle(fileNameList);
        int correct = fileNameList.indexOf(correctAnswer);
        fileNameList.add(fileNameList.remove(correct));
        for (int row = 0; row < guessrows; row++) {
            for (int col = 0; col < guessLinearLayouts[row].getChildCount(); col++) {
                Button newGuessButton = (Button) guessLinearLayouts[row].getChildAt(col);
                newGuessButton.setEnabled(true);
                String fileName = fileNameList.get((row * 2) + col);
                newGuessButton.setText(getCountryName(fileName));
            }
        }
        int row = random.nextInt(guessrows);
        int col = random.nextInt(2);
        LinearLayout guess = guessLinearLayouts[row];
        String country = getCountryName(correctAnswer);
        ((Button) guess.getChildAt(col)).setText(country);
    }

    private String getCountryName(String name) {
        return name.substring(name.indexOf('-') + 1).replace('_', ' ');
    }

    private void animate(boolean animateOut) {
        if (correctanswer == 0)
            return;
        int centreX = (quizLayout.getLeft() + quizLayout.getRight()) / 2;
        int centreY = (quizLayout.getTop() + quizLayout.getBottom()) / 2;
        int radius = Math.max(quizLayout.getWidth(), quizLayout.getHeight());
        Animator animator;
        if (animateOut) {
            animator = ViewAnimationUtils.createCircularReveal(quizLayout, centreX, centreY, radius, 0);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    loadNextFlag();
                }
            });

        } else {
            animator = ViewAnimationUtils.createCircularReveal(quizLayout, centreX, centreY, 0, radius);
        }

        animator.setDuration(500);
        animator.start();

    }

    private View.OnClickListener guessButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Button guess = ((Button) v);
            String guessString = guess.getText().toString();
            String correct = getCountryName(correctAnswer);
            ++totalguess;
            if (guessString.equals(correct)) {
                ++correctanswer;
                Answertextview.setText(correct + "!");
                Answertextview.setTextColor(Color.parseColor("#00CC00"));
                disableButtons();
                if (correctanswer == FLAGS_IN_QUIZ) {
                    DialogFragment quizResults = new DialogFragment() {
                        @Override
                        public Dialog onCreateDialog(Bundle bundle) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setMessage(getString(R.string.results, totalguess, (1000 / (double) totalguess)));
                            builder.setPositiveButton(R.string.reset_quiz, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    quizreset();
                                }
                            });
                            return builder.create();
                        }


                    };
                    quizResults.setCancelable(false);
                    quizResults.show(getFragmentManager(), "quiz results");

                } else {
                    handler.postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    animate(true);
                                }
                            }, 2000);

                }


            } else {
                quizflagImageView.startAnimation(shakeAnimation);
                Answertextview.setText(R.string.incorrect_answer);
                Answertextview.setTextColor(Color.parseColor("#FF0000"));
                guess.setEnabled(false);
            }

        }


    };
    private void disableButtons()
    {
        for(int row=0;row<guessrows;row++)
        {
            LinearLayout guess = guessLinearLayouts[row];
            for(int i=0;i<guess.getChildCount();i++)
            {
                guess.getChildAt(i).setEnabled(false);
            }
        }
    }
}
