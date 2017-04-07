// CountingMadeFunView.java
// View that displays and manages the game
package com.mdhawa3.countingmadefun;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Queue;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.os.CountDownTimer;


public class CountingMadeFunView extends View
{
   // variables for managing the game
   private int level; // current level
   private int viewWidth; // stores the width of this View
   private int viewHeight; // stores the height of this view
   private long animationTime; // how long each image number remains on the screen
   private boolean gameOver; // whether the game has ended
   private boolean gamePaused; // whether the game has ended
   private boolean dialogDisplayed; // whether the game has ended

   // collections of numberImages (ImageViews) and Animators
   private final Queue<ImageView> numberImages =
      new ConcurrentLinkedQueue<ImageView>(); 
   private final Queue<Animator> animators = 
      new ConcurrentLinkedQueue<Animator>();

//Mrinal Start
   private Queue<String> fileNameListImages = new ConcurrentLinkedQueue<String>();
   private Queue<String> fileNameListSounds = new ConcurrentLinkedQueue<String>();
   private AssetFileDescriptor afd;
   private MediaPlayer mediaPlayer;
   private TextView timeRemainingTextView; // displays time remaining for current level
   private TextView timeElapsedTextView; // displays total time elapsed for current play
   private TextView levelTextView; // displays current level
   private RelativeLayout relativeLayout; // displays numberImages
   private Resources resources; // used to load resources
   private LayoutInflater layoutInflater; // used to inflate GUIs
   private CountDownTimer objTimer;
   private Boolean gameRunning;
   private int totalElapsedTime=0;

   // time in milliseconds for number and touched number animations
   private static final int INITIAL_ANIMATION_DURATION = 60000;
   private static final Random random = new Random(); // for random coords
   private static final int IMAGE_DIAMETER = 150; // initial image size
   private static final int IMAGE_DELAY = 1000; // delay in milliseconds
   private Handler numberHandler; // adds new numberImages to the game

   // sound IDs, constants and variables for the game's sounds
   //private static final int HIT_SOUND_ID = 1;
   private static final int MISS_SOUND_ID = 1;
   private static final int DISAPPEAR_SOUND_ID = 2;
   private static final int UHOH_SOUND_ID = 3;
   private static final int APPLAUSE_SOUND_ID = 4;
   private static final int SOUND_PRIORITY = 1;
   private static final int MAX_STREAMS = 5;
   private SoundPool soundPool; // plays sound effects
   private int volume; // sound effect volume
   private Map<Integer, Integer> soundMap; // maps ID to soundpool
   
   // constructs a new CountingMadeFunView
   public CountingMadeFunView(Context context, SharedPreferences sharedPreferences,
                              RelativeLayout parentLayout)
   {
      super(context);

      // save Resources for loading external values
      resources = context.getResources();

      // save LayoutInflater
      layoutInflater = (LayoutInflater) context.getSystemService(
         Context.LAYOUT_INFLATER_SERVICE);

      // get references to various GUI components
      relativeLayout = parentLayout;
      //livesLinearLayout = (LinearLayout) relativeLayout.findViewById(
         //R.id.lifeLinearLayout);
      timeRemainingTextView = (TextView) relativeLayout.findViewById(
         R.id.timeRemainingTextView);
      timeElapsedTextView = (TextView) relativeLayout.findViewById(
         R.id.timeElapsedTextView);
      levelTextView = (TextView) relativeLayout.findViewById(
         R.id.levelTextView);

      numberHandler = new Handler(); // used to add numberImages when game starts
   } // end CountingMadeFunView constructor

   // store CountingMadeFunView's width/height
   @Override
   protected void onSizeChanged(int width, int height, int oldw, int oldh)
   {
      viewWidth = width; // save the new width
      viewHeight = height; // save the new height
   } // end method onSizeChanged

   // called by the CountingMadeFun Activity when it receives a call to onPause
   public void pause()
   {
      gamePaused = true;
      soundPool.release(); // release audio resources
      soundPool = null;
      cancelAnimations(); // cancel all outstanding animations
   } // end method pause

