package com.peersafe.chainbet.utils.common;

import android.os.Environment;
import android.util.Log;

import com.peersafe.chainbet.utils.log.OLLogger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtils
{
    private static String TAG = "FileUtils";

    public static byte[] getBytes(InputStream is) throws Exception
    {
        try
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
            try
            {
                byte[] b = new byte[1000];
                int n;
                while ((n = is.read(b)) != -1)
                {
                    out.write(b, 0, n);
                }
            }
            finally
            {
                is.close();
                out.close();
            }
            return out.toByteArray();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static String readFile(String input) throws Exception
    {
        char[] buffer = new char[4096];
        int len = 0;
        StringBuffer content = new StringBuffer(4096);

        try
        {
            InputStreamReader fr = new InputStreamReader(new FileInputStream(input), "UTF-8");
            BufferedReader br = new BufferedReader(fr);
            try
            {
                while ((len = br.read(buffer)) > -1)
                {
                    content.append(buffer, 0, len);
                }
            }
            finally
            {
                br.close();
                fr.close();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return content.toString();
    }

    public static byte[] readByNIO(String fileName)
    {
        // 第一步 获取通道
        FileInputStream fis = null;
        FileChannel fc = null;
        byte[] result = null;
        try
        {
            fis = new FileInputStream(fileName);
            fc = fis.getChannel();
            result = new byte[(int) fc.size()];
            // 第二步 指定缓冲区
            ByteBuffer bf = ByteBuffer.allocate(1024);
            // 第三步 将通道中的数据读取到缓冲区中
            // fc.read(bf);
            int len = 0;
            int total = 0;
            while ((len = fc.read(bf)) != -1)
            {
                bf.flip();
                while (bf.hasRemaining())
                {
                    bf.get(result, total, len);
                    total = total + len;
                }
                bf.clear();
            }
            bf = null;
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                fc.close();
                fis.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static byte[] getBytesFromFile(File f)
    {
        if (f == null)
        {
            return null;
        }
        try
        {
            FileInputStream stream = new FileInputStream(f);
            ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
            try
            {
                byte[] b = new byte[1000];
                int n;
                while ((n = stream.read(b)) != -1)
                {
                    out.write(b, 0, n);
                }
            }
            finally
            {
                stream.close();
                out.close();
            }
            return out.toByteArray();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 保存到SD卡
     *
     * @param filename
     * @param filecontent
     * @throws Exception
     */
    public void saveToSDCard(String filename, String filecontent) throws Exception
    {
        File file = new File(Environment.getExternalStorageDirectory(), filename);
        FileOutputStream outStream = new FileOutputStream(file);
        outStream.write(filecontent.getBytes());
        outStream.close();
    }

    //判断SD卡是否存在
    public static boolean existSDCard()
    {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED))
        {
            return true;
        } else
        {
            return false;
        }
    }


    public static void makeDir(String home) throws Exception
    {
        File homedir = new File(home);
        if (!homedir.exists())
        {
            try
            {
                homedir.mkdirs();
            }
            catch (Exception ex)
            {
                throw new Exception("Can not mkdir :" + home + " Maybe include special charactor!");
            }
        }
    }

    public static void saveToFile(byte[] content, String dirPth, String fileName) throws IOException
    {
        File file = new File(dirPth);
        if (!file.exists())
        {
            file.mkdir();
        }

        File outFile = new File(dirPth + File.separator + fileName);
        saveToFile(content, outFile);
    }

    public static void saveToFile(byte[] content, File outFile) throws IOException
    {
        FileOutputStream out = null;
        try
        {
            if (!outFile.exists())
            {
                outFile.createNewFile();
            }
            out = new FileOutputStream(outFile);
            out.write(content);
            out.flush();
        }
        catch (IOException e)
        {
            throw e;
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (out != null)
            {
                try
                {
                    out.close();
                }
                catch (Exception e1)
                {
                }
            }
        }
    }

    public static void saveToFile(String str, String filename) throws IOException
    {
        saveToFile(str, filename, "UTF-8");
    }

    public static void saveToFile(String str, String filename, String encode) throws IOException
    {
        if (str == null)
        {
            throw new IllegalArgumentException("string is null");
        }
        File f = new File(filename);
        if (!f.exists())
        {
            f.createNewFile();
            f.setExecutable(true, false);
            f.setWritable(true, false);
            f.setReadable(true, false);
        }

        FileOutputStream fos = new FileOutputStream(filename);

        Writer out = new OutputStreamWriter(fos, encode);

        out.write(str);

        out.close();
    }

    public static void byte2File(byte[] buf, String filePath, String fileName)
    {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try
        {
            File dir = new File(filePath);
            if (!dir.exists() && dir.isDirectory())
            {
                dir.mkdirs();
            }
            file = new File(filePath + File.separator + fileName);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(buf);
            bos.flush();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (bos != null)
            {
                try
                {
                    bos.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            if (fos != null)
            {
                try
                {
                    fos.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    public static int copyDir(String fromDir, String toDir)
    {
        //要复制的文件目录
        File[] currentFiles;
        File root = new File(fromDir);

        //如同判断SD卡是否存在或者文件是否存在
        //如果不存在则 return出去
        if (!root.exists())
        {
            return -1;
        }
        //如果存在则获取当前目录下的全部文件 填充数组
        currentFiles = root.listFiles();

        //目标目录
        File targetDir = new File(toDir);

        //创建目录
        if (!targetDir.exists())
        {
            targetDir.mkdirs();
        }

        //遍历要复制该目录下的全部文件
        for (int i = 0; i < currentFiles.length; i++)
        {
            if (currentFiles[i].isDirectory())//如果当前项为子目录 进行递归
            {
                copyDir(currentFiles[i].getPath() + "/", toDir + currentFiles[i].getName() + "/");

            } else//如果当前项为文件则进行文件拷贝
            {
                copyFile(currentFiles[i].getPath(), toDir + currentFiles[i].getName());
            }
        }
        return 0;
    }

    public static int copyFile(String fromFile, String toFile)
    {
        try
        {
            InputStream fosfrom = new FileInputStream(fromFile);
            OutputStream fosto = new FileOutputStream(toFile);
            byte bt[] = new byte[1024];
            int c;
            while ((c = fosfrom.read(bt)) > 0)
            {
                fosto.write(bt, 0, c);
            }
            fosfrom.close();
            fosto.close();
            return 0;

        }
        catch (Exception ex)
        {
            return -1;
        }
    }

    // 创建Zip包
    public static void zipFolder(String srcFilePath, String zipFilePath) throws Exception
    {
        java.util.zip.ZipOutputStream outZip = new java.util.zip.ZipOutputStream(new java.io
                .FileOutputStream(

                zipFilePath));

        // 打开要输出的文件
        java.io.File file = new java.io.File(srcFilePath);

        // 压缩
        zipFiles(file.getParent() + java.io.File.separator, file.getName(), outZip);

        // 完成,关闭
        outZip.finish();

        outZip.close();

    }

    private static void zipFiles(String folderPath, String filePath, java.util.zip
            .ZipOutputStream zipOut)

            throws Exception
    {
        int index = filePath.lastIndexOf(java.io.File.separator);
        String name = index != -1 ? filePath.substring(index + 1) : null;

        if ("db".equals(name) || "tcerts".equals(name))
        {
            return;
        }

        if (zipOut == null)
        {
            return;
        }

        java.io.File file = new java.io.File(folderPath + filePath);

        // 判断是不是文件
        if (file.isFile())
        {
            java.util.zip.ZipEntry zipEntry = new java.util.zip.ZipEntry(filePath);

            java.io.FileInputStream inputStream = new java.io.FileInputStream(file);

            zipOut.putNextEntry(zipEntry);


            int len;

            byte[] buffer = new byte[100000];


            while ((len = inputStream.read(buffer)) != -1)
            {

                zipOut.write(buffer, 0, len);

            }

            inputStream.close();

            zipOut.closeEntry();
        } else
        {
            // 文件夹的方式,获取文件夹下的子文件
            String fileList[] = file.list();

            // 如果没有子文件, 则添加进去即可
            if (fileList.length <= 0)
            {
                java.util.zip.ZipEntry zipEntry = new java.util.zip.ZipEntry(filePath + java.io
                        .File.separator);

                zipOut.putNextEntry(zipEntry);

                zipOut.closeEntry();
            }

            // 如果有子文件, 遍历子文件
            for (int i = 0; i < fileList.length; i++)
            {

                zipFiles(folderPath, filePath + java.io.File.separator + fileList[i], zipOut);

            }

        }
    }

    public static String unZipFolder(InputStream zipInputstream, String location) throws IOException
    {
        int size;
        byte[] buffer = new byte[1024];

        String userID = null;

        try
        {
            if (!location.endsWith("/"))
            {
                location += "/";
            }
            File f = new File(location);
            if (!f.isDirectory())
            {
                f.mkdirs();
            }
            ZipInputStream zin = new ZipInputStream(zipInputstream);
            try
            {
                boolean first = true;
                ZipEntry ze = null;
                while ((ze = zin.getNextEntry()) != null)
                {
                    String path = location + ze.getName();

                    if (first)
                    {
                        first = false;
                        userID = ze.getName().substring(0, ze.getName().indexOf("/"));
                    }

                    File unzipFile = new File(path);

                    if (ze.isDirectory())
                    {
                        if (!unzipFile.isDirectory())
                        {
                            unzipFile.mkdirs();
                        }
                    } else
                    {
                        // check for and create parent directories if they don't exist
                        File parentDir = unzipFile.getParentFile();
                        if (null != parentDir)
                        {
                            if (!parentDir.isDirectory())
                            {
                                parentDir.mkdirs();
                            }
                        }

                        // unzip the file
                        FileOutputStream out = new FileOutputStream(unzipFile, false);
                        BufferedOutputStream fout = new BufferedOutputStream(out, 1024);
                        try
                        {
                            while ((size = zin.read(buffer, 0, 1024)) > 0)
                            {
                                OLLogger.i(TAG, "unzip " + ze.getName() + ", length=" + size);
                                fout.write(buffer, 0, size);
                            }

                            zin.closeEntry();
                        }
                        finally
                        {
                            fout.flush();
                            fout.close();
                        }
                    }
                }
            }
            finally
            {
                zin.close();
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, "Unzip exception", e);
        }
        finally
        {
            return userID;
        }
    }

    /**
     * 删除文件或者文件夹
     *
     * @param file
     * @return
     */
    public static boolean delete(File file)
    {
        if (file.isDirectory())
        {
            File[] files = file.listFiles();
            if (files != null)
            {
                for (File f : files)
                {
                    delete(f);
                }
            }
        }
        return file.delete();
    }

    public static void unZipFile(InputStream unzipInputStream, String parent)
    {
        ZipInputStream zin = new ZipInputStream(unzipInputStream);
        BufferedInputStream bin = new BufferedInputStream(zin);

        File fout = null;
        ZipEntry entry;
        try
        {
            while ((entry = zin.getNextEntry()) != null)
            {
                fout = new File(parent, entry.getName());
                if (!fout.exists())
                {
                    (new File(fout.getParent())).mkdirs();
                }
                FileOutputStream out = new FileOutputStream(fout, false);
                BufferedOutputStream bout = new BufferedOutputStream(out, 1024);
                int b;
                while ((b = bin.read()) != -1)
                {
                    bout.write(b);
                }
                bout.close();
                out.close();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (bin != null)
                {
                    bin.close();
                }
                if (zin != null)
                {
                    zin.close();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
