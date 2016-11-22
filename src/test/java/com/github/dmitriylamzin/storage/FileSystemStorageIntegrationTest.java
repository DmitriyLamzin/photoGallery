package com.github.dmitriylamzin.storage;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FileSystemStorageIntegrationTest {
    @Configuration
    static class Config{
        @Bean
        StorageService storageService(){
            return new FileSystemStorageService(new TestStorageProperties());
        }
    }
    @Autowired
    StorageService storageService;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void returnsAllFilePathsAsStream(){
        List<Path> allFiles = storageService.loadAll().collect(Collectors.toList());

        assertThat(allFiles.get(0).toString().equals("firstPhoto.png"));
        assertThat(allFiles.get(1).toString().equals("secondPhoto.png"));
    }

    @Test
    public void returnsFileAsResource(){
        Resource resource = storageService.loadAsResource("firstPhoto.png");

        assertThat(resource.getFilename().equals("firstPhoto.png"));
    }

    @Test
    public void throwsFileNotFoundExceptionWhenTryingToLoadNotExistedFile(){
        String notExistedFile = "notExisted.png";

        thrown.expect(StorageFileNotFoundException.class);
        thrown.expectMessage("Could not read file: " + notExistedFile);

        storageService.loadAsResource(notExistedFile);

    }
}
