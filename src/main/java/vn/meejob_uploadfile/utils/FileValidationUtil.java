package vn.meejob_uploadfile.utils;

import vn.meejob_uploadfile.exception.FileStorageException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class FileValidationUtil {

    private static final List<String> ALLOWED_EXTENSIONS = List.of(
            "jpg", "jpeg", "png", "gif", "pdf", "docx", "xlsx", "txt"
    );

    private static final String ALLOWED_MIME_REGEX = "image/.*|application/pdf|text/plain|application/vnd.*";

    public static void validateFile(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new FileStorageException("File name is empty!");
        }

        if (originalFileName.contains("..")) {
            throw new FileStorageException("Filename contains invalid path sequence: " + originalFileName);
        }

        String fileExtension = "";
        int dotIndex = originalFileName.lastIndexOf(".");
        if (dotIndex > 0) {
            fileExtension = originalFileName.substring(dotIndex + 1).toLowerCase();
        }

        if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
            throw new FileStorageException("File type ." + fileExtension + " is not allowed to upload!");
        }

        String mimeType = file.getContentType();
        if (mimeType == null || !mimeType.matches(ALLOWED_MIME_REGEX)) {
            throw new FileStorageException("Invalid MIME type: " + mimeType);
        }
    }
}
