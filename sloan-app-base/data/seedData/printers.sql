-- Controls

INSERT INTO rds.controls (host, zone, name, value, description, editable) VALUES ('build', 'ws-hpp', 'ip', '172.27.13.26', 'East Exception HP printer IP address', 'yes');
INSERT INTO rds.controls (host, zone, name, value, description, editable) VALUES ('build', 'ws-kyo1010', 'ip', '172.27.13.17', 'East Pack Kyocera printer IP address', 'yes');
INSERT INTO rds.controls (host, zone, name, value, description, editable) VALUES ('build', 'ws-zeb1010', 'ip', '172.27.13.16', 'East Pack Zebra 1x4 printer IP address', 'yes');
INSERT INTO rds.controls (host, zone, name, value, description, editable) VALUES ('build', 'ws-zeb1025', 'ip', '172.27.13.25', 'East Exception Zebra 4x6 printer IP address', 'yes');
INSERT INTO rds.controls (host, zone, name, value, description, editable) VALUES ('build', 'ws-zeb1025-b', 'ip', '172.27.13.13', 'East Exception Zebra 1x4 printer IP address', 'yes');

INSERT INTO rds.controls (host, zone, name, value, description, editable) VALUES ('build', 'ws-hpp2360', 'ip', '172.27.13.54', 'West Exception HP printer IP address', 'yes');
INSERT INTO rds.controls (host, zone, name, value, description, editable) VALUES ('build', 'ws-kyo2350', 'ip', '172.27.13.45', 'West Pack Kyocera printer IP address', 'yes');
INSERT INTO rds.controls (host, zone, name, value, description, editable) VALUES ('build', 'ws-zeb2350', 'ip', '172.27.13.44', 'West Pack Zebra 1x4 printer IP address', 'yes');
INSERT INTO rds.controls (host, zone, name, value, description, editable) VALUES ('build', 'ws-zeb2360', 'ip', '172.27.13.53', 'West Exception Zebra 4x6 printer IP address', 'yes');
INSERT INTO rds.controls (host, zone, name, value, description, editable) VALUES ('build', 'ws-zeb2360-b', 'ip', '172.27.13.29', 'West Exception Zebra 1x4 printer IP address', 'yes');

-- Launch

INSERT INTO rds.launch (host, ordinal, nickName, displayName, traceName, home, process, args, delayAfter, termDelay, mode) VALUES ('build', 301, 'ws-kyo1010', 'east pack kyocera', '', '/home/rds/app/bin', 'spr', 'ws-kyo1010', 0, 0, 'daemon');
INSERT INTO rds.launch (host, ordinal, nickName, displayName, traceName, home, process, args, delayAfter, termDelay, mode) VALUES ('build', 302, 'ws-zeb1010', 'east pack zebra', '', '/home/rds/app/bin', 'spr', 'ws-zeb1010', 0, 0, 'daemon');
INSERT INTO rds.launch (host, ordinal, nickName, displayName, traceName, home, process, args, delayAfter, termDelay, mode) VALUES ('build', 303, 'ws-hpp', 'east exception hp', '', '/home/rds/app/bin', 'spr', 'ws-hpp', 0, 0, 'daemon');
INSERT INTO rds.launch (host, ordinal, nickName, displayName, traceName, home, process, args, delayAfter, termDelay, mode) VALUES ('build', 304, 'ws-zeb1025', 'east exception zebra 4x6', '', '/home/rds/app/bin', 'spr', 'ws-zeb1025', 0, 0, 'daemon');
INSERT INTO rds.launch (host, ordinal, nickName, displayName, traceName, home, process, args, delayAfter, termDelay, mode) VALUES ('build', 305, 'ws-zeb1025-b', 'east exception zebra 1x4', '', '/home/rds/app/bin', 'spr', 'ws-zeb1025-b', 0, 0, 'daemon');

INSERT INTO rds.launch (host, ordinal, nickName, displayName, traceName, home, process, args, delayAfter, termDelay, mode) VALUES ('build', 306, 'ws-kyo2350', 'west pack kyocera', '', '/home/rds/app/bin', 'spr', 'ws-kyo2350', 0, 0, 'daemon');
INSERT INTO rds.launch (host, ordinal, nickName, displayName, traceName, home, process, args, delayAfter, termDelay, mode) VALUES ('build', 307, 'ws-zeb2350', 'west pack zebra', '', '/home/rds/app/bin', 'spr', 'ws-zeb2350', 0, 0, 'daemon');
INSERT INTO rds.launch (host, ordinal, nickName, displayName, traceName, home, process, args, delayAfter, termDelay, mode) VALUES ('build', 308, 'ws-hpp2360', 'west exception hp', '', '/home/rds/app/bin', 'spr', 'ws-hpp2360', 0, 0, 'daemon');
INSERT INTO rds.launch (host, ordinal, nickName, displayName, traceName, home, process, args, delayAfter, termDelay, mode) VALUES ('build', 309, 'ws-zeb2360', 'west exception zebra 4x6', '', '/home/rds/app/bin', 'spr', 'ws-zeb2360', 0, 0, 'daemon');
INSERT INTO rds.launch (host, ordinal, nickName, displayName, traceName, home, process, args, delayAfter, termDelay, mode) VALUES ('build', 310, 'ws-zeb2360-b', 'west exception zebra 1x4', '', '/home/rds/app/bin', 'spr', 'ws-zeb2360-b', 0, 0, 'daemon');