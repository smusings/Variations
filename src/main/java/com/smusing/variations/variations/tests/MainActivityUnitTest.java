package com.smusing.variations.variations.tests;

import android.content.Intent;
import android.test.ActivityUnitTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.widget.ListView;

import com.smusing.variations.variations.MainActivity;
import com.smusing.variations.variations.PlaceInfo;
import com.smusing.variations.variations.R;

public class MainActivityUnitTest extends ActivityUnitTestCase<MainActivity> {


    private MainActivity mMainActivity;
    private ListView mMainActivityListView;
    private Intent mInfoIntent;

    public MainActivityUnitTest(){
        super(MainActivity.class);
    }


    @Override
    protected void setUp() throws Exception{
        super.setUp();

        mInfoIntent=new Intent(getInstrumentation()
                .getTargetContext(), MainActivity.class);
        startActivity(mInfoIntent, null, null);

        mMainActivity=getActivity();
        mMainActivityListView=
                (ListView) mMainActivity.findViewById(R.id.display_messages);
    }


    @MediumTest
    public void testNextActivityIsLaunchedWithIntent(){

        mMainActivityListView.performClick();


        mInfoIntent=getStartedActivityIntent();
        assertNotNull("Intent was null", mInfoIntent);
        assertTrue(isFinishCalled());

        final String extra_message=
                mInfoIntent.getStringExtra(PlaceInfo.EXTRA_MESSAGE);
        assertEquals("extra_message is empty", MainActivity.EXTRA_MESSAGE, extra_message);
    }
    /*
        keep getting intent=null test failure
        could be due to the nature of the array
        could be because i wrote code wrong
        could also because it doesnt know
        which listview to click so it clicks on all?

     */




}
