package ru.bratusev.watchproject.activity;

import static ru.bratusev.watchproject.activity.Oprate.BLE_DISCONNECT;
import static ru.bratusev.watchproject.activity.Oprate.BP_DETECT_START;
import static ru.bratusev.watchproject.activity.Oprate.BT_CLOSE;
import static ru.bratusev.watchproject.activity.Oprate.BT_CONNECT;
import static ru.bratusev.watchproject.activity.Oprate.GATT_CLOSE;
import static ru.bratusev.watchproject.activity.Oprate.HEARTWRING_CLOSE;
import static ru.bratusev.watchproject.activity.Oprate.HEARTWRING_OPEN;
import static ru.bratusev.watchproject.activity.Oprate.HEARTWRING_READ;
import static ru.bratusev.watchproject.activity.Oprate.HEART_DETECT_START;
import static ru.bratusev.watchproject.activity.Oprate.PWD_COMFIRM;
import static ru.bratusev.watchproject.activity.Oprate.SPO2H_OPEN;
import static ru.bratusev.watchproject.activity.Oprate.SPORT_CURRENT_READ;
import static ru.bratusev.watchproject.activity.Oprate.START_BLOOD_GLUCOSE;
import static ru.bratusev.watchproject.activity.Oprate.oprateStr;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Process;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

import com.veepoo.protocol.VPOperateManager;
import com.veepoo.protocol.listener.base.IBleNotifyResponse;
import com.veepoo.protocol.listener.base.IBleWriteResponse;
import com.veepoo.protocol.listener.data.IDeviceBTConnectionListener;
import com.veepoo.protocol.listener.data.IDeviceBTInfoListener;
import com.veepoo.protocol.listener.data.IDeviceFunctionStatusChangeListener;
import com.veepoo.protocol.model.datas.BTInfo;
import com.veepoo.protocol.model.enums.EFunctionStatus;
import com.veepoo.protocol.util.VPLogger;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import ru.bratusev.watchproject.R;
import ru.bratusev.watchproject.adapter.GridAdatper;
import ru.bratusev.watchproject.utils.WatchFunctions;
import tech.gujin.toast.ToastUtil;

public class OperaterActivity extends Activity implements AdapterView.OnItemClickListener {
    TextView tv1, tv2, tv3, titleBleInfo;
    GridView mGridView;
    List<Map<String, String>> mGridData = new ArrayList<>();
    GridAdatper mGridAdapter;
    Context mContext = OperaterActivity.this;
    WriteResponse writeResponse = new WriteResponse();
    Boolean debugFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(debugFlag){
            setContentView(R.layout.activity_operate);
            mContext = getApplicationContext();
            String deviceaddress = getIntent().getStringExtra("deviceaddress");
            tv1 = (TextView) super.findViewById(R.id.tv1);
            tv2 = (TextView) super.findViewById(R.id.tv2);
            tv3 = (TextView) super.findViewById(R.id.tv3);
            titleBleInfo = (TextView) super.findViewById(R.id.main_title_ble);
            initGridView();
        }else {
            setContentView(R.layout.activity_operate2);
        }
        VPOperateManager.getInstance().init(this);
        VPOperateManager.getInstance().setAutoConnectBTBySdk(false);
        VPOperateManager.getInstance().registerBTInfoListener(new IDeviceBTInfoListener() {
            @Override
            public void onDeviceBTFunctionNotSupport() {
                /*//showToast("Не поддерживает функцию BT");*/
            }

            @Override
            public void onDeviceBTInfoSettingSuccess(@NotNull BTInfo btInfo) {
                /*//showToast("【BT】- ---> btInfo : " + btInfo.toString());*/
            }

            @Override
            public void onDeviceBTInfoSettingFailed() {
                ////showToast("【BT】- ---> Сбой настройки BT");
            }

            @Override
            public void onDeviceBTInfoReadSuccess(@NotNull BTInfo btInfo) {
                ////showToast("【BT】- ---> BT успешно прочитан, btInfo : " + btInfo.toString());
            }

            @Override
            public void onDeviceBTInfoReadFailed() {
                ////showToast("【BT】- ---> Не удалось выполнить чтение BT");
            }

            @Override
            public void onDeviceBTInfoReport(@NotNull BTInfo btInfo) {
                ////showToast("【BT】- ---> Отчет BT，btInfo = " + btInfo.toString());
            }
        });
        VPOperateManager.getInstance().registerBTConnectionListener(new IDeviceBTConnectionListener() {
            @Override
            public void onDeviceBTConnecting() {
                ////showToast("Подключенное устройство BT");
            }

            @Override
            public void onDeviceBTConnected() {
                //showToast("Устройство BT подключено");
            }

            @Override
            public void onDeviceBTDisconnected() {
                //showToast("Устройство BT отключено");
//                VPOperateManager.getInstance().setBTStatus(false, true, true, false, new IBleWriteResponse() {
//                    @Override
//                    public void onResponse(int code) {
//
//                    }
//                });
            }

            @Override
            public void onDeviceBTConnectTimeout() {
                //showToast("Время ожидания подключения по BT");
            }
        });
        VPOperateManager.getInstance().listenDeviceCallbackData(new IBleNotifyResponse() {
            @Override
            public void onNotify(UUID service, UUID character, byte[] value) {
                super.onNotify(service, character, value);
            }
        });