   // cancel animations and remove ImageViews representing numberImages
   private void cancelAnimations()
   {
      // cancel remaining animations
      for (Animator animator : animators)
         animator.cancel();

      // remove remaining numberImages from the screen
      for (ImageView view : numberImages)
         relativeLayout.removeView(view);

      numberHandler.removeCallbacks(addImageNumberRunnable);
      animators.clear();
      numberImages.clear();
   } // end method cancelAnimations
   
   // called by the CountingMadeFun Activity when it receives a call to onResume
   public void resume(Context context)
   {
      gamePaused = false;
      initializeSoundEffects(context); // initialize app's SoundPool

      if (!dialogDisplayed)
         resetGame(); // start the game
   } // end method resume

   // start a new game
   public void resetGame()
   {
      numberImages.clear(); // empty the List of numberImages
      animators.clear(); // empty the List of Animators
      //livesLinearLayout.removeAllViews(); // clear old lives from screen

      totalElapsedTime=0;
      animationTime = INITIAL_ANIMATION_DURATION; // init animation length
      level = 1; // reset the level
      gameOver = false; // the game is not over
      displayLevel(); // display level

      loadGameSettingsBasedOnLevel();
   } // end method resetGame

   public void loadGameSettingsBasedOnLevel()
   {
      layoutInflater.inflate(R.layout.untouched, null);
      // use AssetManager to get image file names for enabled regions

      fileNameListImages.clear(); // empty list of image file names
      fileNameListSounds.clear();
      List<String> tempFileNamesImages = new ArrayList<String>();
      List<String> tempFileNamesSounds = new ArrayList<String>();
      if(level<=4)
      {
         switch (level) {
            case 1: {
               tempFileNamesImages.addAll(LoadImagesFromAsset(getResources().getString(R.string.level1)));
               tempFileNamesSounds.addAll(LoadSoundsFromAsset(getResources().getString(R.string.level1)));
               animationTime = 60000;
               break;
            }
            case 2: {
               tempFileNamesImages.addAll(LoadImagesFromAsset(getResources().getString(R.string.level2)));
               tempFileNamesSounds.addAll(LoadSoundsFromAsset(getResources().getString(R.string.level2)));
               animationTime = 55000;
               break;
            }
            case 3: {
               tempFileNamesImages.addAll(LoadImagesFromAsset(getResources().getString(R.string.level3)));
               tempFileNamesSounds.addAll(LoadSoundsFromAsset(getResources().getString(R.string.level3)));
               animationTime = 50000;
               break;
            }
            case 4: {
               tempFileNamesImages.addAll(LoadImagesFromAsset(getResources().getString(R.string.level1)));
               tempFileNamesSounds.addAll(LoadSoundsFromAsset(getResources().getString(R.string.level1)));

               tempFileNamesImages.addAll(LoadImagesFromAsset(getResources().getString(R.string.level2)));
               tempFileNamesSounds.addAll(LoadSoundsFromAsset(getResources().getString(R.string.level2)));

               tempFileNamesImages.addAll(LoadImagesFromAsset(getResources().getString(R.string.level3)));
               tempFileNamesSounds.addAll(LoadSoundsFromAsset(getResources().getString(R.string.level3)));
               animationTime = 45000;
               break;
            }
         }

      if(level==4)
      {
         int countRemoved=0;
         for(int i=0; i<20; i++)
         {
            int indexToBeRemoved = new Random().nextInt(30-countRemoved);
            tempFileNamesImages.remove(indexToBeRemoved);
            tempFileNamesSounds.remove(indexToBeRemoved);
            countRemoved++;
         }
      }

         //Collections.sort(tempFileNames);

         for (String file: tempFileNamesImages) {
            fileNameListImages.add(file);
         }
         for (String file: tempFileNamesSounds) {
            fileNameListSounds.add(file);
         }

         for (int i = 1; i <= fileNameListImages.size(); ++i)
            numberHandler.postDelayed(addImageNumberRunnable, IMAGE_DELAY);

            gameRunning=true;

      if (gameRunning) {
         objTimer = new CountDownTimer(animationTime+ IMAGE_DELAY, 1000) {
            public void onTick(long millisUntilFinished) {
               timeRemainingTextView.setText(getResources().getString(R.string.time_remaining) + millisUntilFinished / 1000);
               totalElapsedTime++;
               timeElapsedTextView.setText(getResources().getString(R.string.time_elapsed) + totalElapsedTime);
            }

            public void onFinish() {
            }
         }.start();
         }
      }

      if(gameOver)
      {
         resetGame();
      }
   }

