package ru.bratusev.watchproject.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.veepoo.protocol.VPOperateManager;
import com.veepoo.protocol.listener.base.IBleWriteResponse;
import com.veepoo.protocol.listener.data.IECGDetectListener;
import com.veepoo.protocol.model.datas.EcgDetectInfo;
import com.veepoo.protocol.model.datas.EcgDetectResult;
import com.veepoo.protocol.model.datas.EcgDetectState;
import com.veepoo.protocol.model.datas.EcgDiagnosis;

import java.util.Arrays;

import ru.bratusev.watchproject.R;

public class EcgDetectActivity extends Activity implements View.OnClickListener {
    EcgHeartRealthView mEcgHeartView;
    Button start, stop;
    Button notify;
    Context mContext;
    WriteResponse writeResponse = new WriteResponse();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecgdetect);
        mContext = EcgDetectActivity.this;
        mEcgHeartView = (EcgHeartRealthView) findViewById(R.id.ecg_real_view);
        notify = (Button) findViewById(R.id.greenlightdata);
        start = (Button) findViewById(R.id.start);
        stop = (Button) findViewById(R.id.stop);

        notify.setOnClickListener(this);
        start.setOnClickListener(this);
        stop.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.start)
            VPOperateManager.getInstance().startDetectECG(writeResponse, true, new IECGDetectListener() {
                @Override
                public void onEcgDetectInfoChange(EcgDetectInfo ecgDetectInfo) {
                    String message = "-onEcgDetectInfoChange-:" + ecgDetectInfo.toString();
                    Log.d("MyEcgLog", message);
                }

                @Override
                public void onEcgDetectStateChange(EcgDetectState ecgDetectState) {
                    String message = "-onEcgDetectStateChange-:" + ecgDetectState.toString();
                    Log.d("MyEcgLog", message);
                }

                @Override
                public void onEcgDetectResultChange(EcgDetectResult ecgDetectResult) {
                    String message = "-onEcgDetectResultChange-:" + ecgDetectResult.toString();
                    Log.d("MyEcgLog", message);
                }

                @Override
                public void onEcgADCChange(int[] ecgData) {
                    String message = "-onEcgADCChange-:" + Arrays.toString(ecgData);
                    Log.d("MyEcgLog", message);
                }

                @Override
                public void onEcgDetectDiagnosisChange(EcgDiagnosis ecgDiagnosis) {
                    Log.d("MyEcgLog", ecgDiagnosis.toString());
                }
            });
        else {
            mEcgHeartView.clearData();
            VPOperateManager.getMangerInstance(mContext).stopDetectECG(writeResponse, true, null);
        }
    }

    class WriteResponse implements IBleWriteResponse {
        @Override
        public void onResponse(int code) {
            Log.d("MyEcgLog", code + "");
        }
    }
}
