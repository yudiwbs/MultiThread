package edu.upi.cs.yudiwbs.cobamultithread2;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;


// tanpa viewmodel saat dirotate maka update tidak dapat dilakukan
// walau thread masih berjalan, view model harus digunakan
public class MainActivity extends AppCompatActivity {

    TextView tvHasil;
    private  HasilViewModel model;
    private FutureTask ft;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvHasil = findViewById(R.id.tvHasil);
        model = new ViewModelProvider(this).get(HasilViewModel.class);

        //observer agar bisa update otomatis saat data di model berubah
        final Observer<String> observerPesan = new Observer<String>() {
            @Override
            public void onChanged(@Nullable final String pesan) {
                tvHasil.setText(pesan);
            }
        };

        //daftarkan observer ke model, akan dipanggil jika data di model berubah
        model.getData().observe(this,observerPesan);

    }

    public void onClickCancel(View v) {
        //ambil dari viewmodel karena saat di rotate
        //referensi ke future hilang (thread tetap jalan tapi tdk bisa distop)
        ft = model.ft;
        ft.cancel(true);  //cancel
    }

    public void onClickMulai(View v) {
        Log.d("dyw","Mulai");
        tvHasil.setText("Mulai..");
        //hanya single thread dalam satu waktu
        ExecutorService executor = Executors.newSingleThreadExecutor();

        //harus di oncreate biar saat diroatet tidak hilang
        ft = new FutureTask<String>(new Runnable() {
            @Override
            public void run() {
                Log.d("dyw","Masuk thread");

                //ambil main atau UI thread
                Handler handler = new Handler(Looper.getMainLooper())  {
                    @Override
                    public void handleMessage(Message inputMessage) {
                        //update model --> UI juga akan terupdate
                        //jika tdk menggunakan viewmodel saat dirotate
                        //maka UI tidak akan bisa udpate walau thread
                        // tetap jalan
                        model.setPesan((String)(inputMessage.obj));
                    }
                };

                boolean isStop = false; //jika di cancel, stop
                for (int i=0;i<=10;i++) {
                    try {
                        if (isStop) {
                            Log.d("dyw","Stop, keluar dari loop");
                            break;}
                        // log untuk memudahkan debug
                        Log.d("dyw", String.valueOf(i));
                        Thread.sleep(1000);

                        //update UI untuk progress
                        //jenggunakan sendMessage
                        //lihat method handledMessage di atas
                        Message myMessage = handler.obtainMessage();
                        myMessage.obj = String.valueOf(i);
                        handler.sendMessage(myMessage);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Log.d("dyw","interrupt! thread di-cancel");
                        isStop = true;
                    }
                }
                //selesai, kirim pesan ke UI
                String pesan = "Sukses";
                if (isStop) {
                    pesan = "Distop manual!";
                }
                Message myMessage = handler.obtainMessage();
                myMessage.obj = String.valueOf(pesan);
                handler.sendMessage(myMessage);
            }
        },null);

        model.ft = ft;       //simpan future ke viewmodel sehingga bisa dicancel walau dirotate
        executor.submit(ft); //jalankan future thread

        //kalau executor di terminateed manual akan ada efek aneh
        //jalan tapi langsugn terminated, baru yg kedua normal
    }
}