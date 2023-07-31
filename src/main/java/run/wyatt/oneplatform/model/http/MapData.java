package run.wyatt.oneplatform.model.http;

import java.util.HashMap;

/**
 * @author Wyatt
 * @date 2023/7/6 16:00
 */
public class MapData extends HashMap<String, Object> {
    public MapData() {
        super();
    }

    public MapData(String key, Object value) {
        super();
        this.put(key, value);
    }
}
