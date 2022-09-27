package com.androlua.util;

import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

public class ArcZipUtil {

    public static boolean zip(String dir,String zipdir){
        File dirfile = new File(dir);
        if(dirfile.exists()){
            String name = dirfile.getName();
            if(zipdir==null){
                zipdir = dirfile.getParentFile().getAbsolutePath()+'/'+name+".zip";
            }
            File dest = new File(zipdir);
            if(dirfile.isDirectory()) {
                try {
                    compress(dirfile.listFiles(), dest);
                    return false;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                try {
                    compress(dirfile,dest);
                    return false;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return  false;
    }

    public static void compress(File[] files, File zipFile) throws IOException {
        if (files == null) {
            return;
        }
        ZipArchiveOutputStream out = new ZipArchiveOutputStream(zipFile);
        out.setUseZip64(Zip64Mode.AsNeeded);
        // 将每个文件用ZipArchiveEntry封装
        for (File file : files) {
            if (file == null) {
                continue;
            }
            compressOneFile(file, out, "");
        }
        if (out != null) {
            out.close();
        }
    }

    public static void compress(File srcFile, File destFile) throws IOException {
        ZipArchiveOutputStream out = null;
        try {
            out = new ZipArchiveOutputStream(new BufferedOutputStream(
                    new FileOutputStream(destFile), 1024));
            compressOneFile(srcFile, out, "");
        } finally {
            out.close();
        }
    }

    private static void compressOneFile(File srcFile,
                                        ZipArchiveOutputStream out, String dir) throws IOException {
        if (srcFile.isDirectory()) {// 对文件夹进行处理。
            ZipArchiveEntry entry = new ZipArchiveEntry(dir + srcFile.getName()
                    + "/");
            out.putArchiveEntry(entry);
            out.closeArchiveEntry();
            // 循环文件夹中的所有文件进行压缩处理。
            String[] subFiles = srcFile.list();
            for (String subFile : subFiles) {
                compressOneFile(new File(srcFile.getPath() + "/" + subFile),
                        out, (dir + srcFile.getName() + "/"));
            }
        } else { // 普通文件。
            InputStream is = null;
            try {
                is = new BufferedInputStream(new FileInputStream(srcFile));
                // 创建一个压缩包。
                ZipArchiveEntry entry = new ZipArchiveEntry(srcFile, dir
                        + srcFile.getName());
                out.putArchiveEntry(entry);
                IOUtils.copy(is, out);
                out.closeArchiveEntry();
            } finally {
                if (is != null)
                    is.close();
            }
        }
    }

    public static boolean unzip(String dir,String undir){
       File dirfile = new File(dir);
       if(dirfile.exists()){
           try {
               decompressZip(dirfile,undir);
               return true;
           } catch (IOException e) {
               e.printStackTrace();
           }
       }
        return  false;
    }

    public static boolean unzip(String dir,String undir,String filename){
        File dirfile = new File(dir);
        if(dirfile.exists()){
            try {
                decompressZip(dirfile,filename,undir);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return  false;
    }

    public static void decompressZip(File zipFile, String dir) throws IOException {
        ZipFile zf = new ZipFile(zipFile);
        try {
            for (Enumeration<ZipArchiveEntry> entries = zf.getEntries(); entries
                    .hasMoreElements(); ) {
                ZipArchiveEntry ze = entries.nextElement();
                // 不存在则创建目标文件夹。
                File targetFile = new File(dir, ze.getName());
                // 遇到根目录时跳过。
                if (ze.getName().lastIndexOf("/") == (ze.getName().length() - 1)) {
                    continue;
                }
                // 如果文件夹不存在，创建文件夹。
                if (!targetFile.getParentFile().exists()) {
                    targetFile.getParentFile().mkdirs();
                }

                InputStream i = zf.getInputStream(ze);
                OutputStream o = null;
                try {
                    o = new FileOutputStream(targetFile);
                    IOUtils.copy(i, o);
                } finally {
                    if (i != null) {
                        i.close();
                    }
                    if (o != null) {
                        o.close();
                    }
                }
            }
        } finally {
            zf.close();
        }
    }

    public static void decompressZip(File zipFile, String fileName, String dir)
            throws IOException {
        // 不存在则创建目标文件夹。
        File targetFile = new File(dir, fileName);
        if (!targetFile.getParentFile().exists()) {
            targetFile.getParentFile().mkdirs();
        }

        ZipFile zf = new ZipFile(zipFile);
        Enumeration<ZipArchiveEntry> zips = zf.getEntries();
        ZipArchiveEntry zip = null;
        while (zips.hasMoreElements()) {
            zip = zips.nextElement();
            if (fileName.equals(zip.getName())) {
                OutputStream o = null;
                InputStream i = zf.getInputStream(zip);
                try {
                    o = new FileOutputStream(targetFile);
                    IOUtils.copy(i, o);
                } finally {
                    if (i != null) {
                        i.close();
                    }
                    if (o != null) {
                        o.close();
                    }
                }
            }
        }
    }

    public ZipArchiveEntry readZip(File zipFile, String fileName)
            throws IOException {
        ZipFile zf = new ZipFile(zipFile);
        Enumeration<ZipArchiveEntry> zips = zf.getEntries();
        ZipArchiveEntry zip = null;
        while (zips.hasMoreElements()) {
            zip = zips.nextElement();
            if (fileName.equals(zip.getName())) {
                return zip;
            }
        }
        return null;
    }

    public Enumeration<ZipArchiveEntry> readZip(File zipFile)
            throws IOException {
        ZipFile zf = new ZipFile(zipFile);
        Enumeration<ZipArchiveEntry> zips = zf.getEntries();
        return zips;
    }
}
