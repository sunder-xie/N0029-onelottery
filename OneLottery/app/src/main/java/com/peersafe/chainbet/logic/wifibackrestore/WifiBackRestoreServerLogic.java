package com.peersafe.chainbet.logic.wifibackrestore;

import android.content.Context;
import android.os.Environment;
import android.webkit.MimeTypeMap;

import com.peersafe.chainbet.OneLotteryApplication;
import com.peersafe.chainbet.R;
import com.peersafe.chainbet.db.UserInfo;
import com.peersafe.chainbet.logic.OneLotteryApi;
import com.peersafe.chainbet.manager.OneLotteryManager;
import com.peersafe.chainbet.manager.dbhelper.UserInfoDBHelper;
import com.peersafe.chainbet.model.OLMessageModel;
import com.peersafe.chainbet.utils.common.FileUtils;
import com.peersafe.chainbet.utils.common.StringUtils;
import com.peersafe.chainbet.utils.log.OLLogger;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.request.Method;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.nanohttpd.protocols.http.response.Response.newChunkedResponse;
import static org.nanohttpd.protocols.http.response.Response.newFixedLengthResponse;

public class WifiBackRestoreServerLogic extends NanoHTTPD
{
    private Context mContext;
    private Map<String, String> mReplaceHtmlWordMap = new HashMap<String, String>();   //html界面多语言相关
    private String mIndexHtmlString;
    private boolean mIsImport;

    public WifiBackRestoreServerLogic(Context context, int port, boolean bIsImport)
    {
        super(port);
        mContext = context;

        mIsImport = bIsImport;

        initData();
    }

    private void initData()
    {
        mReplaceHtmlWordMap.put("%title%", mContext.getString(R.string.app_name));
        mReplaceHtmlWordMap.put("%html.emptypass%", mContext.getString(R.string
                .setting_backrestore_html_emptypass));
//		mReplaceHtmlWordMap.put("%html.diffpass%", mContext.getString(R.string
// .setting_backrestore_html_diffpass));
        mReplaceHtmlWordMap.put("%html.shortpass%", mContext.getString(R.string
                .setting_backrestore_html_shortpass));
        mReplaceHtmlWordMap.put("%html.importOk%", mContext.getString(R.string
                .setting_backrestore_html_resotreOk));
        mReplaceHtmlWordMap.put("%html.importFail%", mContext.getString(R.string
                .setting_backrestore_html_resotreFailed));
        mReplaceHtmlWordMap.put("%html.exportFailed%", mContext.getString(R.string
                .setting_backrestore_html_backupFailed));
//		mReplaceHtmlWordMap.put("%html.backupRestore%", mContext.getString(R.string
// .setting_backrestore_html_backupRestore));
//		mReplaceHtmlWordMap.put("%setting.Backup%", mContext.getString(R.string
// .setting_backrestore_html_backup_contact));
//		mReplaceHtmlWordMap.put("%setting.Restore%", mContext.getString(R.string
// .setting_backrestore_html_restore_contact));
//		mReplaceHtmlWordMap.put("%html.pass1%", mContext.getString(R.string
// .setting_backrestore_html_input_pwd));
//		mReplaceHtmlWordMap.put("%html.pass2%", mContext.getString(R.string
// .setting_backrestore_html_confirm_pwd));
//		mReplaceHtmlWordMap.put("%html.close%", mContext.getString(R.string
// .setting_backrestore_html_close));
//		mReplaceHtmlWordMap.put("%html.backup%", mContext.getString(R.string
// .setting_backrestore_html_backup));
//		mReplaceHtmlWordMap.put("%html.restore%", mContext.getString(R.string
// .setting_backrestore_html_restore));
//		mReplaceHtmlWordMap.put("%html.selBackupFile%", mContext.getString(R.string
// .setting_backrestore_html_select_backup_file));
//		mReplaceHtmlWordMap.put("%html.restorePass%", mContext.getString(R.string
// .setting_backrestore_html_restore_pwd));
        mReplaceHtmlWordMap.put("%exportFailFlag%", "0");
        mReplaceHtmlWordMap.put("%importAlertFlag%", "0");
        mReplaceHtmlWordMap.put("%actionFlag%", mIsImport ? "1" : "0");

//		checkDir(WIFI_BACKUP_NAME);
//		checkDir(WIFI_RESTORE_NAME);
    }

