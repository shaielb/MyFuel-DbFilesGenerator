package db.types;

import java.util.HashMap;
import java.util.Map;

public class SqlTypes {

	/**
	 * 
	 */
	private static Map<String, String> _map = new HashMap<String, String>() {{
		put("String", "String");
		put("BigDecimal", "BigDecimal");
	}};
	
	/**
	 * @param type
	 * @return
	 */
	public static String get(String type) {
		return _map.get(type);
	}
}
