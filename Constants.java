package com.example.bombgame;

import java.util.UUID;

public final class Constants {
    public static final int DEFAULT_CODE_LENGTH = 6;
    public static final int MIN_CODE_LENGTH = 1;
    public static final int MAX_CODE_LENGTH = 12;

    public static final int DEFAULT_TIME_TO_DEFUSE = 45;
    public static final int MIN_TIME_TO_DEFUSE = 5;
    public static final int MAX_TIME_TO_DEFUSE = 255;


    public static final String HM_10_MAC = "00:35:FF:0B:9B:97";

    public static final UUID HM_10_CUSTOM_SERVICE = UUID.fromString("0000FFE0-0000-1000-8000-00805F9B34FB");
    public static final UUID HM_10_CUSTOM_CHARACTERISTIC = UUID.fromString("0000FFE1-0000-1000-8000-00805F9B34FB");

    public static final UUID HM_10_CCCD = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB");


    public static final int PACKET_MIN_LENGTH = 6;
}
