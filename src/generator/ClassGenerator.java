package generator;

import java.io.File;
import java.io.IOException;
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

	public ClassGenerator() throws SQLException {
		_connection = new MySqlConnection();
		_connection.connect("localhost", 3306, "myfuel", "myfuel", "1234");
		_entityClassGenerator.setConnection(_connection);
	}

	public void createTablesFiles(String entityPath, String bridgePath, String servicesPath, String visitorPath, String[] tables) throws IOException, SQLException {
		File entitiesDir = null, wrappersDir = null, servicesDir = null, visitorDir = null;
		entitiesDir = FileUtil.deleteSubDirectories(entityPath);
		if (bridgePath != null) {
			wrappersDir = FileUtil.deleteSubDirectories(bridgePath);
		}
		if (servicesPath != null) {
			servicesDir = FileUtil.deleteSubDirectories(servicesPath);
		}
		if (visitorPath != null) {
			visitorDir = FileUtil.deleteSubDirectories(visitorPath);
		}

		for (String table : tables) {
			Map<String, String> info = _connection.retrieveTableMetadata(table);
			FileUtil.mkDirs(entitiesDir);
			_entityClassGenerator.generateEntity(entitiesDir, table, info);
			if (wrappersDir != null) {
				FileUtil.mkDirs(wrappersDir);
				_entityBridgeGenerator.generateEntityBridge(wrappersDir, table, info);
			}

		}
		if (servicesDir != null) {
			_servicesGenerator.generateEntityPools(servicesDir, tables);
		}
		if (visitorDir != null) {
			_entityVisitorGenerator.generateVisitor(visitorDir, tables);
		}
		_connection.close();
	}
}