        if(!debugFlag){
            ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this
                    .findViewById(android.R.id.content)).getChildAt(0);
            new WatchFunctions(viewGroup);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        VPOperateManager.getInstance().setDeviceFunctionStatusChangeListener(new IDeviceFunctionStatusChangeListener() {
            @Override
            public void onFunctionStatusChanged(@NotNull DeviceFunction function, @NotNull EFunctionStatus status) {
                currentState = status;
                des = function.getDes();
            }
        });
    }

    public static EFunctionStatus currentState = null;

    public static String des = null;

    private void initGridView() {
        mGridView = (GridView) findViewById(R.id.main_gridview);
        int i = 0;
        while (i < oprateStr.length) {
            String s = oprateStr[i];
            Map<String, String> map = new HashMap<>();
            map.put("str", s);
            mGridData.add(map);
            i++;
        }
        mGridAdapter = new GridAdatper(this, mGridData);
        mGridView.setAdapter(mGridAdapter);
        mGridView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, View view, int position, long id) {
        ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this
                .findViewById(android.R.id.content)).getChildAt(0);
        WatchFunctions watchFunctions = new WatchFunctions(viewGroup);
        switch (Objects.requireNonNull(mGridData.get(position).get("str"))){
            case HEART_DETECT_START: {
                watchFunctions.startHeartRate();
                break;
            }
            case SPORT_CURRENT_READ: {
                watchFunctions.sportDataRead();
                break;
            }
            case START_BLOOD_GLUCOSE: {
                watchFunctions.startBloodGlucose();
                break;
            }
            case HEARTWRING_READ: {
                watchFunctions.warningRead();
                break;
            }
            case HEARTWRING_OPEN: {
                watchFunctions.warningOpen();
                break;
            }
            case HEARTWRING_CLOSE: {
                watchFunctions.warningClose();
                break;
            }
            case SPO2H_OPEN: {
                watchFunctions.spo2Open();
                break;
            }
            case PWD_COMFIRM: {
                watchFunctions.pwdConfirm();
                break;
            }
            case BT_CONNECT: {
                connectBT();
                break;
            }
            case BT_CLOSE: {
                disconnectBT();
                break;
            }
            case BLE_DISCONNECT: {
                VPOperateManager.getInstance().disconnectWatch(writeResponse);
                break;
            }
            case BP_DETECT_START: {
                watchFunctions.bpDetectStart();
                break;
            }
            case GATT_CLOSE: {
                BluetoothGatt gatt = VPOperateManager.getInstance().getCurrentConnectGatt();
                if (gatt != null) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    gatt.disconnect();
                    gatt.close();
                    gatt = null;
                }
                break;
            }
        }

        // TODO: ECG Crushed
