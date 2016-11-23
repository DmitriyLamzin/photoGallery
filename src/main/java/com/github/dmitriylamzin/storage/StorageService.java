package com.github.dmitriylamzin.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.stream.Stream;
/**
* Service Interface for storing files.
* */
public interface StorageService {

  /**
   * Performs a first initialization of storage.
   * */
  void init();

  /**
   * Stores a single {@code MultipartFile} to the storage.
   *
   * @param file MultipartFile from client to be stored.
   * */
  void store(MultipartFile file);

  /**
   * Stores an array of {@code MultipartFile} to the storage.
   *
   * @param file an Array of MultipartFile from client to be stored.
   * */
  void store(MultipartFile[] file);

  /**
   * Loads all file paths from storage.
   *
   * @return a stream of {@link java.nio.file.Path}.
   * Each path from the stream corresponds to loaded file.
   * */
  Stream<Path> loadAll();

  /**
   * Loads single file path from storage.
   *
   * @param filename a name of file to be loaded.
   *
   * @return a {@link java.nio.file.Path}, which corresponds to requested file.
   * */
  Path load(String filename);

  /**
   * Loads a file as a {@link org.springframework.core.io.Resource} from storage.
   *
   * @param filename a name of file to be loaded.
   *
   * @return a resource of requested file.
   * */
  Resource loadAsResource(String filename);

  /**
   * Clears a storage from files.
   * */
  void deleteAll();

}
