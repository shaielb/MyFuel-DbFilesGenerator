package generator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import utilities.FileUtil;
import utilities.StringUtil;
import xml.parser.XmlParser;

@SuppressWarnings("serial")
public class ControlsMapGenerator extends EntityGenerator {

	public static String PreservedMembersStart = "// preserved members start";
	public static String PreservedMembersEnd = "// preserved members end";

	public static String RegExTypeStart = "// #[$\\w]+ %s start";
	public static String RegExTypeEnd = "// #[$\\w]+ %s end";

	public static String Callback = "callback";
	public static String PrepareQuery = "prepareQuery";

	Set<String> controlsInstanciators = new LinkedHashSet<String>();
	Set<String> instanciators = new LinkedHashSet<String>();
	Set<String> entitiesAssignments = new LinkedHashSet<String>();
	Set<String> tables = new LinkedHashSet<String>();
	Set<String> sceneControls = new LinkedHashSet<String>();
	Set<String> initializers = new LinkedHashSet<String>();
	List<String> fields = new ArrayList<String>();
	List<String> groupedControls = new ArrayList<String>();
	Set<String> entitiesMembers = new LinkedHashSet<String>();
	Set<String> entitiesGetSet = new LinkedHashSet<String>();
	Set<String> controlsMembers = new LinkedHashSet<String>();
	Map<String, List<String>> presevedCallbacks = new HashMap<String, List<String>>();
	Map<String, List<String>> presevedPrepareQury = new HashMap<String, List<String>>();
	List<String> presevedMembers = new ArrayList<String>();

	String queryMap = "";

	public void initializeControls() {
		controlsInstanciators = new LinkedHashSet<String>();
		instanciators = new LinkedHashSet<String>();
		entitiesAssignments = new LinkedHashSet<String>();
		tables = new LinkedHashSet<String>();
		sceneControls = new LinkedHashSet<String>();
		initializers = new LinkedHashSet<String>();
		fields = new ArrayList<String>();
		groupedControls = new ArrayList<String>();
		entitiesMembers = new LinkedHashSet<String>();
		entitiesGetSet = new LinkedHashSet<String>();
		controlsMembers = new LinkedHashSet<String>();
		presevedMembers = new ArrayList<String>();
		presevedCallbacks = new HashMap<String, List<String>>();
		presevedPrepareQury = new HashMap<String, List<String>>();
		
		queryMap = "";
	}

	private Map<String, List<String>> readLinesToPreserveById(String file, String type) throws IOException {//callback
		if (file.contains("LogInScreen")) {
			System.out.println();
		}
		List<String> lines = FileUtil.readAllLines(file);
		boolean collect = false;
		String currentKey = "";
		List<String> currentLines = new ArrayList<String>();
		Map<String, List<String>> linesToPreserveMap = new HashMap<String, List<String>>();
		for (String line : lines) {
			if (line.contains("//")) {
				System.out.println();
			}
			if (line.matches("\t+" + String.format(RegExTypeStart, type))) {
				currentKey = line.substring(line.indexOf("#") + 1, line.indexOf(" " + type + " start"));
				collect = true;
			}
			if (collect) {
				currentLines.add(line);
			}
			if (line.matches("\t+" + String.format(RegExTypeEnd, type))) {
				linesToPreserveMap.put(currentKey, currentLines);
				collect = false;
			}
		}
		return linesToPreserveMap;
	}

	private List<String> readLinesToPreserve(String file, String key, String byStart, String byEnd) throws IOException {//callback
		List<String> lines = FileUtil.readAllLines(file);
		boolean collect = false;
		List<String> linesToRet = new ArrayList<String>();
		Map<String, List<String>> linesToPreserveMap = new HashMap<String, List<String>>();
		for (String line : lines) {
			if (line.contains(byStart)) {
				collect = true;
			}
			if (collect) {
				linesToRet.add(line);
			}
			if (line.contains(byEnd)) {
				collect = false;
			}
		}
		return linesToRet;
	}

