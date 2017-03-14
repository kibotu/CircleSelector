package net.kibotu.circleselector.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import net.kibotu.urlshortener.UrlShortener;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UrlShortener.setEnableLogging(true);

        UrlShortener.shortenUrlByTinyUrl("http://www.google.com")
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(r -> {
                    Log.v(TAG, "[onCreate] shortenUrlByTinyUrl " + r);
                }, Throwable::printStackTrace);

        UrlShortener.shortenUrlByGoogle(this, "http://www.google.com")
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(r -> {
                    Log.v(TAG, "[onCreate] shortenUrlByGoogle " + r);
                }, Throwable::printStackTrace);
    }
}