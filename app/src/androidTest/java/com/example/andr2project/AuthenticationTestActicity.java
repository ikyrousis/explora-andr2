package com.example.andr2project;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AuthenticationTestActicity {

    //You have to change the following credentials to something new every time you would like to run the test
    //If the following credentials are not changed, there will be an error due to an already existing username or email in the database
    private static final String USERNAME = "usertest10";
    private static final String EMAIL = "usertest10@gmail.com";
    private static final String PASSWORD = "usertest10";

    //IMPORTANT! In order to run the following test, within RegisterActivity, you need to comment getCurrentLocation() at onCreate (line 111)
    @Test
    public void _1registerTest(){
        ActivityScenario<MainActivity> activityScenario = ActivityScenario.launch(MainActivity.class);

        //Delay for 3 seconds
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //Click register
        onView(withId(R.id.register)).perform(click());
        //Fill the username field
        onView(withId(R.id.nameId)).perform(typeText(USERNAME), closeSoftKeyboard());
        //Fill the email field
        onView(withId(R.id.emailId)).perform(
                typeText(EMAIL),
                closeSoftKeyboard()
        );
        //Fill the password field
        onView(withId(R.id.passwordId)).perform(
                typeText(PASSWORD),
                closeSoftKeyboard()
        );

        //Click register
        onView(withId(R.id.registeruserbtn)).perform(click());

        //Delay for 5 seconds
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //If successful navigate to home
        onView(withText("Home")).check(matches(isDisplayed()));

        activityScenario.close();
    }

    //the screen should be on HomeFragment and user is already login
    @Test
    public void _2logoutUser(){
        ActivityScenario<MainActivity> activityScenario = ActivityScenario.launch(MainActivity.class);

        //delay 4 seconds
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //click menu hamburger
        onView(withId(R.id.menu_hamburgerBB)).perform(click());

        //click logout
        onView(withId(R.id.nav_view))
                .perform(NavigationViewActions.navigateTo(R.id.logoutId));
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //check if success logout go to login page
        onView(withText("ANDR2Project")).check(matches(isDisplayed()));
        activityScenario.close();

    }

    //the activity should be on MainActivity and user is not login yet
    @Test
    public void _3loginUser(){
        ActivityScenario<MainActivity> activityScenario = ActivityScenario.launch(MainActivity.class);

        //fill email
        onView(withId(R.id.emailId)).perform(
                typeText(EMAIL),
                closeSoftKeyboard()
        );
        //fill password
        onView(withId(R.id.passwordId)).perform(
                typeText(PASSWORD),
                closeSoftKeyboard()
        );

        //click login
        onView(withId(R.id.loginbtn)).perform(click());

        //delay 5 seconds
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //check if success go to home
        onView(withText("Home")).check(matches(isDisplayed()));

        activityScenario.close();

    }

}