	public void generateSceneSwitch(File[] files) throws IOException {
		List<String> list = new ArrayList<String>();
		for (File file : files) {
			String fName = file.getName();
			if (fName.endsWith(".fxml")) {
				String className = fName.substring(0, fName.lastIndexOf("."));
				list.add(String.format(
						"					case \"%s\" : \r\n" + 
								"						_scenesMap.put(\"%s\", sb = new %s(_sceneSwitcher, _client, _context));\r\n" + 
								"						break;\r\n", className, className, className));
			}
		}

		String template = "package sceneswitch;\r\n" + 
				"\r\n" + 
				"import java.io.IOException;\r\n" +
				"import java.util.HashMap;\r\n" + 
				"import java.util.HashSet;\r\n" + 
				"import java.util.Map;\r\n" + 
				"import db.entity.SystemUser;\r\n" +
				"import java.util.Set;\r\n" + 
				"\r\n" + 
				"import javafx.stage.Stage;\r\n" + 
				"import messages.request.IUpdate;\r\n" + 
				"import sceneswitch.SceneBase.ISceneSwitcher;\r\n" + 
				"import wrapper.*;\r\n" + 
				"import client.IClient;\r\n" + 
				"import db.interfaces.IEntity;\r\n" + 
				"\r\n" +
				"public class ScenesSwitch {\r\n" + 
				"\r\n" + 
				"	private IClient _client;\r\n\n" + 
				"	private Context _context = new Context();\r\n\n" + 
				"	private Map<String, SceneBase> _scenesMap = new HashMap<String, SceneBase>();\r\n\n" + 
				"	private ISceneSwitcher _sceneSwitcher;\r\n" +
				"	\r\n" + 
				"	public ScenesSwitch(Stage stage, IClient client) throws Exception {\r\n" + 
				"		_client = client;\r\n" + 
				"		_sceneSwitcher = (scenesName) -> {\r\n" + 
				"			stage.setScene(getScene(scenesName).getScene());\r\n" +
				"		};\r\n" + 
				"	}\r\n" + 
				"\r\n" + 
				"	public void onClose() throws IOException {\r\n" + 
				"		SystemUser su = _context.getSystemUser();\r\n" + 
				"		if (su != null) {\r\n" + 
				"			su.setOnlineStatus(false);\r\n" + 
				"			IUpdate updateRequest = _client.getUpdateRequest();\r\n" + 
				"			Set<IEntity> set = new HashSet<IEntity>();\r\n" + 
				"			set.add(su);\r\n" + 
				"			updateRequest.setEntities(set);\r\n" + 
				"			_client.sendRequest(updateRequest);\r\n" + 
				"		}\r\n" + 
				"	}\r\n\n" +
				"	public SceneBase getScene(String scenesName) {\r\n" + 
				"			SceneBase sb = _scenesMap.get(scenesName);\r\n" + 
				"			if (sb == null) {\r\n" + 
				"				try {\r\n" +
				"					switch(scenesName) {\r\n" + 
				String.join("\r\n", list) +
				"					}\r\n" + 
				"				} catch (Exception e) {\r\n" + 
				"					e.printStackTrace();\r\n" + 
				"				}\r\n" +
				"			}\r\n" + 
				"		return sb;\r\n" + 
				"	}\r\n" + 
				"}\r\n";
		File fileToWrite = new File("C:\\Java\\myFuelWorkspace\\MyFuelClient\\src\\sceneswitch\\ScenesSwitch.java");
		FileUtil.writeToFile(fileToWrite, template);
	}

