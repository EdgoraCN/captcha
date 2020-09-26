mvn clean package jib:dockerBuild
docker push edgora/captcha
docker tag edgora/captcha registry.cn-beijing.aliyuncs.com/edgora-oss/captcha
docker push registry.cn-beijing.aliyuncs.com/edgora-oss/captcha