package run.wyatt.oneplatform.system.model.form;

import lombok.Data;

import java.util.List;

/**
 * @author Wyatt
 * @date 2023/7/8 23:30
 */
@Data
public class GrantForm {
    private Long roleId;
    private List<Long> grantList;
    private List<Long> ungrantList;
}