	public void generateControlsWrappers(File directoryTo) throws Exception {
		Set<String> imports = new HashSet<String>();
		imports.add("import java.util.HashMap;");
		imports.add("import java.util.Map;");
		imports.add("import java.util.ArrayList;");
		imports.add("import java.util.List;");
		imports.add("import javafx.scene.Scene;");
		imports.add("import javafx.scene.image.ImageView;");
		imports.add("import controls.*;");
		imports.add("import sceneswitch.SceneBase;");
		imports.add("import javafx.fxml.FXMLLoader;");
		imports.add("import javafx.scene.Parent;");
		imports.add("import javafx.scene.control.*;");
		imports.add("import annotations.AutoGenerated;");
		imports.add("import action.*;");
		imports.add("import java.util.HashSet;");
		imports.add("import java.util.Set;");
		imports.add("import db.interfaces.IEntity;");
		imports.add("import javafx.scene.text.Text;");
		imports.add("import javafx.scene.layout.BorderPane;");
		imports.add("import widgets.table.Table;");
		imports.add("import table.MfTable;");
		imports.add("import widgets.table.*;");
		imports.add("import db.entity.*;");
		imports.add("import application.Main;");
		imports.add("import java.util.Collection;");
		imports.add("import sceneswitch.Context;");
		imports.add("import messages.QueryContainer;");
		imports.add("import client.IClient;\r\n");

		File outputDir = directoryTo;//"C:\\Java\\myFuelWorkspace\\MyFuelClient\\src\\wrapper");
		File[] files = new File("C:\\Java\\myFuelWorkspace\\MyFuelClient\\src\\application").listFiles();

		generateSceneSwitch(files);

		for (File file : files) {
			String fName = file.getName();
			if (fName.endsWith(".fxml")) {
				initializeControls();
				String className = fName.substring(0, fName.lastIndexOf("."));
				File fileToWrite = new File(String.format("%s\\%s.java", outputDir, className));

				presevedCallbacks = readLinesToPreserveById(fileToWrite.getAbsolutePath(), Callback);
				presevedPrepareQury = readLinesToPreserveById(fileToWrite.getAbsolutePath(), PrepareQuery);
				presevedMembers = readLinesToPreserve(fileToWrite.getAbsolutePath(), "members", PreservedMembersStart, PreservedMembersEnd);
				if (presevedPrepareQury.size() > 0) {
					System.out.println();
				}

				//"C:\\Java\\myFuelWorkspace\\MyFuelClient\\src\\application\\HomeHeatingTrack.fxml"
				prepareCollectionsFromFile(file.getAbsolutePath());

				if (groupedControls.size() > 0) {
					imports.add("import adapter.base.ControlAdapter;");
				}
				String template = StringUtil.replace(
						"package wrapper;\r\n" + 
								"\r\n" + 
								"<Imports>\r\n" + 
								"\r\n" + 
								"@AutoGenerated\r\n" + 
								"public class <ClassName> extends SceneBase {\r\n" + 
								"\r\n" + 
								"<EntitiesMembers>\r\n" + 
								"\r\n" + 
								"<ControlsMembers>\r\n" + 
								"<PreservedMembers>" + 
								"\r\n" + 
								"	public <ClassName>(ISceneSwitcher sceneSwitcher, IClient client, Context context) throws Exception {\r\n" + 
								"		super(sceneSwitcher, client, context);\r\n" + 
								"		initialize();\r\n" + 
								"	}\r\n\n" + 
								"	public void initialize() throws Exception {\r\n" + 
								"		Parent root = FXMLLoader.load(Main.class.getResource(\"<FileName>\"));\r\n" + 
								"		_scene = new Scene(root);\r\n\n" +
								"<Lines>\r\n" + 
								"	}\r\n" +
								"<QueryMap>\r\n" + 
								"\r\n" + 
								"<EntitiesGetSet>\r\n" + 
								"}", new HashMap<String, String>() {{
									put("<EntitiesMembers>", String.join("\r\n", entitiesMembers));
									put("<ControlsMembers>", String.join("\r\n", controlsMembers));
									put("<EntitiesGetSet>", String.join("\r\n\n", entitiesGetSet));
									put("<Imports>", String.join("\r\n", imports));
									put("<ClassName>", className);
									put("<FileName>", fName);
									put("<Lines>", getLines());
									put("<QueryMap>", queryMap);
									put("<PreservedMembers>", (presevedMembers.size() > 0) ? 
											String.join("\r\n", presevedMembers) + "\r\n\n" : "");
								}});

				FileUtil.writeToFile(fileToWrite, template);
			}
		}

		System.out.println("");
	}

	private String getLines() {
		return StringUtil.replace(
				"<SceneControls>" + 
						"<EntitiesInstanciation>" +
						"<EntitiesAssignments>" + 
						"<ControlsInstanciation>" +
						"<Tables>" + 
						"<EntitiesInitializers>" +
						"<EntitiesFields>" +
						"<Groups>", new HashMap<String, String>() {{
							put("<EntitiesInstanciation>", 
									(instanciators.size() > 0) ? ("\t\t//entities instantiation\r\n" + 
											String.join("\r\n", instanciators) + "\r\n\n") : "");
							put("<ControlsInstanciation>",
									(controlsInstanciators.size() > 0) ? ("\t\t//controls instantiation\r\n" + 
											String.join("\r\n", controlsInstanciators) + "\r\n\n") : "");
							put("<EntitiesInitializers>",
									(initializers.size() > 0) ? ("\t\t//initializations\r\n" + 
											String.join("\r\n\n", initializers) + "\r\n\n") : "");
							put("<EntitiesFields>", 
									(fields.size() > 0) ? ("\t\t//fields initializations\r\n" + 
											String.join("\r\n\n", fields) + "\r\n\n") : "");
							put("<Groups>",
									(groupedControls.size() > 0) ? ("\t\t//grouping\r\n" + 
											String.join("\r\n", groupedControls) + "\r\n") : "");
							put("<SceneControls>", 
									(sceneControls.size() > 0) ? ("\t\t//scene switchers\r\n" + 
											String.join("\r\n\n", sceneControls) + "\r\n\n") : "");
							put("<Tables>", 
									(tables.size() > 0) ? ("\t\t//tables instantiation\r\n" + 
											String.join("\r\n\n", tables) + "\r\n\n") : "");
							put("<EntitiesAssignments>", 
									(entitiesAssignments.size() > 0) ? ("\t\t//entities assignments\r\n" + 
											String.join("\r\n", entitiesAssignments) + "\r\n\n") : "");
						}});
	}

