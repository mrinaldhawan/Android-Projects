package com.mdhawa3.ArithmeticQuiz;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivityFragment extends Fragment {

   private static final String TAG = "MainActivityFragment";

   private static final int TOTAL_QUESTIONS = 10;

   private List<String> EnabledOperators = new ArrayList<>();
   private String correctAnswer;
   private Set<String> OperatorSet;
   private SecureRandom random;
   private Animation shakeAnimation;
   private int totalGuesses;
   private int correctAnswers;
   private int guessAnswer;
   private int guessRows;
   private Handler handler;
   private int firstOperand;
   private int secondOperand;
   private int operatorId;
   private String questionText;
   private String operator;

   private LinearLayout quizLinearLayout;
   private TextView questionNumberTextView;
   private TextView QuestionTextView;
   private LinearLayout[] guessLinearLayouts;
   private TextView answerTextView;

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
      super.onCreateView(inflater, container, savedInstanceState);
      View view =
         inflater.inflate(R.layout.fragment_main, container, false);

      random = new SecureRandom();
      handler = new Handler();


      shakeAnimation = AnimationUtils.loadAnimation(getActivity(),
         R.anim.incorrect_shake);
      shakeAnimation.setRepeatCount(3);


      quizLinearLayout =
         (LinearLayout) view.findViewById(R.id.quizLinearLayout);
      questionNumberTextView =
         (TextView) view.findViewById(R.id.questionNumberTextView);
      QuestionTextView = (TextView) view.findViewById(R.id.QuestionTextView);
      guessLinearLayouts = new LinearLayout[4];
      guessLinearLayouts[0] =
         (LinearLayout) view.findViewById(R.id.row1LinearLayout);
      guessLinearLayouts[1] =
         (LinearLayout) view.findViewById(R.id.row2LinearLayout);
      guessLinearLayouts[2] =
         (LinearLayout) view.findViewById(R.id.row3LinearLayout);
      guessLinearLayouts[3] =
         (LinearLayout) view.findViewById(R.id.row4LinearLayout);
      answerTextView = (TextView) view.findViewById(R.id.answerTextView);


      for (LinearLayout row : guessLinearLayouts) {
         for (int column = 0; column < row.getChildCount(); column++) {
            Button button = (Button) row.getChildAt(column);
            button.setOnClickListener(guessButtonListener);
         }
      }


      questionNumberTextView.setText(
         getString(R.string.question, 1, TOTAL_QUESTIONS));
      return view;
   }


   public void updateGuessRows(SharedPreferences sharedPreferences) {

      String choices =
         sharedPreferences.getString(MainActivity.CHOICES, null);
      guessRows = Integer.parseInt(choices) / 2;


      for (LinearLayout layout : guessLinearLayouts)
         layout.setVisibility(View.GONE);


      for (int row = 0; row < guessRows; row++)
         guessLinearLayouts[row].setVisibility(View.VISIBLE);
   }


   public void updateOperators(SharedPreferences sharedPreferences) {
      OperatorSet =
         sharedPreferences.getStringSet(MainActivity.OPERATORS, null);
   }

   public void resetQuiz() {
      int i = 0;

      try {

         EnabledOperators.clear();

         for (String operator : OperatorSet) {
            EnabledOperators.add(operator);
            i++;
         }
      }
      catch (NullPointerException exception) {
         Log.e(TAG, "Null pointer exception while loading operators to array ", exception);
      }

      correctAnswers = 0;
      totalGuesses = 0;

      loadNextQuestion();
   }

   public void loadNextQuestion() {
      List<Integer> guessAnswersList = new ArrayList<Integer>();
     operatorId = random.nextInt(EnabledOperators.size());

      operator = EnabledOperators.get(operatorId);

      if(operator.equals("Addition"))
      {
         firstOperand = random.nextInt(100)+1;
         secondOperand = random.nextInt(100)+1;
         correctAnswer = Integer.toString(firstOperand + secondOperand);
         questionText = "\n" + Integer.toString(firstOperand) + " + " + Integer.toString(secondOperand) ;

      QuestionTextView.setText(questionText);
      }
      else if(operator.equals("Subtraction"))
      {
         firstOperand = random.nextInt(100)+1;
         secondOperand = random.nextInt(firstOperand) + 1;
         correctAnswer = Integer.toString(firstOperand - secondOperand);
         questionText = "\n" + Integer.toString(firstOperand) + " - " + Integer.toString(secondOperand) ;
         QuestionTextView.setText(questionText);
      }
      else if(operator.equals("Multiplication"))
      {
         firstOperand = random.nextInt(25)+1;
         secondOperand = random.nextInt(10)+1;
         correctAnswer = Integer.toString(firstOperand * secondOperand);
         questionText = "\n" + Integer.toString(firstOperand) + " X " + Integer.toString(secondOperand) ;
         QuestionTextView.setText(questionText);
      }
      else {
         List<Integer> listDivisors = new ArrayList<>();
         firstOperand = random.nextInt(300)+1;
         boolean divisorFound = false;
         for(int i=1; i<firstOperand; i++)
            if(firstOperand%i==0)
               listDivisors.add(i);

         secondOperand = listDivisors.get(random.nextInt(listDivisors.size()));
         correctAnswer = Integer.toString(firstOperand/secondOperand);
         questionText = "\n" + Integer.toString(firstOperand) + " / " + Integer.toString(secondOperand) ;
         QuestionTextView.setText(questionText);
      }


      answerTextView.setText("");
      questionNumberTextView.setText(getString(R.string.question, (correctAnswers + 1), TOTAL_QUESTIONS));

      for (int row = 0; row < guessRows; row++) {
         for (int column = 0; column < guessLinearLayouts[row].getChildCount(); column++)
         {
            Button newGuessButton =
                    (Button) guessLinearLayouts[row].getChildAt(column);
            newGuessButton.setEnabled(true);

            while(true) {
               guessAnswer = randomWithRange(Integer.parseInt(correctAnswer)-20, Integer.parseInt(correctAnswer)+20);
               if(guessAnswer>0) {
                  if (guessAnswer != Integer.parseInt(correctAnswer) && !guessAnswersList.contains(guessAnswer)) {
                     guessAnswersList.add(guessAnswer);
                     break;
                  }
               }
            }
            newGuessButton.setText(Integer.toString(guessAnswer));
         }
      }

      int row = random.nextInt(guessRows);
      int column = random.nextInt(2);
      LinearLayout randomRow = guessLinearLayouts[row];
      ((Button) randomRow.getChildAt(column)).setText(correctAnswer);
   }

   private void animate(boolean animateOut) {
      if (correctAnswers == 0)
         return;

      int centerX = (quizLinearLayout.getLeft() +
         quizLinearLayout.getRight()) / 2;
      int centerY = (quizLinearLayout.getTop() +
         quizLinearLayout.getBottom()) / 2;

      int radius = Math.max(quizLinearLayout.getWidth(),
         quizLinearLayout.getHeight());

      Animator animator;

      if (animateOut) {
         animator = ViewAnimationUtils.createCircularReveal(
            quizLinearLayout, centerX, centerY, radius, 0);
         animator.addListener(
            new AnimatorListenerAdapter() {
               // called when the animation finishes
               @Override
               public void onAnimationEnd(Animator animation) {
                  loadNextQuestion();
               }
            }
         );
      }
      else {
         animator = ViewAnimationUtils.createCircularReveal(
            quizLinearLayout, centerX, centerY, 0, radius);
      }

      animator.setDuration(500);
      animator.start();
   }


   private OnClickListener guessButtonListener = new OnClickListener() {
      @Override
      public void onClick(View v) {
         Button guessButton = ((Button) v);
         String guess = guessButton.getText().toString();
         String answer = correctAnswer;
         ++totalGuesses;

         if (guess.equals(answer)) {
            ++correctAnswers;


            answerTextView.setText(answer + "!");
            answerTextView.setTextColor(
               getResources().getColor(R.color.correct_answer,
                  getContext().getTheme()));

            disableButtons();


            if (correctAnswers == TOTAL_QUESTIONS) {

               DialogFragment quizResults = new DialogFragment() {

                     @Override
                     public Dialog onCreateDialog(Bundle bundle) {
                        AlertDialog.Builder builder =
                           new AlertDialog.Builder(getActivity());
                        builder.setMessage(
                           getString(R.string.results,
                              totalGuesses,
                              (1000 / (double) totalGuesses)));

                        builder.setPositiveButton(R.string.reset_quiz,
                           new DialogInterface.OnClickListener() {
                              public void onClick(DialogInterface dialog,
                                 int id) {
                                 resetQuiz();
                              }
                           }
                        );

                        return builder.create();
                     }
                  };


               quizResults.setCancelable(false);
               quizResults.show(getFragmentManager(), "quiz results");
            }
            else {
               handler.postDelayed(
                  new Runnable() {
                     @Override
                     public void run() {
                        animate(true);
                     }
                  }, 2000);
            }
         }
         else {
            QuestionTextView.startAnimation(shakeAnimation);
            answerTextView.setText(R.string.incorrect_answer);
            answerTextView.setTextColor(getResources().getColor(
               R.color.incorrect_answer, getContext().getTheme()));
            guessButton.setEnabled(false);
         }
      }
   };

   private void disableButtons() {
      for (int row = 0; row < guessRows; row++) {
         LinearLayout guessRow = guessLinearLayouts[row];
         for (int i = 0; i < guessRow.getChildCount(); i++)
            guessRow.getChildAt(i).setEnabled(false);
      }
   }

   private int randomWithRange(int firstOperand, int secondOperand)
   {
      int range;
      int min;
      if(firstOperand>secondOperand) {
         min = secondOperand;
         range = (firstOperand - secondOperand) + 1;
      }
      else {
         min = firstOperand;
         range = (secondOperand - firstOperand) + 1;
      }
      return (int)(Math.random() * range) + min;
   }
}
