<?php

ini_set('display_errors', false);
ini_set('log_errors', true);
ini_set('error_log', 'error.log');
error_reporting(E_ALL);
require_once($_SERVER['DOCUMENT_ROOT'] . '/assets/includes/configuration.php');
require_once($_SERVER['DOCUMENT_ROOT'] . '/assets/includes/functions.php');

$mysqli = new mysqli($db['host'], $db['user'], $db['pass'], $db['table'], $db['port']);
if ($mysqli->connect_errno) {
	echo "Failed to connect to MySQL: (" . $mysqli->connect_errno . ") " . $mysqli->connect_error;
}

$res = $mysqli->query("SELECT statistic_first AS first, statistic_startup AS startup, statistic_shutdown AS shutdown, statistic_uptime AS uptime, statistic_players AS maxplayers FROM ws_statistics");
$stats = $res->fetch_assoc();
$uptime = number_format(is_null($stats['shutdown']) ? '100' : $stats['uptime'] / (time() - $stats['first']) * 100, 2);

$res = $mysqli->query("SELECT player_online AS online, player_name AS name FROM ws_players WHERE player_online = 1");
while ($row = $res->fetch_assoc())
	$players[$row['name']] = $row['online'];
$playercount = isset($players) ? count($players) : 0;

$res = $mysqli->query("SELECT COUNT(player_id) AS tracked FROM ws_players");
$trackedplayers = $res->fetch_assoc();
$trackedplayers = $trackedplayers['tracked'];

$res = $mysqli->query("SELECT SUM(distance_walked) AS walked, SUM(distance_swam) AS swam, SUM(distance_fallen) AS fallen, SUM(distance_climbed) AS climbed, SUM(distance_flown) AS flown, SUM(distance_dove) AS dove, SUM(distance_by_minecart) AS by_minecart, SUM(distance_by_boat) AS by_boat, SUM(distance_by_pig) AS by_pig, SUM(distance_by_horse) AS by_horse FROM ws_distances");
if (!$res) {
	echo "Selection failed: (" . $mysqli->errno . ") " . $mysqli->error;
}
$row = $res->fetch_assoc();
$row = array_map('intval', $row);
$total = array_sum($row);

$res = $mysqli->query("SELECT SUM(egg_thrown) AS thrown, SUM(egg_hatch) AS hatch, SUM(egg_spawn) AS spawn FROM ws_eggs");
if (!$res) {
	echo "Selection failed: (" . $mysqli->errno . ") " . $mysqli->error;
}
$eggs = $res->fetch_assoc();

$res = $mysqli->query("SELECT SUM(fish_casts) AS casts, SUM(fish_catches) AS catches FROM ws_fishes");
if (!$res) {
	echo "Selection failed: (" . $mysqli->errno . ") " . $mysqli->error;
}
$fish = $res->fetch_assoc();

$res = $mysqli->query("SELECT SUM(block_broken) AS broken, SUM(block_placed) AS placed, SUM(block_crafted) AS crafted FROM ws_blocks");
$blocks = $res->fetch_assoc();

?>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<link rel="icon" href="/favicon.png" type="image/png">
<link rel="stylesheet" href="/assets/styles/global.css" type="text/css" />
<title>Webstats</title>
</head>
<body>
<header class="<?php if ($stats['startup'] > $stats['shutdown']) echo 'green'; else echo 'red'; ?>">

</header>
<nav>
  <ul class="box">
    <li>Text</li>
  </ul>
</nav>
<article>
  <section class="justified">
	<img src="/players/KovuTheHusky.png" alt="KovuTheHusky" class="<?php if (!isset($players['KovuTheHusky'])) echo 'offline'; ?>" />
	<img src="/players/NightWolf1298.png" alt="NightWolf1298" class="<?php if (!isset($players['NightWolf1298'])) echo 'offline'; ?>" />
	<img src="/players/kuperfox.png" alt="kuperfox" class="<?php if (!isset($players['kuperfox'])) echo 'offline'; ?>" />
	<img src="/players/Cujo27.png" alt="Cujo27" class="<?php if (!isset($players['Cujo27'])) echo 'offline'; ?>" />
	<img src="/players/Prince_of_Wars.png" alt="Prince_of_Wars" class="<?php if (!isset($players['Prince_of_Wars'])) echo 'offline'; ?>" />
  </section>
  <section class="two justified">
    <div>
      <h1>Server</h1>
      <ul>
        <li><?php echo date('Y-m-d h:ia', $stats['startup']);?> Startup</li>
        <li><?php echo is_null($stats['shutdown']) ? 'Never' : date('Y-m-d h:ia', $stats['shutdown']);?> Shutdown</li>
        <li><?php echo $uptime; ?>% Uptime</li>
      </ul>
    </div>
    <div>
      <h1>Players</h1>
      <ul>
        <li><?php echo number_format($playercount); ?> Online</li>
        <li><?php echo number_format($trackedplayers); ?> Tracked</li>
        <li><?php echo number_format($stats['maxplayers']); ?> Maximum</li>
      </ul>
    </div>
  </section>
  <section class="three justified">
    <div>
      <h1>Blocks</h1>
      <ul>
        <li><?php echo number_format($blocks['broken']); ?> Broken</li>
        <li><?php echo number_format($blocks['placed']); ?> Placed</li>
        <li><?php echo number_format($blocks['crafted']); ?> Crafted</li>
      </ul>
    </div>
    <div>
      <h1>Distance</h1>
      <ul>
        <li><?php echo morkm($total); ?> Total</li>
        <li><?php echo morkm($row['walked']); ?> Walked</li>
        <li><?php echo morkm($row['swam']); ?> Swam</li>
        <li><?php echo morkm($row['fallen']); ?> Fallen</li>
        <li><?php echo morkm($row['climbed']); ?> Climbed</li>
        <li><?php echo morkm($row['flown']); ?> Flown</li>
        <li><?php echo morkm($row['dove']); ?> Dove</li>
        <li><?php echo morkm($row['by_minecart']); ?> By Minecart</li>
        <li><?php echo morkm($row['by_boat']); ?> By Boat</li>
        <li><?php echo morkm($row['by_pig']); ?> By Pig</li>
        <li><?php echo morkm($row['by_horse']); ?> By Horse</li>
      </ul>
    </div>
    <div>
      <h1>Other</h1>
      <ul>
        <li><?php echo number_format($eggs['thrown']); ?> Eggs Thrown</li>
        <li><?php echo number_format($eggs['hatch']); ?> Eggs Hatched</li>
        <li><?php echo number_format($eggs['spawn']); ?> Chickens Spawned</li>
        <li><?php echo number_format($fish['casts']); ?> Fishing Rod Casts</li>
        <li><?php echo number_format($fish['catches']); ?> Fish Caught</li>
      </ul>
    </div>
  </section>
</article>
<footer>
  Data from <a href="http://dev.bukkit.org/bukkit-mods/webstats">Webstats</a> ${version} by <a href="http://codeski.com">Codeski</a> 
</footer>
</body>
</html>