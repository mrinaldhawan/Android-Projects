package com.mdhawa3.ArithmeticQuiz;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SettingsActivityFragment extends PreferenceFragment {
   @Override
   public void onCreate(Bundle bundle) {
      super.onCreate(bundle);
      addPreferencesFromResource(R.xml.preferences);
   }
}
