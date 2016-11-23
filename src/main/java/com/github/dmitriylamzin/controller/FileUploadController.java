package com.github.dmitriylamzin.controller;

import com.github.dmitriylamzin.storage.StorageException;
import com.github.dmitriylamzin.storage.StorageFileNotFoundException;
import com.github.dmitriylamzin.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
* RESTLike service for uploading PNG type files.
* */
@Controller
@RequestMapping("/photo")
public class FileUploadController {

  /**
   * The width of an image that will be set up by default.
   * */
  private static int DEFAULT_WIDTH = 200;
  /**
   * The height of an image that will be set up by default.
   * */
  private static int DEFAULT_HEIGHT = 200;
  /**
   * The number of rows of images that will be set up by default.
   * */
  private static String DEFAULT_ROW_COUNT = "4";
  /**
   * The style scheme of gallery page that will be set up by default.
   * */
  private static String DEFAULT_STYLESHEET = "white.css";

  /**
   * The service, which provide storing and retrieving functions for files.
   * */
  private final StorageService storageService;

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  /**
   * Constructor.
   * */
  @Autowired
  public FileUploadController(StorageService storageService) {
    logger.debug("initialization");
    logger.debug("setting up storage service - " + storageService.getClass());
    this.storageService = storageService;
  }

  /**
  * @return HTML form for files uploading.
  * */
  @GetMapping
  public String getUploadForm(Model model) throws IOException {
    logger.info("getting upload form");
    model.addAttribute("stylesheet", DEFAULT_STYLESHEET);
    storageService.deleteAll();
    storageService.init();
    return "uploadForm";
  }

  /**
  * Renders the view with all uploaded files.
   *
   * @return name of HTML which handles all uploaded files.
   * */
  @GetMapping("/gallery")
  public String listUploadedFiles(Model model) throws IOException {
    logger.info("getting gallery page");
    getUploadedFilesWithDefaultAttributes(model);
    return "photoGallery";
  }

  /**
   * Changes the default white scheme of <code>photoGallery</code>
   * page to scheme with black background.
   *
   * @return name of HTML which handles all uploaded files.
   * */
  @GetMapping("/blackbackground")
  public String changeBackgraund(Model model) throws IOException {
    logger.info("setting the black background stylesheet");
    String blackStylesheet = "black.css";
    getUploadedFilesWithDefaultAttributes(model);
    model.addAttribute("stylesheet", blackStylesheet);
    return "photoGallery";
  }

  /**
   * Changes the default number of images rows from <code>DEFAULT_ROW_COUNT</code>
   * to the specified via parameter.
   *
   * @param rowNumber the new number of images rows
   *
   * @return name of HTML which handles all uploaded files.
   * */
  @GetMapping("/row/{rowNumber:\\d+}")
  public String setRowNubers(@PathVariable String rowNumber, Model model) throws IOException {
    logger.info("setting number of image rows to " + rowNumber);
    getUploadedFilesWithDefaultAttributes(model);
    model.addAttribute("row", rowNumber);
    return "photoGallery";
  }

  /**
   * Changes the default sizes of images from <code>DEFAULT_WIDTH</code>
   * and <codeDEFAULT_HEIGHT></code> to the specified size.
   *
   * @param wh the new size of images WIDTHxHEIGHT. Should matches to expression: <code>WWWxHHH</code>
   *
   * @return name of HTML which handles all uploaded files.
   * */
  @GetMapping("/wh/{wh:\\d{3}x\\d{3}}")
  public String setPictureSize(@PathVariable String wh, Model model) throws IOException {
    logger.info("setting image size to " + wh);
    String[] splittedWh = wh.split("x");
    logger.info("Image height " + splittedWh[1]);
    logger.info("Image width " + splittedWh[0]);

    getUploadedFilesWithDefaultAttributes(model);
    model.addAttribute("height", splittedWh[1]);
    model.addAttribute("width", splittedWh[0]);
    return "photoGallery";
  }

  /**
   * Changes the default sizes of images from <code>DEFAULT_WIDTH</code>
   * and <codeDEFAULT_HEIGHT></code> to the original size of picture.
   *
   * @return name of HTML which handles all uploaded files.
   * */
  @GetMapping("/original")
  public String getPicturesWithOriginalSize(Model model) throws IOException {
    logger.info("setting image to original size");
    getUploadedFilesWithDefaultAttributes(model);
    model.addAttribute("height", "");
    model.addAttribute("width", "");
    return "photoGallery";
  }

  private void getUploadedFilesWithDefaultAttributes(Model model) {
    logger.info("loading all images from storage");
    model.addAttribute("files", storageService
            .loadAll()
            .map(path ->
                    MvcUriComponentsBuilder
                            .fromMethodName(FileUploadController.class, "serveFile",
                                    path.getFileName().toString())
                            .build().toString())
            .collect(Collectors.toList()));

    model.addAttribute("height", DEFAULT_HEIGHT);
    model.addAttribute("width", DEFAULT_WIDTH);
    model.addAttribute("stylesheet", DEFAULT_STYLESHEET);
    model.addAttribute("row", DEFAULT_ROW_COUNT);
  }

  /**
   * Loads file to the client.
   *
   * @param filename name of file that need to be loaded
   *
   * @return {@link ResponseEntity} which body is set as Resource of file,
   * <code>HttpStatus</code> is set to 200, and content description is set to
   * <code>attachment; filename="<code>+ name of served file</code>"</code>.
   * */
  @GetMapping("/files/{filename:.+}")
  @ResponseBody
  public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
    logger.info("loading image as a resource with name " + filename);
    Resource file = storageService.loadAsResource(filename);
    return ResponseEntity
            .ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + file.getFilename() + "\"")
            .body(file);
  }

  /**
   * Handles request to upload a file to the server.
   *
   * @param files Array of files that should be uploaded.
   *
   * @return name of HTML which handles all uploaded files.
   * */
  @PostMapping
  public String handleFileUpload(@RequestParam("file") MultipartFile[] files,
                                 RedirectAttributes redirectAttributes) {
    logger.info("uploading images  " + Stream.of(files));

    storageService.store(files);
    redirectAttributes.addFlashAttribute("message",
            "The photos has been uploaded");

    return "redirect:/photo/gallery";
  }

  /**
   * Redirects to the error page if requested file is not found.
   *
   * <p>Sets up a message from handled exception
   *
   * @param fileNotFoundException handled exception.
   *
   * @return view of error page.
   * */
  @ExceptionHandler(StorageFileNotFoundException.class)
  public ModelAndView handleStorageFileNotFound(StorageFileNotFoundException fileNotFoundException) {
    logger.info("file not found: " + fileNotFoundException.getMessage());
    ModelAndView model = new ModelAndView();
    model.addObject("msg", fileNotFoundException.getMessage());
    model.addObject("code", 404);
    model.setViewName("error");
    model.setStatus(HttpStatus.NOT_FOUND);
    return model;
  }

  /**
   * Redirects to the error page if some Storage exception was occurred.
   *
   * <p>Sets up a message from handled exception
   *
   * @param storageException handled exception.
   *
   * @return view of error page.
   * */
  @ExceptionHandler(StorageException.class)
  public ModelAndView handleStorageException(StorageException storageException) {
    logger.info("storage exception has occurred: " + storageException.getMessage());
    ModelAndView model = new ModelAndView();
    model.addObject("msg", storageException.getMessage());
    model.addObject("code", 500);
    model.setViewName("error");
    model.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    return model;
  }
}
