package cn.ac.bmi.cloudphr.web;

import cn.ac.bmi.cloudphr.mindehr.Translator;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ZipUtil;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class MindEHRController {

  @GetMapping("/")
  public String index() {
    return "index";
  }

  @PostMapping(value = "/", produces = "application/zip")
  public String convert(@RequestParam("file") MultipartFile file, Model model, HttpServletResponse response)
        throws IOException {

    String message;

    if (file.isEmpty()) {
      message = "Please select the xmind file first!";
    } else {
      InputStream xmindStream = file.getInputStream();
      try {
        File output = doConvert(xmindStream);
        if (output != null) {
          response.addHeader("Content-Disposition", "attachment;filename =openehr.zip");
          OutputStream responseStream = response.getOutputStream();
          IoUtil.copy(new FileInputStream(output), responseStream);
          response.flushBuffer();
          return null;
        } else {
          message = "Some error exists in your xmind file, check it please!";
        }
      } catch (Exception e) {
        message = "Some error exists in your xmind file, check it please!";
      }
    }
    model.addAttribute("message", message);
    return "index";
  }

  private File doConvert(InputStream xmind) {
    String uuid = UUID.randomUUID().toString().replace("-", "").toLowerCase();
    Path tmpDir = Paths.get(MindEHR.TEMP_DIR.toString(), uuid);
    boolean successful = Translator.convert(xmind, tmpDir.toString());
    if (successful) {
      return ZipUtil.zip(tmpDir.toString());
    }

    return null;
  }
}