	public interface PopulateEntities {
		public String populate(String[] entitiesParts, String field, String nameType);
	}

	private String populateEntities(String[] entitiesParts, String field, String nameType) {
		String fieldObject = "";
		for (int i = 1; i < entitiesParts.length - 1; ++i) {
			String className = StringUtil.swithToUpperCase(entitiesParts[i], "_");
			String objectName = StringUtil.firstToLowerCase(className);

			try {
				Class.forName("db.entity." + className).getDeclaredField("_" + field);
				fieldObject = objectName += nameType;
			} catch (Exception e) {}

			entitiesMembers.add(String.format("\tprivate %s _%s;", className, objectName));
			entitiesGetSet.add(String.format("\tpublic %s get%s() {\n\t\t return _%s;\n\t}", className, 
					StringUtil.firstToUpperCase(objectName), objectName));
			entitiesGetSet.add(String.format("\tpublic void set%s(%s %s) {\n\t\t _%s = %s;\n\t}", 
					StringUtil.firstToUpperCase(objectName), className
					, objectName, objectName, objectName));

			instanciators.add(String.format("\t\t_%s = new %s();", objectName, className));
			if (i > 1) {
				for (int j = 1; j < entitiesParts.length - 1; ++j) {
					String classNameJ = StringUtil.swithToUpperCase(entitiesParts[j], "_");
					String objectNameJ = StringUtil.firstToLowerCase(classNameJ);

					try {
						Class.forName("db.entity." + classNameJ).getDeclaredField("_" + field);
						objectNameJ += nameType;
					} catch (Exception e) {}

					try {
						Class.forName("db.entity." + classNameJ).getDeclaredField("_" + entitiesParts[i]);
						entitiesAssignments.add(String.format("\t\t_%s.set%s(_%s);", objectNameJ, className, objectName));
					} catch (Exception e) {
						try {
							Class.forName("db.entity." + className).getDeclaredField("_" + entitiesParts[j]);
							entitiesAssignments.add(String.format("\t\t_%s.set%s(_%s);", objectName, classNameJ, objectNameJ));
						} catch (Exception e1) {}
					}
				}
			}
		}
		return fieldObject;
	}

