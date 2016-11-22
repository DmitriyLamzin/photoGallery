package com.github.dmitriylamzin.storage;

import org.assertj.core.util.Arrays;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FileSystemStorageTests {


    @Autowired
    StorageService storageService;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldThrowStorageExceptionWhenEmptyFileIsStored(){
        MockMultipartFile multipartFile =
                new MockMultipartFile("file", "test.png", "text/plain", new byte[0]);

        thrown.expect(StorageException.class);
        thrown.expectMessage("Failed to store empty file " + multipartFile.getOriginalFilename());

        storageService.store(multipartFile);
    }

    @Test
    public void shouldThrowStorageExceptionWhenMultipleEmptyFilesAreStored(){
        MockMultipartFile multipartFileFirst =
                new MockMultipartFile("file", "test.png", "text/plain", new byte[0]);

        MockMultipartFile multipartFileSecond =
                new MockMultipartFile("file", "test.png", "text/plain", new byte[0]);

        thrown.expect(StorageException.class);
        thrown.expectMessage("Failed to store empty file " + multipartFileFirst.getOriginalFilename());

        storageService.store(Arrays.array(multipartFileFirst, multipartFileSecond));
    }

    @Test
    public void shouldThrowStorageExceptionWhenNotPngIsStored(){
        MockMultipartFile multipartFile =
                new MockMultipartFile("file", "test.any", "text/plain", "some bytes".getBytes());

        thrown.expect(StorageException.class);
        thrown.expectMessage("Failed to store not png file " + multipartFile.getOriginalFilename());

        storageService.store(multipartFile);
    }
}
