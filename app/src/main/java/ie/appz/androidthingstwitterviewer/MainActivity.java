package ie.appz.androidthingstwitterviewer;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;

import com.google.android.things.contrib.driver.apa102.Apa102;
import com.google.android.things.contrib.driver.button.ButtonInputDriver;
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat;
import com.google.android.things.pio.Gpio;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.SearchTimeline;
import com.twitter.sdk.android.tweetui.TimelineResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends Activity {
    private static final Marquee mMarquee = new Marquee();
    private static final String TAG = "MainActivity";
    private static final Handler repeatHandler = new Handler();
    private List<Tweet> searchResultList;
    private int mCurrentTweet;
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            refreshTimeline();
        }
    };
    private ButtonInputDriver[] mInputDriver = new ButtonInputDriver[3];

    private void refreshTimeline() {
        SearchTimeline searchTimeline = new SearchTimeline.Builder()
                .query("#io17")
                .build();
        searchTimeline.next(0L, new Callback<TimelineResult<Tweet>>() {
            @Override
            public void success(Result<TimelineResult<Tweet>> result) {
                Log.d(TAG, "success: " + result.data.items.size());
                searchResultList = new ArrayList<>(result.data.items);

                showTweet(0);


            }

            @Override
            public void failure(TwitterException exception) {
                exception.printStackTrace();

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(getString(R.string.api_key), getString(R.string.api_secret));
        Fabric.with(this, new Twitter(authConfig));

        refreshTimeline();

        ledSetter(-1);

    }

    @Override
    protected void onStart() {
        super.onStart();

        // Click on the buttons can retry network
        try {

            mInputDriver[0] = new ButtonInputDriver(RainbowHat.BUTTON_A,
                    RainbowHat.BUTTON_LOGIC_STATE,
                    KeyEvent.KEYCODE_A);
            mInputDriver[1] = new ButtonInputDriver(RainbowHat.BUTTON_B,
                    RainbowHat.BUTTON_LOGIC_STATE, KeyEvent.KEYCODE_B);
            mInputDriver[2] = new ButtonInputDriver(RainbowHat.BUTTON_C,
                    RainbowHat.BUTTON_LOGIC_STATE, KeyEvent.KEYCODE_C);

            for (ButtonInputDriver buttonInputDriver : mInputDriver) {
                buttonInputDriver.register();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_A:

                try {
                    Gpio redLed = RainbowHat.openLed(RainbowHat.LED_RED);
                    redLed.setValue(true);
                    redLed.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                showTweet(mCurrentTweet - 1);
                return true;

            case KeyEvent.KEYCODE_B:
                try {
                    Gpio greenLed = RainbowHat.openLed(RainbowHat.LED_GREEN);
                    greenLed.setValue(true);
                    greenLed.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                showTweet(mCurrentTweet + 1);
                return true;

            case KeyEvent.KEYCODE_C:
                try {
                    Gpio blueLed = RainbowHat.openLed(RainbowHat.LED_BLUE);
                    blueLed.setValue(true);
                    blueLed.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                refreshTimeline();

                return true;

        }

        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_A:

                try {
                    Gpio redLed = RainbowHat.openLed(RainbowHat.LED_RED);
                    redLed.setValue(false);
                    redLed.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;

            case KeyEvent.KEYCODE_B:
                try {
                    Gpio greenLed = RainbowHat.openLed(RainbowHat.LED_GREEN);
                    greenLed.setValue(false);
                    greenLed.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;

            case KeyEvent.KEYCODE_C:
                try {
                    Gpio blueLed = RainbowHat.openLed(RainbowHat.LED_BLUE);
                    blueLed.setValue(false);
                    blueLed.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                refreshTimeline();
                return true;

        }

        return false;
    }


    @Override
    protected void onStop() {
        super.onStop();

        for (ButtonInputDriver buttonInputDriver : mInputDriver) {
            buttonInputDriver.unregister();
            try {
                buttonInputDriver.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void showTweet(int i) {
        if (searchResultList == null || searchResultList.size() < i)
            return;

        if (i > RainbowHat.LEDSTRIP_LENGTH || i < 0) {
            i = 0;
        }

        mCurrentTweet = i;

        mMarquee.displayText("@" + searchResultList.get(mCurrentTweet).user.screenName + "- " + searchResultList.get(mCurrentTweet).text);
        ledSetter(mCurrentTweet);
    }

    void ledSetter(int position) {

        try {
            Apa102 ledStrip = RainbowHat.openLedStrip();

            ledStrip.setBrightness((int) (Apa102.MAX_BRIGHTNESS / 5f));
            int[] rainbow = new int[RainbowHat.LEDSTRIP_LENGTH];
            for (int i = 0; i < rainbow.length; i++) {
                if (i == (RainbowHat.LEDSTRIP_LENGTH - 1) - position) {
                    rainbow[i] = Color.BLUE;
                } else {
                    rainbow[i] = Color.BLACK;
                }
            }
            ledStrip.write(rainbow);
            ledStrip.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
