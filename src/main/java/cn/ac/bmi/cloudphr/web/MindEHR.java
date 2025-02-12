package cn.ac.bmi.cloudphr.web;

import cn.ac.bmi.cloudphr.mindehr.helper.Constant;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.io.FileUtil;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MindEHR {

	protected static final Path TEMP_DIR = Paths.get(
				System.getProperty("java.io.tmpdir"),
				getTempDirName()
	);

	public static void main(String[] args) {
		FileUtil.mkdir(TEMP_DIR.toString());
        if (args.length > 0) {
			Constant.LOCAL_CKM_SERVER = args[0];
		}

		SpringApplication.run(MindEHR.class, args);
	}

	private static String getTempDirName() {
		return "openehr-auto-" + DateTime.now().toString().replaceAll("[ :]", "-");
	}
}
