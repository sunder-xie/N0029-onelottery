#peersafe渠道开发
echo start

#增加自动更新的jar包
cp -rf ./webet_pack/peersafe/libs/Bughd_android_sdk_v1.3.7.jar

#增加peersafe update相关文件
cp -rf ./webet_pack/peersafe/update ./app/src/main/java/com/peersafe/chainbet/ui

#替换gradle文件
rm -r -f ./app/build.gradle
rm -r -f ./build.gradle
rm -r -f ./LRecyclerview_library/build.gradle
cp ./webet_pack/peersafe/app/build.gradle ./app
cp ./webet_pack/peersafe/global/build.gradle ./
cp ./webet_pack/peersafe/recycleview/build.gradle ./LRecyclerview_library

#替换清单等文件等为peersafe渠道
rm -r -f ./app/src/main/AndroidManifest.xml
cp ./webet_pack/peersafe/AndroidManifest.xml ./app/src/main/

#自动更新替换
rm -r -f ./app/src/main/java/com/peersafe/chainbet/utils/update/UpdateUtil.java
rm -r -f ./app/src/main/java/com/peersafe/chainbet/utils/common/Defaultcontent.java
cp ./webet_pack/peersafe/UpdateUtil.java ./app/src/main/java/com/peersafe/chainbet/utils/update
cp ./webet_pack/peersafe/Defaultcontent.java ./app/src/main/java/com/peersafe/chainbet/common