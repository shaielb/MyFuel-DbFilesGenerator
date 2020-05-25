package generator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import utilities.StringUtil;

public class ServicesGenerator extends EntityGenerator {

	public void generateEntityPools(File directory, String[] tables) throws IOException {
		Set<String> imports = new HashSet<String>();
		List<String> intializer = new ArrayList<String>();
		List<String> fields = new ArrayList<String>();
		List<String> accessers = new ArrayList<String>();

		imports.add("import java.util.HashMap;");
		imports.add("import java.util.Map;");
		imports.add("import db.interfaces.*;");
		imports.add("import annotations.AutoGenerated;");

		String className = "Services";
		String classTitle = String.format("@AutoGenerated\npublic class %s {", className);

		fields.add("	public interface Instansiate<T> {\r\n" + 
				"		T create();\r\n" + 
				"	}");

		fields.add("	private static Map<String, Instansiate<IEntity>> _entityMap = \r\n" + 
				"			new HashMap<String, Instansiate<IEntity>>();");
		fields.add("	private static Map<String, IEntityBridge> _bridgeMap = \r\n" + 
				"			new HashMap<String, IEntityBridge>();");

		accessers.add(
				"	public static IEntity createEntity(String table) {\r\n" + 
						"		return _entityMap.get(table).create();\r\n" + 
				"	}");
		accessers.add(
				"	public static IEntityBridge getBridge(String table) {\r\n" + 
						"		return _bridgeMap.get(table);\r\n" + 
				"	}");

		for (String table : tables) {
			String entityName = StringUtil.swithToUpperCase(table, "_");

			imports.add(String.format("import db.entity.%s;", entityName));
			imports.add(String.format("import db.entity.bridge.%s;", entityName + "Bridge"));

			intializer.add(String.format("\t\t_entityMap.put(\"%s\", () -> new %s());", 
					table, entityName));
			intializer.add(String.format("\t\t_bridgeMap.put(\"%s\", %s.getInstance());", 
					table, entityName + "Bridge"));
		}
		
		accessers.add(0, String.format(
				"	public static void initialize() {\r\n" + 
						"%s\r\n" + 
				"	}", String.join("\r\n", intializer)));

		createTableFile(directory, classTitle, className, imports, fields, null, accessers);
		System.out.println("");
	}
}
