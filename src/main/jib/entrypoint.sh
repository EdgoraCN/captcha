
echo "The application will start in ${APP_SLEEP}s..." && sleep ${APP_SLEEP}
exec java ${JAVA_OPTS} -noverify -XX:+AlwaysPreTouch -Djava.security.egd=file:/dev/./urandom -cp /tmp/resources:/app/resources/:/app/classes/:/app/libs/* "com.edgora.ms.captcha.CaptchaApp"  "$@"