   private List<String> LoadImagesFromAsset(String level)
   {
      try {
         List<String> tempFileNamesImages = new ArrayList<String>();
         String paths[] = null;
         AssetManager assets = getContext().getAssets();
         paths = assets.list(
                 level + "/"
                         + getResources().getString(R.string.images));
         for (String path : paths)
            tempFileNamesImages.add(
                    level + "/"
                            + getResources().getString(R.string.images) + "/" + path);
         return tempFileNamesImages;
      }
      catch(IOException ex)
      {
         return null;
      }
   }

   private List<String> LoadSoundsFromAsset(String level)
   {
      try {
         List<String> tempFileNamesSounds = new ArrayList<String>();
         String paths[] = null;
         AssetManager assets = getContext().getAssets();
         paths = assets.list(
                 level + "/"
                         + getResources().getString(R.string.sounds));
         for (String path : paths)
            tempFileNamesSounds.add(
                    level + "/"
                            + getResources().getString(R.string.sounds) + "/" + path);
         return tempFileNamesSounds;
      }
      catch(IOException ex)
      {
         return null;
      }
   }


   // create the app's SoundPool for playing game audio
   private void initializeSoundEffects(Context context)
   {
      // initialize SoundPool to play the app's three sound effects
      soundPool = new SoundPool.Builder()
                 .setMaxStreams(MAX_STREAMS)
                 .build();
      // set sound effect volume
      AudioManager manager =
         (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
      volume = manager.getStreamVolume(AudioManager.STREAM_MUSIC);
     // create sound map
      soundMap = new HashMap<Integer, Integer>(); // create new HashMap

      // add each sound effect to the SoundPool
      soundMap.put(MISS_SOUND_ID,
         soundPool.load(context, R.raw.miss, SOUND_PRIORITY));
      soundMap.put(DISAPPEAR_SOUND_ID,
         soundPool.load(context, R.raw.disappear, SOUND_PRIORITY));
      soundMap.put(UHOH_SOUND_ID,
              soundPool.load(context, R.raw.uhoh, SOUND_PRIORITY));
      soundMap.put(APPLAUSE_SOUND_ID,
              soundPool.load(context, R.raw.applause, SOUND_PRIORITY));
   } // end method initializeSoundEffect

   // display level
   private void displayLevel()
   {
      levelTextView.setText(
         resources.getString(R.string.level) + " " + level);
   } // end function displayLevel

   // Runnable used to add new numberImages to the game at the start
   private Runnable addImageNumberRunnable = new Runnable()
   {
      public void run()
      {
         addNewNumber(); // add a new image number to the game
      } // end method run
   }; // end Runnable

   // adds a new number at a random location and starts its animation
   public void addNewNumber()
   {
      // choose two random coordinates for the starting and ending points
      int x = random.nextInt(viewWidth - IMAGE_DIAMETER);
      int y = random.nextInt(viewHeight - IMAGE_DIAMETER);
      int x2 = random.nextInt(viewWidth - IMAGE_DIAMETER);
      int y2 = random.nextInt(viewHeight - IMAGE_DIAMETER);

      // create new number
      final ImageView number =
         (ImageView) layoutInflater.inflate(R.layout.untouched, null);
      numberImages.add(number); // add the new number to our list of numberImages
      number.setLayoutParams(new RelativeLayout.LayoutParams(
              IMAGE_DIAMETER, IMAGE_DIAMETER));

      AssetManager assets = getContext().getAssets();
      // load image
      try {
         // get input stream
         InputStream ims = assets.open(fileNameListImages.remove());
         // load image as Drawable
         Drawable d = Drawable.createFromStream(ims, null);
         // set image to ImageView
         number.setImageDrawable(d);
      }
      catch(IOException ex) {
         return;
      }

      number.setX(x); // set number's starting x location
      number.setY(y); // set number's starting y location
      number.setOnClickListener( // listens for number being clicked
         new OnClickListener()
         {            
            public void onClick(View v)
            {
               touchedNumber(number); // handle touched number
            } // end method onClick
         } // end OnClickListener 
      ); // end call to setOnClickListener 
      relativeLayout.addView(number); // add number to the screen

      // configure and start number's animation
      number.animate().x(x2).y(y2)
         .setDuration(animationTime).setListener(
            new AnimatorListenerAdapter()
            {
               @Override
               public void onAnimationStart(Animator animation)
               {
                  animators.add(animation); // save for possible cancel
               } // end method onAnimationStart

               public void onAnimationEnd(Animator animation)
               {
                  animators.remove(animation); // animation done, remove
                  
                  if (!gamePaused && numberImages.contains(number)) // not touched
                  {
                     missedNumber(number); // lose a life
                  } // end if
               } // end method onAnimationEnd
            } // end AnimatorListenerAdapter
         ); // end call to setListener
   } // end addNewNumber method

   // called when the user touches the screen, but not a number image
   @Override
   public boolean onTouchEvent(MotionEvent event)
   {
      // play the missed sound
      if (soundPool != null)
         soundPool.play(soundMap.get(MISS_SOUND_ID), volume, volume,
            SOUND_PRIORITY, 1, 1f);

      displayLevel(); // update level on screen
      return true;
   } // end method onTouchEvent




   // called when a number image is touched
   private void touchedNumber(ImageView number)
   {
      if(numberImages.peek().equals(number)) {
         relativeLayout.removeView(number);     // remove touched number from screen
         numberImages.remove(number);
         try {
            if(mediaPlayer!=null)
               mediaPlayer.reset();
            afd = getContext().getAssets().openFd(fileNameListSounds.remove());
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
            mediaPlayer.prepare();
            mediaPlayer.start();
         }
         catch(IOException ex)
         {
            return;
         }
      }
      else
      {
         // play the uhoh sounds
         if (soundPool != null)
            soundPool.play(soundMap.get(UHOH_SOUND_ID), volume, volume,
                    SOUND_PRIORITY, 0, 1f);
      }

      // increment level if player touched 10 numberImages in the current level
      if (numberImages.peek()==null)
      {
         objTimer.cancel();
         gameRunning = false;
         ++level; // increment the level
         if(level<5) {
            loadGameSettingsBasedOnLevel();// make game 5% faster than prior level
            displayLevel(); // update level on the screen
         }
         else {
            gameOver = true;
            soundPool.play(soundMap.get(APPLAUSE_SOUND_ID), volume, volume,
                    SOUND_PRIORITY, 0, 1f);
         }
      } // end if

      if(gameOver) {
         gameOver();
      }
   } // end method touchedNumber

   // called when a number finishes its animation without being touched
   public void missedNumber(ImageView number) {
      numberImages.remove(number); // remove number from numberImages List
      relativeLayout.removeView(number); // remove number from screen

      if (gameOver) // if the game is already over, exit
         return;

      // play the disappear sound effect
      if (soundPool != null)
         soundPool.play(soundMap.get(DISAPPEAR_SOUND_ID), volume, volume,
                 SOUND_PRIORITY, 0, 1f);

      gameOver = true;
      gameOver();
   } // end method missedNumber

   private void gameOver()
   {
      cancelAnimations();
      // display reset dialog with total elapsed time
      Builder dialogBuilder = new AlertDialog.Builder(getContext());
      dialogBuilder.setTitle(R.string.game_over);
      dialogBuilder.setMessage(resources.getString(R.string.time_elapsed) +
              " " + totalElapsedTime);
      dialogBuilder.setPositiveButton(R.string.reset_game,
              new DialogInterface.OnClickListener()
              {
                 public void onClick(DialogInterface dialog, int which)
                 {
                    displayLevel(); // ensure that level is up to date
                    dialogDisplayed = false;
                    resetGame(); // start a new game
                 } // end method onClick
              } // end DialogInterface
      ); // end call to dialogBuilder.setPositiveButton
      dialogDisplayed = true;
      dialogBuilder.show(); // display the reset game dialog
   }
} // end class CountingMadeFunView

