package com.baijiacms.qsp.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public final class FileUtil {


    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    public static boolean isWritableFile(File file) {
        return file != null && file.exists() && file.canWrite();
    }

    public static boolean isWritableDirectory(File dir) {
        return dir != null && dir.exists() && dir.isDirectory() && dir.canWrite();
    }

    public static File getOrCreateFile(File parentDir, String name) {
        File file = findFileOrDirectory(parentDir, name);
        if (file == null) {
            file = createFile(parentDir, name);
        }
        return file;
    }

    public static File createFile(File parentDir, String name) {
        File file = new File(parentDir, name);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ex) {
                logger.error("Error creating a file: " + name, ex);
                return null;
            }
        }
        return file;
    }

    public static File getOrCreateDirectory(File parentDir, String name) {
        File dir = findFileOrDirectory(parentDir, name);
        if (dir == null) {
            dir = createDirectory(parentDir, name);
        }
        return dir;
    }

    public static File createDirectory(File parentDir, String name) {
        File dir = new File(parentDir, name);
        dir.mkdir();
        return dir;
    }

    public static File findFileOrDirectory(File parentDir, final String name) {
        File[] files = parentDir.listFiles((dir, filename) -> filename.equalsIgnoreCase(name));
        if (files == null || files.length == 0) {
            return null;
        }

        return files[0];
    }

    public static File findFileRecursively(File parentDir, String path) {
        int idx = path.indexOf("/");
        if (idx == -1) {
            return findFileOrDirectory(parentDir, path);
        }
        String dirName = path.substring(0, idx);
        File dir = findFileOrDirectory(parentDir, dirName);
        if (dir == null) {
            return null;
        }

        return findFileRecursively(dir, path.substring(idx + 1));
    }

    public static String readFileAsString(File file) {
        StringBuilder result = new StringBuilder();
        try (FileInputStream in = new FileInputStream(file)) {
            InputStreamReader inReader = new InputStreamReader(in);
            try (BufferedReader bufReader = new BufferedReader(inReader)) {
                String line;
                while ((line = bufReader.readLine()) != null) {
                    result.append(line);
                }
            }
        } catch (IOException ex) {
            logger.error("Error reading a file", ex);
            return null;
        }
        return result.toString();
    }

    public static void deleteDirectory(File dir) {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                deleteDirectory(file);
            } else {
                file.delete();
            }
        }
        dir.delete();
    }

    public static File getParentDirectory(File parentDir, String path) {
        int idx = path.indexOf('/');
        if (idx == -1) {
            return parentDir;
        }

        String dirName = path.substring(0, idx);
        File dir = findFileOrDirectory(parentDir, dirName);
        if (dir == null) {
            dir = createDirectory(parentDir, dirName);
            ;
        }

        return getParentDirectory(dir, path.substring(idx + 1));
    }

    public static void createDirectories(File parentDir, String dirPath) {
        int idx = dirPath.indexOf('/');
        String dirName = idx == -1 ? dirPath : dirPath.substring(0, idx);
        File dir = findFileOrDirectory(parentDir, dirName);
        if (dir == null) {
            dir = createDirectory(parentDir, dirName);
        }
        if (idx != -1) {
            createDirectories(dir, dirPath.substring(idx + 1));
        }
    }

    public static byte[] getFileContents(String path) {
        File file = new File(path);
        try (FileInputStream in = new FileInputStream(file)) {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                StreamUtils.copy(in, out);
                return out.toByteArray();
            }
        } catch (IOException ex) {
            logger.error("Error reading file: " + path, ex);
            return null;
        }
    }
}
