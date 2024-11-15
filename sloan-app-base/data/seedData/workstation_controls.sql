-- Controls

INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'east-pakt-exception', 'cycleMax', '2', '', 'yes');
INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'east-pakt-exception', 'ip', '172.27.13.27', 'East Pakt Exception Workstation IP', 'yes');
INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'east-pakt-exception', 'labeler', 'ws-zeb1025-b', '', 'yes');
INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'east-pakt-exception', 'login', 'yes', '', 'yes');
INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'east-pakt-exception', 'mode', 'exceptionStation', '', 'yes');
INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'east-pakt-exception', 'name', 'wsEastEx', '', 'yes');
INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'east-pakt-exception', 'poll', '1000', '', 'yes');
INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'east-pakt-exception', 'port', '10000', '', 'yes');
INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'east-pakt-exception', 'printer', 'ws-hpp', '', 'yes');
INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'east-pakt-exception', 'scanner', '/east-pakt-exception/srlScan', 'East Pakt Exception Workstation Scanner', 'yes');
INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'east-pakt-exception', 'shippingLabeler', 'ws-zeb1025', '', 'yes');
INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'east-pakt-exception', 'startScreen', 'termApp.InitScreen', 'East Pakt Exception Workstation Start Screen', 'yes');
INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'east-pakt-exception', 'vim', 'east', '', 'yes');

INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'east-pakt-pack', 'cycleMax', '2', '', 'yes');
INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'east-pakt-pack', 'ip', '172.27.13.15', '', 'yes');
INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'east-pakt-pack', 'labeler', 'ws-zeb1010', '1x4 label printer', 'yes');
INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'east-pakt-pack', 'mode', 'inlinePack', '', 'yes');
INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'east-pakt-pack', 'preZone', '', '', 'yes');
INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'east-pakt-pack', 'printer', 'ws-kyo1010', 'packlist printer', 'yes');
INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'east-pakt-pack', 'runtimeLPN', 'eastPackLPN', '', 'yes');
INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'east-pakt-pack', 'scanner', '/east-pakt-pack/srlScan', '', 'yes');
INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'east-pakt-pack', 'startScreen', 'termApp.InitScreen', '', 'yes');

INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'west-pakt-exception', 'cycleMax', '2', '', 'yes');
INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'west-pakt-exception', 'ip', '172.27.13.55', 'West Pakt Exception Workstation IP', 'yes');
INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'west-pakt-exception', 'labeler', 'ws-zeb2360-b', 'West Pakt Exception Zebra 1x4 printer', 'yes');
INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'west-pakt-exception', 'login', 'yes', '', 'yes');
INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'west-pakt-exception', 'mode', 'exceptionStation', '', 'yes');
INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'west-pakt-exception', 'name', 'wsWestEx', '', 'yes');
INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'west-pakt-exception', 'poll', '1000', '', 'yes');
INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'west-pakt-exception', 'port', '10000', '', 'yes');
INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'west-pakt-exception', 'printer', 'ws-hpp2360', 'West Pakt Exception Workstation packlist printer', 'yes');
INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'west-pakt-exception', 'scanner', '/west-pakt-exception/srlScan', 'West Pakt Exception Workstation hand scanner', 'yes');
INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'west-pakt-exception', 'shippingLabeler', 'ws-zeb2360', 'West Pakt Exception Workstation Zebra 4x6 printer', 'yes');
INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'west-pakt-exception', 'startScreen', 'termApp.InitScreen', 'West Pakt Exception Workstation Start Screen', 'yes');
INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'west-pakt-exception', 'vim', 'west', '', 'yes');

INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'west-pakt-pack', 'cycleMax', '2', '', 'yes');
INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'west-pakt-pack', 'ip', '172.27.13.43', 'West Pakt Inline Pack Workstation IP', 'yes');
INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'west-pakt-pack', 'labeler', 'ws-zeb2350', 'West Pakt Inline Pack Workstation 1x4 label printer', 'yes');
INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'west-pakt-pack', 'mode', 'inlinePack', '', 'yes');
INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'west-pakt-pack', 'preZone', '', '', 'yes');
INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'west-pakt-pack', 'printer', 'ws-kyo2350', 'West Pakt Inline Pack Workstation packlist printer', 'yes');
INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'west-pakt-pack', 'runtimeLPN', 'westPackLPN', 'West Pakt Inline Pack Workstation conveyor scanner handoff', 'yes');
INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'west-pakt-pack', 'scanner', '/west-pakt-pack/srlScan', 'West Pakt Inline Pack Workstation hand scanner', 'yes');
INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'west-pakt-pack', 'startScreen', 'termApp.InitScreen', '', 'yes');

INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'east-cart-build', 'mode', 'cart', '', 'yes');
INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'east-cart-build', 'ip', '172.27.13.12', 'East Cart Build Workstation IP', 'yes');
INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'east-cart-build', 'startScreen', 'termApp.InitScreen', '', 'yes');
INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'east-cart-build', 'login', 'true', '', 'yes');
INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'east-cart-build', 'scanner', '/east-cart-build/srlScan', 'East Cart Build Workstation hand scanner', 'yes');
INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'east-cart-build', 'automode', 'cart', '', 'yes');

INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'west-carton-start', 'mode', 'orderStart', '', 'yes');
INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'west-carton-start', 'ip', '172.27.13.28', 'West Carton Start Workstation IP', 'yes');
INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'west-carton-start', 'startScreen', 'termApp.InitScreen', '', 'yes');
INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'west-carton-start', 'login', 'true', '', 'yes');
INSERT INTO rds.controls(host, zone, name, value, description, editable) VALUES ('build', 'west-carton-start', 'scanner', '/west-carton-start/srlScan', 'West Carton Start Workstation hand scanner', 'yes');

-- Launch

INSERT INTO rds.launch (host, ordinal, nickName, displayName, traceName, home, process, args, delayAfter, termDelay, mode) VALUES ('build', 320, 'East inline pack workstation', 'East inline pack workstation', 'eInline', '/home/rds/app/bin', 'java', 'term.TerminalDriver east-pakt-pack', 15, 0, 'daemon');
INSERT INTO rds.launch (host, ordinal, nickName, displayName, traceName, home, process, args, delayAfter, termDelay, mode) VALUES ('build', 321, 'East exception workstation', 'East exception workstation', 'eExcept', '/home/rds/app/bin', 'java', 'term.TerminalDriver east-pakt-exception', 15, 0, 'daemon');

INSERT INTO rds.launch (host, ordinal, nickName, displayName, traceName, home, process, args, delayAfter, termDelay, mode) VALUES ('build', 322, 'West inline pack workstation', 'West inline pack workstation', 'wInline', '/home/rds/app/bin', 'java', 'term.TerminalDriver west-pakt-pack', 15, 0, 'daemon');
INSERT INTO rds.launch (host, ordinal, nickName, displayName, traceName, home, process, args, delayAfter, termDelay, mode) VALUES ('build', 323, 'West exception workstation', 'West exception workstation', 'wExcept', '/home/rds/app/bin', 'java', 'term.TerminalDriver west-pakt-exception', 15, 0, 'daemon');

INSERT INTO rds.launch (host, ordinal, nickName, displayName, traceName, home, process, args, delayAfter, termDelay, mode) VALUES ('build', 324, 'East cart build workstation', 'East cart build workstation', 'eCartBld', '/home/rds/app/bin', 'java', 'term.TerminalDriver east-cart-build', 15, 0, 'daemon');
INSERT INTO rds.launch (host, ordinal, nickName, displayName, traceName, home, process, args, delayAfter, termDelay, mode) VALUES ('build', 325, 'West carton start workstation', 'West carton start workstation', 'wCrtnSta', '/home/rds/app/bin', 'java', 'term.TerminalDriver west-carton-start', 15, 0, 'daemon');

