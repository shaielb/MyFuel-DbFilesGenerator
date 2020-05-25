package generator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import utilities.FileUtil;

public class EntityGenerator {

	protected void createTableFile(File directory, String classTitle, String className, 
			Set<String> imports, List<String> fields, List<String> constructor, List<String> accessers) throws IOException {
		File file = new File(String.format("%s\\%s.java", directory.getAbsolutePath(), className));
		List<String> list = new ArrayList<String>();
		String path = directory.getAbsolutePath();
		path = path.substring(path.indexOf("\\src\\") + "\\src\\".length()).replace("\\", ".");
		list.add(String.format("package %s;\n\n", path));
		if (imports != null && imports.size() > 0) {
			for (String importStr : imports) {
				list.add(importStr + "\n");
			}
			list.add("\n");
		}
		list.add(classTitle + "\n\n");
		if (fields != null && fields.size() > 0) {
			for (String filed : fields) {
				list.add(filed + "\n\n");
			}
		}
		if (constructor != null && constructor.size() > 0) {
			list.add(String.format("\tpublic %s() {\n", className));
			for (String constructorLines: constructor) {
				list.add(constructorLines + "\n");
			}
			list.add("\t}\n\n");
		}
		if (accessers != null && accessers.size() > 0) {
			for (String accesser : accessers) {
				list.add(accesser + "\n\n");
			}
		}
		list.add("}");
		FileUtil.writeToFile(file, list);
	}
}
