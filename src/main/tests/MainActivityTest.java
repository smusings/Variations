package com.smusing.variations.variations.tests;

import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.test.ViewAsserts;
import android.test.suitebuilder.annotation.MediumTest;
import android.view.View;
import android.widget.ListView;

import com.smusing.variations.variations.MainActivity;
import com.smusing.variations.variations.PlaceInfo;
import com.smusing.variations.variations.R;

public class MainActivityTest
        extends ActivityInstrumentationTestCase2<MainActivity> {

    //set up for all we need
    private MainActivity mMainActivity;
    private ListView mMainActivityListView;
    private static final int TIMEOUT_IN_MS = 5000;

    public MainActivityTest(){
        super(MainActivity.class);
    }


    @Override
    protected void setUp() throws Exception{
        super.setUp();
        //sets up touch mode for clicks
        setActivityInitialTouchMode(true);

        //defines some items from setup
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

    /*
        currently fails, but i think that is because it needs a better
        command to click on specific listviews items
     */
    @MediumTest
    public void testSendMessage(){
        //sets up activity monitor
        Instrumentation.ActivityMonitor receiverAM=
                getInstrumentation().addMonitor(PlaceInfo.class.getName(),null, false);

        //validate receiver is starting
        TouchUtils.clickView(this, mMainActivityListView);
        PlaceInfo receiverActivity=(PlaceInfo)
                receiverAM.waitForActivityWithTimeout(TIMEOUT_IN_MS);
        assertNotNull("Receiver activity is null", receiverActivity);
        assertEquals("Monitor for the RA is not called", 1, receiverAM.getHits());
        assertEquals("Activity is of wrong type", PlaceInfo.class, receiverActivity.getClass());

        //remove activity monitor
        getInstrumentation().removeMonitor(receiverAM);

    }


}
