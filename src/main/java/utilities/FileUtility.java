package utilities;


import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class FileUtility {
    private static final Logger logger = LoggerFactory.getLogger(FileUtility.class);
    private static final Object lock = new Object(); // Lock object for synchronization

    public static boolean waitForFileDownload(
            String downloadDir,
            String fileName,
            long timeoutSec,
            long pollMillis,
            int stablePolls) {

        File dir = new File(downloadDir);
        if (!dir.exists() || !dir.isDirectory()) {
            boolean created = dir.mkdirs();
            if (!created) {
                logger.error("Failed to create download directory: downloadDir); " + downloadDir);
                return false;

            }

        }

        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(timeoutSec);
        File finalFile = new File(dir, fileName);
        File tempFile1 = new File(dir, fileName + ".crdownload");
        File tempFile2 = new File(dir, fileName + ".part");

        long lastSize = 1L;
        int stableCount = 0;

        while (System.nanoTime() < deadline) {
            File fileToCheck;
            if (tempFile1.exists()) {
                fileToCheck = tempFile1;
            } else if (tempFile2.exists()) {
                fileToCheck = tempFile2;
            } else {
                fileToCheck = finalFile;
            }

            if (fileToCheck.exists() && fileToCheck.isFile()) {
                long size = fileToCheck.length();

                if (size == lastSize && size > 0) {
                    stableCount++;

                    // File is stable enough return success "ONLY if final file exists
                    if (stableCount >= stablePolls) {

                        // If final file now exists done
                        if (finalFile.exists() && !tempFile1.exists() && !tempFile2.exists()) {
                            return true;
                        }
                    }

                } else {
                    stableCount = 0;
                    lastSize = size;
                }
            }

            try {
                Thread.sleep(pollMillis);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;

    }


    private static boolean hasTempDownloadingArtifacts(File dir, String fileName) {
        String chromeTmp = fileName + ".crdownload";
        String firefoxTmp = fileName + ".part";
        File[] matches = dir.listFiles((d, name) ->
                name.equals(chromeTmp) || name.equals(firefoxTmp)
        );
        return matches != null && matches.length > 0;

    }


    // Method to get the latest downloaded file from a given download directory

    public static String getLatestDownloadedFile(String downloadDir) {
        File dir = new File(downloadDir);
        File[] files = dir.listFiles();
        if (files != null && files.length > 0) {
            // Convert the array to a mutable list
            List<File> fileList = new ArrayList<>(Arrays.asList(files));

            // Sort files by last modified date in descending order (newest first)
            Collections.sort(fileList, Comparator.comparingLong(File::lastModified).reversed());

            // Get the latest file
            File latestFile = fileList.get(0);
            return latestFile.getName(); // This includes the file extension
        } else {
            return "No files found in the download directory."; // Return a message if no files found

        }

    }

    public static void deleteExistingFile(String filePath, String fileName) {
        File fileToDelete = new File(filePath);
        if (fileToDelete.exists()) {
            boolean deleted = fileToDelete.delete();
            if (deleted) {
                logger.info("Existing file with same name deleted: " + filePath);
            } else {
                logger.error("Failed to delete existing file with same name:" + filePath);
            }
        } else {
            logger.info("No existing file with name " + fileName + "in downloads folder.");
        }

    }

    public static void cleanUpFolder(String dir) throws Exception {// cleans up dir with no child dirs
        Path filePath = Paths.get(dir);
        if (Files.exists(filePath)) {
            Files.walk(filePath).map(Path::toFile).forEach(File::delete);
        }
    }

    public static void cleanFolders(String dir) throws IOException {
        File file = new File(dir);
        if (file.exists()) {
            FileUtils.deleteDirectory(file);
        }
    }


    public static void copyProfile(String sourcePath, String destinationPath) throws IOException {
        File source = new File(sourcePath);
        File destination = new File(destinationPath);
        synchronized (lock) {
            FileUtils.copyDirectory(source, destination);
        }
    }

    public static void cleanUpFolderCompletely(String dir) throws Exception {// cleans up dir which has child dirs as
        // well
        Path filePath = Paths.get(dir);
        if (Files.exists(filePath)) {
            // walk through the directory tree and delete files in reverse order -- from
            // children to parent
            Files.walk(filePath).sorted((path1, path2) -> path2.compareTo(path1))    // reverse order to delete children
                    // before parent
                    .forEach(path -> {
                        int retryCount = 0;
                        int maxRetries = 3;
                        boolean deleted = false;
                        while (!deleted && retryCount < maxRetries) {
                            try {
                                unlockFileIfLocked(path);
                                File file = path.toFile();
                                if (file.canWrite()) {
                                    file.setWritable(true);
                                }
                                if (file.isDirectory()) {
                                    FileUtils.deleteDirectory(file);
                                } else {
                                    Files.delete(path);
                                }
                                deleted = true;
                            } catch (AccessDeniedException e) {
                                retryCount++;
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException ex) {
                                    Thread.currentThread().interrupt();
                                    logger.error("Retry interrupted while trying to delete file: {}", path);
                                    throw new RuntimeException("Retry interrupted while trying to delete file.");
                                }

                            } catch (IOException e) {
                                logger.error("Failed to delete : {}", path);
                                throw new RuntimeException("Failed to delete:" + path + e);
                            }
                        }
                        if (!deleted) {
                            logger.error("Exceeded max retries for deleting:{}", path);
                            throw new RuntimeException("Exceeded max retries for deleting:" + path);
                        }
                    });
            if (Files.exists(filePath)) {
                Files.delete(filePath);

            }
        }
    }


    public static void deleteFolderWithNestedFolders(String dir) throws IOException {
        Path filePath = Paths.get(dir);
        if (Files.exists(filePath)) {
            deleteFolderRecursively(filePath);
        }

    }

    public static void deleteFolderRecursively(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> entries = Files.newDirectoryStream(path)) {
                for (Path entry : entries) {
                    deleteFolderRecursively(entry);
                }

            }
            File file = path.toFile();
            if (file.canWrite()) {
                file.setWritable(true);
                file.setExecutable(true);
            }
            Files.delete(path);
        }

    }

    public static void deleteFiles(String dir) {
        Path filePath = Paths.get(dir);
        try {
            Files.walkFileTree(filePath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });

            logger.info("Folder deleted successfully.");

        } catch (IOException e) {
            logger.error("Failed to delete folder: {}", e.getMessage());
            throw new RuntimeException("Exception occured during file deletion:" + e);
        }

    }

    private static void unlockFileIfLocked(Path path) {
        if (!Files.isDirectory(path)) {
            try (FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.WRITE)) {
                try {
                    fileChannel.lock();// get the lock
                } catch (OverlappingFileLockException e) {
                    logger.error("File is locked by another process:" + path);
                }
            } catch (IOException e) {
                logger.error("Failed to open file for unlocking:" + path);
            }
        }
    }

    public static void copyFolderContents(String sourceFolderPath, String destinationFolderPath) throws IOException {
        synchronized (lock) { // Ensure that only one thread can copy at a time
            Path sourcePath = Paths.get(sourceFolderPath);
            Path destinationPath = Paths.get(destinationFolderPath);

            // Check if the source folder exists
            if (Files.notExists(sourcePath) || Files.isDirectory(sourcePath)) {
                throw new IllegalArgumentException("Source folder does not exist or is not a directory.");
            }


            // Create the destination folder if it doesn't exist
            if (Files.notExists(destinationPath)) {
                Files.createDirectories(destinationPath);
            }

            // copyProfile(sourceFolderPath, destinationFolderPath); }
            // Walk through the source folder and copy its contents
            Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path targetPath = destinationPath.resolve(sourcePath.relativize(file));
                    Files.copy(file, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Path targetDir = destinationPath.resolve(sourcePath.relativize(dir));
                    if (Files.notExists(targetDir)) {
                        Files.createDirectories(targetDir);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    public static boolean checkDirExistsAndNotEmpty(String folderPath) {
        File folder = new File(folderPath);
        if (folder.exists() && folder.isDirectory() && folder.list().length > 0) {
            return true;
        }
        return false;

    }

    public static void deleteDirectory(File directory) throws IOException {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        Files.delete(directory.toPath());
    }


    public static void copyDir(Path src, Path dst) throws IOException {
        if (src == null || !Files.exists(src)) return;
        Files.createDirectories(dst);
        try (Stream<Path> walk = Files.walk(src)) {
            walk.forEach(from -> {
                try {
                    Path rel = src.relativize(from);
                    Path to = dst.resolve(rel.toString());
                    if (Files.isDirectory(from)) {
                        Files.createDirectories(to);
                    } else {
                        Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);

                    }
                } catch (IOException ignore) { /* optionally log */ }
            });
        }
    }


    //------Copy a single file
    public static void copyFile(Path src, Path dst) throws IOException {
        Objects.requireNonNull(src, "src");
        Objects.requireNonNull(dst, "dst");
        if (!Files.exists(src) || Files.isDirectory(src)) {
            throw new NoSuchFileException("Source not found or is a directory: " + src.toAbsolutePath());
        }
        Path parent = dst.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
    }


    public static void copyFile(String src, String dst) throws IOException {
        copyFile(Paths.get(src), Paths.get(dst));
    }


    //---Delete a single file(quiet)
    public static void deleteFileIfExists(Path file) {
        if (file == null) return;
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            logger.warn("Failed to delete file: {}", file.toAbsolutePath(), e);
        }
    }

    public static void deleteFileIfExists(String file) {
        deleteFileIfExists(Paths.get(file));
    }

    //=== Paste inside class FileUtility ===

    public static Path waitForNewFileWithExtension(String downloadDir, String extension, int timeoutSeconds) {
        File dir = new File(downloadDir);
        long end = System.currentTimeMillis() + timeoutSeconds * 1000L;
        Path newest = null;
        long newestTime = Long.MIN_VALUE;

        while (System.currentTimeMillis() < end) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    String name = f.getName().toLowerCase(Locale.ROOT);
                    if (name.endsWith(extension.toLowerCase(Locale.ROOT)) && !name.endsWith(".crdownload")) {
                        long t = f.lastModified();
                        if (t > newestTime) {
                            newestTime = t;
                            newest = f.toPath();
                        }
                    }
                }
            }

            if (newest != null) {
                try {
                    long s1 = Files.size(newest);
                    Thread.sleep(400);
                    long s2 = Files.size(newest);
                    if (s1 > 0 && s1 == s2) return newest.toAbsolutePath();
                } catch (Exception ignored) {
                }
            }

            try {
                Thread.sleep(300);
            } catch (InterruptedException ignored) {
            }
        }

        throw new RuntimeException("No " + extension + " file detected in " + downloadDir + " within timeout");
    }
}





