package ru.bratusev.watchproject.utils;

import static com.veepoo.protocol.model.enums.EFunctionStatus.SUPPORT;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.veepoo.protocol.VPOperateManager;
import com.veepoo.protocol.listener.base.IBleWriteResponse;
import com.veepoo.protocol.listener.data.AbsBloodGlucoseChangeListener;
import com.veepoo.protocol.listener.data.IBPDetectDataListener;
import com.veepoo.protocol.listener.data.ICustomSettingDataListener;
import com.veepoo.protocol.listener.data.IDeviceFuctionDataListener;
import com.veepoo.protocol.listener.data.IHeartDataListener;
import com.veepoo.protocol.listener.data.IHeartWaringDataListener;
import com.veepoo.protocol.listener.data.ILightDataCallBack;
import com.veepoo.protocol.listener.data.IPwdDataListener;
import com.veepoo.protocol.listener.data.ISocialMsgDataListener;
import com.veepoo.protocol.listener.data.ISpo2hDataListener;
import com.veepoo.protocol.listener.data.ISportDataListener;
import com.veepoo.protocol.model.datas.BpData;
import com.veepoo.protocol.model.datas.FunctionDeviceSupportData;
import com.veepoo.protocol.model.datas.FunctionSocailMsgData;
import com.veepoo.protocol.model.datas.HeartData;
import com.veepoo.protocol.model.datas.HeartWaringData;
import com.veepoo.protocol.model.datas.PwdData;
import com.veepoo.protocol.model.datas.Spo2hData;
import com.veepoo.protocol.model.datas.SportData;
import com.veepoo.protocol.model.enums.EBPDetectModel;
import com.veepoo.protocol.model.enums.EBloodGlucoseRiskLevel;
import com.veepoo.protocol.model.enums.EBloodGlucoseStatus;
import com.veepoo.protocol.model.enums.EFunctionStatus;
import com.veepoo.protocol.model.settings.CustomSettingData;
import com.veepoo.protocol.model.settings.HeartWaringSetting;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import ru.bratusev.watchproject.R;
import ru.bratusev.watchproject.activity.OperaterActivity;

public class WatchFunctions {

    private String deviceAddress;
    boolean isSleepPrecision = false;
    int watchDataDay = 3;
    int weatherStyle = 0;
    int contactMsgLength = 0;
    int allMsgLenght = 4;
    private int deviceNumber = -1;
    private String deviceVersion;
    private String deviceTestVersion;
    boolean isOadModel = false;
    boolean isNewSportCalc = false;
    boolean isInPttModel = false;
    ISocialMsgDataListener socialMsgDataListener = new ISocialMsgDataListener() {
        @Override
        public void onSocialMsgSupportDataChange(FunctionSocailMsgData socailMsgData) {}
        @Override
        public void onSocialMsgSupportDataChange2(FunctionSocailMsgData socailMsgData) {}
    };
    WriteResponse writeResponse = new WriteResponse();