/*        if (oprater.equals(DETECT_START_ECG) || oprater.equals(DETECT_STOP_ECG)) {
            startActivity(new Intent(OperaterActivity.this, EcgDetectActivity.class));
        }
        else if (oprater.equals(DEVICE_ECG_ALWAYS_OPEN)) {
            boolean isHaveMetricSystem = true;
            boolean isMetric = true;
            boolean is24Hour = true;
            boolean isOpenAutoHeartDetect = true;
            boolean isOpenAutoBpDetect = true;
            boolean isCelsius = true;
            EFunctionStatus isOpenSportRemain = UNSUPPORT;
            EFunctionStatus isOpenVoiceBpHeart = UNSUPPORT;
            EFunctionStatus isOpenFindPhoneUI = UNSUPPORT;
            EFunctionStatus isOpenStopWatch = UNSUPPORT;
            EFunctionStatus isOpenSpo2hLowRemind = UNSUPPORT;
            EFunctionStatus isOpenWearDetectSkin = UNSUPPORT;
            EFunctionStatus isOpenAutoInCall = UNSUPPORT;
            EFunctionStatus isOpenAutoHRV = UNSUPPORT;
            EFunctionStatus isOpenDisconnectRemind = UNSUPPORT;
            EFunctionStatus isAutoTemperatureDetect = UNSUPPORT;
            boolean isSupportSettingsTemperatureUnit = VpSpGetUtil.getVpSpVariInstance(mContext).isSupportSettingsTemperatureUnit();//是否支持温度单位设置
            boolean isSupportSleep = VpSpGetUtil.getVpSpVariInstance(mContext).isSupportPreciseSleep();//是否支持精准睡眠

            boolean isCanReadTempture = VpSpGetUtil.getVpSpVariInstance(mContext).isSupportReadTempture();//是否支持读取温度
            boolean isCanDetectTempByApp = VpSpGetUtil.getVpSpVariInstance(mContext).isSupportCheckTemptureByApp();//是否可以通过app监测体温

            CustomSetting customSetting = new CustomSetting(isHaveMetricSystem, isMetric, is24Hour, isOpenAutoHeartDetect,
                    isOpenAutoBpDetect, isOpenSportRemain, isOpenVoiceBpHeart, isOpenFindPhoneUI, isOpenStopWatch,
                    isOpenSpo2hLowRemind, isOpenWearDetectSkin, isOpenAutoInCall, isOpenAutoHRV, isOpenDisconnectRemind
            );
            customSetting.setIsOpenLongClickLockScreen(SUPPORT_CLOSE);
            if (isSupportSettingsTemperatureUnit) {
                customSetting.setTemperatureUnit(VpSpGetUtil.getVpSpVariInstance(mContext).getTemperatureUnit()
                        == ETemperatureUnit.CELSIUS ? ETemperatureUnit.FAHRENHEIT : ETemperatureUnit.CELSIUS);
            } else {
                customSetting.setTemperatureUnit(ETemperatureUnit.NONE);
            }
            if (isCanDetectTempByApp) {
                boolean isOpenTempDetect = VpSpGetUtil.getVpSpVariInstance(mContext).isOpenTemperatureDetectByApp();
                customSetting.setIsOpenAutoTemperatureDetect(isOpenTempDetect ? SUPPORT_CLOSE : SUPPORT_OPEN);
            } else {
                customSetting.setIsOpenAutoTemperatureDetect(UNSUPPORT);
            }

            customSetting.setEcgAlwaysOpen(SUPPORT_OPEN);
            VPOperateManager.getInstance().changeCustomSetting(writeResponse, new ICustomSettingDataListener() {
                @Override
                public void OnSettingDataChange(CustomSettingData customSettingData) {
                    Log.d("MyECGLog", customSettingData.toString());
                }
            }, customSetting);
        }
        else if (oprater.equals(READ_ECG_DATA)) {
            TimeData timeData = new TimeData(0, 0, 0, 0, 0, 0);
            VPOperateManager.getInstance().readECGData(writeResponse, timeData, EEcgDataType.MANUALLY, new IECGReadDataListener() {
                @Override
                public void readDataFinish(List<EcgDetectResult> resultList) {
                    Log.d("MyECGLog", resultList.toString());
                }
                @Override
                public void readDiagnosisDataFinish(List<EcgDiagnosis> resultList) {
                    Log.d("MyECGLog", resultList.toString());
                }
            });
        }
        else if (oprater.equals(READ_ECG_ID)) {
            TimeData timeData = new TimeData(0, 0, 0, 0, 0, 0);
            VPOperateManager.getInstance().readECGId(writeResponse, timeData, EEcgDataType.MANUALLY, new IECGReadIdListener() {
                @Override
                public void readIdFinish(int[] ids) {
                    StringBuilder sb = new StringBuilder();
                    for (int j : ids) {
                        sb.append(j).append(",");
                    }
                    //showToast("Считывание идентификатора ЭКГ завершено：" + sb);
                }
            });
        }
        else if (oprater.equals(SET_ECG_NEW_DATA_REPORT)) {
            VPOperateManager.getInstance().setNewEcgDataReportListener(new INewECGDataReportListener() {
                @Override
                public void onNewECGDetectDataReport() {
                    //showToast("Следите за тем, чтобы устройство сообщало о новых данных измерения ЭКГ," +
                            " пожалуйста, ознакомьтесь с данными ЭКГ для получения подробной информации");
                }
            });
            //showToast("Мониторинг настроен, пожалуйста, подойдите к прибору для измерения ЭКГ");
        }
        else if (oprater.equals(DEVICE_ECG_ALWAYS_CLOSE)) {
            boolean isHaveMetricSystem = true;
            boolean isMetric = true;
            boolean is24Hour = true;
            boolean isOpenAutoHeartDetect = true;
            boolean isOpenAutoBpDetect = true;
            boolean isCelsius = true;
            EFunctionStatus isOpenSportRemain = UNSUPPORT;
            EFunctionStatus isOpenVoiceBpHeart = UNSUPPORT;
            EFunctionStatus isOpenFindPhoneUI = UNSUPPORT;
            EFunctionStatus isOpenStopWatch = UNSUPPORT;
            EFunctionStatus isOpenSpo2hLowRemind = UNSUPPORT;
            EFunctionStatus isOpenWearDetectSkin = UNSUPPORT;
            EFunctionStatus isOpenAutoInCall = UNSUPPORT;
            EFunctionStatus isOpenAutoHRV = UNSUPPORT;
            EFunctionStatus isOpenDisconnectRemind = UNSUPPORT;
            EFunctionStatus isAutoTemperatureDetect = UNSUPPORT;
            boolean isSupportSettingsTemperatureUnit = VpSpGetUtil.getVpSpVariInstance(mContext).isSupportSettingsTemperatureUnit();//是否支持温度单位设置
            boolean isSupportSleep = VpSpGetUtil.getVpSpVariInstance(mContext).isSupportPreciseSleep();//是否支持精准睡眠

            boolean isCanReadTempture = VpSpGetUtil.getVpSpVariInstance(mContext).isSupportReadTempture();//是否支持读取温度
            boolean isCanDetectTempByApp = VpSpGetUtil.getVpSpVariInstance(mContext).isSupportCheckTemptureByApp();//是否可以通过app监测体温

            CustomSetting customSetting = new CustomSetting(isHaveMetricSystem, isMetric, is24Hour, isOpenAutoHeartDetect,
                    isOpenAutoBpDetect, isOpenSportRemain, isOpenVoiceBpHeart, isOpenFindPhoneUI, isOpenStopWatch,
                    isOpenSpo2hLowRemind, isOpenWearDetectSkin, isOpenAutoInCall, isOpenAutoHRV, isOpenDisconnectRemind
            );
            customSetting.setIsOpenLongClickLockScreen(SUPPORT_CLOSE);
            if (isSupportSettingsTemperatureUnit) {
                customSetting.setTemperatureUnit(VpSpGetUtil.getVpSpVariInstance(mContext).getTemperatureUnit()
                        == ETemperatureUnit.CELSIUS ? ETemperatureUnit.FAHRENHEIT : ETemperatureUnit.CELSIUS);
            } else {
                customSetting.setTemperatureUnit(ETemperatureUnit.NONE);
            }
            if (isCanDetectTempByApp) {
                boolean isOpenTempDetect = VpSpGetUtil.getVpSpVariInstance(mContext).isOpenTemperatureDetectByApp();
                customSetting.setIsOpenAutoTemperatureDetect(isOpenTempDetect ? SUPPORT_CLOSE : SUPPORT_OPEN);
            } else {
                customSetting.setIsOpenAutoTemperatureDetect(UNSUPPORT);
            }

            customSetting.setEcgAlwaysOpen(SUPPORT_CLOSE);
            VPOperateManager.getInstance().changeCustomSetting(writeResponse, new ICustomSettingDataListener() {
                @Override
                public void OnSettingDataChange(CustomSettingData customSettingData) {
                    String message = "Персонализированные настройки статуса:\n" + customSettingData.toString();
                    sendMsg(message, 1);
                }
            }, customSetting);
        }
        else if (oprater.equals(ECG_AUTO_REPORT_TEXT)) {
            VPOperateManager.getInstance().setECGAutoReportListener(new IECGAutoReportListener() {
                @Override
                public void onECGAutoReport(int ecgValue, TimeData date) {
                    String info = "ECG = " + ecgValue + ", date = " + date.getDateAndClockForSleepSecond();
                    Log.d("MyECGLog", info.toString());
                }
                @Override
                public void onECGDataReport(int[] ints) {
                    Log.d("MyECGLog", ints.toString());
                }
            });
        }*/
        // TODO: BATTERY Incorrect Data
