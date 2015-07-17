package com.zensorium.bluetoothtest;

import java.util.UUID;

public class GlobalVariables {
	public static String EXTRA_DEVICE_ADDRESS = "device_address";
    public static String EXTRA_DEVICE_NAME = "device_name";
    public static String EXTRA_DEVICE_SIGNAL_STRENGTH = "device_signal_strength";
    public static String APP_PREFS = "app_prefs1";
    
    public static int REQUEST_BLE_SCAN_CODE = 11;
    
    /* SBLE Service */
    public static final UUID WEARABLE_SERVICE = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID TX_DATA_CHAR = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID RX_DATA_CHAR = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
    
    /* Client Configuration Descriptor */
    public static final UUID CONFIG_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    
    public static final String DATA_TRANSFER_COMMAND = "FFF400010100032F00D9";
    public static final String DATA_TRANSFER_RESEND_COMMAND = "FFF400010100033100D7";
    public static final String DATA_TRANSFER_ACK_COMMAND = "FFF400010100033200D6";

    public static final String ACK_FLAG_COMMAND = "FFF4000100033200D7";
    public static final String READY_FLAG_COMMAND = "FFF400010100033200D6";
    public static final String STRESS_FLAG_COMMAND = "FFF400000000033700D3";
    public static final String ACTIVITY_FLAG_COMMAND = "FFF400000000033A00D0";
    public static final String STEP_FLAG_COMMAND = "FFF400000000033500D0";
    public static final String HR_FLAG_COMMAND = "FFF400000000033600D0";
    public static final String SLEEP_ER_FLAG_COMMAND = "FFF400000000033800D0";
    public static final String SLEEP_QR_FLAG_COMMAND = "FFF400000000033900D0";
    public static final String SLEEP_SR_FLAG_COMMAND = "FFF400000000033B00D0";
    public static final String CMD_DEVICE_REG_REQUEST = "FFF400000000003CAB26";
    public static final String CMD_DEVICE_REG_FINISHED = "FFF40000AB000332002D";
    public static final String END_FLAG_COMMAND = "FFF400010100033E00CA";

    //---------------- Phone -> device commands ----------------------- //
    //  ACKNOWLEDGE
    public static final String ACK_OK = "fff400003d";
    public static final String ACK_NOTOK = "fff400003c";

    //  ---------------- Phone -> device commands ----------------------- //
    //  ACKNOWLEDGE
    public static final String ACK_NEXT_DATA = "FFF400010100033200D6";

    //  SYNC END
    public static final String CMD_CLEAR_DATA = "FFF400000000034200C8";
    public static final String CMD_SYNC_END = "FFF400000100033E00CB";

    public static final int PACKET_REQUEST_INFO = 0;
    public static final int PACKET_REQUEST_TRANSFER = 1;
    public static final int STEP_SYNC = 1;
    public static final int STRESS_SYNC = 2;
    public static final int SLEEP_SYNC = 7;
    public static final int TIME_SYNC = 8;
    public static final int USER_INFO_SYNC = 11;
    public static final int DEVREGISTRATION = 12;
    public static final int DATA_PACKET_ERASE = 13;

    public static final String STEP                 = "35";
    public static final String STRESS               = "37";
    public static final String SLEEP                = "3B";
    public static final String REGINFO              = "3D";

    public static final String[] SYNC_COMMAND_LIST = {STRESS, STEP, SLEEP};
    public static final int[] SYNC_COMMAND_LIST_CODE = {STRESS_SYNC, STEP_SYNC, SLEEP_SYNC};
    public static final String END_OF_PACKET = "0D0A";

    public static final String ACTION_INTENT_DATA_TO_SYNC_AMOUNT = "zensorium_wearable.action.SYNC_UPDATE";
    public static final String ACTION_INTENT_DATA_TO_SYNC_DATA_RATE = "zensorium_wearable.action.SYNC_DATA_RATE";
    public static final String ACTION_INTENT_DATA_SYNC_RX_COUNTING = "zensorium_wearable.action.SYNC_RX_COUNTING";
    public static final String ACTION_INTENT_DATA_SYNC_COMMAND_INDEX = "zensorium_wearable.action.SYNC_COMMAND_INDEX";
    public static final String ACTION_INTENT_DATA_SYNC_PROGRESS = "zensorium_wearable.action.SYNC_PROGRESS";
    public static final String ACTION_INTENT_DATA_SYNC_COMPLETED = "zensorium_wearable.action.SYNC_COMPLETED";
    public static final String ACTION_INTENT_DATA_SYNC_MESSAGE = "zensorium_wearable.action.SYNC_MESSAGE";
    public static final String ACTION_INTENT_DATA_SYNC_STATUS = "zensorium_wearable.action.SYNC_STATUS";
    public static final String ACTION_INTENT_DATA_SYNC_RESULT = "zensorium_wearable.action.SYNC_RESULT";
}
