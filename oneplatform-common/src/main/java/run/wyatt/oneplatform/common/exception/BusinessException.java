package run.wyatt.oneplatform.common.exception;

/**
 * @author Wyatt
 * @date 2023/6/21 15:09
 */
public class BusinessException extends RuntimeException {
    public BusinessException() {
        super("业务逻辑处理错误");
    }

    public BusinessException(String mesg) {
        super(mesg);
    }
}