/*        if (oprater.equals(BATTERY)) {
            VPOperateManager.getInstance().readBattery(writeResponse, new IBatteryDataListener() {
                @Override
                public void onDataChange(BatteryData batteryData) {
                    Log.d("MyBatteryLog", String.valueOf(batteryData.getBatteryLevel()));
                }
            });
        }*/
        // TODO: Temperature NOT SUPPORTED
/*        else if (oprater.equals(TEMPTURE_DETECT_START)) {
            VPOperateManager.getInstance().startDetectTempture(writeResponse, new ITemptureDetectDataListener() {
                @Override
                public void onDataChange(TemptureDetectData temptureDetectData) {
                    Log.d("MyTemperatureLog", temptureDetectData.toString());
                }
            });
        }
        else if (oprater.equals(READ_TEMPTURE_DATA)) {
            ReadOriginSetting readOriginSetting = new ReadOriginSetting(0, 1, false, watchDataDay);
            VPOperateManager.getInstance().readTemptureDataBySetting(writeResponse, new ITemptureDataListener() {
                @Override
                public void onTemptureDataListDataChange(List<TemptureData> temptureDataList) {
                    Log.d("MyTempLog", temptureDataList.toString());
                }

                @Override
                public void onReadOriginProgressDetail(int day, String date, int allPackage, int currentPackage) {
                    String message = "Ход считывания температурных данных:" + "day=" + day + ",currentPackage=" + currentPackage + ",allPackage=" + allPackage;
                    Log.d("MyTempLog", message);
                }

                @Override
                public void onReadOriginProgress(float progress) {
                    String message = "onReadOriginProgress:" + progress;
                    Log.d("MyTempLog", message);
                }

                @Override
                public void onReadOriginComplete() {
                    String message = "onReadOriginComplete";
                    Log.d("MyTempLog", message);
                }
            }, readOriginSetting);
        }
        else if (oprater.equals(TEMPTURE_DETECT_STOP)) {
            VPOperateManager.getInstance().stopDetectTempture(writeResponse, new ITemptureDetectDataListener() {
                @Override
                public void onDataChange(TemptureDetectData temptureDetectData) {
                    Log.d("MyTemperatureLog", temptureDetectData.toString());
                }
            });
        }*/
        // TODO HRV Not supported
