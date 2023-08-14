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
 * @date 2023/6/9 10:42
 */
@Data
@Entity
@Table(name = "tb_um_user", indexes = {
        @Index(name = "i_um_user_username", columnList = "username", unique = true)
})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "snowflakeIdGenerator")
    @GenericGenerator(name = "snowflakeIdGenerator", strategy = "run.wyatt.oneplatform.model.entity.support.SnowflakeIdGenerator")
    private Long id;            // 主键（雪花算法生成）
    @Column(nullable = false)
    private String username;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private String salt;
    @Column
    private String nickname;
    @Column(length = 1000)
    private String motto;
    @Column(length = 1000)
    private String avatar;
}
