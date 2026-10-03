package com.example.andr2project;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class InstrumentedTest {
    @Rule
    public ActivityTestRule<MapsActivity> activityTestRule = new ActivityTestRule<>(MapsActivity.class);

    Context context;
    @Before
    public void setup(){
        context = activityTestRule.getActivity().getApplicationContext();
    }

    @Test
    public void testNotification(){
        //Arrange
        double latitude = 55.5;
        double longitude = 5.5;
        String name = "test";
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        //Act
        activityTestRule.getActivity().makeNotification(latitude, longitude, name);
        Notification notification = manager.getActiveNotifications()[0].getNotification();

        //Assert
        assertEquals("Found " + name, notification.extras.get(Notification.EXTRA_TITLE));
        assertEquals("You ran into " + name + "at: " + latitude + ";" + longitude, notification.extras.get(Notification.EXTRA_TEXT));
    }
}