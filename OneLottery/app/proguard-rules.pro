# To enable ProGuard in your project, edit project.properties
# to define the proguard.config property as described in that file.
#
# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in ${sdk.dir}/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the ProGuard
# include property in project.properties.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html
 
# Add any project specific keep options here:
 
# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
 
-ignorewarnings
-optimizationpasses 5  
-dontusemixedcaseclassnames  
-dontskipnonpubliclibraryclasses  
-dontpreverify  
-verbose  
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*  

-keep public class * extends android.app.Fragment    
-keep public class * extends android.app.Activity  
-keep public class * extends android.app.Application  
-keep public class * extends android.app.Service  
-keep public class * extends android.content.BroadcastReceiver  
-keep public class * extends android.content.ContentProvider  
-keep public class * extends android.app.backup.BackupAgentHelper  
-keep public class * extends android.preference.Preference  
-keep public class * extends android.support.v7.**
-keep public class com.android.vending.licensing.ILicensingService

#---------------------------------实体类---------------------------------
-keep class com.peersafe.chainbet.model.** { *;}

#----------------------------------js操作实体类-------------------------------------
#保留跟 javascript相关的属性
-keepattributes JavascriptInterface

#保留JavascriptInterface中的方法
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

#这个根据自己的project来设置，这个类用来与js交互，所以这个类中的 字段 ，方法， 等尽量保持
-keepclassmembers public class com.peersafe.chainbet.ui.lottery.BannerViewActivity{
   <fields>;
   <methods>;
   public *;
   private *;
}

-keepclasseswithmembers class * {  
    public <init>(android.content.Context, android.util.AttributeSet);  
}  
  
-keepclasseswithmembers class * {  
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
  
-keepclassmembers class * extends android.app.Activity {   
    public void *(android.view.View);  
}  
  
-keep public class * extends android.view.View {  
    public <init>(android.content.Context);  
    public <init>(android.content.Context, android.util.AttributeSet);  
    public <init>(android.content.Context, android.util.AttributeSet, int);  
    public void set*(...);  
}  

-keepclassmembers enum * {  
    public static **[] values();  
    public static ** valueOf(java.lang.String);  
}  

-keep class * implements android.os.Parcelable {  
    public static final android.os.Parcelable$Creator *;  
}  
  
-keepnames class * implements java.io.Serializable  
  
-keepclassmembers class * implements java.io.Serializable {  
    static final long serialVersionUID;  
    private static final java.io.ObjectStreamField[] serialPersistentFields;  
    !static !transient <fields>;  
    private void writeObject(java.io.ObjectOutputStream);  
    private void readObject(java.io.ObjectInputStream);  
    java.lang.Object writeReplace();  
    java.lang.Object readResolve();  
}  
  
-keepattributes Signature  
  
-keepattributes *Annotation*  
  
-keep class **.R$* { *; }  
  
-dontwarn android.support.v7.**
-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }


# # -------------------------------------------
# #  ######## greenDao混淆  ########## 
# # -------------------------------------------
-keep class de.greenrobot.dao.** {*;}
-keepclassmembers class * extends de.greenrobot.dao.AbstractDao {
    public static java.lang.String TABLENAME;
}
-keep class **$Properties
 
# # -------------------------------------------
# #  ######## umeng混淆  ########## 
# # -------------------------------------------
-keepclassmembers class * {
   public <init>(org.json.JSONObject);
}
 
# # -------------------------------------------
# #  ######## gilde  ##########
# # -------------------------------------------
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# # -------------------------------------------
# #  ######## eventbus混淆  ##########
# # -------------------------------------------
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

# # -------------------------------------------
# #  ######## okhttp3混淆  ##########
# # -------------------------------------------
-dontwarn okhttp3.**

# # -------------------------------------------
# #  ######## becloud混淆  ##########
# # -------------------------------------------
-dontwarn cn.beecloud.**
-dontwarn com.alipay.**
-dontwarn com.baidu.**
-dontwarn com.tencent.**

#保留类签名声明
-keepattributes Signature
#BeeCloud
-keep class cn.beecloud.** { *; }
-keep class com.google.** { *; }
#支付宝
-keep class com.alipay.** { *; }
#微信
-keep class com.tencent.** { *; }
#银联
-keep class com.unionpay.** { *; }
#百度
-keep class com.baidu.** { *; }
-keep class com.dianxinos.** { *; }

