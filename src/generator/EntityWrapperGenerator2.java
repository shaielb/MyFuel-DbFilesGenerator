package generator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import utilities.FileUtil;
import utilities.StringUtil;

public class EntityWrapperGenerator2 extends EntityGenerator {

	public void generateEntityWrapper(File directory, String table, Map<String, String> info) throws IOException {
		Set<String> imports = new HashSet<String>();
		List<String> fields = new ArrayList<String>();
		List<String> accessers = new ArrayList<String>();
		List<String> names = new ArrayList<String>();
		
		String className = StringUtil.swithToUpperCase(table, "_");

		imports.add("import java.util.ArrayList;");
		imports.add("import java.util.List;");
		imports.add("import java.sql.ResultSet;");
		imports.add("import java.sql.SQLException;");
		imports.add("import db.entities.IEntity;");
		imports.add("import db.entities.wrappers.IEntityWrapper;");
		imports.add(String.format("import db.entities.%s.%s;", className.toLowerCase(), className));
		
		fields.add(String.format("\tpublic static String TableName = \"%s\";", table));
		fields.add(String.format("private %s _entity%s", className, className));

		String classTitle = String.format("public class %sDbWrapper implements IEntityWrapper {", className);
		int i = 1;
		for (Entry<String, String> entry : info.entrySet()) {
			String name = entry.getKey();
			String type = entry.getValue();
			String typeName = type.substring(type.lastIndexOf(".") + 1, type.length());
			String attributeName = StringUtil.swithToUpperCase(name, "_");
			
			names.add(String.format("\"%s\"", name));

			imports.add(String.format("import %s;", type));
			fields.add(String.format("\tprivate %s _%s;", name, i, typeName, name));
			accessers.add(String.format("\tpublic %s get%s() {\n\t\treturn _%s;\n\t}", 
					typeName, attributeName, name));
			accessers.add(String.format("\tpublic void set%s(%s %s) {\n\t\t_%s = %s;\n\t}", 
					attributeName, typeName, name, name, name));
			++i;
		}
		
		fields.add(1, String.format("\tpublic static String[] FieldsNames = \r\n" + 
				"\t\tnew String[] { %s }", String.join(", ", names)));

		createTableFile(directory, classTitle, className, imports, fields, null, accessers);
		System.out.println("");
	}
}
