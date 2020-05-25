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

@SuppressWarnings("serial")
public class EntityWrapperGeneratorSelfFkPopulation extends EntityGenerator {

	public void generateEntityWrapper(File directory, String table, Map<String, String> info) throws IOException {
		String className = StringUtil.swithToUpperCase(table, "_");
		String path = directory.getAbsolutePath();
		String packageName = path.substring(path.lastIndexOf("db\\entities") + "db\\entities".length()).replace("\\", ".");
		String template = StringUtil.replace(
				"package db.entities<Package>;\r\n" + 
						"\r\n" + 
						"<Imports>\r\n" + 
						"\r\n" + 
						"class <ClassName>DbWrapper implements IEntityWrapper {\r\n" + 
						"\r\n" + 
						"	private static final String TableName = \"<TableName>\";\r\n\n" +
						"	private static String UpdateStr = \"update %s set %s where id = %d;\";\r\n" + 
						"	private static final String InsertStr = String.format(\"insert into %s values (<PreparedPlaceHoldrs>)\", TableName);\r\n" +
						"	public static String[] FieldsNames = \r\n" + 
						"		new String[] { <Names> };\r\n\n" + 
						"	private <ClassName> _entity<ClassName>;\r\n" + 
						"	\r\n" + 
						"	@Override\r\n" +
						"	public void populateEntity(ResultSet rs, IForeginKeysHandler collector) throws SQLException, InterruptedException {\r\n" + 
						"<GetValue>\r\n" + 
						"	}\r\n\n" +
						"	@Override\r\n" +
						"	public void populatePreparedStatement(StatementType type, PreparedStatement ps, IForeginKeysHandler updater) throws SQLException, InterruptedException {\r\n" + 
						"<SetValue>\r\n" + 
						"<ForeignKeys>" + 
						"	}\r\n\n" +
						"	private void postSetEntity() {\r\n" + 
						"		UpdateStr = String.format(UpdateStr, \r\n" + 
						"						TableName, \r\n" +
						"						\"<PreparedUpdate>\", \r\n" +
						"						_entity<ClassName>.getId());\r\n" +
						"	}\r\n\n" + 
						"	@Override\r\n" +
						"	public String getTableName() {\r\n" +
						"		return TableName;\r\n" + 
						"	}\r\n\n" + 
						"	@Override\r\n" +
						"	public String getUpdateStr() {\r\n" + 
						"		return UpdateStr;\r\n" + 
						"	}\r\n\n" + 
						"	@Override\r\n" +
						"	public String getInsertStr() {\r\n" + 
						"		return InsertStr;\r\n" + 
						"	}\r\n\n" + 
						"	@Override\r\n" +
						"	public String[] getFieldsNames() {\r\n" +
						"		return FieldsNames;\r\n" + 
						"	}\r\n\n" + 
						"	@Override\r\n" +
						"	public Object[] getFieldsValues() {\r\n" +
						"		return new Object[] { <Values> };\r\n" + 
						"	}\r\n\n" + 
						"	@Override\r\n" +
						"	public String[] getFieldsValuesAsStrings() {\r\n" +
						"		return new String[] { <ValuesAsStrings> };\r\n" + 
						"	}\r\n\n" + 
						"	@Override\r\n" + 
						"	public Integer getId() {\r\n" + 
						"		return _entity<ClassName>.getId();\r\n" + 
						"	}\r\n\n" +
						"	@Override\r\n" +
						"	public void setEntity(IEntity initial<ClassName>) {\r\n" +
						"		_entity<ClassName> = (<ClassName>) initial<ClassName>;\r\n" + 
						"		postSetEntity();\r\n" + 
						"	}\r\n\n" + 
						"	@Override\r\n" +
						"	public IEntity getEntity() {\r\n" + 
						"		return _entity<ClassName>;\r\n" + 
						"	}\r\n"+
						"}", new HashMap<String, String>() {{
							put("<ClassName>", className);
							put("<TableName>", table);
							put("<Package>", packageName);
						}});

		List<String> checkers = new ArrayList<String>();
		List<String> assersions = new ArrayList<String>();
		List<String> values = new ArrayList<String>();
		List<String> stringValues = new ArrayList<String>();
		Set<String> imports = new HashSet<String>();
		List<String> names = new ArrayList<String>();
		List<String> preparedUpdate = new ArrayList<String>();
		List<String> preparedPlaceHolder = new ArrayList<String>();
		List<String> updateForeignKeys = new ArrayList<String>();
		List<String> insertForeignKeys = new ArrayList<String>();

		imports.add("import db.connections.MySqlConnection.StatementType;");
		imports.add("import java.sql.PreparedStatement;");
		imports.add("import java.sql.ResultSet;");
		imports.add("import java.sql.SQLException;");
		imports.add("import db.entities.IEntity;");
		imports.add("import db.entities.wrappers.IEntityWrapper;");
		imports.add("import db.IForeginKeysHandler;");
		imports.add(String.format("import db.entities.%s;", className));

		int i = 0;
		boolean fkFound = false;
		for (Entry<String, String> entry : info.entrySet()) {
			String name = entry.getKey();
			if (!name.equals("id")) {
				names.add(String.format("\"%s\"", name));
				String type = entry.getValue();
				String typeName = type.substring(type.lastIndexOf(".") + 1, type.length());
				String attributeName = StringUtil.swithToUpperCase(name.replaceAll("_fk$", ""), "_");

				imports.add(String.format("import %s;", type));
				preparedUpdate.add(String.format("%s = ?", name));
				boolean isFk = false;
				if (name.endsWith("_fk")) {
					fkFound = true;
					isFk = true;
					updateForeignKeys.add(StringUtil.replace(
							"		updater.update(_entity<ClassName>.get<AttributeName>());"
							, new HashMap<String, String>() {{
								put("<ClassName>", className);
								put("<AttributeName>", attributeName);
							}}));
					insertForeignKeys.add(StringUtil.replace(
							"		ps.setObject(" + i + ", updater.insert(_entity<ClassName>.get<AttributeName>()));"
							, new HashMap<String, String>() {{
								put("<ClassName>", className);
								put("<AttributeName>", attributeName);
							}}));
				}
				else {
					checkers.add(StringUtil.replace(
							"		ps.setObject(" + i + ", _entity<ClassName>.get<AttributeName>());"
							, new HashMap<String, String>() {{
								put("<ClassName>", className);
								put("<AttributeName>", attributeName);
							}}));
				}
				boolean isFkF = isFk;
				assersions.add(StringUtil.replace(
						"		_entity<ClassName>.set<AttributeName>((<TypeName>) <Fk>;"
						, new HashMap<String, String>() {{
							put("<ClassName>", className);
							put("<AttributeName>", attributeName);
							put("<TypeName>", typeName);
							put("<Name>", name);
							put("<Fk>", isFkF ? "collector.collect(<TypeName>.class, rs.getInt(\"<Name>\")))" : "rs.getObject(\"<Name>\"))");
						}}));
				values.add(StringUtil.replace("_entity<ClassName>.get<AttributeName>()<Fk>", 
						new HashMap<String, String>() {{
							put("<AttributeName>", attributeName);
							put("<ClassName>", className);
							put("<Fk>", isFkF ? ".getId()" : "");
						}}));
				stringValues.add(StringUtil.replace("<CommasBefore>_entity<ClassName>.get<AttributeName>()<Fk><CommasAfter>", 
						new HashMap<String, String>() {{
							put("<AttributeName>", attributeName);
							put("<ClassName>", className);
							put("<CommasBefore>", "String".equals(typeName) ? "\"'\" + " : "");
							put("<CommasAfter>", "String".equals(typeName) ? " + \"'\"" : ".toString()");
							put("<Fk>", isFkF ? ".getId()" : "");
						}}));
				preparedPlaceHolder.add("?");
			}
			++i;
		}
		boolean isFkFound = fkFound;
		template = StringUtil.replace(template, new HashMap<String, String>() {{
			put("<SetValue>", String.join("\n", checkers));
			put("<GetValue>", String.join("\n", assersions));
			put("<Imports>", String.join("\n", imports));
			put("<Names>", String.join(", ", names));
			put("<Values>", String.join(", ", values));
			put("<ValuesAsStrings>", String.join(", ", stringValues));
			put("<PreparedUpdate>", String.join(", ", preparedUpdate));
			put("<PreparedPlaceHoldrs>", String.join(", ", preparedPlaceHolder));
			put("<ForeignKeys>",	isFkFound ?   
					"		if (StatementType.Update.equals(type)) {\r\n" + 
							"	<UpdateForeignKeys>\r\n" +	
							"		}\r\n" +
							"		else if (StatementType.Insert.equals(type)) {\r\n" + 
							"	<InsertForeignKeys>\r\n" +	
					"		}\r\n" : "");
		}});
		template = StringUtil.replace(template, new HashMap<String, String>() {{
			put("<UpdateForeignKeys>", String.join("\n", updateForeignKeys));
			put("<InsertForeignKeys>", String.join("\n", insertForeignKeys));
		}});

		File file = new File(String.format("%s\\%s.java", directory.getAbsolutePath(), className + "DbWrapper"));
		FileUtil.writeToFile(file, template);
		System.out.println("");
	}
}
