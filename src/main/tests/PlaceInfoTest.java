package com.smusing.variations.variations.tests;

import android.app.Instrumentation;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.test.ViewAsserts;
import android.test.suitebuilder.annotation.MediumTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;

import com.smusing.variations.variations.PlaceInfo;
import com.smusing.variations.variations.PlaceMaps;
import com.smusing.variations.variations.R;

public class PlaceInfoTest
        extends ActivityInstrumentationTestCase2<PlaceInfo> {

    private PlaceInfo mPlaceInfo;
    private ListView mPlaceInfoList;
    private Button mPlaceInfoButton;
    private static final int TIMEOUT_IN_MS = 5000;

    public PlaceInfoTest(){
        super(PlaceInfo.class);
    }

    @Override
    public void setUp(){
        setActivityInitialTouchMode(true);
        Intent intent=new Intent(getInstrumentation().getTargetContext(),
                PlaceInfo.class);

        mPlaceInfo=getActivity();
        mPlaceInfoButton=
                (Button)mPlaceInfo.findViewById(R.id.map_button);
        mPlaceInfoList=
                (ListView)mPlaceInfo.findViewById(R.id.display_name);
    }


    //make sure everythign is working and there
    public void testPreconditions(){
        assertNotNull("mPlaiceInfo is null", mPlaceInfo);
        assertNotNull("mPlaiceInfoButton is null", mPlaceInfoButton);
        assertNotNull("mPlaiceInfoList is null", mPlaceInfoList);
    }

    @MediumTest
    public void verfiyButtonPlacement(){
        final View decorView=mPlaceInfo.getWindow().getDecorView();

        ViewAsserts.assertOnScreen(decorView, mPlaceInfoButton);

        final ViewGroup.LayoutParams layoutParams=
                mPlaceInfoButton.getLayoutParams();
        assertNotNull(layoutParams);
        assertEquals(layoutParams.width, WindowManager.LayoutParams.MATCH_PARENT);
        assertEquals(layoutParams.height, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    @MediumTest
    public void verifyListViewPlacement(){
        final View decorView=mPlaceInfo.getWindow().getDecorView();

        ViewAsserts.assertOnScreen(decorView, mPlaceInfoList);

        final ViewGroup.LayoutParams layoutParams=
                mPlaceInfoList.getLayoutParams();
        assertNotNull(layoutParams);
        assertEquals(layoutParams.width, WindowManager.LayoutParams.MATCH_PARENT);
        assertEquals(layoutParams.height, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    @MediumTest
    public void testSendIntenttoMap(){
        Instrumentation.ActivityMonitor receiverAM=
                getInstrumentation().addMonitor(PlaceMaps.class.getName(),
                        null, false);

        TouchUtils.clickView(this, mPlaceInfoButton);
        PlaceMaps placeMaps=(PlaceMaps)
                receiverAM.waitForActivityWithTimeout(TIMEOUT_IN_MS);
        assertNotNull("PlaceMaps us null", placeMaps);
        assertEquals("Monitor for placeMaps has not been called",
                1, receiverAM.getHits());
        assertEquals("Activity is wrong",
                PlaceMaps.class, placeMaps.getClass());

        //remove the activity monitor
        getInstrumentation().removeMonitor(receiverAM);
    }
}
