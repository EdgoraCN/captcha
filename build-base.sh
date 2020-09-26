docker build --no-cache . -t edgora/jdk-11-font:alpine
docker push  edgora/jdk-11-font:alpine
docker tag  edgora/jdk-11-font:alpine  registry.cn-beijing.aliyuncs.com/edgora-oss/jdk-11-font:alpine
docker push registry.cn-beijing.aliyuncs.com/edgora-oss/jdk-11-font:alpine