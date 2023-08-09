package run.wyatt.oneplatform.dao;

import org.apache.ibatis.annotations.Mapper;
import run.wyatt.oneplatform.model.entity.TodoLog;

import java.util.Collection;
import java.util.List;

/**
 * @author Wyatt
 * @date 2023/7/18 12:34
 */
@Mapper
public interface TodoLogDao {
    long insert(TodoLog record);
    List<TodoLog> findByUuids(Long tenant, Collection<String> uuids);
    List<TodoLog> findByTodoUuidSortBySubmitTimeDesc(Long tenant, String todoUuid);
}
