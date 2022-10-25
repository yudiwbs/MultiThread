package edu.upi.cs.yudiwbs.cobamultithread2;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.concurrent.FutureTask;

public class HasilViewModel extends ViewModel {
    private String pesan;

    public HasilViewModel() {
        pesan = "-";
    }

    private MutableLiveData<String> data;
    public FutureTask ft;  //supaya bisa di-cancel setelah rotate

    //class lain mengakses data melalui ini
    //perhatikan ini LiveData, bukan MutableLiveData
    public LiveData<String> getData() {
        //jika belum ada, create
        if (data == null) {
            data = new MutableLiveData<String>();
            data.setValue(pesan);
        }
        return data;
    }

    public void setPesan(String pesan) {
        data.setValue(pesan);
    }

}
