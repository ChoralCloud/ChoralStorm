CREATE KEYSPACE choraldatastream WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 1};

CREATE TABLE choraldatastream.raw_data (device_id text, device_data text, device_timestamp timestamp, time timestamp, primary key((device_id),device_timestamp));