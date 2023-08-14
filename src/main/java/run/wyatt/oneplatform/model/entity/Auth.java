package run.wyatt.oneplatform.model.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * @author Wyatt
 * @date 2023/6/12 14:49
 */
@Data
@Entity
@Table(name = "tb_um_auth", indexes = {
        @Index(name = "i_um_auth_identifier", columnList = "identifier", unique = true),
        @Index(name = "i_um_auth_name", columnList = "name"),
})
public class Auth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "snowflakeIdGenerator")
    @GenericGenerator(name = "snowflakeIdGenerator", strategy = "run.wyatt.oneplatform.model.entity.support.SnowflakeIdGenerator")
    private Long id;            // 主键（雪花算法生成）
    @Column(nullable = false)
    private String identifier;  // 权限标识符
    @Column
    private String name;        // 权限名称
    @Column(length = 1000)
    private String description; // 权限描述
    @Column(nullable = false)
    private Boolean activated = false;  // 激活标志
}
