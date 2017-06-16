FROM redis:3.2.9

ENV REDIS_PORT 6379
ENV REDIS_HOME /usr/local/etc/redis

COPY configs/redis.conf /usr/local/etc/redis/redis.conf
CMD [ "redis-server", "/usr/local/etc/redis/redis.conf" ]