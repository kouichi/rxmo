package jp.aknot.rxmo;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer);
            switch (item.getItemId()) {
                case R.id.navi_item_chapter1_list11:
                    executeChapter1List11();
                    drawer.closeDrawer(Gravity.LEFT);
                    return true;
                default:
                    return false;
            }
        });
    }

    private void executeChapter1List11() {
        // 挨拶の言葉を通知する Flowable
        Flowable<String> flowable = Flowable.create(new FlowableOnSubscribe<String>() {
            @Override
            public void subscribe(FlowableEmitter<String> emitter) throws Exception {
                String[] values = {"Hello, world!", "こんにちは、世界！"};
                for (String value : values) {
                    // 購読解除されている場合は処理を止める
                    // RxJava2 では、通知は行わないが、処理を続けるかどうかは実装者が決める
                    if (emitter.isCancelled()) {
                        return;
                    }
                    // データを通知する
                    emitter.onNext(value);
                }
                // 完了したことを通知する
                emitter.onComplete();
            }
        }, BackpressureStrategy.BUFFER); // 超過したデータはバッファする

        flowable
                // Subscriber の処理を別スレッドで行うようにする
                .observeOn(Schedulers.computation())
                // 購読する
                .subscribe(new Subscriber<String>() {

                    // データ数のリクエストおよび購読の解除を行うオブジェクト
                    private Subscription subscription;

                    @Override
                    public void onSubscribe(Subscription subscription) {
                        Log.d("Subscriber", "onSubscribe: UiThread=" + Utils.isUiThread());

                        this.subscription = subscription;
                        // 受け取るデータ数をリクエストする
//                        this.subscription.request(1L);
                        this.subscription.request(Long.MAX_VALUE); // 制限なし(onNextでデータ数のリクエストは不要)
                    }

                    @Override
                    public void onNext(String value) {
                        String threadName = Thread.currentThread().getName();
                        Log.d("Subscriber", threadName + ": " + value);

                        // 次に受け取るデータ数をリクエストする
//                        this.subscription.request(1L);
                    }

                    @Override
                    public void onError(Throwable error) {
                        String threadName = Thread.currentThread().getName();
                        Log.d("Subscriber", threadName + ": エラーが発生しました", error);
                    }

                    @Override
                    public void onComplete() {
                        String threadName = Thread.currentThread().getName();
                        Log.d("Subscriber", threadName + ": 完了しました");
                    }
                });
    }
}