	private void populateTableControl(String controlName, String fxId, Map<String, List<String>> grouped, 
			PopulateEntities populateEntities) {
		String[] fxIdParts = fxId.split("\\$\\$\\$");
		String nameType = "";
		String entityNameType = "";
		String enumType = "";
		String enumValue = "";
		if (fxIdParts.length > 1 && fxIdParts[1].contains("$")) {
			String[] firstParts = fxIdParts[0].split("\\$");
			enumType = firstParts[firstParts.length - 1];
			String[] ntParts = fxIdParts[1].split("\\$");
			enumValue = ntParts[0];
			fxIdParts[0] += "$" + ntParts[1];
		}
		if (fxIdParts.length > 1) {
			nameType = StringUtil.swithToUpperCase(fxIdParts[1], new String[] { "\\$", "_" });
			if (fxIdParts[1].contains("$")) {
				entityNameType = nameType;
			}
		}
		String nameTypeF = nameType;
		String fxIdF = fxId;

		String[] paramValuesParts = fxIdParts[0].split("\\$\\$");
		String[] entitiesParts = paramValuesParts[0].split("\\$");

		String field = entitiesParts[entitiesParts.length - 1];

		if (field.contains("special_sale_key")) {
			System.out.println();
		}

		String fieldVarName = StringUtil.swithToUpperCase(field, "_");
		fieldVarName = StringUtil.firstToLowerCase(fieldVarName);
		// check who's field this is
		String fieldObject = populateEntities.populate(entitiesParts, field, entityNameType);

		String fvName = fieldVarName;
		String fo = fieldObject;
		String value = (paramValuesParts.length > 1) ? StringUtil.firstToUpperCase(paramValuesParts[1]) : "";
		String setValidValue = "";
		String validValue = "";
		if (paramValuesParts.length > 1) {
			String enumClass = field;
			if (enumClass.endsWith("_enum")) {
				try {
					Class.forName("db.entity." + StringUtil.swithToUpperCase(field, "_"));
					validValue = String.format("_client.getEnum(\"%s\", \"%s\")", enumClass, paramValuesParts[1]);
				} catch (Exception e) {
					validValue = String.format("\"%s\"", paramValuesParts[1]);
				}
			}
			else {
				validValue = String.format("\"%s\"", paramValuesParts[1]);
			}
			String fValidValue = validValue;
			setValidValue = StringUtil.replace(
					"\n\t\t_<FieldVarName>Control<Value><NameType>.setValidValue(<ValidValue>);",
					new HashMap<String, String>() {{
						put("<FieldVarName>", fvName);
						put("<ValidValue>", fValidValue);
						put("<NameType>", nameTypeF);
					}});
		}

		controlsMembers.add(StringUtil.replace(
				"\tprivate Mf<Control> _<FieldVarName>Control<Value><NameType>;",
				new HashMap<String, String>() {{
					put("<Control>", controlName);
					put("<Value>", value);
					put("<FieldVarName>", fvName);
					put("<NameType>", nameTypeF);
				}}));

		controlsInstanciators.add(
				StringUtil.replace(
						"\t\t_<FieldVarName>Control<Value><NameType> = new Mf<Control>((<Control>) _scene.lookup(\"#<FxId>\"));",
						new HashMap<String, String>() {{
							put("<Control>", controlName);
							put("<Value>", value);
							put("<FieldVarName>", fvName);
							put("<FxId>", fxIdF);
							put("<NameType>", nameTypeF);
						}}));
		String fSetValidValue = setValidValue;
		fields.add(
				StringUtil.replace(
						"\t\t_<FieldVarName>Control<Value><NameType>.setField(_<Entity>.getClass().getDeclaredField(\"_<FieldName>\"));\r\n" +
								"\t\t_<FieldVarName>Control<Value><NameType>.setEntity(_<Entity>);<SetValidValue>",
								new HashMap<String, String>() {{
									put("<FieldVarName>", fvName);
									put("<Value>", value);
									put("<SetValidValue>", fSetValidValue);
									put("<FieldName>", field);
									put("<Entity>", fo);
									put("<NameType>", nameTypeF);
								}}));
		String groupKey = paramValuesParts[0];
		if (!grouped.containsKey(groupKey)) {
			grouped.put(groupKey, new ArrayList<String>());
		}
		grouped.get(groupKey).add("_" + fieldVarName + "Control" + value + nameTypeF);
	}

