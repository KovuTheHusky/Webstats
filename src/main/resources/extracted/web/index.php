<?php
require_once $_SERVER['DOCUMENT_ROOT'] . '/assets/includes/overall_header.php';

$res = $mysqli->query("SELECT player_name FROM ws_players");
while ($row = $res->fetch_assoc()) {
	$player[] = $row['player_name'];
	if (!file_exists($_SERVER['DOCUMENT_ROOT'] . '/players/' . $row['player_name'] . '.png') || filemtime($_SERVER['DOCUMENT_ROOT'] . '/players/' . $row['player_name'] . '.png') < time() - 300)
		playerHead($row['player_name']);
}

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
<?php require_once $_SERVER['DOCUMENT_ROOT'] . '/assets/includes/page_header.php'; ?>
<?php require_once $_SERVER['DOCUMENT_ROOT'] . '/assets/includes/navigation.php'; ?>
  <article>
    <div class="face_roll">
      <ul>
<?php foreach ($player as $p) { ?>
        <li><a href="/player/<?php echo strtolower($p); ?>"><img src="/players/<?php echo strtolower($p); ?>.png" alt="<?php echo $p; ?>" class="" /></a></li>
<?php } ?>
      </ul>
    </div>
    <section class="two justified">
      <div>
        <h1>Server</h1>
        <ul>
          <li><?php echo 'Never';?> Startup</li>
          <li><?php echo 'Never';?> Shutdown</li>
          <li><?php echo '0'; ?>% Uptime</li>
        </ul>
      </div>
      <div>
        <h1>Players</h1>
        <ul>
          <li><?php echo '0'; ?> Online</li>
          <li><?php echo '0'; ?> Tracked</li>
          <li><?php echo '0'; ?> Peak</li>
          <li><?php echo '0'; ?> Maximum</li>
        </ul>
      </div>
    </section>
    <section class="three justified">
      <div>
        <h1>Blocks</h1>
        <ul>
          <li><?php echo '0'; ?> Broken</li>
          <li><?php echo '0'; ?> Placed</li>
          <li><?php echo '0'; ?> Crafted</li>
        </ul>
      </div>
      <div>
        <h1>Items</h1>
        <ul>
          <li><?php echo '0'; ?> Broken</li>
          <li><?php echo '0'; ?> Crafted</li>
          <li><?php echo '0'; ?> Dropped</li>
          <li><?php echo '0'; ?> Picked Up</li>
          <li><?php echo '0'; ?> Used</li>
        </ul>
      </div>
      <div>
        <h1>Deaths</h1>
        <ul>
          <li><?php echo '0'; ?> Kills</li>
          <li><?php echo '0'; ?> Deaths</li>
          <li><?php echo '0'; ?> Most Kills</li>
          <li><?php echo '0'; ?> Most Deaths</li>
        </ul>
      </div>
    </section>
    <section class="three justified">
      <div>
        <h1>Distance</h1>
        <ul>
          <li><?php echo '0'; ?> Total</li>
          <li><?php echo '0'; ?> Walked</li>
          <li><?php echo '0'; ?> Swam</li>
          <li><?php echo '0'; ?> Fallen</li>
          <li><?php echo '0'; ?> Climbed</li>
          <li><?php echo '0'; ?> Flown</li>
          <li><?php echo '0'; ?> Dove</li>
          <li><?php echo '0'; ?> By Minecart</li>
          <li><?php echo '0'; ?> By Boat</li>
          <li><?php echo '0'; ?> By Pig</li>
          <li><?php echo '0'; ?> By Horse</li>
        </ul>
      </div>
      <div>
        <h1>Other</h1>
        <ul>
          <li><?php echo '0'; ?> Eggs Thrown</li>
          <li><?php echo '0'; ?> Eggs Hatched</li>
          <li><?php echo '0'; ?> Chickens Spawned</li>
          <li><?php echo '0'; ?> Fishing Rod Casts</li>
          <li><?php echo '0'; ?> Fish Caught</li>
        </ul>
      </div>
      <div style="visibility: hidden;"></div>
    </section>
  </article>
<?php require_once $_SERVER['DOCUMENT_ROOT'] . '/assets/includes/page_footer.php'; ?>
</body>
</html>