package run.wyatt.oneplatform.model.exception;

/**
 * @author Wyatt
 * @date 2023/6/21 10:35
 */
public class DatabaseException extends RuntimeException {
    public DatabaseException() {
        super("数据库错误");
    }
}
