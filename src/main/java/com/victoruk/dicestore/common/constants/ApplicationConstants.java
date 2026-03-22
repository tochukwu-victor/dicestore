package com.victoruk.dicestore.common.constants;

public class ApplicationConstants {

    private ApplicationConstants(){


        throw new AssertionError("Utility class can not be instantiated ");
    }

    //JWT
    public static final String JWT_SECRET_KEY = "JWT_SECRET";
    public static final String JWT_SECRET_DEFAULT_VALUE = "jxgEQeXHuPq8VdbyYFNkANdudQ53YUn4";
    public static final String JWT_HEADER = "Authorization";


    //Contact
    public static final String  OPEN_MESSAGE = "OPEN";
    public static final String  CLOSED_MESSAGE = "CLOSED";
}
