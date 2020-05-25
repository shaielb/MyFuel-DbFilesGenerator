package generator;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;

import configuration.Configuration;

public class EntityFilesGenerator {

	private static ClassGenerator _classGenerator;

	public static void main(String[] args) throws IOException {
		try {
			Map<String, String> arguments = Configuration.arguemnts();

			_classGenerator = new ClassGenerator();

			String tablesArg = arguments.get("tables");
			if (tablesArg != null) {
				String entityPath = arguments.get("entity_path");
				String bridgePath = arguments.get("bridge_path");
				String servicesPath = arguments.get("services_path");
				String visitorPath = arguments.get("visitor_path");
				String[] tables = tablesArg.split(",");
				try {
					_classGenerator.createTablesFiles(entityPath, bridgePath, servicesPath, visitorPath, tables);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static String getArg(String[] args, String flag) {
		int index = (Arrays.asList(args)).indexOf(flag);
		if (index <= -1 || (index + 1) >= args.length) {
			System.out.println(String.format("Couldn't Find '%s' argument", flag));
			return null;
		}
		return args[(Arrays.asList(args)).indexOf(flag) + 1];
	}
}
