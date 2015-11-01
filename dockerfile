# Build myapp server Docker container
FROM coreos/apache
MAINTAINER Marcel and Robin
COPY app /var/www/