    private ViewGroup viewGroup;
    public WatchFunctions(ViewGroup viewGroup) {
        this.viewGroup = viewGroup;
        pwdConfirm();
        viewGroup.findViewById(R.id.startMeasurement).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMeasurement();
            }
        });
    }

    public void startMeasurement(){
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(6);
        startHeartRate();
        //executor.scheduleAtFixedRate(this::startHeartRate, 10, 1, TimeUnit.SECONDS);
        //executor.scheduleAtFixedRate(this::sportDataRead, 10, 1, TimeUnit.SECONDS);
        //executor.scheduleAtFixedRate(this::startBloodGlucose, 10, 1, TimeUnit.SECONDS);
        //executor.scheduleAtFixedRate(this::warningRead, 10, 1, TimeUnit.SECONDS);
        //executor.scheduleAtFixedRate(this::spo2Open, 10, 1, TimeUnit.SECONDS);
        //executor.scheduleAtFixedRate(this::bpDetectStart, 10, 1, TimeUnit.SECONDS);
    }

    public void startHeartRate() {
        VPOperateManager.getInstance().startDetectHeart(writeResponse, new IHeartDataListener() {
            @Override
            public void onDataChange(HeartData heart) {
                ((TextView) viewGroup.findViewById(R.id.pulse_value)).setText(heart.getData());
                Log.d("MyHeartLog", heart.toString());
            }
        });
    }

    public void sportDataRead() {
        VPOperateManager.getInstance().readSportStep(writeResponse, new ISportDataListener() {
            @Override
            public void onSportDataChange(SportData sportData) {
                ((TextView) viewGroup.findViewById(R.id.steps_value)).setText(sportData.getStep());
                Log.d("MySportLog", sportData.toString());
            }
        });
    }

    public void startBloodGlucose() {
        VPOperateManager.getInstance().startBloodGlucoseDetect(writeResponse, new AbsBloodGlucoseChangeListener() {
            @Override
            public void onDetectError(int opt, EBloodGlucoseStatus status) {
                Log.d("MyGlucoseLog", "[onDetectError: opt = " + opt + ", status=" + status + "]");
            }
            @Override
            public void onBloodGlucoseDetect(int progress, float bloodGlucose, EBloodGlucoseRiskLevel riskLevel) {
                ((TextView) viewGroup.findViewById(R.id.glucose_value)).setText(String.valueOf(bloodGlucose));
                Log.d("MyGlucoseLog", "[progress:" + progress + " bloodGlucose: " + bloodGlucose + "]");
            }
            @Override
            public void onBloodGlucoseStopDetect() {
                Log.d("MyGlucoseLog", "Stop Blood Glucose Detect");
            }
        });
    }

    public void warningRead() {
        VPOperateManager.getInstance().readHeartWarning(writeResponse, new IHeartWaringDataListener() {
            @Override
            public void onHeartWaringDataChange(HeartWaringData heartWaringData) {
                ((TextView) viewGroup.findViewById(R.id.heartWarning_value)).setText(heartWaringData.getHeartLow()
                        + " / " + heartWaringData.getHeartHigh());
                Log.d("MyHeartWaringLog", heartWaringData.toString());
            }
        });
    }

    public void warningOpen() {
        VPOperateManager.getInstance().settingHeartWarning(writeResponse, new IHeartWaringDataListener() {
            @Override
            public void onHeartWaringDataChange(HeartWaringData heartWaringData) {
                ((TextView) viewGroup.findViewById(R.id.heartWarning_value)).setText(heartWaringData.getHeartLow()
                        + " / " + heartWaringData.getHeartHigh());
                Log.d("MyHeartWaringLog", heartWaringData.toString());
            }
        }, new HeartWaringSetting(120, 110, true));
    }

    public void warningClose() {
        VPOperateManager.getInstance().settingHeartWarning(writeResponse, new IHeartWaringDataListener() {
            @Override
            public void onHeartWaringDataChange(HeartWaringData heartWaringData) {
                String message = "Отключение сигнализации о частоте сердечных сокращений:\n" + heartWaringData.toString();
                Log.d("MyHeartWarningLog", message);
            }
        }, new HeartWaringSetting(120, 110, false));
    }

    public void pwdConfirm() {
        boolean is24Hourmodel = false;
        VPOperateManager.getInstance().confirmDevicePwd(writeResponse, new IPwdDataListener() {
            @Override
            public void onPwdDataChange(PwdData pwdData) {
                String message = "PwdData:\n" + pwdData.toString();
                deviceNumber = pwdData.getDeviceNumber();
                deviceVersion = pwdData.getDeviceVersion();
                deviceTestVersion = pwdData.getDeviceTestVersion();
                Log.d("MyPWD", message);
            }
        }, new IDeviceFuctionDataListener() {
            @Override
            public void onFunctionSupportDataChange(FunctionDeviceSupportData functionSupport) {
                EFunctionStatus newCalcSport = functionSupport.getNewCalcSport();
                if (newCalcSport != null && newCalcSport.equals(SUPPORT)) {
                    isNewSportCalc = true;
                } else {
                    isNewSportCalc = false;
                }
                watchDataDay = functionSupport.getWathcDay();
                weatherStyle = functionSupport.getWeatherStyle();
                contactMsgLength = functionSupport.getContactMsgLength();
                allMsgLenght = functionSupport.getAllMsgLength();
                isSleepPrecision = functionSupport.getPrecisionSleep() == SUPPORT;
                Log.d("MyPWD", newCalcSport.toString());
            }
        }, socialMsgDataListener, new ICustomSettingDataListener() {
            @Override
            public void OnSettingDataChange(CustomSettingData customSettingData) {
                Log.d("MyPWD", customSettingData.toString());
            }
        }, "0000", is24Hourmodel);

    }

    public void spo2Open() {
        byte[] cmd = new byte[20];
        cmd[0] = (byte) 0xf3;
        cmd[1] = (byte) 0x08;
        VPOperateManager.getInstance().startDetectSPO2H(writeResponse, new ISpo2hDataListener() {
            @Override
            public void onSpO2HADataChange(Spo2hData spo2HData) {
                ((TextView) viewGroup.findViewById(R.id.oxygen_value)).setText(spo2HData.getValue());
                Log.d("MySpO2Log", spo2HData.toString());
            }
        }, new ILightDataCallBack() {
            @Override
            public void onGreenLightDataChange(int[] data) {
                String message = "Кислород в крови - фотоэлектрический сигнал:\n" + Arrays.toString(data);
                Log.d("MySpO2Log", message.toString());
            }
        });
    }

    public void bpDetectStart() {
        VPOperateManager.getInstance().startDetectBP(writeResponse, new IBPDetectDataListener() {
            @Override
            public void onDataChange(BpData bpData) {
                ((TextView) viewGroup.findViewById(R.id.pressure_value)).setText(bpData.getHighPressure()
                        + " / " + bpData.getLowPressure());
                Log.d("MyBPLog", bpData.toString());
            }
        }, EBPDetectModel.DETECT_MODEL_PUBLIC);
    }
}


class WriteResponse implements IBleWriteResponse {
    @Override
    public void onResponse(int code) {
    }
}
