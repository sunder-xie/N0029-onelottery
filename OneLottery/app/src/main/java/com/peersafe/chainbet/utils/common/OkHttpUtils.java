package com.peersafe.chainbet.utils.common;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * AUTHOR : sunhaitao.
 * DATA : 17/2/28
 * DESCRIPTION :
 */

public class OkHttpUtils
{

    public static String getJsonUrl(String url)
    {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder().url(url).build();
        try
        {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful())
            {
                return response.body().string();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return "";
    }
}
