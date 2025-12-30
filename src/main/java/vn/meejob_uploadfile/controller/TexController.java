package vn.meejob_uploadfile.controller;

import vn.meejob_uploadfile.payload.UploadFileResponse;
import vn.meejob_uploadfile.service.FileStorageService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/compile-tex")
@Tag(name = "Tex Live Controller", description = "API dùng cho tex")
public class TexController {
	
	private static final Path PDF_ROOT = Paths.get("/tmp");
	
	@PostMapping
	public ResponseEntity<Map<String, String>> compile(@RequestBody Map<String, String> body)
	        throws Exception {

	    String latex = body.get("latex");
	    
	    // 1. tạo ID file
        String fileId = UUID.randomUUID().toString();
        Path workDir = Files.createTempDirectory("latex_");
        
        
        Path texFile = workDir.resolve("cv.tex");
        Files.writeString(texFile, latex);

	    ProcessBuilder pb = new ProcessBuilder(
	        "xelatex",
	        "-interaction=nonstopmode",
	        "cv.tex"
	    );
	    pb.directory(workDir.toFile());
	    pb.start().waitFor();

	    // 3. move PDF sang storage
        Path pdfSource = workDir.resolve("cv.pdf");
        Files.createDirectories(PDF_ROOT);

        Path pdfTarget = PDF_ROOT.resolve(fileId + ".pdf");
        Files.move(pdfSource, pdfTarget, StandardCopyOption.REPLACE_EXISTING);
        
        // 4. trả URL https://meejobuploadfile.onrender.com
        //String pdfUrl = "http://localhost:8080/api/files/view/" + fileId + ".pdf";
		String pdfUrl = "https://meejobuploadfile.onrender.com/api/files/view/" + fileId + ".pdf";

        Map<String, String> response = new HashMap<>();
        response.put("pdfUrl", pdfUrl);

        return ResponseEntity.ok(response);
	}

}
