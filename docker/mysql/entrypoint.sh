#!/bin/sh
set -eu

# Windows 挂载文件可能显示为 0777，MySQL 会忽略可被所有用户写入的配置。
cp /etc/mysql/my-system-charset.cnf /etc/mysql/conf.d/my-system-charset.cnf
chmod 0644 /etc/mysql/conf.d/my-system-charset.cnf

exec /usr/local/bin/docker-entrypoint.sh "$@"
