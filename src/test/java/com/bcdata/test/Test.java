package com.bcdata.test;

public class Test {
    public static void main (String[] args) {
        String test = "aaa,bbb,ccc,ddd,";
        String[] tokens = test.split (",");
        System.out.println (tokens.length);
    }
}