/*        else if (oprater.equals(HRV_ORIGIN_READ)) {
            VPOperateManager.getInstance().readHRVOrigin(writeResponse, new IHRVOriginDataListener() {
                @Override
                public void onReadOriginProgress(float progress) {
                    Log.d("MyHRVDataLog", progress + "");
                }
                @Override
                public void onReadOriginProgressDetail(int day, String date, int allPackage, int currentPackage) {}
                @Override
                public void onHRVOriginListener(HRVOriginData hrvOriginData) {
                    Log.d("MyHRVDataLog", hrvOriginData.toString());

                }
                @Override
                public void onDayHrvScore(int day, String date, int hrvSocre) {}
                @Override
                public void onReadOriginComplete() {
                    Log.d("MyHRVDataLog", "On read complete");
                }
            }, watchDataDay);
        }*/
        // TODO S22 Not supported
/*        else if (oprater.equals(S22_READ_DATA)) {
            TimeData timeData = new TimeData(2017, 9, 11, 8, 13, 20);
            VPOperateManager.getInstance().readAutoDetectOriginDataFromS22(writeResponse, new IAutoDetectOriginDataListener() {
                @Override
                public void onAutoDetectOriginDataChangeListener(List<AutoDetectOriginData> autoDetectOriginDataList) {
                    for (AutoDetectOriginData autoDetectOriginData : autoDetectOriginDataList) {
                        Log.d("MyS22Log", autoDetectOriginData.toString());
                    }
                }
            }, timeData);
        }
        else if (oprater.equals(S22_READ_STATE)) {
            VPOperateManager.getInstance().readAutoDetectStateFromS22(writeResponse, new ICustomProtocolStateListener() {

                @Override
                public void onS22AutoDetectStateChangeListener(AutoDetectStateData autoDetectStateData) {
                    Log.d("MyS22Log", autoDetectStateData.toString());
                }
            });
        }
        else if (oprater.equals(S22_SETTING_STATE_OPEN)) {
            AutoDetectStateSetting autoDetectStateSetting = new AutoDetectStateSetting();
            autoDetectStateSetting.setSpo2h24Hour(SUPPORT_OPEN);
            VPOperateManager.getInstance().setAutoDetectStateToS22(writeResponse, new ICustomProtocolStateListener() {
                @Override
                public void onS22AutoDetectStateChangeListener(AutoDetectStateData autoDetectStateData) {}
            }, autoDetectStateSetting);
        }
        else if (oprater.equals(S22_SETTING_STATE_CLOSE)) {
            AutoDetectStateSetting autoDetectStateSetting = new AutoDetectStateSetting();
            autoDetectStateSetting.setSpo2h24Hour(SUPPORT_CLOSE);
            VPOperateManager.getInstance().setAutoDetectStateToS22(writeResponse, new ICustomProtocolStateListener() {
                @Override
                public void onS22AutoDetectStateChangeListener(AutoDetectStateData autoDetectStateData) {}
            }, autoDetectStateSetting);
        }*/
        // TODO BODY Component Not supported
