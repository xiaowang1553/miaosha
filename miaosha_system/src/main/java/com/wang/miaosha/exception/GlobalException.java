package com.wang.miaosha.exception;

import com.wang.miaosha.result.CodeMsg;

public class GlobalException extends RuntimeException {
    private static final long serialVersionUID=1L;

    private CodeMsg cm;

    public GlobalException(CodeMsg cm){
        super(cm.toString());
        this.cm=cm;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public CodeMsg getCm() {
        return cm;
    }
}
