package ru.bratusev.watchproject.activity;

import static com.veepoo.protocol.util.VpBleByteUtil.isBeyondVp;
import static com.veepoo.protocol.util.VpBleByteUtil.isBrandDevice;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.inuker.bluetooth.library.Code;
import com.inuker.bluetooth.library.Constants;
import com.inuker.bluetooth.library.log.VPLocalLogger;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;
import com.inuker.bluetooth.library.utils.BluetoothUtils;
import com.veepoo.protocol.VPOperateManager;
import com.veepoo.protocol.listener.base.IABleConnectStatusListener;
import com.veepoo.protocol.listener.base.IABluetoothStateListener;
import com.veepoo.protocol.listener.base.IBleWriteResponse;
import com.veepoo.protocol.listener.base.IConnectResponse;
import com.veepoo.protocol.listener.base.INotifyResponse;
import com.veepoo.protocol.util.VPLogger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;
import ru.bratusev.watchproject.DeviceCompare;
import ru.bratusev.watchproject.R;
import ru.bratusev.watchproject.adapter.BleScanViewAdapter;
import ru.bratusev.watchproject.adapter.DividerItemDecoration;
import ru.bratusev.watchproject.adapter.OnRecycleViewClickCallback;
import tech.gujin.toast.ToastUtil;

public class MainActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener, OnRecycleViewClickCallback {
    Context mContext = MainActivity.this;
    private final int REQUEST_CODE = 1;
    List<SearchResult> mListData = new ArrayList<>();
    List<String> mListAddress = new ArrayList<>();
    SwipeRefreshLayout mSwipeRefreshLayout;
    BleScanViewAdapter bleConnectAdatpter;
    Handler mHandler = new Handler();
    private BluetoothManager mBManager;
    private BluetoothAdapter mBAdapter;
    private BluetoothLeScanner mBScanner;
    final static int MY_PERMISSIONS_REQUEST_BLUETOOTH = 0x55;
    RecyclerView mRecyclerView;
    TextView mTitleTextView;
    private boolean mIsOadModel;
    BluetoothLeScannerCompat mScanner;

    @Override
    protected void onDestroy() {
        VPLocalLogger.stopMonitor();
        VPOperateManager.getInstance().disconnectWatch(new IBleWriteResponse() {
            @Override
            public void onResponse(int code) {

            }
        });
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ToastUtil.initialize(this);
        VPOperateManager.getInstance().init(this);
        VPOperateManager.getInstance().init(this);
        mScanner = BluetoothLeScannerCompat.getScanner();
        VPLogger.setDebug(true);
        initRecyleView();
        checkPermission();
        registerBluetoothStateListener();
        createFile();
        VPLocalLogger.startMonitor(this);
    }

