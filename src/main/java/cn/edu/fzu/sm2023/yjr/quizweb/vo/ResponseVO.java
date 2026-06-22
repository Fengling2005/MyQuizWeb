package cn.edu.fzu.sm2023.yjr.quizweb.vo;

public class ResponseVO<T> {
    private int code;
    private String msg;
    private T data;

    public ResponseVO(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // 成功响应 - 无数据
    public static ResponseVO ok() {
        return new ResponseVO(200, "操作成功", null);
    }

    // 成功响应 - 有数据
    public static <T> ResponseVO<T> ok(T data) {
        return new ResponseVO<>(200, "操作成功", data);
    }

    // 成功响应 - 自定义消息
    public static ResponseVO ok(String msg) {
        return new ResponseVO(200, msg, null);
    }

    // 成功响应 - 有数据且自定义消息
    public static <T> ResponseVO<T> ok(T data, String msg) {
        return new ResponseVO<>(200, msg, data);
    }

    // 错误响应 - 默认消息
    public static ResponseVO error() {
        return new ResponseVO(500, "操作失败", null);
    }

    // 错误响应 - 自定义消息
    public static ResponseVO error(String msg) {
        return new ResponseVO(500, msg, null);
    }

    // 错误响应 - 自定义错误码和消息
    public static ResponseVO error(int code, String msg) {
        return new ResponseVO(code, msg, null);
    }

    // 错误响应 - 有数据
    public static <T> ResponseVO<T> error(String msg, T data) {
        return new ResponseVO<>(500, msg, data);
    }

    // 通用失败响应
    public static ResponseVO fail(int code, Object data) {
        return new ResponseVO(code, "操作失败", data);
    }

    // 通用失败响应 - 自定义消息
    public static ResponseVO fail(int code, String msg, Object data) {
        return new ResponseVO(code, msg, data);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}