    private void checkDir(String foldPath)
    {
        File desDir = new File(foldPath);
        if (!desDir.exists())
        {
            boolean result = desDir.mkdirs();
            OLLogger.i("WIFI", foldPath + " mkdirs " + result + ", exist? " + desDir.exists());
        }
    }

    private void processHtmlInterfaceShow()
    {
        String answer = getFromAssets("WifiBackRestore/index.html");

        for (String key : mReplaceHtmlWordMap.keySet())
        {
            answer = answer.replace(key, mReplaceHtmlWordMap.get(key));
        }
        mIndexHtmlString = answer;
    }

    @Override
    public Response serve(IHTTPSession session)
    {
        String uriStr = session.getUri();
        String filePath = "WifiBackRestore";
        String mimeType = NanoHTTPD.MIME_HTML;
        Method method = session.getMethod();
        if (uriStr.equals("/") || uriStr.equals("/index.html"))
        {
            filePath = filePath + "/index.html";
        } else if (Method.POST == method && uriStr.equals("/importWallet"))
        {
            Map<String, String> files = new HashMap<String, String>();
            try
            {
                session.parseBody(files);
            }
            catch (IOException ioe)
            {
                return newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT,
                        "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
            }
            catch (ResponseException re)
            {
                return newFixedLengthResponse(re.getStatus(), NanoHTTPD.MIME_PLAINTEXT, re
                        .getMessage());
            }

            int err = processRestoreFile(session.getParms(), files);
            mReplaceHtmlWordMap.put("%importAlertFlag%", err == 0 ? "1" : "0");

            switch (err)
            {
                case 1:
                case 2:
                    OneLotteryManager.getInstance().SendEventBus(mContext.getString(R.string
                                    .setting_backrestore_html_emptypass_orfile),
                            OLMessageModel.STMSG_MODEL_IMPORT_WALLET_ERR);
                    break;
                case 3:
                    OneLotteryManager.getInstance().SendEventBus(mReplaceHtmlWordMap.get("%html" +
                                    ".shortpass%"),
                            OLMessageModel.STMSG_MODEL_IMPORT_WALLET_ERR);
                case 8:
                    OneLotteryManager.getInstance().SendEventBus(mContext.getString(R.string
                                    .login_fail),
                            OLMessageModel.STMSG_MODEL_IMPORT_WALLET_ERR);
                case 0:
                    break;
                default:
                    OneLotteryManager.getInstance().SendEventBus(mReplaceHtmlWordMap.get("%html" +
                                    ".importFail%"),
                            OLMessageModel.STMSG_MODEL_IMPORT_WALLET_ERR);
                    break;
            }
            filePath = filePath + "/index.html";
        } else if (Method.POST == method && uriStr.equals("/exportWallet"))
        {
            Map<String, String> files = new HashMap<String, String>();
            try
            {
                session.parseBody(files);
            }
            catch (IOException ioe)
            {
                return newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT,
                        "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
            }
            catch (ResponseException re)
            {
                return newFixedLengthResponse(re.getStatus(), NanoHTTPD.MIME_PLAINTEXT, re
                        .getMessage());
            }

            try
            {
                if (session.getParms().containsKey("pas"))
                {
                    String pwd = session.getParms().get("pas");
                    if (StringUtils.isEmpty(pwd))
                    {
                        return newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHTTPD
                                        .MIME_PLAINTEXT,
                                mContext.getString(R.string.common_enter_password));
                    }
                    if (pwd.length() < 8)
                    {
                        return newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHTTPD
                                        .MIME_PLAINTEXT,
                                mContext.getString(R.string.register_password_length_not_correct));
                    }
                    if (StringUtils.isEmpty(OneLotteryApi.getCurUserId()))
                    {
                        return newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHTTPD
                                        .MIME_PLAINTEXT,
                                mContext.getString(R.string.not_login));
                    }
                    long isSuccess = OneLotteryApi.login(OneLotteryApi.getCurUserId(), pwd);
                    if (isSuccess != OneLotteryApi.SUCCESS)
                    {
                        return newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHTTPD
                                        .MIME_PLAINTEXT,
                                mContext.getString(R.string.login_fail));
                    } else
                    {
                        return processBackupFile(pwd, 1, "");
                    }
                }
                mReplaceHtmlWordMap.put("%exportFailFlag%", "1");
                processHtmlInterfaceShow();
                return newFixedLengthResponse(Status.OK, mimeType, mIndexHtmlString);
            }
            catch (Exception ioe)
            {
                return newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT,
                        "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
            }
        } else
        {
            filePath = filePath + uriStr;
            mimeType = getMimeTypeFromPath(filePath);
        }

        try
        {
            if (filePath.contains("/index.html"))
            {
                processHtmlInterfaceShow();
                return newFixedLengthResponse(Status.OK, mimeType, mIndexHtmlString);
            } else
            {
                InputStream inputStream = mContext.getResources().getAssets().open(filePath);
                return newChunkedResponse(Status.OK, mimeType, inputStream);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return super.serve(session);
    }

    private Response processBackupFile(String pwd, int num, String errMessage)
    {
        String extra = "user_extra_info";

        UserInfo me = UserInfoDBHelper.getInstance().getCurPrimaryAccount();

        OLLogger.i("WIFI", "num=" + num);
        String filepath = null;
        String infoPath = null;
        String extraPath = null;

        if (num > 2)
        {
            return newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT,
                    "SERVER INTERNAL ERROR: IOException: " + errMessage);
        } else if (num > 1)
        {
            filepath = "/sdcard/" + me.getUserId();
            infoPath = "/sdcard/" + me.getUserId() + File.separator + me.getUserId();
            extraPath = "/sdcard/" + me.getUserId() + File.separator + extra;
            checkDir(filepath);
            checkDir(infoPath);
            checkDir(extraPath);
        } else
        {
            filepath = Environment.getExternalStorageDirectory() + "/" + me.getUserId() + "/";
            infoPath = Environment.getExternalStorageDirectory() + "/" + me.getUserId() + "/" + me.getUserId();
            extraPath = Environment.getExternalStorageDirectory() + "/" + me.getUserId() + "/" + extra;
            checkDir(filepath);
            checkDir(infoPath);
            checkDir(extraPath);
        }

        File target = new File(infoPath + ".zip");
        String zipFolder = OneLotteryApplication.getAppContext().getFilesDir().getAbsolutePath() +
                File.separator + "onelottery" + File.separator + "crypto" +
                File.separator + "client" + File.separator + me.getUserId();

        try
        {
            FileUtils.zipFolder(zipFolder, target.getAbsolutePath());
        }
        catch (FileNotFoundException e)
        {
            return processBackupFile(pwd, num + 1, e.getMessage());
        }
        catch (Exception e)
        {
            return newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT,
                    "SERVER INTERNAL ERROR: IOException: " + e.getMessage());
        }

        String dirPath = null;
        if (num > 2)
        {
            return newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT,
                    "SERVER INTERNAL ERROR: IOException: " + errMessage);
        } else if (num > 1)
        {
            dirPath = "/sdcard/onechain/";
            checkDir(dirPath);
        } else
        {
            dirPath = Environment.getExternalStorageDirectory() + "/onechain/";
            checkDir(dirPath);
        }

        File dirTarget = new File(dirPath + me.getUserId() + ".zip");

        try
        {
            FileUtils.zipFolder(filepath, dirTarget.getAbsolutePath());
        }
        catch (FileNotFoundException e)
        {
            return processBackupFile(pwd, num + 1, errMessage);
        }
        catch (Exception e)
        {
            return newFixedLengthResponse(Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT,
                    "SERVER INTERNAL ERROR: IOException: " + e.getMessage());
        }

        byte[] bytes = FileUtils.getBytesFromFile(dirTarget);

        byte[] encryptBytes = OneLotteryApi.encryptFile(pwd, bytes);
        InputStream inputStream = new ByteArrayInputStream(encryptBytes);
        Response response = newChunkedResponse(Status.OK, "application/octet-stream", inputStream);
        if (response.getStatus() == Status.OK)
        {
            dirTarget.delete();
        }
        response.addHeader("content-disposition", "attachment; filename=" + me.getUserId());
        return response;
    }

    public int processRestoreFile(Map<String, String> params, Map<String, String> files)
    {
        if (!params.containsKey("importPwd") || !params.containsKey("file"))
        {
            return 1;
        }

        String resPwd = params.get("importPwd");
        String fileName = params.get("file");
        String userID = fileName;

        if (StringUtils.isEmpty(resPwd) || StringUtils.isEmpty(fileName))
        {
            return 2;
        }

        if (resPwd.length() < 8)
        {
            return 3;
        }

        String zipFolder = OneLotteryApplication.getAppContext().getFilesDir().getAbsolutePath() +
                File.separator + "onelottery" + File.separator + "crypto" +
                File.separator + "client" + File.separator;

        FileInputStream fis = null;
        FileOutputStream fos = null;
        for (String s : files.keySet())
        {
            String toUnzipName = zipFolder + fileName + ".zip";
            try
            {
                // 先将文件写入client的目录
                fis = new FileInputStream(files.get(s));

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
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
                return 4;
            }
            catch (IOException e)
            {
                e.printStackTrace();
                return 5;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return 6;
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
                }
            }

            FileInputStream in = null;
            try
            {
                // 然后将文件解密
                File zipFile = new File(toUnzipName);
                if (!zipFile.exists())
                {
                    return 7;
                }

                byte[] bytes = FileUtils.getBytesFromFile(zipFile);

                byte[] decryptbytes = OneLotteryApi.decryptFile(resPwd, bytes);

                // 最后将文件解压
                InputStream unzipInputStream = new ByteArrayInputStream(decryptbytes);
                FileUtils.unZipFolder(unzipInputStream, zipFolder);

                if (zipFile.exists())
                {
                    zipFile.delete();
                }

                File userFile = new File(zipFolder + userID + "/" + userID + ".zip");
                byte[] bytesFromFile = FileUtils.getBytesFromFile(userFile);
                InputStream InputStream = new ByteArrayInputStream(bytesFromFile);

                File rootFile = new File(zipFolder + userID);
                if(rootFile.exists())
                {
                    FileUtils.delete(rootFile);
                }

                //解压用户钱包
                FileUtils.unZipFolder(InputStream,zipFolder);

                long isSuccess = OneLotteryApi.checkCurUserPwd(userID, resPwd);
                if (isSuccess != OneLotteryApi.SUCCESS)
                {
                    File file = new File(zipFolder + userID);
                    FileUtils.delete(file);
                    return 8;
                } else
                {
                    OneLotteryManager.getInstance().SendEventBus(userID, OLMessageModel
                            .STMSG_MODEL_IMPORT_WALLET_OK);
                }
            }

            catch (IOException e)
            {
                e.printStackTrace();
                return 9;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return 10;
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
                }
            }

        }

        return 0;
    }

    private String getMimeTypeFromPath(String path)
    {
        String extension = path;
        int lastDot = extension.lastIndexOf('.');
        if (lastDot != -1)
        {
            extension = extension.substring(lastDot + 1);
        }

        // Convert the URI string to lower case to ensure compatibility with MimeTypeMap (see
        // CB-2185).
        extension = extension.toLowerCase(Locale.getDefault());
        if (extension.equals("3ga"))
        {
            return "audio/3gpp";
        } else if (extension.equals("js"))
        {
            // Missing from the map :(.
            return "text/javascript";
        }
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    public String getFromAssets(String fileName)
    {
        String result = "";
        try
        {
            InputStream inputStream = mContext.getResources().getAssets().open(fileName);
            InputStreamReader inputReader = new InputStreamReader(inputStream);
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line = "";

            while ((line = bufReader.readLine()) != null)
            {
                result += line;
            }
            return result;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return result;
    }
}
