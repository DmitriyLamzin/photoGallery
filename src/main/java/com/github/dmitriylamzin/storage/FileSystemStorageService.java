package com.github.dmitriylamzin.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Service
public class FileSystemStorageService implements StorageService {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final Path rootLocation;

  @Autowired
  public FileSystemStorageService(StorageProperties properties) {
    this.rootLocation = Paths.get(properties.getLocation());
  }

  @Override
  public void store(MultipartFile file) {
    logger.info("Storing file: " + file.getOriginalFilename());
    try {
      if (file.isEmpty()) {
        logger.debug("File is empty: " + file.getOriginalFilename());
        throw new StorageException("Failed to store empty file " + file.getOriginalFilename());
      } else if (!file.getOriginalFilename().endsWith(".png")) {
        logger.debug("File extension is not .png: " + file.getOriginalFilename());
        throw new StorageException("Failed to store not png file " + file.getOriginalFilename());
      }
      Files.copy(file.getInputStream(),
              this.rootLocation.resolve(file.getOriginalFilename()));
    } catch (IOException e) {
      logger.error("IOException has occurred: " + e.getMessage());
      throw new StorageException("Failed to store file " + file.getOriginalFilename(), e);
    }
  }

  @Override
  public void store(MultipartFile[] files) {
    logger.info("Storing array of files");
    for (MultipartFile file : files) {
      store(file);
    }
  }


  @Override
  public Stream<Path> loadAll() {
    logger.info("Loading all files form storage");
    try {
      return Files.walk(this.rootLocation, 1)
              .filter(path -> !path.equals(this.rootLocation))
              .map(path -> this.rootLocation.relativize(path));
    } catch (IOException e) {
      logger.error("IOException has occurred: " + e.getMessage());
      throw new StorageException("Failed to read stored files", e);
    }
  }

  @Override
  public Path load(String filename) {
    return rootLocation.resolve(filename);
  }

  @Override
  public Resource loadAsResource(String filename) {
    logger.info("Loading file as resource: " + filename);
    try {
      Path file = load(filename);
      Resource resource = new UrlResource(file.toUri());
      if (resource.exists() || resource.isReadable()) {
        logger.debug("Resource has been found: " + filename);
        return resource;
      } else {
        logger.debug("resource does not exist or does not readable: " + filename);
        throw new StorageFileNotFoundException("Could not read file: " + filename);
      }
    } catch (MalformedURLException e) {
      logger.error("Malformed URL has occurred: " + e.getMessage());
      throw new StorageFileNotFoundException("Could not read file: " + filename, e);
    }
  }

  @Override
  public void deleteAll() {
    logger.info("Deleting all files from storage");
    FileSystemUtils.deleteRecursively(rootLocation.toFile());
  }

  @Override
  public void init() {
    logger.info("Initializing storage");
    try {
      Files.createDirectory(rootLocation);
    } catch (IOException e) {
      logger.error("IOException has occurred: " + e.getMessage());
      throw new StorageException("Could not initialize storage", e);
    }
  }
}