/*        else if (oprater.equals(DETECT_START_BODY_COMPONENT)) {
            VPOperateManager.getInstance().startDetectBodyComponent(writeResponse, new IBodyComponentDetectListener() {
                @Override
                public void onDetecting(int progress, int leadState) {}
                @Override
                public void onDetectSuccess(@NotNull BodyComponent bodyComponent) {//showToast("Успешное измерение：" + bodyComponent);}
                @Override
                public void onDetectFailed(@NotNull DetectState detectState) {//showToast("Ошибка измерения：" + detectState);}
                @Override
                public void onDetectStop() {
                    //showToast("Остановка измерения");
                }
            });
            //showToast("Измеряются данные о составе тела....");
        }
        else if (oprater.equals(DETECT_STOP_BODY_COMPONENT)) {
            //showToast("Конечное измерение данных о составе тела");
            VPOperateManager.getInstance().stopDetectBodyComponent(writeResponse);}
        else if (oprater.equals(READ_BODY_COMPONENT_ID)) {
            VPOperateManager.getInstance().readBodyComponentId(writeResponse, new IBodyComponentReadIdListener() {
                @Override
                public void readIdFinish(@NotNull ArrayList<Integer> ids) {
                    //showToast("Считывание завершено, количество идентификаторов：" + ids.size());
                }
            });}
        else if (oprater.equals(READ_BODY_COMPONENT_DATA)) {
            VPOperateManager.getInstance().readBodyComponentData(writeResponse, new IBodyComponentReadDataListener() {
                @Override
                public void readBodyComponentDataFinish(@Nullable List<BodyComponent> bodyComponentList) {
                    //showToast("Считывание данных о составе тела завершено：" + bodyComponentList.toString());
                }
            });}
        else if (oprater.equals(SET_BODY_COMPONENT_NEW_DATA_REPORT)) {
            VPOperateManager.getInstance().setBodyComponentReportListener(new INewBodyComponentReportListener() {
                @Override
                public void onNewBodyComponentReport() {
                    //showToast("Следите за тем, чтобы устройство сообщало новые данные о составе тела," +
                            " пожалуйста, ознакомьтесь с данными о составе тела для получения подробной информации");
                }
            });
            //showToast("Мониторинг настроен, пожалуйста, перейдите к прибору для измерения состава тела");
        }*/
        // TODO: BLOOD COMPOSITION Not supported
