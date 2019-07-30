package utils;

import exceptions.MyFileException;
import exceptions.eErrorCodes;
import magit.BasicFile;
import magit.Blob;
import magit.Folder;
import magit.eFileTypes;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileManager {
    public static String readFile(Path filePath) throws IOException {
        return FileUtils.readFileToString(new File(filePath.toUri()), Settings.FILE_ENCODING);
    }

    public static List<File> scanFolder(String rootPath) {
        return new ArrayList<>(Arrays.asList(Objects.requireNonNull(new File(rootPath).listFiles())));
    }

    public static void zipFile(BasicFile file, Path pathToObject) throws MyFileException {
        String sourceFile = file.getFullPathName();
        FileOutputStream fos;
        if (file.getType() == eFileTypes.FOLDER) {
            ((Folder) file).calcFolderSHAONE();
            File folderTXT = new File(pathToObject.toString() + File.separator + Settings.TEMP_FOLDER_NAME);
            try {
                folderTXT.createNewFile();
                PrintWriter writer = new PrintWriter(folderTXT.getPath());
                writer.println(((Folder) file).getContent());
                writer.close();
                sourceFile = folderTXT.getPath();
            } catch (IOException e) {
                throw new MyFileException(eErrorCodes.CREATE_TEMP_FOLDER_FILE_FAILED, file.getName());
            }

        }
        try {
            fos = new FileOutputStream(pathToObject.toString() + File.separator + ((Blob) file).getSHA_ONE());
        } catch (FileNotFoundException e) {
            throw new MyFileException(eErrorCodes.OPEN_FILE_FAILED, pathToObject.toString() + File.separator + ((Blob) file).getSHA_ONE());
        }

        ZipOutputStream zipOut = new ZipOutputStream(fos);
        File fileToZip = new File(sourceFile);
        FileInputStream fis;

        try {
            fis = new FileInputStream(fileToZip);
        } catch (FileNotFoundException e) {
            throw new MyFileException(eErrorCodes.OPEN_FILE_FAILED, pathToObject.toString() + File.separator + ((Blob) file).getSHA_ONE());
        }
        ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
        try {
            zipOut.putNextEntry(zipEntry);
        } catch (IOException e) {
            throw new MyFileException(eErrorCodes.INSERT_FILE_TO_ZIP_FAILED, fileToZip.getName());
        }
        byte[] bytes = new byte[1024];
        int length;
        try {
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
        } catch (IOException e) {
            throw new MyFileException(eErrorCodes.WRITE_TO_FILE_FAILED, fileToZip.getName());
        }

        try {
            zipOut.close();
            fis.close();
            fos.close();
        } catch (IOException e) {
            throw new MyFileException(eErrorCodes.CLOSE_FILE_FAILED, e.getMessage());
        }
        if (file.getType() == eFileTypes.FOLDER)
            fileToZip.delete();
    }

    public static File unZipFile(File file, String pathToTempFolder) {
        File dir = new File(pathToTempFolder);
        // create output directory if it doesn't exist
        if(!dir.exists()) dir.mkdirs();
        FileInputStream fis;
        //buffer for read and write data to file
        byte[] buffer = new byte[1024];
        try {
            fis = new FileInputStream(file);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry ze = zis.getNextEntry();
            while(ze != null){
                String fileName = ze.getName();
                File newFile = new File(pathToTempFolder + File.separator + fileName);
                //System.out.println("Unzipping to "+newFile.getAbsolutePath());
                //create directories for sub directories in zip
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                //close this ZipEntry
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            //close last ZipEntry
            zis.closeEntry();
            zis.close();
            fis.close();
            return Objects.requireNonNull(dir.listFiles())[0];
        } catch (IOException e) {
            return null;
        }
    }
}