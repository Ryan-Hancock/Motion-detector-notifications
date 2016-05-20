package activity;

import android.preference.PreferenceActivity;

import com.example.deraz.motiontest.R;

import java.util.List;

/**
 * Created by deraz on 24/03/2016.
 */
public class MyPrefenceActivity extends PreferenceActivity
{
    @Override
    public void onBuildHeaders(List<Header> target)
    {
        loadHeadersFromResource(R.xml.headers_preference, target);
    }

    @Override
    protected boolean isValidFragment(String fragmentName)
    {
        return MyPreferenceFragment.class.getName().equals(fragmentName);
    }
}
