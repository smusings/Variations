package com.smusing.variations.variations.tests;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;

import com.smusing.variations.variations.MainActivity;
import com.smusing.variations.variations.R;

public class MainActivityTest
        extends ActivityInstrumentationTestCase2<MainActivity> {

    private MainActivity mMainActivity;
    private ListView mMainActivityListView;

    public MainActivityTest(){
        super(MainActivity.class);
    }


    @Override
    protected void setUp() throws Exception{
        mMainActivity=getActivity();
        mMainActivityListView=
                (ListView) mMainActivity.findViewById(R.id.display_messages);

    }

    //make sure everythign is working and there
    public void testPreconditions(){
        assertNotNull("mMainActivity is null", mMainActivity);
        assertNotNull("mMainActiivityListVIew is null", mMainActivityListView);
    }
}