	private void populateActionControl(String controlName, String fxId) {
		String[] fxIdParts = fxId.split("\\$\\$\\$");
		String nameType = "";
		if (fxIdParts.length > 1 && fxIdParts[1].contains("$")) {
			String[] ntParts = fxIdParts[1].split("\\$");
			fxIdParts[0] += "$" + ntParts[1];
		}
		if (fxIdParts.length > 1) {
			nameType = StringUtil.swithToUpperCase(fxIdParts[1], "$");
		}
		String nameTypeF = nameType;
		String fxIdF = fxId;

		String[] parts = fxIdParts[0].split("\\$");
		String action = parts[1];
		String table = parts[parts.length - 1];
		String className = StringUtil.swithToUpperCase(table, "_");
		String objectName = StringUtil.firstToLowerCase(className);

		for (int i = 2; i < parts.length; ++i) {
			String cName = StringUtil.swithToUpperCase(parts[i], "_");
			String oName = StringUtil.firstToLowerCase(cName);

			entitiesMembers.add(String.format("\tprivate %s _%s;", cName, oName));
			instanciators.add(String.format("\t\t_%s = new %s();", oName, cName));
		}

		String capabilityEntity = "";
		String responseContent = "\t\t\t\n";
		if ("insert".equals(action) || "remove".equals(action)) {
			capabilityEntity = "\t\t<ObjectName><UAction>Capability<NameType>.addEntity(_" + objectName + ");\n";
		}
		else if ("collect".equals(action)) {
			action = "filter";
			capabilityEntity = "\t\t<ObjectName><UAction>Capability<NameType>.setQueryContainers(prepareQuery(_<ObjectName>));\n";

			List<String> listQury = presevedPrepareQury.get(fxIdF);
			List<String> listCallback = presevedCallbacks.get(fxIdF);
			if (listQury != null && listQury.size() > 0) {
				queryMap = String.join("\r\n", listQury) + "\n";
			}
			else {
				queryMap = StringUtil.replace(
						"\n	private List<QueryContainer> prepareQuery(<ClassName> <ObjectName>) {\r\n" + 
								"		List<QueryContainer> containers = new ArrayList<QueryContainer>();\r\n" + 
								"		\r\n" + 
								"		Map<String, String> queryMap = new HashMap<String, String>();\r\n" + 
								"		\r\n" + 
								"		QueryContainer container = new QueryContainer();\r\n" + 
								"		container.setQueryEntity(<ObjectName>);\r\n" + 
								"		container.setQueryMap(queryMap);\r\n" + 
								"		containers.add(container);\r\n" + 
								"		return containers;\r\n" +
								"	}\r\n",
								new HashMap<String, String>() {{
									put("<ObjectName>", objectName);
									put("<ClassName>", className);
								}});
			}
			if (listCallback != null && listCallback.size() > 0) {
				responseContent = String.join("\r\n", listCallback) + "\n";
			}
			else {
				responseContent = 
						"			Collection<IEntity> entities = response.getEntities();\r\n" + 
								"			for (IEntity ientity : entities) {\r\n" + 
								"				<ClassName> entity = (<ClassName>) ientity;\r\n" + 
								"			}\n";
			}

			//entityUiTable = "\t\t\t<ObjectName>Table.getObservableList().clear();\n" + 
			//"\t\t\t<ObjectName>Table.getObservableList().addAll(response.getEntitiesAsSet());\n";
		}
		else if ("update".equals(action)) {
			capabilityEntity = 
					"\t\tSet<IEntity> <ObjectName><UAction>Entities = new HashSet<IEntity>();\n" + 
							"\t\t<ObjectName><UAction>Capability<NameType>.setEntities(<ObjectName><UAction>Entities);\n";
		}

		String actionF = action.toLowerCase().equals("plain") ? "" : action;

		controlsMembers.add(StringUtil.replace(
				"\tprivate Mf<Control> _<Action><ClassName>Control<NameType>;",
				new HashMap<String, String>() {{
					put("<Control>", controlName);
					put("<Action>", actionF);
					put("<ClassName>", className);
					put("<NameType>", nameTypeF);
				}}));

		controlsMembers.add(StringUtil.replace(
				"\tprivate ActionControl _<ObjectName><Action>Action<NameType>;",
				new HashMap<String, String>() {{
					put("<ObjectName>", objectName);
					put("<Action>", actionF);
					put("<NameType>", nameTypeF);
				}}));

		initializers.add(
				StringUtil.replace(
						"\t\t_<Action><ClassName>Control<NameType> = new Mf<Control>((<Control>) _scene.lookup(\"#<FxId>\"));\n" + 
								"\t\t_<ObjectName><Action>Action<NameType> = new ActionControl();\n" + 
								"\t\t_<ObjectName><Action>Action<NameType>.setControl(_<Action><ClassName>Control<NameType>);\n" + 
								"\t\t<UAction>Capability <ObjectName><UAction>Capability<NameType> = new <UAction>Capability();\n" +
								capabilityEntity + 

								"\t\t_<ObjectName><Action>Action<NameType>.addCapability(<ObjectName><UAction>Capability<NameType>);\n" + 
								"\t\t_<ObjectName><Action>Action<NameType>.setClient(_client);\n" +
								"\t\t_<ObjectName><Action>Action<NameType>.setCallback((response) -> {\n" + 
								responseContent + 
								"\t\t});",
								new HashMap<String, String>() {{
									put("<ClassName>", className);
									put("<ObjectName>", objectName);
									put("<Control>", controlName);
									put("<Action>", actionF);
									put("<UAction>", StringUtil.firstToUpperCase(actionF));
									put("<FxId>", fxIdF);
									put("<NameType>", nameTypeF);
								}}));
	}

