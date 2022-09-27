package com.androlua.util;

import com.androlua.LuaUtil;
import com.luajava.LuaError;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class comZipUtil
{

	public static void ZipCompress(String str, String str2) throws Exception {
		File s = new File(str);
		if(!s.exists()){
			throw new Exception(s.getPath() + "所指文件不存在");
		}
		if(str2==null){
			str2=s.getParentFile().getAbsolutePath() + "/" + s.getName() + ".zip";
		}
		if (!s.getParentFile().exists())
		{
			if (!s.getParentFile().mkdirs())
			{
				throw new Exception(s.getParentFile().getPath() + "文件目录创建失败不存在");
			}
		}
		FileOutputStream dest = new FileOutputStream(str2);
		CheckedOutputStream checksum = new CheckedOutputStream(dest, new CRC32());
		ZipOutputStream zipOutputStream  = new ZipOutputStream(new BufferedOutputStream(checksum));
		BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(zipOutputStream);
		zipOutputStream.setMethod(ZipOutputStream.DEFLATED);
		compress(zipOutputStream, bufferedOutputStream, new File(str), null);
		bufferedOutputStream.close();
		zipOutputStream.close();
		return;
	}

	public static void ZipUncompress(String str, String str2) throws Exception {
		File file = new File(str);
		if (!file.exists()) {
			throw new Exception(file.getPath() + "所指文件不存在");
		}
		File target=new File(str2);
		if (!target.exists())
		{
			if (!target.mkdirs())
			{
				throw new Exception(target.getPath() + "文件目录创建失败不存在");
			}
		}
		FileInputStream fis= new FileInputStream(file);
		CheckedInputStream checksum = new CheckedInputStream(fis, new CRC32());
		ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(checksum));
		while (true) {
			ZipEntry nextEntry = zipInputStream.getNextEntry();
			if (nextEntry == null) {
				return;
			}
			if (!nextEntry.isDirectory()) {
				File file2 = new File(str2, nextEntry.getName());
				if (!file2.exists()) {
					new File(file2.getParent()).mkdirs();
				}
				FileOutputStream fileOutputStream = new FileOutputStream(file2);
				BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
				byte[] bArr = new byte[256];
				while (true) {
					int read = zipInputStream.read(bArr);
					if (read == -1) {
						break;
					}
					bufferedOutputStream.write(bArr, 0, read);
					bufferedOutputStream.flush();
				}
				bufferedOutputStream.close();
				fileOutputStream.close();
			}
		}
	}

	public static void compress(ZipOutputStream zipOutputStream, BufferedOutputStream bufferedOutputStream, File file, String str) throws IOException {
		if (file.isDirectory()) {
			File[] listFiles = file.listFiles();
			if (listFiles.length == 0) {
				zipOutputStream.putNextEntry(new ZipEntry(str + "/"));
				return;
			}
			for (int i = 0; i < listFiles.length; i++) {
				if(str!=null) {
					compress(zipOutputStream, bufferedOutputStream, listFiles[i], str + "/" +listFiles[i].getName());
				}else{
					compress(zipOutputStream, bufferedOutputStream, listFiles[i], listFiles[i].getName());
				}
			}
			return;
		}
		zipOutputStream.putNextEntry(new ZipEntry(str));
		FileInputStream fileInputStream = new FileInputStream(file);
		BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
		byte[] bArr = new byte[256];
		while (true) {
			int read = bufferedInputStream.read(bArr);
			if (read != -1) {
				bufferedOutputStream.write(bArr, 0, read);
				bufferedOutputStream.flush();
			} else {
				bufferedInputStream.close();
				fileInputStream.close();
				return;
			}
		}
	}

	public static boolean zip(String sourceFilePath, String zipFilePath) {
		try {
			ZipCompress(sourceFilePath, zipFilePath);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static boolean unzip(String zipPath, String destPath) {
			try {
				ZipUncompress(zipPath, destPath);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
	}
}
