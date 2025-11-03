package vn.meejob_uploadfile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import vn.meejob_uploadfile.property.FileStorageProperties;


@SpringBootApplication
@EnableConfigurationProperties({
        FileStorageProperties.class
})
public class MeeJobUploadFileApplication {

    public static void main(String[] args) {
        SpringApplication.run(MeeJobUploadFileApplication.class, args);
    }

}
