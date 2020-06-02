package generator;

import java.io.File;
import java.sql.SQLException;
import java.util.Map;

import db.connections.MySqlConnection;
import utilities.FileUtil;

public class ClassGenerator {

	private MySqlConnection _connection = new MySqlConnection();

	private EntityClassGenerator _entityClassGenerator = new EntityClassGenerator();
	private EntityBridgeGenerator _entityBridgeGenerator = new EntityBridgeGenerator();
	private ServicesGenerator _servicesGenerator = new ServicesGenerator();
	private EntityVisitorGenerator _entityVisitorGenerator = new EntityVisitorGenerator();
	private ControlsMapGenerator _controlsWrappers = new ControlsMapGenerator();

	public ClassGenerator() throws SQLException {
		_connection = new MySqlConnection();
		_connection.connect("localhost", 3306, "myfuel", "myfuel", "1234");
		_entityClassGenerator.setConnection(_connection);
	}

	public void createTablesFiles(String entityPath, String bridgePath, String servicesPath, String visitorPath, String controlsWrappers, String[] tables) throws Exception {
		File entitiesDir = null, bridgeDir = null, servicesDir = null, visitorDir = null, controlsWrappersDir = null;
		entitiesDir = FileUtil.deleteSubDirectories(entityPath);
		if (bridgePath != null) {
			bridgeDir = FileUtil.deleteSubDirectories(bridgePath);
		}
		if (servicesPath != null) {
			servicesDir = FileUtil.deleteSubDirectories(servicesPath);
		}
		if (visitorPath != null) {
			visitorDir = FileUtil.deleteSubDirectories(visitorPath);
		}
		if (controlsWrappers != null) {
			controlsWrappersDir = FileUtil.deleteSubDirectories(controlsWrappers);
		}

		for (String table : tables) {
			Map<String, String> info = _connection.retrieveTableMetadata(table);
			FileUtil.mkDirs(entitiesDir);
			_entityClassGenerator.generateEntity(entitiesDir, table, info);
			if (bridgeDir != null) {
				FileUtil.mkDirs(bridgeDir);
				_entityBridgeGenerator.generateEntityBridge(bridgeDir, table, info);
			}

		}
		if (servicesDir != null) {
			_servicesGenerator.generateEntityPools(servicesDir, tables);
		}
		if (visitorDir != null) {
			_entityVisitorGenerator.generateVisitor(visitorDir, tables);
		}
		if (controlsWrappersDir != null) {
			_controlsWrappers.generateControlsWrappers(controlsWrappersDir);
		}
		_connection.close();
	}
}