	private void populateUiTables(String controlName, String fxId) {
		String[] fxIdParts = fxId.split("\\$\\$\\$");
		String nameType = "";
		if (fxIdParts.length > 1 && fxIdParts[1].contains("$")) {
			String[] ntParts = fxIdParts[1].split("\\$");
			fxIdParts[0] += "$" + ntParts[1];
		}
		if (fxIdParts.length > 1) {
			nameType = StringUtil.swithToUpperCase(fxIdParts[1], "$");
		}
		String nameTypeF = nameType;
		String fxIdF = fxId;

		String[] parts = fxIdParts[0].split("\\$");
		String editable = parts[1];
		String type = parts[2];
		String table = parts[3];
		String className = StringUtil.swithToUpperCase(table, "_");
		String objectName = StringUtil.firstToLowerCase(className);

		entitiesMembers.add(String.format("\tprivate %s _%s;", className, objectName));
		instanciators.add(String.format("\t\t_%s = new %s();", objectName, className));

		controlsMembers.add(StringUtil.replace(
				"\tprivate Table<<ClassName>> _<FieldVarName>TableWrapper<NameType>;",
				new HashMap<String, String>() {{
					put("<ClassName>", className);
					put("<FieldVarName>", objectName);
					put("<NameType>", nameTypeF);
				}}));

		controlsMembers.add(StringUtil.replace(
				"\tprivate MfTable<<ClassName>> _<FieldVarName>Table<NameType>;",
				new HashMap<String, String>() {{
					put("<ClassName>", className);
					put("<FieldVarName>", objectName);
					put("<NameType>", nameTypeF);
				}}));

		tables.add(
				StringUtil.replace(
						"\t\tBorderPane <FieldVarName>Bp = (BorderPane) _scene.lookup(\"#<FxId>\");\n" + 
								"\t\t_<FieldVarName>TableWrapper<NameType> = new Table<<ClassName>>();\n" + 
								"\t\t_<FieldVarName>Table<NameType> = new MfTable<<ClassName>>(<ClassName>.class);\n" + 
								"\t\t_<FieldVarName>Table<NameType>.setEditable(<Editable>);\n" + 
								"\t\tMf<UType>Decorator<<ClassName>> <FieldVarName><UType>Decorator<NameType> = new Mf<UType>Decorator<<ClassName>>();\n" + 
								"\t\t_<FieldVarName>TableWrapper<NameType>.addDecorator(<FieldVarName><UType>Decorator);\n" + 
								"\t\t_<FieldVarName>TableWrapper<NameType>.setTable(_<FieldVarName>Table);\n" + 
								"\t\t<FieldVarName>Bp.setCenter(_<FieldVarName>Table<NameType>);",
								new HashMap<String, String>() {{
									put("<ClassName>", className);
									put("<FieldVarName>", objectName);
									put("<UType>", StringUtil.firstToUpperCase(type));
									put("<Editable>", "editable".equals(editable) ? "true" : "false");
									put("<FxId>", fxIdF);
									put("<NameType>", nameTypeF);
								}}));
	}

	private void populateUiSearch(String controlName, String fxId) {
		String[] fxIdParts = fxId.split("\\$\\$\\$");
		String nameType = "";
		if (fxIdParts.length > 1 && fxIdParts[1].contains("$")) {
			String[] ntParts = fxIdParts[1].split("\\$");
			fxIdParts[0] += "$" + ntParts[1];
		}
		if (fxIdParts.length > 1) {
			nameType = StringUtil.swithToUpperCase(fxIdParts[1], "$");
		}
		String nameTypeF = nameType;
		String fxIdF = fxId;

		String[] parts = fxIdParts[0].split("\\$");
		String editable = parts[1];
		String type = parts[2];
		String table = parts[3];
		String className = StringUtil.swithToUpperCase(table, "_");
		String objectName = StringUtil.firstToLowerCase(className);

		entitiesMembers.add(String.format("\tprivate %s _%s;", className, objectName));
		instanciators.add(String.format("\t\t_%s = new %s();", objectName, className));

		controlsMembers.add(StringUtil.replace(
				"\tprivate SearchPanel _<FieldVarName>SearchPnl;",
				new HashMap<String, String>() {{
					put("<FieldVarName>", objectName);
				}}));

		tables.add(
				StringUtil.replace(
						"\t\tBorderPane <FieldVarName>Bp = (BorderPane) _scene.lookup(\"#<FxId>\");\n" + 
								"\t\t_<FieldVarName>SearchPnl = new SearchPanel(<ClassName>.class, _client, (response) -> {\n" + 
								"\t\t\t<FieldVarName>Table<NameType>.setRows(response.getEntities());\n" + 
								"\t\t});\r\n" +
								"\t\t<FieldVarName>Bp.setCenter(_<FieldVarName>SearchPnl);",
								new HashMap<String, String>() {{
									put("<ClassName>", className);
									put("<FieldVarName>", objectName);
									put("<FxId>", fxIdF);
									put("<NameType>", nameTypeF);
								}}));
	}

