FROM edgora/ppgojob-agent:2.8.0 as agent
FROM adoptopenjdk/openjdk11:alpine
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.ustc.edu.cn/g' /etc/apk/repositories
RUN apk add  --update  curl git docker-cli bash tzdata ttf-ubuntu-font-family ca-certificates msttcorefonts-installer fontconfig && rm -rf /var/cache/apk/*
RUN   fc-cache -f
RUN cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
RUN echo "Asia/Shanghai" >  /etc/timezone

COPY --from=agent /usr/local/bin/dockerize /usr/local/bin/dockerize
COPY --from=agent /usr/local/bin/gojq /usr/local/bin/gojq
