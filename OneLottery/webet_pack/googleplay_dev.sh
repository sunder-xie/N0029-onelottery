#google渠道的开发
echo start

#删除自动更新的包
rm -r -f ./app/libs/Bughd_android_sdk_v1.3.7.jar

#替换gradle文件
rm -r -f ./app/build.gradle
rm -r -f ./build.gradle
rm -r -f ./LRecyclerview_library/build.gradle
cp ./webet_pack/GooglePlay/app/build.gradle ./app
cp ./webet_pack/GooglePlay/global/build.gradle ./
cp ./webet_pack/GooglePlay/recycleview/build.gradle ./LRecyclerview_library

#替换清单等文件为googleplay渠道
rm -r -f ./app/src/main/AndroidManifest.xml
rm -r -f ./app/src/main/java/com/peersafe/chainbet/ui/update
cp ./webet_pack/GooglePlay/AndroidManifest.xml ./app/src/main/

#自动更新替换
rm -r -f ./app/src/main/java/com/peersafe/chainbet/utils/update/UpdateUtil.java
rm -r -f ./app/src/main/java/com/peersafe/chainbet/utils/common/Defaultcontent.java
cp ./webet_pack/GooglePlay/UpdateUtil.java ./app/src/main/java/com/peersafe/chainbet/utils/update
cp ./webet_pack/GooglePlay/Defaultcontent.java ./app/src/main/java/com/peersafe/chainbet/utils/common