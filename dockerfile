# Build myapp server Docker container
FROM coreos/apache
MAINTAINER MyName
COPY app /var/www/
