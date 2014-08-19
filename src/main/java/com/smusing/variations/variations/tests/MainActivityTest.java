package com.smusing.variations.variations.tests;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.test.ViewAsserts;
import android.test.suitebuilder.annotation.MediumTest;
import android.view.View;
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
        super.setUp();

        setActivityInitialTouchMode(true);

        Intent intent=new Intent(getInstrumentation().getTargetContext(),
                MainActivity.class);

        mMainActivity=getActivity();
        mMainActivityListView=
                (ListView) mMainActivity.findViewById(R.id.display_messages);

    }

    //make sure everythign is working and there
    public void testPreconditions(){
        assertNotNull("mMainActivity is null", mMainActivity);
        assertNotNull("mMainActiivityListVIew is null", mMainActivityListView);
    }


    //checks ot make sure the ListView is there
    @MediumTest
    public void isListThere(){
        final View listView=mMainActivity.getWindow().getDecorView();
        ViewAsserts.assertOnScreen(listView, mMainActivityListView);
        assertTrue(View.GONE==mMainActivityListView.getVisibility());
    }

}
