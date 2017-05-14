package com.peersafe.chainbet.logic.sdbackrestore;

import android.os.Environment;

import com.peersafe.chainbet.OneLotteryApplication;
import com.peersafe.chainbet.db.UserInfo;
import com.peersafe.chainbet.logic.OneLotteryApi;
import com.peersafe.chainbet.manager.OneLotteryManager;
import com.peersafe.chainbet.manager.dbhelper.UserInfoDBHelper;
import com.peersafe.chainbet.model.OLMessageModel;
import com.peersafe.chainbet.utils.common.ConstantCode;
import com.peersafe.chainbet.utils.common.FileUtils;
import com.peersafe.chainbet.utils.common.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * AUTHOR : sunhaitao.
 * DATA : 17/4/18
 * DESCRIPTION :
 */

public class SdBackRestoreLogic
{
    private String extra = "user_extra_info";

    public boolean processBackFile(String pwd, int num)
    {
        UserInfo me = UserInfoDBHelper.getInstance().getCurPrimaryAccount();
        String rootPath = null;
        String infoPath = null;
        String extraPath = null;
        File rootFile = null;
        File infoFile = null;
        if (num > 2)
        {
            new FileNotFoundException();
            return false;
        } else if (num > 1)
        {
            rootPath = "/sdcard/" + me.getUserId();
            infoPath = "/sdcard/" + me.getUserId() + File.separator + me.getUserId();
            extraPath = "/sdcard/" + me.getUserId() + File.separator + extra;

            rootFile = checkDir(rootPath);
            infoFile = checkDir(infoPath);
            checkDir(extraPath);

        } else
        {
            rootPath = Environment.getExternalStorageDirectory() + File.separator + me.getUserId();
            infoPath = Environment.getExternalStorageDirectory() + File.separator + me.getUserId
                    () + File.separator + me.getUserId();
            extraPath = Environment.getExternalStorageDirectory() + File.separator + me.getUserId
                    () + File.separator + extra;

            rootFile = checkDir(rootPath);
            infoFile = checkDir(infoPath);
            checkDir(extraPath);
        }

        File target = new File(infoPath + ".zip");
        String zipFolder = OneLotteryApplication.getAppContext().getFilesDir().getAbsolutePath() +
                File.separator + "onelottery" + File.separator + "crypto" + File.separator +
                "client" + File.separator + me.getUserId();

        try
        {
            FileUtils.zipFolder(zipFolder, target.getAbsolutePath());
        }
        catch (FileNotFoundException e)
        {
            processBackFile(pwd, num + 1);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }

        //删除用户文本
        infoFile.delete();

        //将文本目录压缩到指定的目录
        String dirPath = null;
        if (num > 2)
        {
            new FileNotFoundException();
            return false;
        } else if (num > 1)
        {
            dirPath = "/sdcard/" + me.getUserId();
        } else
        {
            dirPath = Environment.getExternalStorageDirectory() + File.separator + me.getUserId();
        }

        File newTarget = new File(dirPath + ".zip");
        try
        {
            FileUtils.zipFolder(rootPath, newTarget.getAbsolutePath());
        }
        catch (FileNotFoundException e)
        {
            processBackFile(pwd, num + 1);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }

        FileUtils.delete(rootFile);

        byte[] bytesFromFile = FileUtils.getBytesFromFile(newTarget);
        byte[] bytes = OneLotteryApi.encryptFile(pwd, bytesFromFile);

        //删除多余的文件
        FileUtils.delete(newTarget);

        if (saveFileToLocal(num, me, bytes))
        {
            return false;
        }

        return true;
    }

    //保存到本地指定目录
    private boolean saveFileToLocal(int num, UserInfo me, byte[] bytes)
    {
        String folder = null;
        if (num > 2)
        {
            new FileNotFoundException();
            return true;
        } else if (num > 1)
        {
            folder = ConstantCode.CommonConstant.INNER_PATH;
        } else
        {
            folder = ConstantCode.CommonConstant.EXTER_PATH;
        }

        try
        {
            FileUtils.saveToFile(bytes, folder, me.getUserId());
        }
        catch (FileNotFoundException e)
        {
            saveFileToLocal(num + 1, me, bytes);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return true;
        }
        return false;
    }

    public boolean processRestoreFile(String pwd, File file)
    {
        if (StringUtils.isEmpty(pwd) || file == null)
        {
            return false;
        }

        //解密文件
        byte[] bytes = FileUtils.getBytesFromFile(file);

        byte[] decryptbytes = OneLotteryApi.decryptFile(pwd, bytes);

        // 最后将文件解压
        InputStream unzipInputStream = new ByteArrayInputStream(decryptbytes);
        String parent = Environment.getExternalStorageDirectory() + File.separator + "onechain";

        //解压zip文件到指定目录下
        FileUtils.unZipFile(unzipInputStream, parent);

        String userId = file.getName();

        //获取用户列表
        File userZip = new File(parent + File.separator + file.getName() + File.separator + file
                .getName() + ".zip");
        if (!userZip.exists())
        {
            return false;
        }
        //存在直接导入到手机目录中
        String zipFolder = OneLotteryApplication.getAppContext().getFilesDir().getAbsolutePath() +
                File.separator + "onelottery" + File.separator + "crypto" +
                File.separator + "client" + File.separator;

        FileInputStream fis = null;
        FileOutputStream fos = null;

        String toUnzipName = zipFolder + userId + ".zip";
        try
        {
            // 先将文件写入client的目录
            fis = new FileInputStream(userZip);

            File writeFile = new File(toUnzipName);
            if (writeFile.exists())
            {
                writeFile.delete();
            } else
            {
                writeFile.createNewFile();
            }

            fos = new FileOutputStream(toUnzipName);

            byte[] buffer = new byte[1024];
            while (true)
            {
                int byteRead = fis.read(buffer);
                if (byteRead == -1)
                {
                    break;
                }
                fos.write(buffer, 0, byteRead);
            }
            fos.flush();

        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
        finally
        {
            try
            {
                if (fis != null)
                {
                    fis.close();
                }
                if (fos != null)
                {
                    fos.close();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
                return false;
            }
        }

        FileInputStream in = null;
        try
        {
            // 然后将文件解密
            File zipFile = new File(toUnzipName);
            if (!zipFile.exists())
            {
                return false;
            }

            byte[] bytess = FileUtils.getBytesFromFile(zipFile);

            // 最后将文件解压
            InputStream InputStream = new ByteArrayInputStream(bytess);
            userId = FileUtils.unZipFolder(InputStream, zipFolder);

            if (zipFile.exists())
            {
                zipFile.delete();
            }

            long isSuccess = OneLotteryApi.checkCurUserPwd(userId, pwd);
            if (isSuccess != OneLotteryApi.SUCCESS)
            {
                FileUtils.delete(new File(zipFolder + userId));
                return false;
            } else
            {
                OneLotteryManager.getInstance().SendEventBus(userId, OLMessageModel
                        .STMSG_MODEL_IMPORT_SD_WALLENT_OK);
            }

            File parentFile = new File(parent);
            if (parentFile.exists())
            {
                FileUtils.delete(parentFile);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
        finally
        {
            try
            {
                if (in != null)
                {
                    in.close();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }

    private File checkDir(String foldPath)
    {
        File desDir = new File(foldPath);
        if (!desDir.exists())
        {
            desDir.mkdirs();
        }

        return desDir;
    }
}