#Android Studio中包含PayPal依赖，需要添加
-dontwarn com.paypal.**
-dontwarn io.card.payment.**
-dontwarn okhttp3.**
-dontwarn okio.**

-keep class com.paypal.** { *; }
-keep class io.card.payment.** { *; }

-keep interface okhttp3.** { *; }
-keep interface okio.** { *; }

-keep class okhttp3.** { *; }
-keep class okio.** { *; }

# # -------------------------------------------
# #  ######## 阿里百川混淆  ##########
# # -------------------------------------------
-keep class com.alibaba.sdk.android.feedback.impl.FeedbackServiceImpl {*;}
-keep class com.alibaba.sdk.android.feedback.impl.FeedbackAPI {*;}
-keep class com.alibaba.sdk.android.feedback.util.IWxCallback {*;}
-keep class com.alibaba.sdk.android.feedback.util.IUnreadCountCallback{*;}
-keep class com.alibaba.sdk.android.feedback.FeedbackService{*;}
-keep public class com.alibaba.mtl.log.model.LogField {public *;}
-keep class com.taobao.securityjni.**{*;}
-keep class com.taobao.wireless.security.**{*;}
-keep class com.ut.secbody.**{*;}
-keep class com.taobao.dp.**{*;}
-keep class com.alibaba.wireless.security.**{*;}
-keep class com.ta.utdid2.device.**{*;}

# # -------------------------------------------
# #  ######## 友盟分享混淆  ##########
# # -------------------------------------------
-dontusemixedcaseclassnames
    -dontshrink
    -dontoptimize
    -dontwarn com.google.android.maps.**
    -dontwarn android.webkit.WebView
    -dontwarn com.umeng.**
    -dontwarn com.tencent.weibo.sdk.**
    -dontwarn com.facebook.**
    -keep public class javax.**
    -keep public class android.webkit.**
    -dontwarn android.support.v4.**
    -keep enum com.facebook.**
    -keepattributes Exceptions,InnerClasses,Signature
    -keepattributes *Annotation*
    -keepattributes SourceFile,LineNumberTable

    -keep public interface com.facebook.**
    -keep public interface com.tencent.**
    -keep public interface com.umeng.socialize.**
    -keep public interface com.umeng.socialize.sensor.**
    -keep public interface com.umeng.scrshot.**
    -keep class com.android.dingtalk.share.ddsharemodule.** { *; }
    -keep public class com.umeng.socialize.* {*;}


    -keep class com.facebook.**
    -keep class com.facebook.** { *; }
    -keep class com.umeng.scrshot.**
    -keep public class com.tencent.** {*;}
    -keep class com.umeng.socialize.sensor.**
    -keep class com.umeng.socialize.handler.**
    -keep class com.umeng.socialize.handler.*
    -keep class com.umeng.weixin.handler.**
    -keep class com.umeng.weixin.handler.*
    -keep class com.umeng.qq.handler.**
    -keep class com.umeng.qq.handler.*
    -keep class UMMoreHandler{*;}
    -keep class com.tencent.mm.sdk.modelmsg.WXMediaMessage {*;}
    -keep class com.tencent.mm.sdk.modelmsg.** implements   com.tencent.mm.sdk.modelmsg.WXMediaMessage$IMediaObject {*;}
    -keep class im.yixin.sdk.api.YXMessage {*;}
    -keep class im.yixin.sdk.api.** implements im.yixin.sdk.api.YXMessage$YXMessageData{*;}
    -keep class com.tencent.mm.sdk.** {
     *;
    }
    -keep class com.tencent.mm.opensdk.** {
   *;
    }
    -dontwarn twitter4j.**
    -keep class twitter4j.** { *; }

    -keep class com.tencent.** {*;}
    -dontwarn com.tencent.**
    -keep public class com.umeng.com.umeng.soexample.R$*{
    public static final int *;
    }
    -keep public class com.linkedin.android.mobilesdk.R$*{
    public static final int *;
        }
    -keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    }

    -keep class com.tencent.open.TDialog$*
    -keep class com.tencent.open.TDialog$* {*;}
    -keep class com.tencent.open.PKDialog
    -keep class com.tencent.open.PKDialog {*;}
    -keep class com.tencent.open.PKDialog$*
    -keep class com.tencent.open.PKDialog$* {*;}

    -keep class com.sina.** {*;}
    -dontwarn com.sina.**
    -keep class  com.alipay.share.sdk.** {
       *;
    }
    -keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
    }

    -keep class com.linkedin.** { *; }
    -keepattributes Signature

