package nl.andarabski.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

public class FileUploadUtil {

    @Value("${file.upload-dir}")
    public static final String UPLOAD_DIR = System.getProperty("user.dir") + "/UPLOADS";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

    private static final List<String> ALLOWED_IMAGE_TYPES = List.of("jpg", "jpeg", "png", "gif");
    private static final List<String> ALLOWED_DOCUMENT_TYPES = List.of("pdf", "doc", "docx");

    public static String savePhoto(MultipartFile file) throws IOException {
        return save(file, ALLOWED_IMAGE_TYPES, "photo");
    }

    public static String saveDocument(MultipartFile file) throws IOException {
        return save(file, ALLOWED_DOCUMENT_TYPES, "cv");
    }

    private static String save(MultipartFile file, List<String> allowedExtensions, String typeLabel) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(typeLabel + " bestand mag niet leeg zijn.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IOException("Bestand " + file.getOriginalFilename() + " is groter dan 5 MB.");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);

        if (!allowedExtensions.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException(typeLabel + " bestandstype ." + extension + " is niet toegestaan.");
        }

        // Zorg dat de uploads-directory bestaat
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }


        // Bestandsnaam veilig genereren
        String filename = System.currentTimeMillis() + "_" + originalFilename;
        Path filePath = uploadPath.resolve(filename);

        // Bestand opslaan
        file.transferTo(filePath.toFile());

        return filePath.toString();

    }

    private static String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex != -1) ? filename.substring(dotIndex + 1) : "";
    }
}