/*        else if (oprater.equals(READ_BLOOD_COMPOSITION_CALIBRATION)) {
            VPOperateManager.getInstance().readBloodComponentCalibration(writeResponse, new IBloodComponentOptListener() {
                @Override
                public void onBloodCompositionSettingFailed() {}
                @Override
                public void onBloodCompositionSettingSuccess(boolean isOpen, @NotNull BloodComponent bloodComposition) {}
                @Override
                public void onBloodCompositionReadFailed() {
                    //showToast("При калибровке не удалось определить состав крови");
                }
                @Override
                public void onBloodCompositionReadSuccess(boolean isOpen, @NotNull BloodComponent bloodComposition) {
                    //showToast("Считайте состав крови и успешно выполните калибровку：" + isOpen + "," + bloodComposition);
                    OperaterActivity.this.isBloodCompositionOpen = isOpen;
                }
            });}
        else if (oprater.equals(SETTING_BLOOD_COMPOSITION_CALIBRATION)) {
            BloodComponent bloodComponent = new BloodComponent(99f, 88f, 77f, 66f, 55f);
            VPOperateManager.getInstance().settingBloodComponentCalibration(writeResponse, isBloodCompositionOpen, bloodComponent, new IBloodComponentOptListener() {
                @Override
                public void onBloodCompositionSettingFailed() {
                    //showToast("Не удалось настроить калибровку компонентов крови");
                }
                @Override
                public void onBloodCompositionSettingSuccess(boolean isOpen, @NotNull BloodComponent bloodComposition) {
                    //showToast("Успешно настроена калибровка компонентов крови：" + isOpen + "," + bloodComposition);
                }
                @Override
                public void onBloodCompositionReadFailed() {}
                @Override
                public void onBloodCompositionReadSuccess(boolean isOpen, @NotNull BloodComponent bloodComposition) {}
            });
        }*/
        // TODO: BLOOD COMPONENT Not supported
/*      else if (oprater.equals(DETECT_START_BLOOD_COMPONENT)) {
            VPOperateManager.getInstance().startDetectBloodComponent(writeResponse, isBloodCompositionOpen, new IBloodComponentDetectListener() {
                @Override
                public void onDetectComplete(@NotNull BloodComponent bloodComponent) {
                    //showToast("Завершено измерение состава крови：" + bloodComponent);
                }
                @Override
                public void onDetectStop() {
                    //showToast("Окончание измерения состава крови");
                }
                @Override
                public void onDetecting(int progress, @NotNull BloodComponent bloodComponent) {
                    if (progress % 50 == 0) {
                        //showToast("Измерение состава крови..");
                    }
                }
                @Override
                public void onDetectFailed(@NotNull EBloodComponentDetectState errorState) {
                    //showToast("Не удалось измерить состав крови：" + errorState);
                }
            });}
        else if (oprater.equals(DETECT_STOP_BLOOD_COMPONENT)) {
            VPOperateManager.getInstance().stopDetectBloodComponent(writeResponse);
        }
*/
    }

    private void showToast(String msg) {
        ToastUtil.show(msg);
    }

    static class WriteResponse implements IBleWriteResponse {
        @Override
        public void onResponse(int code) {
        }
    }

    @Override
    protected void onDestroy() {
        VPOperateManager.getInstance().disconnectWatch(new IBleWriteResponse() {
            @Override
            public void onResponse(int i) {
            }
        });
        super.onDestroy();
    }

    private void connectBT() {
        VPOperateManager.getInstance().connectBT(VPOperateManager.getCurrentDeviceAddress(), new IDeviceBTConnectionListener() {
            @Override
            public void onDeviceBTConnecting() {
                //showToast("Подключение устройства BT");
            }

            @Override
            public void onDeviceBTConnected() {
                //showToast("Устройство BT подключено");
            }

            @Override
            public void onDeviceBTDisconnected() {
                //showToast("Устройство BT отключено");
            }

            @Override
            public void onDeviceBTConnectTimeout() {
                //showToast("Время ожидания подключения по BT");
            }
        });
    }

    private void disconnectBT() {
        VPOperateManager.getInstance().disconnectBT(VPOperateManager.getCurrentDeviceAddress(), new IDeviceBTConnectionListener() {
            @Override
            public void onDeviceBTConnecting() {
                //showToast("BT设备连接中");
            }

            @Override
            public void onDeviceBTConnected() {
                //showToast("BT设备已连接");
            }

            @Override
            public void onDeviceBTDisconnected() {
                //showToast("BT设备已断开");
            }

            @Override
            public void onDeviceBTConnectTimeout() {
                //showToast("BT连接超时");
            }
        });
    }
}