	private void populateSceneControls(String controlName, String fxId) {
		String[] fxIdParts = fxId.split("\\$\\$\\$");
		String nameType = "";
		if (fxIdParts.length > 1 && fxIdParts[1].contains("$")) {
			String[] ntParts = fxIdParts[1].split("\\$");
			fxIdParts[0] += "$" + ntParts[1];
		}
		if (fxIdParts.length > 1) {
			nameType = StringUtil.swithToUpperCase(fxIdParts[1], "$");
		}
		String nameTypeF = nameType;
		String fxIdF = fxId;

		String[] parts = fxId.split("\\$");
		String sceneName = parts[1];

		controlsMembers.add(StringUtil.replace(
				"\tprivate Mf<Control> _<LSceneName>Control<NameType>;",
				new HashMap<String, String>() {{
					put("<LSceneName>", StringUtil.firstToLowerCase(sceneName));
					put("<Control>", controlName);
					put("<NameType>", nameTypeF);
				}}));

		sceneControls.add(
				StringUtil.replace(
						"\t\t_<LSceneName>Control<NameType> = new Mf<Control>((<Control>) _scene.lookup(\"#<FxId>\"));\n" + 
								"\t\t_<LSceneName>Control<NameType>.addEvent((event) -> { _switcher.switchScene(\"<SceneName>\"); });",
								new HashMap<String, String>() {{
									put("<LSceneName>", StringUtil.firstToLowerCase(sceneName));
									put("<SceneName>", sceneName);
									put("<Control>", controlName);
									put("<FxId>", fxIdF);
									put("<NameType>", nameTypeF);
								}}));
	}

	private void populateContext(String controlName, String fxId) {
		String[] fxIdParts = fxId.split("\\$\\$\\$");
		String nameType = "";
		if (fxIdParts.length > 1 && fxIdParts[1].contains("$")) {
			String[] ntParts = fxIdParts[1].split("\\$");
			fxIdParts[0] += "$" + ntParts[1];
		}
		if (fxIdParts.length > 1) {
			nameType = StringUtil.swithToUpperCase(fxIdParts[1], "$");
		}
		String nameTypeF = nameType;
		String fxIdF = fxId;
	}

	private void prepareCollectionsFromFile(String file) {
		try {
			Map<String, List<String>> grouped = new HashMap<String, List<String>>();
			// preserved members start
			XmlParser.parse(file, 
					(node) -> {
						String controlName = node.getNodeName();
						NamedNodeMap attributes = node.getAttributes();
						Node fxIdNode = attributes.getNamedItem("fx:id");
						if (fxIdNode != null) {
							String fxId = fxIdNode.getNodeValue();
							if (fxId.startsWith("table$")) {
								populateTableControl(controlName, fxId, grouped, 
										(entitiesParts, field, nameType) -> {
											return populateEntities(entitiesParts, field, nameType);
										});
							}
							else if (fxId.startsWith("action$")) {
								populateActionControl(controlName, fxId);
							}
							else if (fxId.startsWith("uitable$")) {
								populateUiTables(controlName, fxId);
							}
							else if (fxId.startsWith("uitable$")) {
								populateUiTables(controlName, fxId);
							}
							else if (fxId.startsWith("scene$")) {
								populateSceneControls(controlName, fxId);
							}
							else if (fxId.startsWith("context$")) {
								populateTableControl(controlName, fxId, grouped, 
										(entitiesParts, field, nameType) -> {
											String className = StringUtil.swithToUpperCase(entitiesParts[1], "_");
											return String.format("_context.get%s()", className);
										});
							}
							System.out.println();
						}
					});

			for (Entry<String, List<String>> entry : grouped.entrySet()) {
				if (entry.getValue().size() > 1) {
					groupedControls.add(String.format("\t\tgroupControls(new ControlAdapter[] { %s });", 
							String.join(", ", entry.getValue())));
				}
			}
			System.out.println();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
