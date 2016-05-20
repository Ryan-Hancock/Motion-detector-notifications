package activity;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.deraz.SecurityMotionApp.R;

/**
 * Created by deraz on 24/03/2016.
 */
public class MyPreferenceFragment extends PreferenceFragment
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.fragment_preference);
    }
}