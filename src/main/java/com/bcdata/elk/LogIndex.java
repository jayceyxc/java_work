package com.bcdata.elk;

public enum LogIndex {
    LOG_KEY_INDEX(0),
    TIME_INDEX(4),
    AD_ID_INDEX(6),
    PUSH_ID_INDEX(7),
    IP_INDEX(8),
    CITY_INDEX(10),
    BIDDER_INDEX(11),
    USER_INDEX(14),
    HOST_INDEX(22),
    SP_INDEX(24),
    URL_INDEX(25),
    PRICE_INDEX(32),
    UA_INDEX(35),
    NET_INDEX(40),
    LACCI_INDEX(-1);


    private final int value;

    private LogIndex(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
