package com.peersafe.chainbet.model;

import java.io.Serializable;

/**
 * AUTHOR : sunhaitao.
 * DATA : 17/4/24
 * DESCRIPTION :
 */

public class VersionModel implements Serializable
{
    /**
     * name : 众想钱
     * version : 4
     * changelog : 1、上传更新版本
     * 2、替换chainCode
     * updated_at : 1491059860
     * versionShort : 1.1-0401-2308
     * build : 4
     * installUrl : http://download.fir
     * .im/v2/app/install/58b017a1959d694e3200064d?download_token
     * =fde826bbbb1c4fc8011e7c43e53b785d&source=update
     * install_url : http://download.fir
     * .im/v2/app/install/58b017a1959d694e3200064d?download_token
     * =fde826bbbb1c4fc8011e7c43e53b785d&source=update
     * direct_install_url : http://download.fir
     * .im/v2/app/install/58b017a1959d694e3200064d?download_token
     * =fde826bbbb1c4fc8011e7c43e53b785d&source=update
     * update_url : http://fir.im/webet
     * binary : {"fsize":15787165}
     */

    private String name;
    private String version;
    private String changelog;
    private Long updated_at;
    private String versionShort;
    private String build;
    private String installUrl;
    private String install_url;
    private String direct_install_url;
    private String update_url;
    /**
     * fsize : 15787165
     */

    private BinaryBean binary;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public String getChangelog()
    {
        return changelog;
    }

    public void setChangelog(String changelog)
    {
        this.changelog = changelog;
    }

    public long getUpdated_at()
    {
        return updated_at;
    }

    public void setUpdated_at(long updated_at)
    {
        this.updated_at = updated_at;
    }

    public String getVersionShort()
    {
        return versionShort;
    }

    public void setVersionShort(String versionShort)
    {
        this.versionShort = versionShort;
    }

    public String getBuild()
    {
        return build;
    }

    public void setBuild(String build)
    {
        this.build = build;
    }

    public String getInstallUrl()
    {
        return installUrl;
    }

    public void setInstallUrl(String installUrl)
    {
        this.installUrl = installUrl;
    }

    public String getInstall_url()
    {
        return install_url;
    }

    public void setInstall_url(String install_url)
    {
        this.install_url = install_url;
    }

    public String getDirect_install_url()
    {
        return direct_install_url;
    }

    public void setDirect_install_url(String direct_install_url)
    {
        this.direct_install_url = direct_install_url;
    }

    public String getUpdate_url()
    {
        return update_url;
    }

    public void setUpdate_url(String update_url)
    {
        this.update_url = update_url;
    }

    public BinaryBean getBinary()
    {
        return binary;
    }

    public void setBinary(BinaryBean binary)
    {
        this.binary = binary;
    }

    public static class BinaryBean implements Serializable
    {
        private long fsize;

        public long getFsize()
        {
            return fsize;
        }

        public void setFsize(long fsize)
        {
            this.fsize = fsize;
        }
    }
}
