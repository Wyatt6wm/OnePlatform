package run.wyatt.oneplatform.system.model.form;

import lombok.Data;

import java.util.List;

/**
 * @author Wyatt
 * @date 2023/7/12 15:42
 */
@Data
public class BindForm {
    private Long userId;
    private List<Long> bindList;
    private List<Long> unbindList;
}