    private void createFile() {
        String fileSDK = getExternalFilesDir(null) + File.separator + "LTEPH_GPS_1.rtcm";
        File file = new File(fileSDK);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.scan) {
            scanDevice();
        }
    }

    private void initRecyleView() {
        mSwipeRefreshLayout = (SwipeRefreshLayout) super.findViewById(R.id.mian_swipeRefreshLayout);
        mRecyclerView = (RecyclerView) super.findViewById(R.id.main_recylerlist);
        mTitleTextView = (TextView) super.findViewById(R.id.main_title);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        bleConnectAdatpter = new BleScanViewAdapter(this, mListData);
        mRecyclerView.setAdapter(bleConnectAdatpter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));
        bleConnectAdatpter.setBleItemOnclick(this);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mTitleTextView.setText("Сканирующее оборудование V" + getAppVersion(mContext));
    }


    private String[] permissions = new String[]{
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ANSWER_PHONE_CALLS,
            Manifest.permission.BLUETOOTH_PRIVILEGED,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT
    };
    private int requestCode = 123;

    private String[] addPermission(String[] permissions, String permission) {
        String[] newPermissions = new String[permissions.length + 1];
        System.arraycopy(permissions, 0, newPermissions, 0, permissions.length);
        newPermissions[permissions.length] = permission;
        return newPermissions;
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkPermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == this.requestCode) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    // Handle permission denied
                }
            }
            initBLE();
        }
    }

    private void checkPermission() {
        String[] permissionsToRequest = new String[]{};
        for (String permission : permissions) {
            int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest = addPermission(permissionsToRequest, permission);
            }
        }
        if (permissionsToRequest.length > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest,
                    requestCode
            );
        }
    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.BLUETOOTH_PRIVILEGED}, MY_PERMISSIONS_REQUEST_BLUETOOTH);
            }
        }
    }

    private boolean scanDevice() {
        if (!mListAddress.isEmpty()) {
            mListAddress.clear();
        }
        if (!mListData.isEmpty()) {
            mListData.clear();
            bleConnectAdatpter.notifyDataSetChanged();
        }

        if (!BluetoothUtils.isBluetoothEnabled()) {
            Toast.makeText(mContext, "Bluetooth не включен", Toast.LENGTH_SHORT).show();
            return true;
        }
        startScan();
        VPOperateManager.getInstance().startScanDevice(mSearchResponse);
        return false;
    }


    private void startScan() {
        ScanSettings settings = new ScanSettings.Builder()
                .setLegacy(false)
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setReportDelay(1000)
                .setUseHardwareBatchingIfSupported(false)
                .build();
        List<ScanFilter> filters = new ArrayList<>();
        ScanFilter scanFilter;

        ScanFilter.Builder scanFilterBuilder = new ScanFilter.Builder();
        scanFilter = scanFilterBuilder.build();
        filters.add(scanFilter);
        final ScanCallback mScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);

            }

            @Override
            public void onBatchScanResults(final List<ScanResult> results) {
                super.onBatchScanResults(results);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < results.size(); i++) {
                            ScanResult scanResult = results.get(i);

                            BluetoothDevice device = scanResult.getDevice();
                            if (!mListAddress.contains(device.getAddress())) {
                                mListData.add(new SearchResult(device, scanResult.getRssi(), scanResult.getScanRecord().getBytes()));
                                mListAddress.add(device.getAddress());
                            }
                        }
                        mListData.sort(new DeviceCompare());
                        bleConnectAdatpter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
            }
        };
        mScanner.startScan(filters, settings, mScanCallback);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScanner.stopScan(mScanCallback);
                refreshStop();
            }
        }, 6 * 1000);
    }


    private void connectDevice(final String mac, final String deviceName) {

        VPOperateManager.getInstance().registerConnectStatusListener(mac, mBleConnectStatusListener);

        VPOperateManager.getInstance().connectDevice(mac, deviceName, new IConnectResponse() {

            @Override
            public void connectState(int code, BleGattProfile profile, boolean isoadModel) {
                if (code == Code.REQUEST_SUCCESS) {
                    mIsOadModel = isoadModel;
                }
            }
        }, new INotifyResponse() {
            @Override
            public void notifyState(int state) {
                if (state == Code.REQUEST_SUCCESS) {
                    Intent intent = new Intent(mContext, OperaterActivity.class);
                    intent.putExtra("isoadmodel", mIsOadModel);
                    intent.putExtra("deviceaddress", mac);
                    startActivity(intent);
                }
            }
        });
    }

    private void registerBluetoothStateListener() {
        VPOperateManager.getInstance().registerBluetoothStateListener(mBluetoothStateListener);
    }

    private final IABleConnectStatusListener mBleConnectStatusListener = new IABleConnectStatusListener() {

        @Override
        public void onConnectStatusChanged(String mac, int status) {
            if (status == Constants.STATUS_CONNECTED) {
                Log.d("MyLog", "STATUS_CONNECTED");
            } else if (status == Constants.STATUS_DISCONNECTED) {
                Log.d("MyLog", "STATUS_DISCONNECTED");
            }
        }
    };

    private final IABluetoothStateListener mBluetoothStateListener = new IABluetoothStateListener() {
        @Override
        public void onBluetoothStateChanged(boolean openOrClosed) {
        }
    };


    public static boolean isShowDevice(byte[] scanRecord) {
        if (isBeyondVp(scanRecord)) {
            if (isBrandDevice(scanRecord)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private final SearchResponse mSearchResponse = new SearchResponse() {
        @Override
        public void onSearchStarted() {
            Log.d("MyLog", "onSearchStarted");
        }

        @Override
        public void onDeviceFounded(final SearchResult device) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!mListAddress.contains(device.getAddress()) /*&& isShowDevice(device.scanRecord)*/) {
                        mListData.add(device);
                        mListAddress.add(device.getAddress());
                    }
                    mListData.sort(new DeviceCompare());
                    bleConnectAdatpter.notifyDataSetChanged();
                }
            });
        }

        @Override
        public void onSearchStopped() {
            refreshStop();
            Log.d("MyLog", "onSearchStopped");
        }

        @Override
        public void onSearchCanceled() {
            refreshStop();
            Log.d("MyLog", "onSearchCanceled");
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (BluetoothUtils.isBluetoothEnabled()) {
                scanDevice();
            } else {
                refreshStop();
            }
        }
    }

    @Override
    public void onRefresh() {
        if (checkBLE()) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d("MyLog", "onRefresh");
                    scanDevice();
                }
            }, 3000);
        }
    }

    private void initBLE() {
        mBManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (null != mBManager) {
            mBAdapter = mBManager.getAdapter();
        }
        mBScanner = mBAdapter.getBluetoothLeScanner();
        checkBLE();
    }

    private boolean checkBLE() {
        if (!BluetoothUtils.isBluetoothEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return true;
            }
            startActivityForResult(enableBtIntent, REQUEST_CODE);
            return false;
        } else {
            return true;
        }
    }

    void refreshStop() {
        Log.d("MyLog", "refreshComlete");
        if (mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void OnRecycleViewClick(int position) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, "Подключаюсь, пожалуйста, подождите...", Toast.LENGTH_SHORT).show();
            }
        });
        SearchResult searchResult = mListData.get(position);
        connectDevice(searchResult.getAddress(), searchResult.getName());
    }

    public String getAppVersion(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            String version = info.versionName;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
