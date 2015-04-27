package kr.nectarine.android.fruitygcm.rxjava;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Func1;

/**
 * http://stackoverflow.com/a/25292833
 */
public class RetryWithDelay implements Func1<Observable<? extends Throwable>, Observable<?>> {

    private final int maxRetries;
    private final int retryDelay;
    private final TimeUnit timeUnit;
    private int retryCount;

    public RetryWithDelay(int maxRetries, int retryDelay, TimeUnit timeUnit) {
        this.maxRetries = maxRetries;
        this.retryDelay = retryDelay;
        this.timeUnit = timeUnit;
        this.retryCount = 0;
    }


    @Override
    public Observable<?> call(Observable<? extends Throwable> observable) {
        return observable
                .flatMap(new Func1<Throwable, Observable<?>>() {
                    @Override
                    public Observable<?> call(Throwable throwable) {
                        if (++retryCount < maxRetries) {
                            // When this Observable calls onNext, the original
                            // Observable will be retried (i.e. re-subscribed).
                            return Observable.timer(retryDelay, timeUnit);
                        }
                        // Max retries hit. Just pass the error along.
                        return Observable.error(throwable);
                    }
                });
    }
}
