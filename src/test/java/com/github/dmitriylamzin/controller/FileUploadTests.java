package com.github.dmitriylamzin.controller;


import com.github.dmitriylamzin.storage.StorageException;
import com.github.dmitriylamzin.storage.StorageFileNotFoundException;
import com.github.dmitriylamzin.storage.StorageService;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class FileUploadTests {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private StorageService storageService;

    @Test
    public void shouldListAllFiles() throws Exception {
        given(this.storageService.loadAll())
                .willReturn(Stream.of(Paths.get("first.png"), Paths.get("second.png")));

        this.mvc.perform(get("/photo/gallery"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("files",
                        Matchers.contains("http://localhost/photo/files/first.png",
                                "http://localhost/photo/files/second.png")))
                .andExpect(model().attribute("row", "4"))
                .andExpect(model().attribute("stylesheet", "white.css"));
    }

    @Test
    public void shouldChangeRowsNumber() throws Exception {
        given(this.storageService.loadAll())
                .willReturn(Stream.of(Paths.get("first.png"), Paths.get("second.png")));
        this.mvc.perform(get("/photo/row/3"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("row", "3"));
    }

    @Test
    public void shouldChangeBackgroundColorToBlack() throws Exception {
        given(this.storageService.loadAll())
                .willReturn(Stream.of(Paths.get("first.png"), Paths.get("second.png")));

        this.mvc.perform(get("/photo/blackbackground"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("stylesheet", "black.css"));
    }

    @Test
    public void shouldSetHeightAndWidthTo230x230() throws Exception {
        given(this.storageService.loadAll())
                .willReturn(Stream.of(Paths.get("first.png"), Paths.get("second.png")));

        this.mvc.perform(get("/photo/wh/230x230"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("height", "230"))
                .andExpect(model().attribute("width", "230"));
    }

    @Test
    public void shouldGetModelWithOriginalSize() throws Exception {
        given(this.storageService.loadAll())
                .willReturn(Stream.of(Paths.get("first.png"), Paths.get("second.png")));

        this.mvc.perform(get("/photo/original"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("height", ""))
                .andExpect(model().attribute("width", ""));
    }

    @Test
    public void shouldGetUploadForm() throws Exception {
        this.mvc.perform(get("/photo"))
                .andExpect(status().isOk())
                .andExpect(view().name("uploadForm"));
    }

    @Test
    public void shouldSaveUploadedFile() throws Exception {
        MockMultipartFile multipartFile =
                new MockMultipartFile("file", "test.png", "text/plain", "Spring Framework".getBytes());
        MultipartFile[] multipartFiles = {multipartFile};
        this.mvc.perform(fileUpload("/photo").file(multipartFile))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/photo/gallery"));

        then(this.storageService).should().store(multipartFiles);
    }

    @Test
    public void should404WhenMissingFile() throws Exception {
        given(this.storageService.loadAsResource("test.txt"))
                .willThrow(new StorageFileNotFoundException("test.txt"));

        this.mvc.perform(get("/photo/files/test.txt"))
                .andExpect(status().isNotFound())
                .andExpect(model().attribute("msg", "test.txt"));
    }

    @Test
    public void should500WhenStorageException() throws Exception {
        given(this.storageService.loadAsResource("test.txt"))
                .willThrow(new StorageException("test.txt"));
        this.mvc.perform(get("/photo/files/test.txt"))
                .andExpect(status().is5xxServerError())
                .andExpect(model().attribute("msg", "test.txt"));
    }

}
