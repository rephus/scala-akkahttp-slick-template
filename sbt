java -Dfile.encoding=UTF8  -Xmx1536M -Xss1M -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -jar `dirname $0`/sbt-launch.jar "$@"

