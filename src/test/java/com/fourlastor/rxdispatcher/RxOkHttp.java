package com.fourlastor.rxdispatcher;

import com.squareup.okhttp.*;
import rx.Observable;
import rx.Subscriber;

import java.io.IOException;

/**
 * Thanks to Paul Betts
 *
 * @link {https://gist.github.com/paulcbetts/2274581f24ded7502011}
 */

public class RxOkHttp {
    public static Observable<Response> request(final OkHttpClient client, final Request request) {
        return Observable.create(new Observable.OnSubscribe<Response>() {
            @Override
            public void call(final Subscriber<? super Response> subj) {
                final Call call = client.newCall(request);

                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        subj.onError(e);
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        subj.onNext(response);
                        subj.onCompleted();
                    }
                });
            }
        });
    }
}
